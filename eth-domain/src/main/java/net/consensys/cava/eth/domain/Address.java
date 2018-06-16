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

import net.consensys.cava.bytes.Bytes;

import com.google.common.base.Objects;

/**
 * An Ethereum account address.
 */
public final class Address {

  /**
   * Create an address from Bytes.
   *
   * <p>
   * The address must be exactly 20 bytes.
   *
   * @param bytes The bytes for this address.
   * @return An address.
   * @throws IllegalArgumentException If {@code bytes.size() != 20}.
   */
  public static Address fromBytes(Bytes bytes) {
    requireNonNull(bytes);
    checkArgument(bytes.size() == SIZE, "Expected %s bytes but got %s", SIZE, bytes.size());
    return new Address(bytes);
  }

  private static final int SIZE = 20;

  private final Bytes delegate;

  private Address(Bytes value) {
    this.delegate = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Address)) {
      return false;
    }
    Address other = (Address) obj;
    return delegate.equals(other.delegate);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(delegate);
  }

  @Override
  public String toString() {
    return "Address{" + delegate.toHexString() + '}';
  }

  /**
   * @return A hex-encoded version of the address.
   */
  public String toHexString() {
    return delegate.toHexString();
  }

  /**
   * @return The bytes for this address.
   */
  public Bytes toBytes() {
    return delegate;
  }
}
