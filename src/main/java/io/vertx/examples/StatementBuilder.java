package io.vertx.examples;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StatementBuilder extends CodeBuilder {

  public static StatementBuilder ifThenElse(ExpressionBuilder condition, StatementBuilder thenBody, StatementBuilder elseBody) {
    return StatementBuilder.render((renderer) -> {
      renderer.getLang().renderIfThenElse(condition, thenBody, elseBody, renderer);
    });
  }

  public static StatementBuilder block(List<StatementBuilder> statements) {
    return StatementBuilder.render(renderer -> renderer.getLang().renderBlock(statements, renderer));
  }

  public static StatementBuilder render(Consumer<Renderer> c) {
    return new StatementBuilder() {
      @Override
      public void render(Renderer renderer) {
        c.accept(renderer);
      }
    };
  }

  public static StatementBuilder render(String s) {
    return new StatementBuilder() {
      @Override
      public void render(Renderer renderer) {
        renderer.append(s);
      }
    };
  }
}
