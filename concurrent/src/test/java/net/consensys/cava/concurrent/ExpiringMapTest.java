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
package net.consensys.cava.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpiringMapTest {

  private Instant currentTime;
  private ExpiringMap<Integer, String> map;

  @BeforeEach
  void setup() {
    currentTime = Instant.now();
    map = new ExpiringMap<>(() -> currentTime.toEpochMilli());
  }

  @Test
  void canAddAndRemoveWithoutExpiry() {
    map.put(1, "foo");
    assertTrue(map.containsKey(1));
    assertTrue(map.containsValue("foo"));
    assertEquals("foo", map.get(1));
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());

    String removed = map.remove(1);
    assertEquals("foo", removed);
    assertFalse(map.containsKey(1));
    assertFalse(map.containsValue("foo"));
    assertNull(map.get(1));
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());

    assertNull(map.remove(1));
  }

  @Test
  void canAddAndRemoveWithExpiry() {
    map.put(1, "foo", currentTime.plusMillis(1).toEpochMilli());
    assertTrue(map.containsKey(1));
    assertTrue(map.containsValue("foo"));
    assertEquals("foo", map.get(1));
    assertEquals(1, map.size());
    assertFalse(map.isEmpty());

    String removed = map.remove(1);
    assertEquals("foo", removed);
    assertFalse(map.containsKey(1));
    assertFalse(map.containsValue("foo"));
    assertNull(map.get(1));
    assertEquals(0, map.size());
    assertTrue(map.isEmpty());

    assertNull(map.remove(1));
  }

  @Test
  void itemIsExpiredAfterExpiry() {
    Instant futureTime = Instant.now().plusSeconds(10);
    map.put(1, "foo", futureTime.toEpochMilli());
    assertTrue(map.containsKey(1));
    assertEquals("foo", map.get(1));
    currentTime = futureTime;
    assertFalse(map.containsKey(1));
  }

  @Test
  void itemIsMissingAfterExpiry() {
    Instant futureTime = Instant.now().plusSeconds(10);
    map.put(1, "foo", futureTime.toEpochMilli());
    assertTrue(map.containsKey(1));
    assertEquals("foo", map.get(1));
    currentTime = futureTime;
    assertNull(map.get(1));
  }

  @Test
  void addingExpiredItemRemovesExisting() {
    map.put(1, "foo");
    String prev = map.put(1, "bar", 0);
    assertEquals("foo", prev);
    assertFalse(map.containsKey(1));
  }

  @Test
  void doesNotExpireItemThatWasReplaced() {
    Instant futureTime = Instant.now().plusSeconds(10);
    map.put(1, "foo", futureTime.toEpochMilli());
    map.put(1, "bar", futureTime.plusSeconds(1).toEpochMilli());
    currentTime = futureTime;
    assertTrue(map.containsKey(1));
    assertEquals("bar", map.get(1));
  }
}
