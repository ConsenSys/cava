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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.bytes.Bytes32;
import net.consensys.cava.crypto.sodium.SecretBox;
import net.consensys.cava.crypto.sodium.Signature;
import net.consensys.cava.crypto.sodium.Sodium;
import net.consensys.cava.junit.BouncyCastleExtension;
import net.consensys.cava.scuttlebutt.Identity;
import net.consensys.cava.scuttlebutt.Invite;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(BouncyCastleExtension.class)
class SecureScuttlebuttHandshakeClientTest {

  @BeforeAll
  static void checkAvailable() {
    assumeTrue(Sodium.isAvailable(), "Sodium native library is not available");
  }

  @Test
  void initialMessage() {
    Signature.KeyPair serverLongTermKeyPair = Signature.KeyPair.random();
    Bytes32 networkIdentifier = Bytes32.random();
    SecureScuttlebuttHandshakeClient client = SecureScuttlebuttHandshakeClient
        .create(Signature.KeyPair.random(), networkIdentifier, serverLongTermKeyPair.publicKey());
    SecureScuttlebuttHandshakeServer server =
        SecureScuttlebuttHandshakeServer.create(serverLongTermKeyPair, networkIdentifier);
    Bytes initialMessage = client.createHello();
    server.readHello(initialMessage);
    client.readHello(server.createHello());
    assertEquals(client.sharedSecret(), server.sharedSecret());
    assertEquals(client.sharedSecret2(), server.sharedSecret2());
  }

  @Test
  void initialMessageDifferentNetworkIdentifier() {
    Signature.KeyPair serverLongTermKeyPair = Signature.KeyPair.random();
    SecureScuttlebuttHandshakeClient client = SecureScuttlebuttHandshakeClient
        .create(Signature.KeyPair.random(), Bytes32.random(), serverLongTermKeyPair.publicKey());
    SecureScuttlebuttHandshakeServer server =
        SecureScuttlebuttHandshakeServer.create(serverLongTermKeyPair, Bytes32.random());
    Bytes initialMessage = client.createHello();
    assertThrows(HandshakeException.class, () -> server.readHello(initialMessage));
  }

  @Test
  void identityMessageExchanged() {
    Signature.KeyPair clientLongTermKeyPair = Signature.KeyPair.random();
    Signature.KeyPair serverLongTermKeyPair = Signature.KeyPair.random();
    Bytes32 networkIdentifier = Bytes32.random();
    SecureScuttlebuttHandshakeClient client = SecureScuttlebuttHandshakeClient
        .create(clientLongTermKeyPair, networkIdentifier, serverLongTermKeyPair.publicKey());
    SecureScuttlebuttHandshakeServer server =
        SecureScuttlebuttHandshakeServer.create(serverLongTermKeyPair, networkIdentifier);
    Bytes initialMessage = client.createHello();
    server.readHello(initialMessage);
    client.readHello(server.createHello());
    server.readIdentityMessage(client.createIdentityMessage());
    assertEquals(clientLongTermKeyPair.publicKey(), server.clientLongTermPublicKey());
  }

  @Test
  void acceptMessageExchanged() {
    Signature.KeyPair clientLongTermKeyPair = Signature.KeyPair.random();
    Signature.KeyPair serverLongTermKeyPair = Signature.KeyPair.random();
    Bytes32 networkIdentifier = Bytes32.random();
    SecureScuttlebuttHandshakeClient client = SecureScuttlebuttHandshakeClient
        .create(clientLongTermKeyPair, networkIdentifier, serverLongTermKeyPair.publicKey());
    SecureScuttlebuttHandshakeServer server =
        SecureScuttlebuttHandshakeServer.create(serverLongTermKeyPair, networkIdentifier);
    Bytes initialMessage = client.createHello();
    server.readHello(initialMessage);
    client.readHello(server.createHello());
    server.readIdentityMessage(client.createIdentityMessage());
    client.readAcceptMessage(server.createAcceptMessage());
    assertEquals(client.sharedSecret(), server.sharedSecret());
    assertEquals(client.sharedSecret2(), server.sharedSecret2());
    assertEquals(client.sharedSecret3(), server.sharedSecret3());
  }

  @Test
  void finalSecretsUsed() {
    Signature.KeyPair clientLongTermKeyPair = Signature.KeyPair.random();
    Signature.KeyPair serverLongTermKeyPair = Signature.KeyPair.random();
    Bytes32 networkIdentifier = Bytes32.random();
    SecureScuttlebuttHandshakeClient client = SecureScuttlebuttHandshakeClient
        .create(clientLongTermKeyPair, networkIdentifier, serverLongTermKeyPair.publicKey());
    SecureScuttlebuttHandshakeServer server =
        SecureScuttlebuttHandshakeServer.create(serverLongTermKeyPair, networkIdentifier);
    Bytes initialMessage = client.createHello();
    server.readHello(initialMessage);
    client.readHello(server.createHello());
    server.readIdentityMessage(client.createIdentityMessage());
    client.readAcceptMessage(server.createAcceptMessage());

    {
      Bytes encrypted = SecretBox.encrypt(
          Bytes.fromHexString("deadbeef"),
          SecretBox.Key.fromBytes(server.serverToClientSecretBoxKey()),
          SecretBox.Nonce.fromBytes(server.serverToClientNonce()));

      Bytes decrypted = SecretBox.decrypt(
          encrypted,
          SecretBox.Key.fromBytes(client.serverToClientSecretBoxKey()),
          SecretBox.Nonce.fromBytes(client.serverToClientNonce()));
      assertEquals(Bytes.fromHexString("deadbeef"), decrypted);
    }

    {
      Bytes encrypted = SecretBox.encrypt(
          Bytes.fromHexString("deadbeef"),
          SecretBox.Key.fromBytes(client.clientToServerSecretBoxKey()),
          SecretBox.Nonce.fromBytes(client.clientToServerNonce()));

      Bytes decrypted = SecretBox.decrypt(
          encrypted,
          SecretBox.Key.fromBytes(server.clientToServerSecretBoxKey()),
          SecretBox.Nonce.fromBytes(server.clientToServerNonce()));
      assertEquals(Bytes.fromHexString("deadbeef"), decrypted);
    }
  }

  @Test
  void fromInviteWrongCurve() {
    assertThrows(
        IllegalArgumentException.class,
        () -> SecureScuttlebuttHandshakeClient.fromInvite(
            Bytes32.random(),
            new Invite("localhost", 30303, Identity.randomSECP256K1(), Signature.KeyPair.random().secretKey())));
  }

}
