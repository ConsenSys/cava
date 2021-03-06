/*
 * Copyright 2018 ConsenSys AG.
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
package net.consensys.cava.devp2p

import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KProperty

// Extension methods that allow an AtomicLong to be treated as a Long property

internal operator fun AtomicLong.getValue(thisRef: Any?, property: KProperty<*>): Long = this.get()

internal operator fun AtomicLong.setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
  this.set(value)
}

internal operator fun AtomicLong.inc(): AtomicLong {
  this.incrementAndGet()
  return this
}

internal operator fun AtomicLong.dec(): AtomicLong {
  this.decrementAndGet()
  return this
}
