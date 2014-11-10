package io.vertx.examples;

import com.sun.source.tree.LambdaExpressionTree;
import io.vertx.codegen.TypeInfo;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface Lang {

  default void renderIfThenElse(ExpressionBuilder condition, StatementBuilder thenBody, StatementBuilder elseBody, CodeWriter writer) {
    writer.append("if ");
    condition.render(writer);
    writer.append(" {\n");
    writer.indent();
    thenBody.render(writer);
    writer.unindent();
    writer.append("}");
    if (elseBody != null) {
      writer.append(" else {\n");
      writer.indent();
      elseBody.render(writer);
      writer.unindent();
      writer.append("}");
    }
  }

  default void renderParenthesized(ExpressionBuilder expression, CodeWriter writer) {
    writer.append('(');
    expression.render(writer);
    writer.append(')');
  }

  default void renderEquals(ExpressionBuilder expression, ExpressionBuilder arg, CodeWriter writer) {
    expression.render(writer);
    writer.append(" == ");
    arg.render(writer);
  }

  default void renderConditionalExpression(ExpressionBuilder condition, ExpressionBuilder trueExpression, ExpressionBuilder falseExpression, CodeWriter writer) {
    condition.render(writer);
    writer.append(" ? ");
    trueExpression.render(writer);
    writer.append(" : ");
    falseExpression.render(writer);
  }

  default void renderAssign(ExpressionBuilder variable, ExpressionBuilder expression, CodeWriter writer) {
    variable.render(writer);
    writer.append(" = ");
    expression.render(writer);
  }

  default void renderBlock(List<StatementBuilder> statements, CodeWriter writer) {
    statements.forEach(statement -> {
      statement.render(writer);
      writer.append(";\n");
    });
  }

  default void renderMemberSelect(ExpressionBuilder expression, String identifier, CodeWriter writer) {
    expression.render(writer);
    writer.append('.').append(identifier);
  }

  default void renderMethodInvocation(ExpressionBuilder expression, List<ExpressionBuilder> arguments, CodeWriter writer) {
    expression.render(writer);
    writer.append('(');
    for (int i = 0; i < arguments.size(); i++) {
      if (i > 0) {
        writer.append(", ");
      }
      arguments.get(i).render(writer);
    }
    writer.append(')');
  }

  default void renderBinary(ExpressionBuilder left, String op, ExpressionBuilder right, CodeWriter writer) {
    left.render(writer);
    writer.append(" ").append(op).append(" ");
    right.render(writer);
  }

  default void renderCharacters(String value, CodeWriter writer) {
    for (int i = 0;i < value.length();i++) {
      char c = value.charAt(i);
      switch (c) {
        case '\n':
          writer.append("\\n");
          break;
        case '"':
          writer.append("\\\"");
          break;
        case '\\':
          writer.append("\\\\");
          break;
        default:
          writer.append(c);
      }
    }
  }

  default void renderStringLiteral(String value, CodeWriter writer) {
    writer.append('"');
    renderCharacters(value, writer);
    writer.append('"');
  }

  default void renderBooleanLiteral(String value, CodeWriter writer) {
    writer.append(value);
  }

  default void renderIntegerLiteral(String value, CodeWriter writer) {
    writer.append(value);
  }

  default void renderPostFixIncrement(ExpressionBuilder expression, CodeWriter writer) {
    expression.render(writer);
    writer.append("++");
  }

  //

  String getExtension();

  //

  default ExpressionBuilder stringLiteral(String value) {
    return ExpressionBuilder.render(renderer -> renderer.getLang().renderStringLiteral(value, renderer));
  }

  default ExpressionBuilder combine(ExpressionBuilder left, String op, ExpressionBuilder right) {
    return ExpressionBuilder.render(renderer -> renderer.getLang().renderBinary(left, op, right, renderer));
  }

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
