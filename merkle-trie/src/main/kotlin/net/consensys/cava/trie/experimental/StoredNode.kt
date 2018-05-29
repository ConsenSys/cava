package net.consensys.cava.trie.experimental

import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import net.consensys.cava.bytes.Bytes
import net.consensys.cava.bytes.Bytes32
import net.consensys.cava.rlp.RLP
import java.lang.ref.SoftReference
import java.util.concurrent.atomic.AtomicReference

internal class StoredNode<V> : Node<V> {
  private val nodeFactory: StoredNodeFactory<V>
  private val hash: Bytes32
  private var loaded: SoftReference<Node<V>>? = null
  private val loader = AtomicReference<Deferred<Node<V>>>()

  constructor(nodeFactory: StoredNodeFactory<V>, hash: Bytes32) {
    this.nodeFactory = nodeFactory
    this.hash = hash
  }

  constructor(nodeFactory: StoredNodeFactory<V>, node: Node<V>) {
    this.nodeFactory = nodeFactory
    this.hash = node.hash()
    this.loaded = SoftReference(node)
  }

  override suspend fun accept(visitor: NodeVisitor<V>, path: Bytes): Node<V> {
    val node = load()
    val resultNode = node.accept(visitor, path)
    if (node === resultNode) {
      return this
    }
    return resultNode
  }

  override suspend fun path(): Bytes = load().path()

  override suspend fun value(): V? = load().value()

  // Getting the rlp representation is only needed when persisting a concrete node
  override fun rlp(): Bytes = throw UnsupportedOperationException()

  override fun rlpRef(): Bytes {
    val loadedNode = loaded?.get()
    if (loadedNode != null) {
      return loadedNode.rlpRef()
    }
    // If this node was stored, then it must have a rlp larger than a hash
    return RLP.encodeValue(hash)
  }

  override fun hash(): Bytes32 = hash

  override suspend fun replacePath(path: Bytes): Node<V> = load().replacePath(path)

  private suspend fun load(): Node<V> {
    val loadedNode = loaded?.get()
    if (loadedNode != null) {
      return loadedNode
    }

    val deferred: Deferred<Node<V>> = async(start = CoroutineStart.LAZY) {
      val node = nodeFactory.retrieve(hash)
      loaded = SoftReference(node)
      loader.set(null)
      node
    }

    while (!loader.compareAndSet(null, deferred)) {
      // already loading
      val prevDeferred = loader.get()
      if (prevDeferred != null) {
        return prevDeferred.await()
      }
    }

    // we've set the loader

    // check for a loaded node again, in case a loader just completed
    val node = loaded?.get()
    if (node != null) {
      // remove our loader, if it's still set
      loader.compareAndSet(deferred, null)
      return node
    }

    return deferred.await()
  }

  fun unload() {
    loaded = null
  }
}