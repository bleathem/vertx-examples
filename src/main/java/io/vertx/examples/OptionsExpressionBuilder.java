package io.vertx.examples;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class OptionsExpressionBuilder extends ExpressionBuilder {

  private Member member;
  private LinkedHashMap<String, Member> members = new LinkedHashMap<>();

  public Iterable<Member> getMembers() {
    return members.values();
  }

  @Override
  public ExpressionBuilder onMemberSelect(String identifier) {
    if (identifier.length() > 3 && identifier.startsWith("set")) {
      String name = Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4);
      member = members.get(name);
      if (member == null) {
        members.put(name, member = new Member.Single(name));
      }
    } else if (identifier.length() > 3 && identifier.startsWith("add")) {
      String name = Character.toLowerCase(identifier.charAt(3)) + identifier.substring(4) + "s"; // 's' for plural
      member = members.get(name);
      if (member == null) {
        members.put(name, member = new Member.Array(name));
      }
    } else {
      throw new UnsupportedOperationException("Not implemented");
    }
    return this;
  }

  @Override
  public ExpressionBuilder onMethodInvocation(List<ExpressionBuilder> arguments) {
    if (arguments.size() == 1) {
      member.append(arguments.get(0));
      member = null;
    } else {
      throw new UnsupportedOperationException("not yet implemented");
    }
    return this;
  }
}
