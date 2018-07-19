/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.consensys.cava.io.file;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static java.util.Objects.requireNonNull;

import net.consensys.cava.io.IOConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/**
 * Utility methods for working with files.
 */
public final class Files {
  private Files() {}

  /**
   * Create a file, if it does not already exist.
   *
   * @param path The path to the file to create.
   * @param attrs An optional list of file attributes to set atomically when creating the file.
   * @return <tt>true</tt> if the file was created.
   * @throws IOException If an I/O error occurs or the parent directory does not exist.
   */
  public static boolean createFileIfMissing(Path path, FileAttribute<?>... attrs) throws IOException {
    requireNonNull(path);
    try {
      java.nio.file.Files.createFile(path, attrs);
    } catch (FileAlreadyExistsException e) {
      return false;
    }
    return true;
  }

  /**
   * Delete a directory and all files contained within it.
   *
   * @param directory The directory to delete.
   * @throws IOException If an I/O error occurs.
   */
  public static void deleteRecursively(Path directory) throws IOException {
    checkNotNull(directory);

    walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * Copies the content of a resource to a file.
   *
   * @param resourceName The resource name.
   * @param destination The destination file.
   * @param options Options specifying how the destination file should be opened.
   * @return The destination file.
   * @throws IOException If an I/O error occurs.
   */
  public static Path copyResource(String resourceName, Path destination, OpenOption... options) throws IOException {
    requireNonNull(resourceName);
    requireNonNull(destination);

    try (OutputStream out = java.nio.file.Files.newOutputStream(destination, options)) {
      copyResource(resourceName, out);
    }
    return destination;
  }

  /**
   * Copies the content of a resource to an output stream.
   *
   * @param resourceName The resource name.
   * @param out The output stream.
   * @return The total bytes written.
   * @throws IOException If an I/O error occurs.
   */
  public static long copyResource(String resourceName, OutputStream out) throws IOException {
    try (InputStream in = Files.class.getClassLoader().getResourceAsStream(resourceName)) {
      long total = 0L;
      byte[] buf = new byte[4096];
      int n;
      while ((n = in.read(buf)) > 0) {
        out.write(buf, 0, n);
        total += n;
      }
      return total;
    }
  }

  /**
   * Write a temporary file and then replace target.
   *
   * @param path The target file to be replaced (if it exists).
   * @param bytes The bytes to be written.
   * @throws IOException If an I/O error occurs.
   */
  public static void atomicReplace(Path path, byte[] bytes) throws IOException {
    requireNonNull(bytes);
    Path directory = path.getParent();
    java.nio.file.Files.createDirectories(directory);
    Path tempFile = java.nio.file.Files.createTempFile(directory, "." + path.getName(0), ".tmp");
    try {
      java.nio.file.Files.write(tempFile, bytes);
      java.nio.file.Files.move(tempFile, path, REPLACE_EXISTING, ATOMIC_MOVE);
    } catch (Throwable e) {
      try {
        java.nio.file.Files.delete(tempFile);
      } catch (IOException e2) {
        e.addSuppressed(e2);
      }
      throw e;
    }
  }

  /**
   * Write a temporary file and then replace target.
   *
   * @param path The target file to be replaced (if it exists).
   * @param fn A consumer that will be provided a buffered {@link Writer} instance that will write to the file.
   * @throws IOException If an I/O error occurs.
   */
  public static void atomicReplace(Path path, IOConsumer<Writer> fn) throws IOException {
    atomicReplace(path, UTF_8, fn);
  }

  /**
   * Write a temporary file and then replace target.
   *
   * @param path The target file to be replaced (if it exists).
   * @param charset The charset of the file.
   * @param fn A consumer that will be provided a buffered {@link Writer} instance that will write to the file.
   * @throws IOException If an I/O error occurs.
   */
  public static void atomicReplace(Path path, Charset charset, IOConsumer<Writer> fn) throws IOException {
    requireNonNull(charset);
    requireNonNull(fn);
    Path directory = path.getParent();
    java.nio.file.Files.createDirectories(directory);
    Path tempFile = java.nio.file.Files.createTempFile(directory, "." + path.getName(0), ".tmp");
    Writer writer = null;
    try {
      writer = java.nio.file.Files.newBufferedWriter(tempFile, charset);
      fn.accept(writer);
      writer.flush();
      writer.close();
      java.nio.file.Files.move(tempFile, path, REPLACE_EXISTING, ATOMIC_MOVE);
    } catch (Throwable e) {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e2) {
          e.addSuppressed(e2);
        }
      }
      try {
        java.nio.file.Files.delete(tempFile);
      } catch (IOException e2) {
        e.addSuppressed(e2);
      }
      throw e;
    }
  }

  private static volatile WatchService watchService;

  /**
   * Listens to changes made to a file outside the program on the file system.
   *
   * @param file the file to watch for changes
   * @param changeListener the function called when changes are detected
   * @param modifiers modifiers that configure the polling period for file changes.
   * @throws IOException If an I/O error occurs.
   */
  @SuppressWarnings("unchecked")
  public static void listenToFileChanges(Path file, Consumer<Path> changeListener, WatchEvent.Modifier... modifiers)
      throws IOException {
    Objects.requireNonNull(file);
    checkArgument(!file.toFile().isDirectory());
    if (watchService == null) {
      synchronized (Files.class) {
        if (watchService == null) {
          watchService = FileSystems.getDefault().newWatchService();
        }
      }
    }
    WatchKey registration =
        file.getParent().register(watchService, new Kind<?>[] {ENTRY_CREATE, ENTRY_MODIFY}, modifiers);

    CompletableFuture.runAsync(() -> {
      while (true) {
        WatchKey key;
        try {
          key = watchService.take();
        } catch (InterruptedException e) {
          break;
        }

        for (WatchEvent<?> ev : key.pollEvents()) {
          if (ev.kind() == OVERFLOW) {
            continue;
          }
          WatchEvent<Path> event = (WatchEvent<Path>) ev;
          Path filename = event.context();
          if (file.equals(file.getParent().resolve(filename))) {
            changeListener.accept(file);
          }
        }

        if (!key.reset()) {
          break;
        }
      }
      registration.cancel();
    });
  }
}
