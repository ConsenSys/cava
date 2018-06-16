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
package net.consensys.cava.net.tls;

import javax.net.ssl.TrustManagerFactory;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.vertx.core.Vertx;
import io.vertx.core.net.TrustOptions;

final class InsecureTrustOptions implements TrustOptions {

  static InsecureTrustOptions INSTANCE = new InsecureTrustOptions();

  private InsecureTrustOptions() {}

  @Override
  public TrustOptions clone() {
    return this;
  }

  @Override
  public TrustManagerFactory getTrustManagerFactory(Vertx vertx) {
    return InsecureTrustManagerFactory.INSTANCE;
  }
}
