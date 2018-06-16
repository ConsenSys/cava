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
import net.consensys.cava.crypto.Hash.keccak256
import net.consensys.cava.rlp.RLP

internal class NullNode<V> private constructor() : Node<V> {

  companion object {
    private val RLP_NULL = RLP.encodeByteArray(ByteArray(0))
    private val HASH = keccak256(RLP_NULL)
    private val instance = NullNode<Any>()

    @Suppress("UNCHECKED_CAST")
    fun <V> instance(): NullNode<V> = instance as NullNode<V>
  }

  override suspend fun accept(visitor: NodeVisitor<V>, path: Bytes): Node<V> = visitor.visit(this, path)

  override suspend fun path(): Bytes = Bytes.EMPTY

  override suspend fun value(): V? = null

  override fun rlp(): Bytes = RLP_NULL

  override fun rlpRef(): Bytes = RLP_NULL

  override fun hash(): Bytes32 = HASH

  override suspend fun replacePath(path: Bytes): Node<V> = this
}
