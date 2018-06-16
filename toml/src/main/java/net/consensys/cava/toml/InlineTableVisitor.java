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

import net.consensys.cava.toml.internal.TomlParser.KeyContext;
import net.consensys.cava.toml.internal.TomlParser.KeyvalContext;
import net.consensys.cava.toml.internal.TomlParser.ValContext;
import net.consensys.cava.toml.internal.TomlParserBaseVisitor;

import java.util.List;

final class InlineTableVisitor extends TomlParserBaseVisitor<MutableTomlTable> {

  private final MutableTomlTable table = new MutableTomlTable();

  @Override
  public MutableTomlTable visitKeyval(KeyvalContext ctx) {
    KeyContext keyContext = ctx.key();
    ValContext valContext = ctx.val();
    if (keyContext != null && valContext != null) {
      List<String> path = keyContext.accept(new KeyVisitor());
      if (path != null && !path.isEmpty()) {
        Object value = valContext.accept(new ValueVisitor());
        if (value != null) {
          table.set(path, value, new TomlPosition(ctx));
        }
      }
    }
    return table;
  }

  @Override
  protected MutableTomlTable aggregateResult(MutableTomlTable aggregate, MutableTomlTable nextResult) {
    return table;
  }

  @Override
  protected MutableTomlTable defaultResult() {
    return table;
  }
}
