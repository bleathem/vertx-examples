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
  public void renderBlock(List<StatementModel> statements, CodeWriter writer) {
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
  public ExpressionModel classExpression(TypeInfo.Class type) {
    return ExpressionModel.render("Java.type(\"" + type.getName() + "\")");
  }

  @Override
  public ExpressionModel console(ExpressionModel expression) {
    return ExpressionModel.render(renderer -> {
      renderer.append("console.log(");
      expression.render(renderer);
      renderer.append(")");
    });
  }

  @Override
  public ExpressionModel options(TypeInfo.Class optionType) {
    return new OptionsExpressionModel() {
      @Override
      public void render(CodeWriter writer) {
        renderJsonObject(getMembers(), writer);
      }
    };
  }

  @Override
  public ExpressionModel jsonObject() {
    return new JsonObjectLiteralExpressionModel(this, this::renderJsonObject);
  }

  @Override
  public ExpressionModel jsonArray() {
    return new JsonArrayLiteralExpressionModel(this::renderJsonArray);
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

  private void renderJsonArray(List<ExpressionModel> values, CodeWriter writer) {
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
  public ExpressionModel asyncResultHandler(LambdaExpressionTree.BodyKind bodyKind, String resultName, CodeModel body) {
    return lambda(null, null, Arrays.asList(resultName, resultName + "_err"), body);
  }

  @Override
  public ExpressionModel lambda(LambdaExpressionTree.BodyKind bodyKind, List<TypeInfo> parameterTypes, List<String> parameterNames, CodeModel body) {
    return ExpressionModel.render((renderer) -> {
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
  public ExpressionModel staticFactory(TypeInfo.Class type, String methodName) {
    return ExpressionModel.render(renderer -> {
      JavaScriptRenderer jsRenderer = (JavaScriptRenderer) renderer;
      jsRenderer.modules.add(type);
      renderer.append(type.getSimpleName()).append('.').append(methodName);
    });
  }

  @Override
  public StatementModel variable(TypeInfo type, String name, ExpressionModel initializer) {
    return StatementModel.render(renderer -> {
      renderer.append("var ").append(name);
      if (initializer != null) {
        renderer.append(" = ");
        initializer.render(renderer);
      }
    });
  }

  @Override
  public StatementModel enhancedForLoop(String variableName, ExpressionModel expression, StatementModel body) {
    return StatementModel.render((renderer) -> {
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
  public StatementModel forLoop(StatementModel initializer, ExpressionModel condition, ExpressionModel update, StatementModel body) {
    return StatementModel.render((renderer) -> {
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
  public ExpressionModel asyncResult(String identifier) {
    return ExpressionModel.forMemberSelect((member) -> {
      switch (member) {
        case "succeeded":
          return ExpressionModel.forMethodInvocation((args) -> ExpressionModel.render("(" + identifier + " != null)"));
        case "result":
          return ExpressionModel.forMethodInvocation((args) -> ExpressionModel.render(identifier));
        case "cause":
          return ExpressionModel.forMethodInvocation((args) -> ExpressionModel.render(identifier + "_err"));
        case "failed":
          return ExpressionModel.forMethodInvocation((args) -> ExpressionModel.render("(" + identifier + " == null)"));
        default:
          throw new UnsupportedOperationException("Not implemented");
      }
    });
  }
}
