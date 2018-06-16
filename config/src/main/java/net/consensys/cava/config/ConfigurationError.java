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
package net.consensys.cava.config;

import javax.annotation.Nullable;

/**
 * Provides details regarding an error in the configuration.
 */
public final class ConfigurationError extends RuntimeException {

  @Nullable
  private final DocumentPosition position;

  /**
   * Construct an error.
   *
   * @param message The error message.
   */
  public ConfigurationError(String message) {
    this(null, message);
  }

  /**
   * Construct an error.
   *
   * @param message The error message.
   * @param cause The cause of the error.
   */
  public ConfigurationError(String message, @Nullable Throwable cause) {
    this(null, message, cause);
  }

  /**
   * Construct an error.
   *
   * @param position The location of the error in the configuration document.
   * @param message The error message.
   */
  public ConfigurationError(@Nullable DocumentPosition position, String message) {
    super(message);
    this.position = position;
  }

  /**
   * Construct an error.
   *
   * @param position The location of the error in the configuration document.
   * @param message The error message.
   * @param cause The cause of the error.
   */
  public ConfigurationError(@Nullable DocumentPosition position, String message, @Nullable Throwable cause) {
    super(message, cause);
    this.position = position;
  }

  /**
   * @return The position in the input where the error occurred, or <tt>null</tt> if no position information is
   *         available.
   */
  @Nullable
  public DocumentPosition position() {
    return position;
  }

  @Override
  public String toString() {
    if (position == null) {
      return getMessage();
    }
    return getMessage() + " (" + position + ")";
  }
}
