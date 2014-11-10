package io.vertx.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonArrayLiteralExpressionModel extends ExpressionModel {

  private final BiConsumer<List<ExpressionModel>, CodeWriter> renderer;
  private String member;
  private List<ExpressionModel> values = new ArrayList<>();

  public JsonArrayLiteralExpressionModel(BiConsumer<List<ExpressionModel>, CodeWriter> renderer) {
    this.renderer = renderer;
  }

  @Override
  public ExpressionModel onMemberSelect(String identifier) {
    this.member = identifier;
    return this;
  }

  @Override
  public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
    switch (member) {
      case "add":
        values.add(arguments.get(0));
        break;
      default:
        throw new UnsupportedOperationException("Method " + member + " not yet implemented");
    }
    return this;
  }

  @Override
  public void render(CodeWriter writer) {
    this.renderer.accept(values, writer);
  }
}
