package io.vertx.examples;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import io.vertx.codegen.ClassKind;
import io.vertx.codegen.TypeInfo;
import io.vertx.core.AbstractVerticle;
import org.junit.Assert;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ConvertingProcessor extends AbstractProcessor {

  private static final javax.tools.JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
  private static final Locale locale = Locale.getDefault();
  private static final Charset charset = Charset.forName("UTF-8");

  public static Map<String, String> convert(ClassLoader loader, Lang lang, String... sources) throws Exception {
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StandardJavaFileManager manager = javac.getStandardFileManager(diagnostics, locale, charset);
    List<File> files = new ArrayList<>();
    for (String source : sources) {
      URL url = loader.getResource(source);
      if (url == null) {
        throw new Exception("Cannot resolve source " + source + "");
      }
      Assert.assertNotNull(url);
      files.add(new File(url.toURI()));
    }
    Iterable<? extends JavaFileObject> fileObjects = manager.getJavaFileObjects(files.toArray(new File[files.size()]));
    StringWriter out = new StringWriter();
    JavaCompiler.CompilationTask task = javac.getTask(
        out,
        manager,
        diagnostics,
        Collections.<String>emptyList(),
        Collections.<String>emptyList(),
        fileObjects);
    task.setLocale(locale);
    ConvertingProcessor processor = new ConvertingProcessor(lang);
    task.setProcessors(Collections.<Processor>singletonList(processor));
    if (task.call()) {
      return processor.getResult();
    } else {
      StringWriter message = new StringWriter();
      PrintWriter writer = new PrintWriter(message);
      writer.append("Compilation of ").append(Arrays.toString(sources)).println(" failed:");
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics())  {
        writer.append(diagnostic.getMessage(locale));
      }
      writer.println("console:");
      writer.append(out.getBuffer());
      throw new Exception(message.toString());
    }
  }

  private Map<String, String> result = new HashMap<>();
  private Trees trees;
  private DeclaredType AbstractVerticleType;
  private DeclaredType SystemType;
  private Attr attr;
  private Lang lang;

  public ConvertingProcessor(Lang lang) {
    this.lang = lang;
  }

  public Map<String, String> getResult() {
    return result;
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton("*");
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.trees = Trees.instance(processingEnv);
    this.AbstractVerticleType = (DeclaredType) processingEnv.getElementUtils().getTypeElement(AbstractVerticle.class.getName()).asType();
    this.SystemType = (DeclaredType) processingEnv.getElementUtils().getTypeElement(System.class.getName()).asType();
    Context context = ((JavacProcessingEnvironment)processingEnv).getContext();
    this.attr = Attr.instance(context);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element rootElt : roundEnv.getRootElements()) {
      if (processingEnv.getTypeUtils().isAssignable(rootElt.asType(), AbstractVerticleType)) {
        for (Element enclosedElt : rootElt.getEnclosedElements()) {
          if (enclosedElt instanceof ExecutableElement) {
            ExecutableElement exeElt = (ExecutableElement) enclosedElt;
            if (exeElt.getSimpleName().toString().equals("start") && exeElt.getParameters().isEmpty()) {
              attributeClass(rootElt);
              TreePath path = trees.getPath(exeElt);
              TreePathScanner<CodeBuilder, VisitContext> visitor = new TreePathScanner<CodeBuilder, VisitContext>() {

                public StatementBuilder scan(StatementTree tree, VisitContext visitContext) {
                  return (StatementBuilder) scan((Tree) tree, visitContext);
                }

                public ExpressionBuilder scan(ExpressionTree tree, VisitContext visitContext) {
                  return (ExpressionBuilder) scan((Tree) tree, visitContext);
                }

                @Override
                public CodeBuilder visitForLoop(ForLoopTree node, VisitContext p) {
                  if (node.getInitializer().size() != 1) {
                    throw new UnsupportedOperationException();
                  }
                  if (node.getUpdate().size() != 1) {
                    throw new UnsupportedOperationException();
                  }
                  StatementBuilder initializer = scan(node.getInitializer().get(0), p);
                  ExpressionBuilder update = scan(node.getUpdate().get(0).getExpression(), p);
                  StatementBuilder body = scan(node.getStatement(), p);
                  ExpressionBuilder condition = scan(node.getCondition(), p);
                  return lang.forLoop(initializer, condition, update, body);
                }

                @Override
                public CodeBuilder visitEnhancedForLoop(EnhancedForLoopTree node, VisitContext p) {
                  ExpressionBuilder expression = scan(node.getExpression(), p);
                  StatementBuilder body = scan(node.getStatement(), p);
                  return lang.enhancedForLoop(node.getVariable().getName().toString(), expression, body);
                }

                @Override
                public CodeBuilder visitAssignment(AssignmentTree node, VisitContext context) {
                  ExpressionBuilder variable = scan(node.getVariable(), context);
                  ExpressionBuilder expression = scan(node.getExpression(), context);
                  return ExpressionBuilder.forAssign(variable, expression);
                }

                @Override
                public StatementBuilder visitVariable(VariableTree node, VisitContext p) {
                  JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) node;
                  ExpressionBuilder initializer;
                  if (node.getInitializer() != null) {
                    initializer = scan(node.getInitializer(), p);
                  } else {
                    initializer = null;
                  }
                  TypeInfo type = TypeInfo.create(processingEnv.getElementUtils(), processingEnv.getTypeUtils(), Collections.emptyList(), decl.type);
                  return lang.variable(
                      type,
                      decl.name.toString(),
                      initializer
                  );
                }

                @Override
                public StatementBuilder visitIf(IfTree node, VisitContext visitContext) {
                  ExpressionBuilder condition = scan(node.getCondition(), visitContext);
                  StatementBuilder thenBody = scan(node.getThenStatement(), visitContext);
                  StatementBuilder elseBody = node.getElseStatement() != null ? scan(node.getElseStatement(), visitContext) : null;
                  return StatementBuilder.ifThenElse(condition, thenBody, elseBody);
                }

                @Override
                public CodeBuilder visitConditionalExpression(ConditionalExpressionTree node, VisitContext visitContext) {
                  ExpressionBuilder condition = scan(node.getCondition(), visitContext);
                  ExpressionBuilder trueExpression = scan(node.getTrueExpression(), visitContext);
                  ExpressionBuilder falseExpression = scan(node.getFalseExpression(), visitContext);
                  return ExpressionBuilder.forConditionalExpression(condition, trueExpression, falseExpression);
                }

                @Override
                public ExpressionBuilder visitUnary(UnaryTree node, VisitContext p) {
                  ExpressionBuilder expression = scan(node.getExpression(), p);
                  switch (node.getKind()) {
                    case POSTFIX_INCREMENT:
                      return expression.onPostFixIncrement();
                    default:
                      throw new UnsupportedOperationException();
                  }
                }

                @Override
                public CodeBuilder visitExpressionStatement(ExpressionStatementTree node, VisitContext context) {
                  ExpressionBuilder expression = scan(node.getExpression(), context);
                  return StatementBuilder.render(renderer -> expression.render(renderer));
                }

                @Override
                public ExpressionBuilder visitBinary(BinaryTree node, VisitContext p) {
                  ExpressionBuilder left = scan(node.getLeftOperand(), p);
                  ExpressionBuilder right = scan(node.getRightOperand(), p);
                  String op;
                  switch (node.getKind()) {
                    case PLUS:
                      op = "+";
                      break;
                    case LESS_THAN:
                      op = "<";
                      break;
                    case MULTIPLY:
                      op = "*";
                      break;
                    default:
                      throw new UnsupportedOperationException();
                  }
                  return ExpressionBuilder.render(renderer -> renderer.getLang().renderBinary(left, op, right, renderer));
                }

                @Override
                public ExpressionBuilder visitLiteral(LiteralTree node, VisitContext p) {
                  switch (node.getKind()) {
                    case STRING_LITERAL:
                      return ExpressionBuilder.render(renderer -> renderer.getLang().renderStringLiteral(node.getValue().toString(), renderer));
                    case BOOLEAN_LITERAL:
                      return ExpressionBuilder.render(renderer -> renderer.getLang().renderBooleanLiteral(node.getValue().toString(), renderer));
                    case INT_LITERAL:
                      return ExpressionBuilder.render(renderer -> renderer.getLang().renderIntegerLiteral(node.getValue().toString(), renderer));
                    default:
                      throw new UnsupportedOperationException();
                  }
                }

                @Override
                public ExpressionBuilder visitIdentifier(IdentifierTree node, VisitContext context) {
                  JCTree.JCIdent ident = (JCTree.JCIdent) node;
                  if (ident.sym instanceof TypeElement) {
                    if (ident.type.equals(SystemType)) {
                      return ExpressionBuilder.forMemberSelect("out", () ->
                          ExpressionBuilder.forMemberSelect("println", () ->
                              ExpressionBuilder.forMethodInvocation(args -> lang.console(args.get(0)))));
                    } else {
                      TypeInfo.Class type = (TypeInfo.Class) TypeInfo.create(processingEnv.getElementUtils(), processingEnv.getTypeUtils(), Collections.emptyList(), ident.type);
                      if (type.getKind() == ClassKind.API) {
                        return ExpressionBuilder.forMemberSelect((identifier) -> lang.staticFactory(type, identifier));
                      } else if (type.getKind() == ClassKind.JSON_OBJECT) {
                        return ExpressionBuilder.forNew(args -> {
                          switch (args.size()) {
                            case 0:
                              return lang.jsonObject();
                            default:
                              throw new UnsupportedOperationException();
                          }
                        });
                      } else if (type.getKind() == ClassKind.JSON_ARRAY) {
                        return ExpressionBuilder.forNew(args -> {
                          switch (args.size()) {
                            case 0:
                              return lang.jsonArray();
                            default:
                              throw new UnsupportedOperationException();
                          }
                        });
                      } else if (type.getKind() == ClassKind.OPTIONS) {
                        return ExpressionBuilder.forMemberSelect("options", () -> ExpressionBuilder.forMethodInvocation(args -> lang.options(type)));
                      } else {
                        return lang.classExpression(type);
                      }
                    }
                  } else {
                    ExpressionBuilder alias = context.getAlias(ident.sym);
                    if (alias != null) {
                      return alias;
                    } else {
                      return ExpressionBuilder.render(node.getName().toString());
                    }
                  }
                }

                @Override
                public CodeBuilder visitNewClass(NewClassTree node, VisitContext visitContext) {
                  ExpressionBuilder identifier = scan(node.getIdentifier(), visitContext);
                  List<ExpressionBuilder> arguments = node.getArguments().stream().map(arg -> scan(arg, visitContext)).collect(Collectors.toList());
                  return identifier.onNew(arguments);
                }

                @Override
                public CodeBuilder visitParenthesized(ParenthesizedTree node, VisitContext visitContext) {
                  ExpressionBuilder expression = scan(node.getExpression(), visitContext);
                  return ExpressionBuilder.forParenthesized(expression);
                }

                @Override
                public ExpressionBuilder visitMemberSelect(MemberSelectTree node, VisitContext p) {
                  ExpressionBuilder expression = scan(node.getExpression(), p);
                  return expression.onMemberSelect(node.getIdentifier().toString());
                }

                @Override
                public ExpressionBuilder visitMethodInvocation(MethodInvocationTree node, VisitContext p) {
                  ExpressionBuilder methodSelect = scan(node.getMethodSelect(), p);
                  List<ExpressionBuilder> arguments = node.getArguments().stream().map(argument -> scan(argument, p)).collect(Collectors.toList());
                  return methodSelect.onMethodInvocation(arguments);
                }

                @Override
                public StatementBuilder visitBlock(BlockTree node, VisitContext p) {
                  List<StatementBuilder> statements = node.getStatements().stream().map((statement) -> scan(statement, p)).collect(Collectors.toList());
                  return StatementBuilder.block(statements);
                }

                @Override
                public ExpressionBuilder visitLambdaExpression(LambdaExpressionTree node, VisitContext p) {
                  List<String> parameterNames = node.getParameters().stream().map(parameter -> parameter.getName().toString()).collect(Collectors.toList());
                  List<TypeInfo> parameterTypes = node.getParameters().stream().
                      map(parameter -> TypeInfo.create(processingEnv.getElementUtils(), processingEnv.getTypeUtils(), Collections.emptyList(), ((JCTree.JCVariableDecl) parameter).type)).
                      collect(Collectors.toList());
                  int size = parameterNames.size();
                  if (size > 0) {
                    JCTree.JCVariableDecl last = (JCTree.JCVariableDecl) node.getParameters().get(size - 1);
                    if (last.vartype instanceof JCTree.JCTypeApply) {
                      JCTree.JCTypeApply typeApply = (JCTree.JCTypeApply) last.vartype;
                      if (typeApply.clazz instanceof JCTree.JCFieldAccess) {
                        JCTree.JCFieldAccess clazz = (JCTree.JCFieldAccess) typeApply.clazz;
                        Symbol.ClassSymbol sym = (Symbol.ClassSymbol) clazz.sym;
                        TypeInfo type = TypeInfo.create(processingEnv.getElementUtils(), processingEnv.getTypeUtils(), Collections.emptyList(), sym.type);
                        if (type.getKind() == ClassKind.ASYNC_RESULT) {
                          ExpressionBuilder result = lang.asyncResult(last.name.toString());
                          CodeBuilder body = scan(node.getBody(), p.putAlias(last.sym, result));
                          return lang.asyncResultHandler(node.getBodyKind(), last.name.toString(), body);
                        }
                      }
                    }
                  }
                  CodeBuilder body = scan(node.getBody(), p);
                  return lang.lambda(node.getBodyKind(), parameterTypes, parameterNames, body);
                }

                @Override
                public CodeBuilder visitMethod(MethodTree node, VisitContext p) {
                  return scan(node.getBody(), p);
                }
              };
              CodeBuilder src = visitor.scan(path, new VisitContext());
              Renderer renderer = new Renderer(lang);
              src.render(renderer);
              result.put(rootElt.toString().replace('.', '/') + '.' + lang.getExtension(), renderer.getBuffer().toString());
            }
          }
        }
      }
    }
    return false;
  }

  public void attributeClass(Element classElement) {
    assert classElement.getKind() == ElementKind.CLASS;
    JCTree.JCClassDecl ct = (JCTree.JCClassDecl) trees.getTree(classElement);
    if (ct.sym != null) {
      if ((ct.sym.flags_field & Flags.UNATTRIBUTED) != 0) {
        attr.attribClass(ct.pos(), ct.sym);
      }
    }
  }
}
