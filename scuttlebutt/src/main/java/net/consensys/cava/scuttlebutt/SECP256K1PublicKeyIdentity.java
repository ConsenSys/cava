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
package net.consensys.cava.scuttlebutt;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.crypto.SECP256K1;

import java.util.Objects;

/**
 * SECP256K1 Scuttlebutt identity backed by a public key.
 *
 * This representation doesn't support signing messages.
 */
final class SECP256K1PublicKeyIdentity implements Identity {

  private final SECP256K1.PublicKey publicKey;

  SECP256K1PublicKeyIdentity(SECP256K1.PublicKey keyPair) {
    this.publicKey = keyPair;
  }

  @Override
  public Bytes sign(Bytes message) {
    throw new UnsupportedOperationException("Cannot sign messages with a public key identity");
  }

  @Override
  public boolean verify(Bytes signature, Bytes message) {
    return SECP256K1.verify(message, SECP256K1.Signature.fromBytes(signature), publicKey);
  }

  @Override
  public String publicKeyAsBase64String() {
    return publicKey.bytes().toBase64String();
  }

  @Override
  public Bytes publicKeyBytes() {
    return publicKey.bytes();
  }

  @Override
  public String curveName() {
    return "secp256k1";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SECP256K1PublicKeyIdentity identity = (SECP256K1PublicKeyIdentity) o;
    return publicKey.equals(identity.publicKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicKey);
  }

  @Override
  public String toString() {
    return toCanonicalForm();
  }
}
