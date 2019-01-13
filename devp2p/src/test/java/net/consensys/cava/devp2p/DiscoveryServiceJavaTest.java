/*
 * Copyright 2019 ConsenSys AG.
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
package net.consensys.cava.devp2p;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.consensys.cava.concurrent.AsyncCompletion;
import net.consensys.cava.concurrent.AsyncResult;
import net.consensys.cava.crypto.SECP256K1;
import net.consensys.cava.junit.BouncyCastleExtension;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(BouncyCastleExtension.class)
class DiscoveryServiceJavaTest {

  @Test
  void setUpAndShutDownAsync() throws Exception {
    DiscoveryService service = DiscoveryService.Companion.open(SECP256K1.KeyPair.random());
    service.shutdown();
    AsyncCompletion completion = service.awaitTerminationAsync();
    completion.join();
    assertTrue(completion.isDone());
  }

  @Test
  void lookupAsync() throws Exception {
    DiscoveryService service = DiscoveryService.Companion.open(SECP256K1.KeyPair.random());
    AsyncResult<List<Peer>> result = service.lookupAsync(SECP256K1.KeyPair.random().publicKey());
    List<Peer> peers = result.get();
    service.shutdown();
    assertTrue(peers.isEmpty());
  }

  @Test
  void managePeerRepository() throws Exception {
    SECP256K1.KeyPair peerKeyPair = SECP256K1.KeyPair.random();
    EphemeralPeerRepository repository = new EphemeralPeerRepository();
    DiscoveryService service = DiscoveryService.Companion.open(
        SECP256K1.KeyPair.random(),
        0,
        "localhost",
        Collections.singletonList(URI.create("enode://" + peerKeyPair.publicKey().toHexString() + "@127.0.0.1:10000")),
        repository);
    AsyncResult<Peer> result = repository.getAsync(peerKeyPair.publicKey());
    assertEquals(peerKeyPair.publicKey(), result.get().getNodeId());
    AsyncResult<Peer> byURI =
        repository.getAsync(URI.create("enode://" + peerKeyPair.publicKey().toHexString() + "@127.0.0.1:10000"));
    assertEquals(peerKeyPair.publicKey(), byURI.get().getNodeId());
    AsyncResult<Peer> byURIString =
        repository.getAsync("enode://" + peerKeyPair.publicKey().toHexString() + "@127.0.0.1:10000");
    assertEquals(peerKeyPair.publicKey(), byURIString.get().getNodeId());
    service.shutdown();
  }
}
