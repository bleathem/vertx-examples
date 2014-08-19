package io.vertx.examples;

import com.sun.source.tree.LambdaExpressionTree;
import io.vertx.codegen.TypeInfo;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Lang {

  default void renderIfThenElse(ExpressionBuilder condition, StatementBuilder thenBody, StatementBuilder elseBody, Renderer renderer) {
    renderer.append("if ");
    condition.render(renderer);
    renderer.append(" {\n");
    renderer.indent();
    thenBody.render(renderer);
    renderer.unindent();
    renderer.append("}");
    if (elseBody != null) {
      renderer.append(" else {\n");
      renderer.indent();
      elseBody.render(renderer);
      renderer.unindent();
      renderer.append("}");
    }
  }

  default void renderParenthesized(ExpressionBuilder expression, Renderer renderer) {
    renderer.append('(');
    expression.render(renderer);
    renderer.append(')');
  }

  default void renderEquals(ExpressionBuilder expression, ExpressionBuilder arg, Renderer renderer) {
    expression.render(renderer);
    renderer.append(" == ");
    arg.render(renderer);
  }

  default void renderConditionalExpression(ExpressionBuilder condition, ExpressionBuilder trueExpression, ExpressionBuilder falseExpression, Renderer renderer) {
    condition.render(renderer);
    renderer.append(" ? ");
    trueExpression.render(renderer);
    renderer.append(" : ");
    falseExpression.render(renderer);
  }

  default void renderAssign(ExpressionBuilder variable, ExpressionBuilder expression, Renderer renderer) {
    variable.render(renderer);
    renderer.append(" = ");
    expression.render(renderer);
  }

  default void renderBlock(List<StatementBuilder> statements, Renderer renderer) {
    statements.forEach(statement -> {
      statement.render(renderer);
      renderer.append(";\n");
    });
  }

  default void renderMemberSelect(ExpressionBuilder expression, String identifier, Renderer renderer) {
    expression.render(renderer);
    renderer.append('.').append(identifier);
  }

  default void renderMethodInvocation(ExpressionBuilder expression, List<ExpressionBuilder> arguments, Renderer renderer) {
    expression.render(renderer);
    renderer.append('(');
    for (int i = 0; i < arguments.size(); i++) {
      if (i > 0) {
        renderer.append(", ");
      }
      arguments.get(i).render(renderer);
    }
    renderer.append(')');
  }

  default void renderBinary(ExpressionBuilder left, String op, ExpressionBuilder right, Renderer renderer) {
    left.render(renderer);
    renderer.append(" ").append(op).append(" ");
    right.render(renderer);
  }

  default void renderStringLiteral(String value, Renderer renderer) {
    renderer.append('"');
    for (int i = 0;i < value.length();i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\n':
          renderer.append("\\n");
          break;
        case '"':
          renderer.append("\\\"");
          break;
        case '\\':
          renderer.append("\\\\");
          break;
        default:
          renderer.append(c);
      }
    }
    renderer.append('"');
  }

  default void renderBooleanLiteral(String value, Renderer renderer) {
    renderer.append(value);
  }

  default void renderIntegerLiteral(String value, Renderer renderer) {
    renderer.append(value);
  }

  default void renderPostFixIncrement(ExpressionBuilder expression, Renderer renderer) {
    expression.render(renderer);
    renderer.append("++");
  }

  //

  String getExtension();

  //

  ExpressionBuilder classExpression(TypeInfo.Class type);

  ExpressionBuilder lambda(LambdaExpressionTree.BodyKind bodyKind, List<TypeInfo> parameterTypes, List<String> parameterNames, CodeBuilder body);

  ExpressionBuilder asyncResult(String identifier);

  ExpressionBuilder asyncResultHandler(LambdaExpressionTree.BodyKind bodyKind, String resultName, CodeBuilder body);

  ExpressionBuilder staticFactory(TypeInfo.Class type, String methodName);

  StatementBuilder variable(TypeInfo type, String name, ExpressionBuilder initializer);

  StatementBuilder enhancedForLoop(String variableName, ExpressionBuilder expression, StatementBuilder body);

  StatementBuilder forLoop(StatementBuilder initializer, ExpressionBuilder condition, ExpressionBuilder update, StatementBuilder body);

  //

  ExpressionBuilder options(TypeInfo.Class optionType);

  ExpressionBuilder jsonObject();

  ExpressionBuilder jsonArray();

  ExpressionBuilder console(ExpressionBuilder expression);

}
