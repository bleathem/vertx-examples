package io.vertx.examples;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ExpressionBuilder extends CodeBuilder {

  public ExpressionBuilder onMemberSelect(String identifier) {
    if (identifier.equals("equals")) {
      return ExpressionBuilder.forMethodInvocation(args -> {
        if (args.size() == 1) {
          return ExpressionBuilder.render(renderer -> {
            renderer.getLang().renderEquals(ExpressionBuilder.this, args.get(0), renderer);
          });
        } else {
          throw new UnsupportedOperationException("Not yet implemented"); // Equals overloading
        }
      });
    } else {
      return ExpressionBuilder.render((renderer) -> {
        renderer.getLang().renderMemberSelect(ExpressionBuilder.this, identifier, renderer);
      });
    }
  }

  public ExpressionBuilder onNew(List<ExpressionBuilder> arguments) {
    throw new UnsupportedOperationException("Not implemented with arguments " + arguments);
  }

  public ExpressionBuilder onMethodInvocation(List<ExpressionBuilder> arguments) {
    return ExpressionBuilder.render((renderer) -> {
      renderer.getLang().renderMethodInvocation(ExpressionBuilder.this, arguments, renderer);
    });
  }

  public ExpressionBuilder onPostFixIncrement() {
    return ExpressionBuilder.render((renderer) -> {
      renderer.getLang().renderPostFixIncrement(ExpressionBuilder.this, renderer);
    });
  }

  public static ExpressionBuilder forNew(Function<List<ExpressionBuilder>, ExpressionBuilder> f) {
    return new ExpressionBuilder() {
      @Override
      public ExpressionBuilder onNew(List<ExpressionBuilder> arguments) {
        return f.apply(arguments);
      }
    };
  }

  public static ExpressionBuilder forMemberSelect(String expected, Supplier<ExpressionBuilder> f) {
    return new ExpressionBuilder() {
      @Override
      public ExpressionBuilder onMemberSelect(String identifier) {
        if (expected.equals(identifier)) {
          return f.get();
        } else {
          throw new UnsupportedOperationException();
        }
      }
    };
  }

  public static ExpressionBuilder forMemberSelect(Function<String, ExpressionBuilder> f) {
    return new ExpressionBuilder() {
      @Override
      public ExpressionBuilder onMemberSelect(String identifier) {
        return f.apply(identifier);
      }
    };
  }

  public static ExpressionBuilder forParenthesized(ExpressionBuilder expression) {
    return ExpressionBuilder.render((renderer) -> {
      renderer.getLang().renderParenthesized(expression, renderer);
    });
  }

  public static ExpressionBuilder forConditionalExpression(ExpressionBuilder condition, ExpressionBuilder trueExpression, ExpressionBuilder falseExpression) {
    return ExpressionBuilder.render((renderer) -> {
      renderer.getLang().renderConditionalExpression(condition, trueExpression, falseExpression, renderer);
    });
  }

  public static ExpressionBuilder forAssign(ExpressionBuilder variable, ExpressionBuilder expression) {
    return ExpressionBuilder.render((renderer) -> {
      renderer.getLang().renderAssign(variable, expression, renderer);
    });
  }

  public static ExpressionBuilder forMethodInvocation(Function<List<ExpressionBuilder>, ExpressionBuilder> f) {
    return new ExpressionBuilder() {
      @Override
      public ExpressionBuilder onMethodInvocation(List<ExpressionBuilder> arguments) {
        return f.apply(arguments);
      }
    };
  }

  public static ExpressionBuilder render(Consumer<CodeWriter> c) {
    return new ExpressionBuilder() {
      @Override
      public void render(CodeWriter writer) {
        c.accept(writer);
      }
    };
  }

  public static ExpressionBuilder render(Supplier<String> f) {
    return new ExpressionBuilder() {
      @Override
      public void render(CodeWriter writer) {
        writer.append(f.get());
      }
    };
  }

  public static ExpressionBuilder render(String s) {
    return new ExpressionBuilder() {
      @Override
      public void render(CodeWriter writer) {
        writer.append(s);
      }
    };
  }
}
