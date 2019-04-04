/*
 * Copyright 2019 ConsenSys AG.
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
package net.consensys.cava.scuttlebutt.rpc;

/**
 * An RPC message response body which contains an error
 */
public class RPCErrorBody {

  private String name;
  private String message;
  private String stack;

  public RPCErrorBody() {

  }

  /**
   * A description of an error that occurred while performing an RPC request.
   *
   * @param name the name of the error type
   * @param message the message describing the error
   * @param stack the stack trace from the error
   */
  public RPCErrorBody(String name, String message, String stack) {
    this.name = name;
    this.message = message;
    this.stack = stack;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getStack() {
    return stack;
  }

  public void setStack(String stack) {
    this.stack = stack;
  }
}
