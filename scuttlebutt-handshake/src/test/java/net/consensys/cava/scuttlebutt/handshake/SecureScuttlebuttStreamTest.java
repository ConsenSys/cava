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
package net.consensys.cava.scuttlebutt.handshake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.bytes.Bytes32;
import net.consensys.cava.crypto.sodium.Sodium;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SecureScuttlebuttStreamTest {

  @BeforeAll
  static void checkAvailable() {
    assumeTrue(Sodium.isAvailable(), "Sodium native library is not available");
  }

  @Test
  void streamExchange() {
    Bytes32 clientToServerKey = Bytes32.random();
    Bytes clientToServerNonce = Bytes.random(24);
    Bytes32 serverToClientKey = Bytes32.random();
    Bytes serverToClientNonce = Bytes.random(24);
    SecureScuttlebuttStream clientToServer =
        new SecureScuttlebuttStream(clientToServerKey, clientToServerNonce, serverToClientKey, serverToClientNonce);
    SecureScuttlebuttStream serverToClient =
        new SecureScuttlebuttStream(clientToServerKey, clientToServerNonce, serverToClientKey, serverToClientNonce);

    Bytes encrypted = clientToServer.sendToServer(Bytes.fromHexString("deadbeef"));
    assertEquals(Bytes.fromHexString("deadbeef").size() + 34, encrypted.size());

    Bytes decrypted = serverToClient.readFromClient(encrypted);
    assertEquals(Bytes.fromHexString("deadbeef"), decrypted);

    Bytes response = serverToClient.sendToClient(Bytes.fromHexString("deadbeef"));
    assertEquals(Bytes.fromHexString("deadbeef").size() + 34, response.size());

    Bytes responseDecrypted = clientToServer.readFromServer(response);
    assertEquals(Bytes.fromHexString("deadbeef"), responseDecrypted);
  }

  @Test
  void longMessage() {
    Bytes32 clientToServerKey = Bytes32.random();
    Bytes clientToServerNonce = Bytes.random(24);
    Bytes32 serverToClientKey = Bytes32.random();
    Bytes serverToClientNonce = Bytes.random(24);
    SecureScuttlebuttStream clientToServer =
        new SecureScuttlebuttStream(clientToServerKey, clientToServerNonce, serverToClientKey, serverToClientNonce);
    SecureScuttlebuttStream serverToClient =
        new SecureScuttlebuttStream(clientToServerKey, clientToServerNonce, serverToClientKey, serverToClientNonce);

    Bytes payload = Bytes.random(5128);
    Bytes encrypted = clientToServer.sendToServer(payload);
    assertEquals(5128 + 34 + 34, encrypted.size());
    Bytes decrypted = serverToClient.readFromClient(encrypted);
    assertEquals(payload, decrypted);

    Bytes encrypted2 = serverToClient.sendToClient(payload);
    assertEquals(5128 + 34 + 34, encrypted2.size());

    Bytes decrypted2 = clientToServer.readFromServer(encrypted2);
    assertEquals(payload, decrypted2);
  }

  @Test
  void multipleMessages() {
    Bytes32 clientToServerKey = Bytes32.random();
    Bytes clientToServerNonce = Bytes.random(24);
    Bytes32 serverToClientKey = Bytes32.random();
    Bytes serverToClientNonce = Bytes.random(24);
    SecureScuttlebuttStream clientToServer =
        new SecureScuttlebuttStream(clientToServerKey, clientToServerNonce, serverToClientKey, serverToClientNonce);
    SecureScuttlebuttStream serverToClient =
        new SecureScuttlebuttStream(clientToServerKey, clientToServerNonce, serverToClientKey, serverToClientNonce);

    for (int i = 0; i < 10; i++) {
      Bytes encrypted = clientToServer.sendToServer(Bytes.fromHexString("deadbeef"));
      assertEquals(Bytes.fromHexString("deadbeef").size() + 34, encrypted.size());

      Bytes decrypted = serverToClient.readFromClient(encrypted);
      assertEquals(Bytes.fromHexString("deadbeef"), decrypted);

      Bytes response = serverToClient.sendToClient(Bytes.fromHexString("deadbeef"));
      assertEquals(Bytes.fromHexString("deadbeef").size() + 34, response.size());

      Bytes responseDecrypted = clientToServer.readFromServer(response);
      assertEquals(Bytes.fromHexString("deadbeef"), responseDecrypted);
    }
  }
}
