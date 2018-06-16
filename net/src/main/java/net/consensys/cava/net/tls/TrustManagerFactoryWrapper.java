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

import io.vertx.core.Vertx;
import io.vertx.core.net.TrustOptions;

final class TrustManagerFactoryWrapper implements TrustOptions {

  private final TrustManagerFactory trustManagerFactory;

  TrustManagerFactoryWrapper(TrustManagerFactory trustManagerFactory) {
    this.trustManagerFactory = trustManagerFactory;
  }

  @Override
  public TrustOptions clone() {
    return new TrustManagerFactoryWrapper(trustManagerFactory);
  }

  @Override
  public TrustManagerFactory getTrustManagerFactory(Vertx vertx) {
    return trustManagerFactory;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TrustManagerFactoryWrapper)) {
      return false;
    }
    TrustManagerFactoryWrapper other = (TrustManagerFactoryWrapper) obj;
    return trustManagerFactory.equals(other.trustManagerFactory);
  }

  @Override
  public int hashCode() {
    return trustManagerFactory.hashCode();
  }
}
