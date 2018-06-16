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

import net.consensys.cava.toml.internal.TomlParser.BasicUnescapedContext;
import net.consensys.cava.toml.internal.TomlParser.EscapedContext;
import net.consensys.cava.toml.internal.TomlParser.LiteralBodyContext;
import net.consensys.cava.toml.internal.TomlParser.MlBasicUnescapedContext;
import net.consensys.cava.toml.internal.TomlParser.MlLiteralBodyContext;
import net.consensys.cava.toml.internal.TomlParserBaseVisitor;

final class QuotedStringVisitor extends TomlParserBaseVisitor<StringBuilder> {

  private final StringBuilder builder = new StringBuilder();

  @Override
  public StringBuilder visitLiteralBody(LiteralBodyContext ctx) {
    return builder.append(ctx.getText());
  }

  @Override
  public StringBuilder visitMlLiteralBody(MlLiteralBodyContext ctx) {
    return builder.append(ctx.getText());
  }

  @Override
  public StringBuilder visitBasicUnescaped(BasicUnescapedContext ctx) {
    return builder.append(ctx.getText());
  }

  @Override
  public StringBuilder visitMlBasicUnescaped(MlBasicUnescapedContext ctx) {
    return builder.append(ctx.getText());
  }

  @Override
  public StringBuilder visitEscaped(EscapedContext ctx) {
    String text = ctx.getText();
    if (text.isEmpty()) {
      return builder;
    }
    assert (text.charAt(0) == '\\');
    if (text.length() == 1) {
      return builder.append('\\');
    }
    switch (text.charAt(1)) {
      case '"':
        return builder.append('"');
      case '\\':
        return builder.append('\\');
      case 'b':
        return builder.append('\b');
      case 'f':
        return builder.append('\f');
      case 'n':
        return builder.append('\n');
      case 'r':
        return builder.append('\r');
      case 't':
        return builder.append('\t');
      case 'u':
        assert (text.length() == 6);
        return builder.append(convertUnicodeEscape(text.substring(2), ctx));
      case 'U':
        assert (text.length() == 10);
        return builder.append(convertUnicodeEscape(text.substring(2), ctx));
      default:
        throw new TomlParseError("Invalid escape sequence '" + text + "'", new TomlPosition(ctx));
    }
  }

  private char[] convertUnicodeEscape(String hexChars, EscapedContext ctx) {
    try {
      return Character.toChars(Integer.parseInt(hexChars, 16));
    } catch (IllegalArgumentException e) {
      throw new TomlParseError("Invalid unicode escape sequence", new TomlPosition(ctx));
    }
  }

  @Override
  protected StringBuilder aggregateResult(StringBuilder aggregate, StringBuilder nextResult) {
    return aggregate == null ? null : nextResult;
  }

  @Override
  protected StringBuilder defaultResult() {
    return builder;
  }
}
