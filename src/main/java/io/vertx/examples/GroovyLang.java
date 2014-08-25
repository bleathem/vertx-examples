package io.vertx.examples;

import com.sun.source.tree.LambdaExpressionTree;
import io.vertx.codegen.TypeInfo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class GroovyLang implements Lang {

  static class GroovyRenderer extends Renderer {
    LinkedHashSet<TypeInfo.Class> imports = new LinkedHashSet<>();
    GroovyRenderer(Lang lang) {
      super(lang);
    }
  }

  @Override
  public void renderBlock(List<StatementBuilder> statements, Renderer renderer) {
    if (renderer instanceof GroovyRenderer) {
      Lang.super.renderBlock(statements, renderer);
    } else {
      GroovyRenderer langRenderer = new GroovyRenderer(this);
      Lang.super.renderBlock(statements, langRenderer);
      for (TypeInfo.Class importedType : langRenderer.imports) {
        renderer.append("import ").append(importedType.getName().replace("io.vertx.", "io.vertx.groovy.")).append('\n');
      }
      renderer.append(langRenderer.getBuffer());
    }
  }

  @Override
  public String getExtension() {
    return "groovy";
  }

  // Marker class for Groovy Strings
  static abstract class GStringLiteralBuilder extends ExpressionBuilder {

    @Override
    public final void render(Renderer renderer) {
      renderer.append('"');
      renderCharacters(renderer);
      renderer.append('"');
    }

    protected abstract void renderCharacters(Renderer renderer);
  }

  private static GStringLiteralBuilder gstring(Consumer<Renderer> characters) {
    return new GStringLiteralBuilder() {
      @Override
      protected void renderCharacters(Renderer renderer) {
        characters.accept(renderer);
      }
    };
  }

  @Override
  public ExpressionBuilder stringLiteral(String value) {
    return gstring(renderer -> Lang.super.renderCharacters(value, renderer));
  }

  @Override
  public ExpressionBuilder combine(ExpressionBuilder left, String op, ExpressionBuilder right) {
    if (left instanceof GStringLiteralBuilder) {
      GStringLiteralBuilder gleft = (GStringLiteralBuilder) left;
      if (right instanceof GStringLiteralBuilder) {
        GStringLiteralBuilder gright = (GStringLiteralBuilder) right;
        return gstring(renderer -> {
          gleft.renderCharacters(renderer);
          gright.renderCharacters(renderer);
        });
      } else {
        return gstring(renderer -> {
          gleft.renderCharacters(renderer);
          renderer.append("${");
          right.render(renderer);
          renderer.append("}");
        });
      }
    } else if (right instanceof GStringLiteralBuilder) {
      GStringLiteralBuilder gright = (GStringLiteralBuilder) right;
      return gstring(renderer -> {
        renderer.append("${");
        left.render(renderer);
        renderer.append("}");
        gright.renderCharacters(renderer);
      });
    } else {
      return Lang.super.combine(left, op, right);
    }
  }

  @Override
  public ExpressionBuilder classExpression(TypeInfo.Class type) {
    return ExpressionBuilder.render(type.getName());
  }

  @Override
  public ExpressionBuilder lambda(LambdaExpressionTree.BodyKind bodyKind, List<TypeInfo> parameterTypes, List<String> parameterNames, CodeBuilder body) {
    return ExpressionBuilder.render(renderer -> {
      renderer.append("{");
      for (int i = 0;i < parameterNames.size();i++) {
        if (i == 0) {
          renderer.append(" ");
        } else {
          renderer.append(", ");
        }
        renderer.append(parameterNames.get(i));
      }
      renderer.append(" ->\n");
      renderer.indent();
      body.render(renderer);
      renderer.unindent();
      renderer.append("}");
    });
  }

  @Override
  public ExpressionBuilder asyncResult(String identifier) {
    return ExpressionBuilder.render(renderer -> renderer.append(identifier));
  }

  @Override
  public ExpressionBuilder asyncResultHandler(LambdaExpressionTree.BodyKind bodyKind, String resultName, CodeBuilder body) {
    return lambda(null, null, Arrays.asList(resultName), body);
  }

  @Override
  public ExpressionBuilder staticFactory(TypeInfo.Class type, String methodName) {
    return ExpressionBuilder.render(renderer -> {
      GroovyRenderer jsRenderer = (GroovyRenderer) renderer;
      jsRenderer.imports.add(type);
      renderer.append(type.getSimpleName()).append('.').append(methodName);
    });
  }

  @Override
  public StatementBuilder variable(TypeInfo type, String name, ExpressionBuilder initializer) {
    return StatementBuilder.render(renderer -> {
      renderer.append("def ").append(name);
      if (initializer != null) {
        renderer.append(" = ");
        initializer.render(renderer);
      }
    });
  }

  @Override
  public StatementBuilder enhancedForLoop(String variableName, ExpressionBuilder expression, StatementBuilder body) {
    return StatementBuilder.render(renderer -> {
      expression.render(renderer);
      renderer.append(".each { ").append(variableName).append(" ->\n");
      renderer.indent();
      body.render(renderer);
      renderer.unindent();
      renderer.append("}");
    });
  }

  @Override
  public StatementBuilder forLoop(StatementBuilder initializer, ExpressionBuilder condition, ExpressionBuilder update, StatementBuilder body) {
    return StatementBuilder.render(renderer -> {
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
  public ExpressionBuilder options(TypeInfo.Class optionType) {
    return new OptionsExpressionBuilder() {
      @Override
      public void render(Renderer renderer) {
        renderJsonObject(getMembers(), renderer);
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

  private void renderJsonObject(Iterable<Member> members, Renderer renderer) {
    Iterator<Member> iterator = members.iterator();
    if (iterator.hasNext()) {
      renderer.append("[");
      while (iterator.hasNext()) {
        Member member = iterator.next();
        renderer.append(member.name);
        renderer.append(":");
        if (member instanceof Member.Single) {
          ((Member.Single) member).value.render(renderer);
        } else {
          renderJsonArray(((Member.Array) member).values, renderer);
        }
        if (iterator.hasNext()) {
          renderer.append(", ");
        }
      }
      renderer.append("]");
    } else {
      renderer.append("[:]");
    }
  }

  private void renderJsonArray(List<ExpressionBuilder> values, Renderer renderer) {
    renderer.append('[');
    for (int i = 0;i < values.size();i++) {
      if (i > 0) {
        renderer.append(", ");
      }
      values.get(i).render(renderer);
    }
    renderer.append(']');
  }

  @Override
  public ExpressionBuilder console(ExpressionBuilder expression) {
    return ExpressionBuilder.render(renderer -> {
      renderer.append("println(");
      expression.render(renderer);
      renderer.append(")");
    });
  }
}
