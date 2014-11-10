package io.vertx.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JsonObjectModel extends ExpressionModel {

  public static ExpressionModel CLASS_MODEL = ExpressionModel.forNew(args -> {
    switch (args.size()) {
      case 0:
        return new JsonObjectModel();
      default:
        throw new UnsupportedOperationException();
    }
  });

  private String member;
  private List<Member> entries = new ArrayList<>();

  public JsonObjectModel() {
  }

  public Iterable<Member> getMembers() {
    return entries;
  }

  @Override
  public ExpressionModel onMemberSelect(String identifier) {
    this.member = identifier;
    return this;
  }

  @Override
  public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
    switch (member) {
      case "put":
        entries.add(new Member.Single(arguments.get(0)).append(arguments.get(1)));
        break;
      default:
        throw new UnsupportedOperationException("Method " + member + " not yet implemented");
    }
    return this;
  }

  @Override
  public void render(CodeWriter writer) {
    writer.getLang().renderJsonObject(this, writer);
  }
}
