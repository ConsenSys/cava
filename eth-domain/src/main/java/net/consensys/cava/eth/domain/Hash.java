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
package net.consensys.cava.eth.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static net.consensys.cava.crypto.Hash.keccak256;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.bytes.Bytes32;

import com.google.common.base.Objects;

/**
 * An Ethereum hash.
 */
public final class Hash {

  /**
   * Create a Hash from Bytes.
   *
   * <p>
   * The hash must be exactly 32 bytes.
   *
   * @param bytes The bytes for this hash.
   * @return A hash.
   * @throws IllegalArgumentException If {@code bytes.size() != 32}.
   */
  public static Hash fromBytes(Bytes bytes) {
    requireNonNull(bytes);
    checkArgument(bytes.size() == SIZE, "Expected %s bytes but got %s", SIZE, bytes.size());
    return new Hash(Bytes32.wrap(bytes));
  }

  /**
   * Create a Hash from Bytes32.
   *
   * @param bytes The bytes for this hash.
   * @return A hash.
   */
  public static Hash fromBytes(Bytes32 bytes) {
    requireNonNull(bytes);
    return new Hash(bytes);
  }

  public static Hash hash(Bytes value) {
    return new Hash(keccak256(value));
  }

  private static final int SIZE = 32;

  private final Bytes32 delegate;

  private Hash(Bytes32 value) {
    requireNonNull(value);
    this.delegate = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Hash)) {
      return false;
    }
    Hash hash = (Hash) obj;
    return delegate.equals(hash.delegate);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(delegate);
  }

  @Override
  public String toString() {
    return "Hash{" + delegate.toHexString() + '}';
  }

  /**
   * @return A hex-encoded version of the hash.
   */
  public String toHexString() {
    return delegate.toHexString();
  }

  /**
   * @return The bytes for this hash.
   */
  public Bytes toBytes() {
    return delegate;
  }
}
