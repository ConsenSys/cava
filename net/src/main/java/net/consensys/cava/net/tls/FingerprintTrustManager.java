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

import net.consensys.cava.bytes.Bytes;

import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

import com.google.common.hash.Hashing;
import io.netty.util.internal.EmptyArrays;

final class FingerprintTrustManager implements X509TrustManager {

  static FingerprintTrustManager record(Path repositoryPath) {
    return new FingerprintTrustManager(repositoryPath, true);
  }

  static FingerprintTrustManager whitelist(Path repositoryPath) {
    return new FingerprintTrustManager(repositoryPath, false);
  }

  private final FingerprintRepository repository;
  private final boolean acceptNewFingerprints;

  private FingerprintTrustManager(Path repositoryPath, boolean acceptNewFingerprints) {
    this.repository = new FingerprintRepository(repositoryPath);
    this.acceptNewFingerprints = acceptNewFingerprints;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    checkTrusted(chain, authType, true);
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    checkTrusted(chain, authType, false);
  }

  void checkTrusted(X509Certificate[] chain, String authType, boolean isClient) throws CertificateException {
    X509Certificate cert = chain[0];
    Bytes fingerprint = Bytes.wrap(Hashing.sha256().hashBytes(cert.getEncoded()).asBytes());
    if (repository.contains(fingerprint)) {
      return;
    }

    if (acceptNewFingerprints) {
      repository.addFingerprint(fingerprint);
      return;
    }

    throw new CertificateException(
        "Certificate for "
            + cert.getSubjectDN()
            + " with unknown fingerprint "
            + fingerprint.toHexString().substring(2).toLowerCase());
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return EmptyArrays.EMPTY_X509_CERTIFICATES;
  }
}
