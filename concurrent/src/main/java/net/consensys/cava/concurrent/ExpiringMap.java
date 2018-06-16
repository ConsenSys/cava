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

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;

/**
 * A concurrent hash map that stores values along with an expiry.
 *
 * Values are stored in the map until their expiry is reached, after which they will no longer be available and will
 * appear as if removed. The actual removal is done lazily whenever the map is accessed, or when the
 * {@link #purgeExpired()} method is invoked.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public final class ExpiringMap<K, V> implements Map<K, V> {

  private static final long MAX_EXPIRY = Long.MAX_VALUE;

  // Uses object equality, to ensure uniqueness as a value in the storage map
  private static final class ExpiringEntry<K, V> implements Comparable<ExpiringEntry<K, V>> {
    private K key;
    private V value;
    private long expiry;

    ExpiringEntry(K key, V value, long expiry) {
      this.key = key;
      this.value = value;
      this.expiry = expiry;
    }

    @Override
    public int compareTo(ExpiringEntry<K, V> o) {
      return Long.compare(expiry, o.expiry);
    }
  }

  private final ConcurrentHashMap<K, ExpiringEntry<K, V>> storage = new ConcurrentHashMap<>();
  private final PriorityBlockingQueue<ExpiringEntry<K, V>> expiryQueue = new PriorityBlockingQueue<>();
  private final LongSupplier currentTimeSupplier;

  /**
   * Construct an empty map.
   */
  public ExpiringMap() {
    this(System::currentTimeMillis);
  }

  @VisibleForTesting
  ExpiringMap(LongSupplier currentTimeSupplier) {
    this.currentTimeSupplier = currentTimeSupplier;
  }

  @Override
  public V get(Object key) {
    requireNonNull(key);
    purgeExpired();
    ExpiringEntry<K, V> entry = storage.get(key);
    return (entry == null) ? null : entry.value;
  }

  @Override
  public boolean containsKey(Object key) {
    requireNonNull(key);
    purgeExpired();
    return storage.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    requireNonNull(value);
    purgeExpired();
    return storage.values().stream().anyMatch(e -> e.value.equals(value));
  }

  @Override
  public int size() {
    purgeExpired();
    return storage.size();
  }

  @Override
  public boolean isEmpty() {
    purgeExpired();
    return storage.isEmpty();
  }

  @Override
  public V put(K key, V value) {
    requireNonNull(key);
    requireNonNull(value);
    purgeExpired();
    ExpiringEntry<K, V> oldEntry = storage.put(key, new ExpiringEntry<>(key, value, MAX_EXPIRY));
    return (oldEntry == null) ? null : oldEntry.value;
  }

  /**
   * Associates the specified value with the specified key in this map, and expires the entry when the specified expiry
   * time is reached. If the map previously contained a mapping for the key, the old value is replaced by the specified
   * value.
   *
   * @param key The key with which the specified value is to be associated.
   * @param value The value to be associated with the specified key.
   * @param expiry The expiry time for the value, in milliseconds since the epoch.
   * @return The previous value associated with {@code key}, or {@code null} if there was no mapping for {@code key}.
   */
  public synchronized V put(K key, V value, long expiry) {
    requireNonNull(key);
    requireNonNull(value);
    if (expiry >= MAX_EXPIRY) {
      return put(key, value);
    }

    long now = currentTimeSupplier.getAsLong();
    if (expiry <= now) {
      return remove(key);
    }

    purgeExpired(now);
    ExpiringEntry<K, V> newEntry = new ExpiringEntry<>(key, value, expiry);
    ExpiringEntry<K, V> oldEntry = storage.put(key, newEntry);
    expiryQueue.offer(newEntry);
    return (oldEntry == null) ? null : oldEntry.value;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    requireNonNull(m);
    purgeExpired();
    for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
      storage.put(e.getKey(), new ExpiringEntry<>(e.getKey(), e.getValue(), MAX_EXPIRY));
    }
  }

  @Override
  public V remove(Object key) {
    requireNonNull(key);
    purgeExpired();
    ExpiringEntry<K, V> entry = storage.remove(key);
    if (entry == null) {
      return null;
    }
    if (entry.expiry < MAX_EXPIRY) {
      expiryQueue.remove(entry);
    }
    return entry.value;
  }

  @Override
  public synchronized boolean remove(Object key, Object value) {
    requireNonNull(key);
    requireNonNull(value);
    purgeExpired();
    ExpiringEntry<K, V> entry = storage.get(key);
    if (entry == null || !value.equals(entry.value)) {
      return false;
    }
    storage.remove(key);
    if (entry.expiry < MAX_EXPIRY) {
      expiryQueue.remove(entry);
    }
    return true;
  }

  @Override
  public synchronized void clear() {
    expiryQueue.clear();
    storage.clear();
  }

  @Override
  public Set<K> keySet() {
    purgeExpired();
    return storage.keySet();
  }

  @Override
  public Collection<V> values() {
    purgeExpired();
    return storage.values().stream().map(e -> e.value).collect(Collectors.toList());
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    purgeExpired();
    return storage.entrySet().stream().map(e -> new Map.Entry<K, V>() {
      @Override
      public K getKey() {
        return e.getKey();
      }

      @Override
      public V getValue() {
        return e.getValue().value;
      }

      @Override
      public V setValue(V value) {
        throw new UnsupportedOperationException();
      }
    }).collect(Collectors.toSet());
  }

  /**
   * Force immediate expiration of any key/value pairs that have reached their expiry.
   */
  public void purgeExpired() {
    purgeExpired(currentTimeSupplier.getAsLong());
  }

  private synchronized void purgeExpired(long oldest) {
    ExpiringEntry<K, V> head;
    while ((head = expiryQueue.peek()) != null && head.expiry <= oldest) {
      // only remove if it's still mapped to the same entry (object equality is used)
      storage.remove(head.key, head);
      expiryQueue.remove();
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ExpiringMap)) {
      return false;
    }
    ExpiringMap other = (ExpiringMap) obj;
    return storage.equals(other.storage);
  }

  @Override
  public int hashCode() {
    return storage.hashCode();
  }
}
