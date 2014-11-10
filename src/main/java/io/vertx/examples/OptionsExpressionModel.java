package io.vertx.examples;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class OptionsExpressionModel extends ExpressionModel {

  private Member member;
  private LinkedHashMap<String, Member> members = new LinkedHashMap<>();

  public Iterable<Member> getMembers() {
    return members.values();
  }

  @Override
  public ExpressionModel onMemberSelect(String identifier) {
    if (identifier.length() > 3 && identifier.startsWith("set")) {
      String name = Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4);
      member = members.get(name);
      if (member == null) {
        members.put(name, member = new Member.Single(ExpressionModel.render(name)));
      }
    } else if (identifier.length() > 3 && identifier.startsWith("add")) {
      String name = Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4) + "s"; // 's' for plural
      member = members.get(name);
      if (member == null) {
        members.put(name, member = new Member.Array(ExpressionModel.render(name)));
      }
    } else {
      throw new UnsupportedOperationException("Not implemented");
    }
    return this;
  }

  @Override
  public ExpressionModel onMethodInvocation(List<ExpressionModel> arguments) {
    if (arguments.size() == 1) {
      member.append(arguments.get(0));
      member = null;
    } else {
      throw new UnsupportedOperationException("not yet implemented");
    }
    return this;
  }
}
