package io.vertx.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonArrayExpressionModel extends ExpressionModel {

  public static final ExpressionModel CLASS_MODEL = ExpressionModel.forNew(args -> {
    switch (args.size()) {
      case 0:
        return new JsonArrayExpressionModel();
      default:
        throw new UnsupportedOperationException();
    }
  });

  private String member;
  private List<ExpressionModel> values = new ArrayList<>();

  public JsonArrayExpressionModel() {
  }

  public List<ExpressionModel> getValues() {
    return values;
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
    writer.getLang().renderJsonArray(this, writer);
  }
}
