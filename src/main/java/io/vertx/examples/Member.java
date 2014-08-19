package io.vertx.examples;

import java.util.ArrayList;
import java.util.List;

/**
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
public abstract class Member {

  final String name;

  protected abstract Member append(ExpressionBuilder builder);

  public Member(String name) {
    this.name = name;
  }

  public static class Single extends Member {

    ExpressionBuilder value;

    public Single(String name) {
      super(name);
    }

    @Override
    protected Member append(ExpressionBuilder value) {
      this.value = value;
      return this;
    }
  }

  public static class Array extends Member {

    List<ExpressionBuilder> values = new ArrayList<>();

    public Array(String name) {
      super(name);
    }

    @Override
    protected Member append(ExpressionBuilder value) {
      this.values.add(value);
      return this;
    }
  }
}
