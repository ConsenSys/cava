/*
 * Copyright 2018, ConsenSys Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.consensys.cava.trie.experimental

import net.consensys.cava.bytes.Bytes
import net.consensys.cava.bytes.Bytes32
import net.consensys.cava.trie.CompactEncoding.bytesToPath
import java.util.function.Function

internal fun bytesIdentity(b: Bytes): Bytes = b
internal fun stringSerializer(s: String): Bytes = Bytes.wrap(s.toByteArray(Charsets.UTF_8))
internal fun stringDeserializer(b: Bytes): String = String(b.toArrayUnsafe(), Charsets.UTF_8)

/**
 * An in-memory [MerkleTrie].
 *
 * @param <V> The type of values stored by this trie.
 * @param valueSerializer A function for serializing values to bytes.
 * @constructor Creates an empty trie.
 */
class MerklePatriciaTrie<V>(valueSerializer: (V) -> Bytes) : MerkleTrie<Bytes, V> {

  companion object {
    /**
     * Create a trie with keys and values of type [Bytes].
     */
    @JvmStatic
    fun storingBytes(): MerklePatriciaTrie<Bytes> = MerklePatriciaTrie(::bytesIdentity)

    /**
     * Create a trie with value of type [String].
     *
     * Strings are stored in UTF-8 encoding.
     */
    @JvmStatic
    fun storingStrings(): MerklePatriciaTrie<String> = MerklePatriciaTrie(::stringSerializer)
  }

  private val getVisitor = GetVisitor<V>()
  private val removeVisitor = RemoveVisitor<V>()
  private val nodeFactory: DefaultNodeFactory<V> = DefaultNodeFactory(valueSerializer)
  private var root: Node<V> = NullNode.instance()

  /**
   * Creates an empty trie.
   *
   * @param valueSerializer A function for serializing values to bytes.
   */
  constructor(valueSerializer: Function<V, Bytes>) : this(valueSerializer::apply)

  override suspend fun get(key: Bytes): V? = root.accept(getVisitor, bytesToPath(key)).value()

  override suspend fun put(key: Bytes, value: V?) {
    if (value == null) {
      return remove(key)
    }
    this.root = root.accept(PutVisitor(nodeFactory, value), bytesToPath(key))
  }

  override suspend fun remove(key: Bytes) {
    this.root = root.accept(removeVisitor, bytesToPath(key))
  }

  override fun rootHash(): Bytes32 = root.hash()

  /**
   * @return A string representation of the object.
   */
  override fun toString(): String {
    return javaClass.simpleName + "[" + rootHash() + "]"
  }
}
