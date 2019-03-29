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

import net.consensys.cava.bytes.Bytes;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A request which returns a 'source' type result (e.g. opens up a stream that is followed by the request ID.)
 */
public class RPCStreamRequest {

  private final RPCFunction function;
  private final List<Object> arguments;

  /**
   * The details for the function (the name of the function and its arguments.)
   * @param function the function to be invoked
   * @param arguments the arguments for the function (can be any arbitrary class which can be marshalled into JSON.)
   */
  public RPCStreamRequest(RPCFunction function, List<Object> arguments) {
    this.function = function;
    this.arguments = arguments;
  }

  /**
   * @return The byte representation for the request after it is marshalled into a JSON string.
   * @throws JsonProcessingException if an error was thrown while marshalling to JSON
   */
  public Bytes toEncodedRpcMessage() throws JsonProcessingException {
    RPCRequestBody body = new RPCRequestBody(function.asList(), RPCRequestType.SOURCE, arguments);
    return RPCCodec.encodeRequest(body.asBytes(), getRPCFlags());
  }

  /**
   * @return The correct RPC flags for a stream request
   */
  public RPCFlag[] getRPCFlags() {
    return new RPCFlag[] {RPCFlag.Stream.STREAM, RPCFlag.BodyType.JSON};
  }

}
