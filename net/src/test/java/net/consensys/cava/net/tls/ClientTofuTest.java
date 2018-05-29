package net.consensys.cava.net.tls;

import static net.consensys.cava.net.tls.SecurityTestUtils.startServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.consensys.cava.junit.TempDirectory;
import net.consensys.cava.junit.TempDirectoryExtension;
import net.consensys.cava.junit.VertxExtension;
import net.consensys.cava.junit.VertxInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.net.ssl.SSLException;

import com.google.common.hash.Hashing;
import io.netty.util.internal.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.SelfSignedCertificate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TempDirectoryExtension.class)
@ExtendWith(VertxExtension.class)
class ClientTofuTest {

  private static String caValidFingerprint;
  private static String fooFingerprint;
  private static HttpServer caValidServer;
  private static HttpServer fooServer;
  private static HttpServer otherFooServer;

  private Path knownServersFile;
  private HttpClient client;

  @BeforeAll
  static void startServers(@TempDirectory Path tempDir, @VertxInstance Vertx vertx) throws Exception {
    SelfSignedCertificate caSignedCert = SelfSignedCertificate.create("localhost");
    SecurityTestUtils.configureJDKTrustStore(tempDir, caSignedCert);
    caValidFingerprint = StringUtil.toHexStringPadded(
        Hashing
            .sha256()
            .hashBytes(SecurityTestUtils.loadPEM(Paths.get(caSignedCert.keyCertOptions().getCertPath())))
            .asBytes());

    caValidServer = vertx
        .createHttpServer(new HttpServerOptions().setSsl(true).setPemKeyCertOptions(caSignedCert.keyCertOptions()))
        .requestHandler(context -> context.response().end("OK"));
    startServer(caValidServer);

    SelfSignedCertificate fooCert = SelfSignedCertificate.create("foo.com");
    fooFingerprint = StringUtil.toHexStringPadded(
        Hashing
            .sha256()
            .hashBytes(SecurityTestUtils.loadPEM(Paths.get(fooCert.keyCertOptions().getCertPath())))
            .asBytes());

    fooServer = vertx
        .createHttpServer(new HttpServerOptions().setSsl(true).setPemKeyCertOptions(fooCert.keyCertOptions()))
        .requestHandler(context -> context.response().end("OK"));
    startServer(fooServer);

    SelfSignedCertificate otherFooCert = SelfSignedCertificate.create("foo.com");
    otherFooServer = vertx
        .createHttpServer(new HttpServerOptions().setSsl(true).setPemKeyCertOptions(otherFooCert.keyCertOptions()))
        .requestHandler(context -> context.response().end("OK"));
    startServer(otherFooServer);
  }

  @BeforeEach
  void setupClient(@TempDirectory Path tempDir, @VertxInstance Vertx vertx) throws Exception {
    knownServersFile = tempDir.resolve("knownclients.txt");
    Files.deleteIfExists(knownServersFile);
    Files.write(knownServersFile, Collections.singletonList("#First line"));

    HttpClientOptions options = new HttpClientOptions();
    options
        .setSsl(true)
        .setTrustOptions(VertxTrustOptions.trustServerOnFirstUse(knownServersFile, false))
        .setConnectTimeout(1500)
        .setReuseAddress(true)
        .setReusePort(true);
    client = vertx.createHttpClient(options);
  }

  @AfterEach
  void cleanupClient() {
    client.close();
  }

  @AfterAll
  static void stopServers() {
    caValidServer.close();
    fooServer.close();
    otherFooServer.close();
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
  }

  @Test
  void shouldNotValidateUsingCertificate() throws Exception {
    CompletableFuture<Integer> statusCode = new CompletableFuture<>();
    client
        .post(
            caValidServer.actualPort(),
            "localhost",
            "/sample",
            response -> statusCode.complete(response.statusCode()))
        .exceptionHandler(statusCode::completeExceptionally)
        .end();
    assertEquals((Integer) 200, statusCode.join());

    List<String> knownServers = Files.readAllLines(knownServersFile);
    assertEquals(2, knownServers.size());
    assertEquals("#First line", knownServers.get(0));
    assertEquals("localhost:" + caValidServer.actualPort() + " " + caValidFingerprint, knownServers.get(1));
  }

  @Test
  void shouldValidateOnFirstUse() throws Exception {
    CompletableFuture<Integer> statusCode = new CompletableFuture<>();
    client
        .post(fooServer.actualPort(), "localhost", "/sample", response -> statusCode.complete(response.statusCode()))
        .exceptionHandler(statusCode::completeExceptionally)
        .end();
    assertEquals((Integer) 200, statusCode.join());

    List<String> knownServers = Files.readAllLines(knownServersFile);
    assertEquals(2, knownServers.size());
    assertEquals("#First line", knownServers.get(0));
    assertEquals("localhost:" + fooServer.actualPort() + " " + fooFingerprint, knownServers.get(1));
  }

  @Test
  void shouldRejectDifferentCertificate() throws Throwable {
    // do a first connection
    shouldValidateOnFirstUse();

    CompletableFuture<Integer> statusCode = new CompletableFuture<>();
    client
        .post(
            otherFooServer.actualPort(),
            "localhost",
            "/sample",
            response -> statusCode.complete(response.statusCode()))
        .exceptionHandler(statusCode::completeExceptionally)
        .end();
    try {
      statusCode.join();
    } catch (CompletionException e) {
      assertTrue(e.getCause() instanceof SSLException);
    }
  }
}