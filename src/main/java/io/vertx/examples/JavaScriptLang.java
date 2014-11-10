package io.vertx.examples;

import com.sun.source.tree.LambdaExpressionTree;
import io.vertx.codegen.Helper;
import io.vertx.codegen.TypeInfo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class JavaScriptLang implements Lang {

  static class JavaScriptRenderer extends CodeWriter {
    LinkedHashSet<TypeInfo.Class> modules = new LinkedHashSet<>();
    JavaScriptRenderer(Lang lang) {
      super(lang);
    }
  }

  @Override
  public void renderBlock(List<StatementBuilder> statements, CodeWriter writer) {
    if (writer instanceof JavaScriptRenderer) {
      Lang.super.renderBlock(statements, writer);
    } else {
      JavaScriptRenderer langRenderer = new JavaScriptRenderer(this);
      Lang.super.renderBlock(statements, langRenderer);
      for (TypeInfo.Class module : langRenderer.modules) {
        writer.append("var ").append(module.getSimpleName()).append(" = require(\"").
            append(module.getModuleName()).append("-js/").append(Helper.convertCamelCaseToUnderscores(module.getSimpleName())).append("\");\n");
      }
      writer.append(langRenderer.getBuffer());
    }
  }

  @Override
  public String getExtension() {
    return "js";
  }

  @Override
  public ExpressionBuilder classExpression(TypeInfo.Class type) {
    return ExpressionBuilder.render("Java.type(\"" + type.getName() + "\")");
  }

  @Override
  public ExpressionBuilder console(ExpressionBuilder expression) {
    return ExpressionBuilder.render(renderer -> {
      renderer.append("console.log(");
      expression.render(renderer);
      renderer.append(")");
    });
  }

  @Override
  public ExpressionBuilder options(TypeInfo.Class optionType) {
    return new OptionsExpressionBuilder() {
      @Override
      public void render(CodeWriter writer) {
        renderJsonObject(getMembers(), writer);
      }
    };
  }

  @Override
  public ExpressionBuilder jsonObject() {
    return new JsonObjectLiteralExpressionBuilder(this, this::renderJsonObject);
  }

  @Override
  public ExpressionBuilder jsonArray() {
    return new JsonArrayLiteralExpressionBuilder(this::renderJsonArray);
  }

  private void renderJsonObject(Iterable<Member> members, CodeWriter writer) {
    writer.append("{");
    for (Iterator<Member> iterator = members.iterator();iterator.hasNext();) {
      Member member = iterator.next();
      writer.append("\"").append(member.name).append("\" : ");
      if (member instanceof Member.Single) {
        ((Member.Single) member).value.render(writer);
      } else {
        renderJsonArray(((Member.Array) member).values, writer);
      }
      if (iterator.hasNext()) {
        writer.append(", ");
      }
    }
    writer.append("}");
  }

  private void renderJsonArray(List<ExpressionBuilder> values, CodeWriter writer) {
    writer.append('[');
    for (int i = 0;i < values.size();i++) {
      if (i > 0) {
        writer.append(", ");
      }
      values.get(i).render(writer);
    }
    writer.append(']');
  }

  @Override
  public ExpressionBuilder asyncResultHandler(LambdaExpressionTree.BodyKind bodyKind, String resultName, CodeBuilder body) {
    return lambda(null, null, Arrays.asList(resultName, resultName + "_err"), body);
  }

  @Override
  public ExpressionBuilder lambda(LambdaExpressionTree.BodyKind bodyKind, List<TypeInfo> parameterTypes, List<String> parameterNames, CodeBuilder body) {
    return ExpressionBuilder.render((renderer) -> {
      renderer.append("function (");
      for (int i = 0; i < parameterNames.size(); i++) {
        if (i > 0) {
          renderer.append(", ");
        }
        renderer.append(parameterNames.get(i));
      }
      renderer.append(") {\n");
      renderer.indent();
      body.render(renderer);
      renderer.unindent();
      renderer.append("}");
    });
  }

  @Override
  public ExpressionBuilder staticFactory(TypeInfo.Class type, String methodName) {
    return ExpressionBuilder.render(renderer -> {
      JavaScriptRenderer jsRenderer = (JavaScriptRenderer) renderer;
      jsRenderer.modules.add(type);
      renderer.append(type.getSimpleName()).append('.').append(methodName);
    });
  }

  @Override
  public StatementBuilder variable(TypeInfo type, String name, ExpressionBuilder initializer) {
    return StatementBuilder.render(renderer -> {
      renderer.append("var ").append(name);
      if (initializer != null) {
        renderer.append(" = ");
        initializer.render(renderer);
      }
    });
  }

  @Override
  public StatementBuilder enhancedForLoop(String variableName, ExpressionBuilder expression, StatementBuilder body) {
    return StatementBuilder.render((renderer) -> {
      renderer.append("Array.prototype.forEach.call(");
      expression.render(renderer);
      renderer.append(", function(").append(variableName).append(") {\n");
      renderer.indent();
      body.render(renderer);
      renderer.unindent();
      renderer.append("})");
    });
  }

  @Override
  public StatementBuilder forLoop(StatementBuilder initializer, ExpressionBuilder condition, ExpressionBuilder update, StatementBuilder body) {
    return StatementBuilder.render((renderer) -> {
      renderer.append("for (");
      initializer.render(renderer);
      renderer.append(';');
      condition.render(renderer);
      renderer.append(';');
      update.render(renderer);
      renderer.append(") {\n");
      renderer.indent();
      body.render(renderer);
      renderer.unindent();
      renderer.append("}");
    });
  }

  @Override
  public ExpressionBuilder asyncResult(String identifier) {
    return ExpressionBuilder.forMemberSelect((member) -> {
      switch (member) {
        case "succeeded":
          return ExpressionBuilder.forMethodInvocation((args) -> ExpressionBuilder.render("(" + identifier + " != null)"));
        case "result":
          return ExpressionBuilder.forMethodInvocation((args) -> ExpressionBuilder.render(identifier));
        case "cause":
          return ExpressionBuilder.forMethodInvocation((args) -> ExpressionBuilder.render(identifier + "_err"));
        case "failed":
          return ExpressionBuilder.forMethodInvocation((args) -> ExpressionBuilder.render("(" + identifier + " == null)"));
        default:
          throw new UnsupportedOperationException("Not implemented");
      }
    });
  }
}
