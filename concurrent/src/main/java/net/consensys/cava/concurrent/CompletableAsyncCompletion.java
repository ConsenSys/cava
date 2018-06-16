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

/**
 * An {@link AsyncCompletion} that can later be completed successfully or with a provided exception.
 */
public interface CompletableAsyncCompletion extends AsyncCompletion {

  /**
   * Complete this completion.
   *
   * @return <tt>true</tt> if this invocation caused this completion to transition to a completed state, else
   *         <tt>false</tt>.
   */
  boolean complete();

  /**
   * Complete this completion with the given exception.
   *
   * @param ex The exception to complete this result with.
   * @return <tt>true</tt> if this invocation caused this completion to transition to a completed state, else
   *         <tt>false</tt>.
   */
  boolean completeExceptionally(Throwable ex);
}
