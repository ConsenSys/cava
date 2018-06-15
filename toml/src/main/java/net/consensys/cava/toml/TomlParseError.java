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
package net.consensys.cava.toml;

/**
 * An error that occurred while parsing.
 */
public final class TomlParseError extends RuntimeException {

  private final TomlPosition position;

  TomlParseError(String message, TomlPosition position) {
    super(message);
    this.position = position;
  }

  TomlParseError(String message, TomlPosition position, Throwable cause) {
    super(message, cause);
    this.position = position;
  }

  /**
   * @return The position in the input where the error occurred.
   */
  public TomlPosition position() {
    return position;
  }

  @Override
  public String toString() {
    return getMessage() + " (" + position + ")";
  }
}
