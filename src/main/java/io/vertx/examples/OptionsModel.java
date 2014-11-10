package io.vertx.examples;

import io.vertx.codegen.TypeInfo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class OptionsModel extends ExpressionModel {

  public static ExpressionModel create(TypeInfo.Class type) {
    return ExpressionModel.forNew(args -> new OptionsModel(type));
  }

  private final TypeInfo.Class type;
  private final OptionsModel parent;
  private final String name;
  private final ExpressionModel value;
  private final boolean plural;

  private OptionsModel(TypeInfo.Class type) {
    this(type, null, null, null, false);
  }

  private OptionsModel(TypeInfo.Class type, OptionsModel parent, String name, ExpressionModel value, boolean plural) {
    this.parent = parent;
    this.name = name;
    this.value = value;
    this.plural = plural;
    this.type = type;
  }

  public TypeInfo.Class getType() {
    return type;
  }

  public Iterable<Member> getMembers() {
    return getMemberMap().values();
  }

  private Map<String, Member> getMemberMap() {
    if (parent == null) {
      return new LinkedHashMap<>();
    } else {
      Map<String, Member> members = parent.getMemberMap();
      ExpressionModel nameExpr = ExpressionModel.render(name);
      Member member = members.computeIfAbsent(name, name -> plural ? new Member.Array(nameExpr) : new Member.Single(nameExpr));
      member.append(value);
      return members;
    }
  }

  @Override
  public ExpressionModel onMemberSelect(String identifier) {
    String name;
    boolean plural;
    if (identifier.length() > 3 && identifier.startsWith("set")) {
      name = Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4);
      plural = false;
    } else if (identifier.length() > 3 && identifier.startsWith("add")) {
      name = Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4) + "s"; // 's' for plural
      plural = true;
    } else {
      throw new UnsupportedOperationException("Not implemented");
    }
    return new ExpressionModel() {
      @Override
      public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
        if (arguments.size() == 1) {
          ExpressionModel value = arguments.get(0);
          return new OptionsModel(type, OptionsModel.this, name, value, plural);
        } else {
          throw new UnsupportedOperationException("not yet implemented");
        }
      }
    };
  }

  public void render(CodeWriter writer) {
    writer.getLang().renderOptions(this, writer);
  }
}
