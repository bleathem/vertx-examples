package io.vertx.examples;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonObjectLiteralExpressionBuilder extends ExpressionBuilder {

  private final BiConsumer<Iterable<Member>, CodeWriter> renderer;
  private final Lang lang;
  private String member;
  private LinkedHashMap<String, Member> entries = new LinkedHashMap<>();

  public JsonObjectLiteralExpressionBuilder(Lang lang, BiConsumer<Iterable<Member>, CodeWriter> renderer) {
    this.lang = lang;
    this.renderer = renderer;
  }

  @Override
  public ExpressionBuilder onMemberSelect(String identifier) {
    this.member = identifier;
    return this;
  }

  @Override
  public ExpressionBuilder onMethodInvocation(List<ExpressionBuilder> arguments) {
    switch (member) {
      case "put":
        String name = unwrapQuotedString(arguments.get(0).render(lang));
        entries.put(name, new Member.Single(name).append(arguments.get(1)));
        break;
      default:
        throw new UnsupportedOperationException("Method " + member + " not yet implemented");
    }
    return this;
  }

  static String unwrapQuotedString(String s) {
    int len = s.length();
    if (len > 2 && s.charAt(0) == '"' && s.charAt(len - 1) == '"') {
      return s.substring(1, len - 1);
    } else {
      throw new IllegalArgumentException("Illegal quoted string " + s);
    }
  }

  @Override
  public void render(CodeWriter writer) {
    this.renderer.accept(entries.values(), writer);
  }
}
