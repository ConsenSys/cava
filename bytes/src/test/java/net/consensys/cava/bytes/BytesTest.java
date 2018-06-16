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
package net.consensys.cava.bytes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BytesTest extends CommonBytesTests {

  @Override
  Bytes h(String hex) {
    return Bytes.fromHexString(hex);
  }

  @Override
  MutableBytes m(int size) {
    return MutableBytes.create(size);
  }

  @Override
  Bytes w(byte[] bytes) {
    return Bytes.wrap(bytes);
  }

  @Override
  Bytes of(int... bytes) {
    return Bytes.of(bytes);
  }

  @Test
  void wrapEmpty() {
    Bytes wrap = Bytes.wrap(new byte[0]);
    assertEquals(Bytes.EMPTY, wrap);
  }

  @ParameterizedTest
  @MethodSource("wrapProvider")
  void wrap(Object arr) {
    byte[] bytes = (byte[]) arr;
    Bytes value = Bytes.wrap(bytes);
    assertEquals(bytes.length, value.size());
    assertArrayEquals(value.toArray(), bytes);
  }

  private static Stream<Arguments> wrapProvider() {
    return Stream.of(
        Arguments.of(new Object[] {new byte[10]}),
        Arguments.of(new Object[] {new byte[] {1}}),
        Arguments.of(new Object[] {new byte[] {1, 2, 3, 4}}),
        Arguments.of(new Object[] {new byte[] {-1, 127, -128}}));
  }

  @Test
  void wrapNull() {
    assertThrows(NullPointerException.class, () -> Bytes.wrap((byte[]) null));
  }

  /**
   * Checks that modifying a wrapped array modifies the value itself.
   */
  @Test
  void wrapReflectsUpdates() {
    byte[] bytes = new byte[] {1, 2, 3, 4, 5};
    Bytes value = Bytes.wrap(bytes);

    assertEquals(bytes.length, value.size());
    assertArrayEquals(value.toArray(), bytes);

    bytes[1] = 127;
    bytes[3] = 127;

    assertEquals(bytes.length, value.size());
    assertArrayEquals(value.toArray(), bytes);
  }

  @Test
  void wrapSliceEmpty() {
    assertEquals(Bytes.EMPTY, Bytes.wrap(new byte[0], 0, 0));
    assertEquals(Bytes.EMPTY, Bytes.wrap(new byte[] {1, 2, 3}, 0, 0));
    assertEquals(Bytes.EMPTY, Bytes.wrap(new byte[] {1, 2, 3}, 2, 0));
  }

  @ParameterizedTest
  @MethodSource("wrapSliceProvider")
  void wrapSlice(Object arr, int offset, int length) {
    assertWrapSlice((byte[]) arr, offset, length);
  }

  private static Stream<Arguments> wrapSliceProvider() {
    return Stream.of(
        Arguments.of(new byte[] {1, 2, 3, 4}, 0, 4),
        Arguments.of(new byte[] {1, 2, 3, 4}, 0, 2),
        Arguments.of(new byte[] {1, 2, 3, 4}, 2, 1),
        Arguments.of(new byte[] {1, 2, 3, 4}, 2, 2));
  }

  private void assertWrapSlice(byte[] bytes, int offset, int length) {
    Bytes value = Bytes.wrap(bytes, offset, length);
    assertEquals(length, value.size());
    assertArrayEquals(value.toArray(), Arrays.copyOfRange(bytes, offset, offset + length));
  }

  @Test
  void wrapSliceNull() {
    assertThrows(NullPointerException.class, () -> Bytes.wrap(null, 0, 2));
  }

  @Test
  void wrapSliceNegativeOffset() {
    assertThrows(IndexOutOfBoundsException.class, () -> assertWrapSlice(new byte[] {1, 2, 3, 4}, -1, 4));
  }

  @Test
  void wrapSliceOutOfBoundOffset() {
    assertThrows(IndexOutOfBoundsException.class, () -> assertWrapSlice(new byte[] {1, 2, 3, 4}, 5, 1));
  }

  @Test
  void wrapSliceNegativeLength() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> assertWrapSlice(new byte[] {1, 2, 3, 4}, 0, -2));
    assertEquals("Invalid negative length", exception.getMessage());
  }

  @Test
  void wrapSliceTooBig() {
    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> assertWrapSlice(new byte[] {1, 2, 3, 4}, 2, 3));
    assertEquals("Provided length 3 is too big: the value has only 2 bytes from offset 2", exception.getMessage());
  }

  /**
   * Checks that modifying a wrapped array modifies the value itself, but only if within the wrapped slice.
   */
  @Test
  void wrapSliceReflectsUpdates() {
    byte[] bytes = new byte[] {1, 2, 3, 4, 5};
    assertWrapSlice(bytes, 2, 2);
    bytes[2] = 127;
    bytes[3] = 127;
    assertWrapSlice(bytes, 2, 2);

    Bytes wrapped = Bytes.wrap(bytes, 2, 2);
    Bytes copy = wrapped.copy();

    // Modify the bytes outside of the wrapped slice and check this doesn't affect the value (that
    // it is still equal to the copy from before the updates)
    bytes[0] = 127;
    assertEquals(copy, wrapped);

    // Sanity check for copy(): modify within the wrapped slice and check the copy differs now.
    bytes[2] = 42;
    assertNotEquals(copy, wrapped);
  }

  @Test
  void ofBytes() {
    assertArrayEquals(Bytes.of().toArray(), new byte[] {});
    assertArrayEquals(Bytes.of((byte) 1, (byte) 2).toArray(), new byte[] {1, 2});
    assertArrayEquals(Bytes.of((byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5).toArray(), new byte[] {1, 2, 3, 4, 5});
    assertArrayEquals(Bytes.of((byte) -1, (byte) 2, (byte) -3).toArray(), new byte[] {-1, 2, -3});
  }

  @Test
  void ofInts() {
    assertArrayEquals(Bytes.of(1, 2).toArray(), new byte[] {1, 2});
    assertArrayEquals(Bytes.of(1, 2, 3, 4, 5).toArray(), new byte[] {1, 2, 3, 4, 5});
    assertArrayEquals(Bytes.of(0xff, 0x7f, 0x80).toArray(), new byte[] {-1, 127, -128});
  }

  @Test
  void ofIntsTooBig() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.of(2, 3, 256));
    assertEquals("3th value 256 does not fit a byte", exception.getMessage());
  }

  @Test
  void ofIntsTooLow() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.of(2, -1, 3));
    assertEquals("2th value -1 does not fit a byte", exception.getMessage());
  }

  @Test
  void minimalBytes() {
    assertEquals(h("0x"), Bytes.minimalBytes(0));
    assertEquals(h("0x01"), Bytes.minimalBytes(1));
    assertEquals(h("0x04"), Bytes.minimalBytes(4));
    assertEquals(h("0x10"), Bytes.minimalBytes(16));
    assertEquals(h("0xFF"), Bytes.minimalBytes(255));
    assertEquals(h("0x0100"), Bytes.minimalBytes(256));
    assertEquals(h("0x0200"), Bytes.minimalBytes(512));
    assertEquals(h("0x010000"), Bytes.minimalBytes(1L << 16));
    assertEquals(h("0x01000000"), Bytes.minimalBytes(1L << 24));
    assertEquals(h("0x0100000000"), Bytes.minimalBytes(1L << 32));
    assertEquals(h("0x010000000000"), Bytes.minimalBytes(1L << 40));
    assertEquals(h("0x01000000000000"), Bytes.minimalBytes(1L << 48));
    assertEquals(h("0x0100000000000000"), Bytes.minimalBytes(1L << 56));
    assertEquals(h("0xFFFFFFFFFFFFFFFF"), Bytes.minimalBytes(-1L));
  }

  @Test
  void ofUnsignedShort() {
    assertEquals(h("0x0000"), Bytes.ofUnsignedShort(0));
    assertEquals(h("0x0001"), Bytes.ofUnsignedShort(1));
    assertEquals(h("0x0100"), Bytes.ofUnsignedShort(256));
    assertEquals(h("0xFFFF"), Bytes.ofUnsignedShort(65535));
  }

  @Test
  void ofUnsignedShortNegative() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.ofUnsignedShort(-1));
    assertEquals(
        "Value -1 cannot be represented as an unsigned short (it is negative or too big)",
        exception.getMessage());
  }

  @Test
  void ofUnsignedShortTooBig() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.ofUnsignedShort(65536));
    assertEquals(
        "Value 65536 cannot be represented as an unsigned short (it is negative or too big)",
        exception.getMessage());
  }

  @Test
  void asUnsignedBigIntegerConstants() {
    assertEquals(bi("0"), Bytes.EMPTY.unsignedBigIntegerValue());
    assertEquals(bi("1"), Bytes.of(1).unsignedBigIntegerValue());
  }

  @Test
  void asSignedBigIntegerConstants() {
    assertEquals(bi("0"), Bytes.EMPTY.bigIntegerValue());
    assertEquals(bi("1"), Bytes.of(1).bigIntegerValue());
  }

  @Test
  void fromHexStringLenient() {
    assertEquals(Bytes.of(), Bytes.fromHexStringLenient(""));
    assertEquals(Bytes.of(), Bytes.fromHexStringLenient("0x"));
    assertEquals(Bytes.of(0), Bytes.fromHexStringLenient("0"));
    assertEquals(Bytes.of(0), Bytes.fromHexStringLenient("0x0"));
    assertEquals(Bytes.of(0), Bytes.fromHexStringLenient("00"));
    assertEquals(Bytes.of(0), Bytes.fromHexStringLenient("0x00"));
    assertEquals(Bytes.of(1), Bytes.fromHexStringLenient("0x1"));
    assertEquals(Bytes.of(1), Bytes.fromHexStringLenient("0x01"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("1FF2A"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x1FF2A"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x1ff2a"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x1fF2a"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("01FF2A"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x01FF2A"));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x01ff2A"));
  }

  @Test
  void fromHexStringLenientInvalidInput() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexStringLenient("foo"));
    assertEquals("Illegal character 'o' found at index 1 in hex binary representation 'foo'", exception.getMessage());
  }

  @Test
  void fromHexStringLenientLeftPadding() {
    assertEquals(Bytes.of(), Bytes.fromHexStringLenient("", 0));
    assertEquals(Bytes.of(0), Bytes.fromHexStringLenient("", 1));
    assertEquals(Bytes.of(0, 0), Bytes.fromHexStringLenient("", 2));
    assertEquals(Bytes.of(0, 0), Bytes.fromHexStringLenient("0x", 2));
    assertEquals(Bytes.of(0, 0, 0), Bytes.fromHexStringLenient("0", 3));
    assertEquals(Bytes.of(0, 0, 0), Bytes.fromHexStringLenient("0x0", 3));
    assertEquals(Bytes.of(0, 0, 0), Bytes.fromHexStringLenient("00", 3));
    assertEquals(Bytes.of(0, 0, 0), Bytes.fromHexStringLenient("0x00", 3));
    assertEquals(Bytes.of(0, 0, 1), Bytes.fromHexStringLenient("0x1", 3));
    assertEquals(Bytes.of(0, 0, 1), Bytes.fromHexStringLenient("0x01", 3));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("1FF2A", 3));
    assertEquals(Bytes.of(0x00, 0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x1FF2A", 4));
    assertEquals(Bytes.of(0x00, 0x00, 0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x1ff2a", 5));
    assertEquals(Bytes.of(0x00, 0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x1fF2a", 4));
    assertEquals(Bytes.of(0x00, 0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("01FF2A", 4));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x01FF2A", 3));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexStringLenient("0x01ff2A", 3));
  }

  @Test
  void fromHexStringLenientLeftPaddingInvalidInput() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexStringLenient("foo", 10));
    assertEquals("Illegal character 'o' found at index 1 in hex binary representation 'foo'", exception.getMessage());
  }

  @Test
  void fromHexStringLenientLeftPaddingInvalidSize() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexStringLenient("0x001F34", 2));
    assertEquals("Hex value 0x001F34 is too big: expected at most 2 bytes but got 3", exception.getMessage());
  }

  @Test
  void fromHexString() {
    assertEquals(Bytes.of(), Bytes.fromHexString("0x"));
    assertEquals(Bytes.of(0), Bytes.fromHexString("00"));
    assertEquals(Bytes.of(0), Bytes.fromHexString("0x00"));
    assertEquals(Bytes.of(1), Bytes.fromHexString("0x01"));
    assertEquals(Bytes.of(1, 0xff, 0x2a), Bytes.fromHexString("01FF2A"));
    assertEquals(Bytes.of(1, 0xff, 0x2a), Bytes.fromHexString("0x01FF2A"));
    assertEquals(Bytes.of(1, 0xff, 0x2a), Bytes.fromHexString("0x01ff2a"));
    assertEquals(Bytes.of(1, 0xff, 0x2a), Bytes.fromHexString("0x01fF2a"));
  }

  @Test
  void fromHexStringInvalidInput() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexString("fooo"));
    assertEquals("Illegal character 'o' found at index 1 in hex binary representation 'fooo'", exception.getMessage());
  }

  @Test
  void fromHexStringNotLenient() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexString("0x100"));
    assertEquals("Invalid odd-length hex binary representation '0x100'", exception.getMessage());
  }

  @Test
  void fromHexStringLeftPadding() {
    assertEquals(Bytes.of(), Bytes.fromHexString("0x", 0));
    assertEquals(Bytes.of(0, 0), Bytes.fromHexString("0x", 2));
    assertEquals(Bytes.of(0, 0, 0, 0), Bytes.fromHexString("0x", 4));
    assertEquals(Bytes.of(0, 0), Bytes.fromHexString("00", 2));
    assertEquals(Bytes.of(0, 0), Bytes.fromHexString("0x00", 2));
    assertEquals(Bytes.of(0, 0, 1), Bytes.fromHexString("0x01", 3));
    assertEquals(Bytes.of(0x00, 0x01, 0xff, 0x2a), Bytes.fromHexString("01FF2A", 4));
    assertEquals(Bytes.of(0x01, 0xff, 0x2a), Bytes.fromHexString("0x01FF2A", 3));
    assertEquals(Bytes.of(0x00, 0x00, 0x01, 0xff, 0x2a), Bytes.fromHexString("0x01ff2a", 5));
    assertEquals(Bytes.of(0x00, 0x00, 0x01, 0xff, 0x2a), Bytes.fromHexString("0x01fF2a", 5));
  }

  @Test
  void fromHexStringLeftPaddingInvalidInput() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexString("fooo", 4));
    assertEquals("Illegal character 'o' found at index 1 in hex binary representation 'fooo'", exception.getMessage());
  }

  @Test
  void fromHexStringLeftPaddingNotLenient() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexString("0x100", 4));
    assertEquals("Invalid odd-length hex binary representation '0x100'", exception.getMessage());
  }

  @Test
  void fromHexStringLeftPaddingInvalidSize() {
    Throwable exception = assertThrows(IllegalArgumentException.class, () -> Bytes.fromHexStringLenient("0x001F34", 2));
    assertEquals("Hex value 0x001F34 is too big: expected at most 2 bytes but got 3", exception.getMessage());
  }
}
