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
package net.consensys.cava.units.ethereum;

import static com.google.common.base.Preconditions.checkArgument;

import net.consensys.cava.bytes.Bytes;
import net.consensys.cava.bytes.MutableBytes;
import net.consensys.cava.units.bigints.UInt256;

import java.math.BigInteger;

import com.google.common.base.Objects;

/**
 * A unit measure of Gas as used by the Ethereum VM.
 */
public final class Gas {

  private final static int MAX_CONSTANT = 64;
  private final static BigInteger BI_MAX_CONSTANT = BigInteger.valueOf(MAX_CONSTANT);
  private final static UInt256 UINT256_MAX_CONSTANT = UInt256.valueOf(MAX_CONSTANT);
  private static Gas CONSTANTS[] = new Gas[MAX_CONSTANT + 1];
  static {
    CONSTANTS[0] = new Gas(0L);
    for (int i = 1; i <= MAX_CONSTANT; ++i) {
      CONSTANTS[i] = new Gas(i);
    }
  }

  private final long value;

  private Gas(long value) {
    this.value = value;
  }

  /**
   * Return a {@link Gas} containing the specified value.
   *
   * @param value The value to create a {@link Gas} for.
   * @return A {@link Gas} containing the specified value.
   * @throws IllegalArgumentException If the value is negative.
   */
  public static Gas valueOf(UInt256 value) {
    if (value.compareTo(UINT256_MAX_CONSTANT) <= 0) {
      return CONSTANTS[value.intValue()];
    }
    if (!value.fitsLong()) {
      throw new IllegalArgumentException("Gas value cannot be larger than 2^63 -1");
    }
    return new Gas(value.longValue());
  }

  /**
   * Return a {@link Gas} containing the specified value.
   *
   * @param value The value to create a {@link Gas} for.
   * @return A {@link Gas} containing the specified value.
   * @throws IllegalArgumentException If the value is negative.
   */
  public static Gas valueOf(long value) {
    checkArgument(value >= 0, "Argument must be positive");
    if (value <= MAX_CONSTANT) {
      return CONSTANTS[(int) value];
    }
    return new Gas(value);
  }

  /**
   * Return a {@link Gas} containing the specified value.
   *
   * @param value The value to create a {@link Gas} for.
   * @return A {@link Gas} containing the specified value.
   * @throws IllegalArgumentException If the value is negative.
   */
  public static Gas valueOf(BigInteger value) {
    checkArgument(value.signum() >= 0, "Argument must be positive");
    if (value.compareTo(BI_MAX_CONSTANT) <= 0) {
      return CONSTANTS[value.intValue()];
    }
    try {
      return new Gas(value.longValueExact());
    } catch (ArithmeticException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  /**
   * The price of this amount of gas given the provided price per unit of gas.
   *
   * @param gasPrice The price per unit of gas.
   * @return The price of this amount of gas for a per unit of gas price of {@code gasPrice}.
   */
  public Wei priceFor(Wei gasPrice) {
    return Wei.valueOf(gasPrice.uint256Value().multiply(value).uint256Value());
  }

  public Gas add(Gas other) {
    return Gas.valueOf(Math.addExact(value, other.value));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Gas)) {
      return false;
    }
    Gas gas = (Gas) o;
    return value == gas.value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  public Bytes toBytes() {
    MutableBytes bytes = MutableBytes.create(8);
    bytes.setLong(0, value);
    return bytes;
  }

  @Override
  public String toString() {
    return "Gas{" + "value=" + value + '}';
  }

  public Bytes toMinimalBytes() {
    return Bytes.minimalBytes(value);
  }

  public int compareTo(long other) {
    return Long.compare(value, other);
  }
}
