/*
 * Copyright 2018, ConsenSys Inc.
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
package net.consensys.cava.net.tls;

import static java.nio.file.Files.createDirectories;
import static net.consensys.cava.io.file.Files.atomicReplace;
import static net.consensys.cava.io.file.Files.copy;
import static net.consensys.cava.io.file.Files.createFileIfMissing;

import net.consensys.cava.bytes.Bytes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class FingerprintRepository {

  private final Path fingerprintFile;
  private final Set<Bytes> fingerprints;

  FingerprintRepository(Path fingerprintFile) {
    try {
      createDirectories(fingerprintFile.getParent());
      createFileIfMissing(fingerprintFile);
    } catch (IOException e) {
      throw new TLSEnvironmentException("Cannot create fingerprint file " + fingerprintFile, e);
    }
    try {
      this.fingerprintFile = fingerprintFile;
      try (Stream<String> lines = Files.lines(fingerprintFile)) {
        this.fingerprints = lines
            .map(String::trim)
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .map(Bytes::fromHexString)
            .collect(Collectors.toSet());
      }
    } catch (IOException e) {
      throw new TLSEnvironmentException("Cannot read fingerprint file " + fingerprintFile, e);
    }
  }

  boolean contains(Bytes fingerprint) {
    return fingerprints.contains(fingerprint);
  }

  void addFingerprint(Bytes fingerprint) {
    try {
      if (!contains(fingerprint)) {
        synchronized (fingerprints) {
          if (!contains(fingerprint)) {
            atomicReplace(fingerprintFile, writer -> {
              copy(fingerprintFile, writer);
              writer.write(fingerprint.toHexString().substring(2).toLowerCase());
              writer.write(System.lineSeparator());
            });
            fingerprints.add(fingerprint);
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
