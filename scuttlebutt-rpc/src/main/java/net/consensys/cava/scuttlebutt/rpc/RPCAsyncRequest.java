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
import com.fasterxml.jackson.databind.ObjectMapper;

public class RPCAsyncRequest {


  private final RPCFunction function;
  private final List<Object> arguments;

  /**
   *
   * @param function the function to be in invoked. If the function is in a namespace, the first n-1 items in the array
   *        are the namespace followed by the function name (e.g. 'blobs.get' becomes ['blobs', 'get']).
   * @param arguments The arguments passed to the function being invoked. Each item can be any arbitrary object which is
   *        JSON serializable (e.g. String, Int, list, object.)
   *
   */
  public RPCAsyncRequest(RPCFunction function, List<Object> arguments) {
    this.function = function;
    this.arguments = arguments;
  }

  /**
   * Encode the RPC request as bytes.
   *
   * @param objectMapper the object mapper to serialize the request with
   * @return an RPC request serialized into bytes
   * @throws JsonProcessingException thrown if there is an error while serializing the request to bytes
   */
  public Bytes toEncodedRpcMessage(ObjectMapper objectMapper) throws JsonProcessingException {
    return RPCCodec.encodeRequest(
        new RPCRequestBody(function.asList(), RPCRequestType.ASYNC, arguments).asBytes(objectMapper),
        getRPCFlags());
  }

  public RPCFlag[] getRPCFlags() {
    return new RPCFlag[] {RPCFlag.BodyType.JSON};
  }

}
