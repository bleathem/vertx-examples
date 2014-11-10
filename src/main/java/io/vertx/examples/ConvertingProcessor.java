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
  private TypeInfo.Factory factory;

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
    this.factory = new TypeInfo.Factory(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
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
              TreePathScanner<CodeModel, VisitContext> visitor = new TreePathScanner<CodeModel, VisitContext>() {

                public StatementModel scan(StatementTree tree, VisitContext visitContext) {
                  return (StatementModel) scan((Tree) tree, visitContext);
                }

                public ExpressionModel scan(ExpressionTree tree, VisitContext visitContext) {
                  return (ExpressionModel) scan((Tree) tree, visitContext);
                }

                @Override
                public CodeModel visitForLoop(ForLoopTree node, VisitContext p) {
                  if (node.getInitializer().size() != 1) {
                    throw new UnsupportedOperationException();
                  }
                  if (node.getUpdate().size() != 1) {
                    throw new UnsupportedOperationException();
                  }
                  StatementModel initializer = scan(node.getInitializer().get(0), p);
                  ExpressionModel update = scan(node.getUpdate().get(0).getExpression(), p);
                  StatementModel body = scan(node.getStatement(), p);
                  ExpressionModel condition = scan(node.getCondition(), p);
                  return lang.forLoop(initializer, condition, update, body);
                }

                @Override
                public CodeModel visitEnhancedForLoop(EnhancedForLoopTree node, VisitContext p) {
                  ExpressionModel expression = scan(node.getExpression(), p);
                  StatementModel body = scan(node.getStatement(), p);
                  return lang.enhancedForLoop(node.getVariable().getName().toString(), expression, body);
                }

                @Override
                public CodeModel visitAssignment(AssignmentTree node, VisitContext context) {
                  ExpressionModel variable = scan(node.getVariable(), context);
                  ExpressionModel expression = scan(node.getExpression(), context);
                  return ExpressionModel.forAssign(variable, expression);
                }

                @Override
                public StatementModel visitVariable(VariableTree node, VisitContext p) {
                  JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) node;
                  ExpressionModel initializer;
                  if (node.getInitializer() != null) {
                    initializer = scan(node.getInitializer(), p);
                  } else {
                    initializer = null;
                  }
                  TypeInfo type = factory.create(decl.type);
                  return lang.variable(
                      type,
                      decl.name.toString(),
                      initializer
                  );
                }

                @Override
                public StatementModel visitIf(IfTree node, VisitContext visitContext) {
                  ExpressionModel condition = scan(node.getCondition(), visitContext);
                  StatementModel thenBody = scan(node.getThenStatement(), visitContext);
                  StatementModel elseBody = node.getElseStatement() != null ? scan(node.getElseStatement(), visitContext) : null;
                  return StatementModel.ifThenElse(condition, thenBody, elseBody);
                }

                @Override
                public CodeModel visitConditionalExpression(ConditionalExpressionTree node, VisitContext visitContext) {
                  ExpressionModel condition = scan(node.getCondition(), visitContext);
                  ExpressionModel trueExpression = scan(node.getTrueExpression(), visitContext);
                  ExpressionModel falseExpression = scan(node.getFalseExpression(), visitContext);
                  return ExpressionModel.forConditionalExpression(condition, trueExpression, falseExpression);
                }

                @Override
                public ExpressionModel visitUnary(UnaryTree node, VisitContext p) {
                  ExpressionModel expression = scan(node.getExpression(), p);
                  switch (node.getKind()) {
                    case POSTFIX_INCREMENT:
                      return expression.onPostFixIncrement();
                    default:
                      throw new UnsupportedOperationException();
                  }
                }

                @Override
                public CodeModel visitExpressionStatement(ExpressionStatementTree node, VisitContext context) {
                  ExpressionModel expression = scan(node.getExpression(), context);
                  return StatementModel.render(renderer -> expression.render(renderer));
                }

                @Override
                public ExpressionModel visitBinary(BinaryTree node, VisitContext p) {
                  ExpressionModel left = scan(node.getLeftOperand(), p);
                  ExpressionModel right = scan(node.getRightOperand(), p);
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
                  return lang.combine(left, op, right);
                }

                @Override
                public ExpressionModel visitLiteral(LiteralTree node, VisitContext p) {
                  switch (node.getKind()) {
                    case STRING_LITERAL:
                      return lang.stringLiteral(node.getValue().toString());
                    case BOOLEAN_LITERAL:
                      return ExpressionModel.render(renderer -> renderer.getLang().renderBooleanLiteral(node.getValue().toString(), renderer));
                    case INT_LITERAL:
                      return ExpressionModel.render(renderer -> renderer.getLang().renderIntegerLiteral(node.getValue().toString(), renderer));
                    default:
                      throw new UnsupportedOperationException();
                  }
                }

                @Override
                public ExpressionModel visitIdentifier(IdentifierTree node, VisitContext context) {
                  JCTree.JCIdent ident = (JCTree.JCIdent) node;
                  if (ident.sym instanceof TypeElement) {
                    if (ident.type.equals(SystemType)) {
                      return ExpressionModel.forMemberSelect("out", () ->
                          ExpressionModel.forMemberSelect("println", () ->
                              ExpressionModel.forMethodInvocation(args -> lang.console(args.get(0)))));
                    } else {
                      TypeInfo.Class type = (TypeInfo.Class) factory.create(ident.type);
                      if (type.getKind() == ClassKind.API) {
                        return ExpressionModel.forMemberSelect((identifier) -> lang.staticFactory(type, identifier));
                      } else if (type.getKind() == ClassKind.JSON_OBJECT) {
                        return ExpressionModel.forNew(args -> {
                          switch (args.size()) {
                            case 0:
                              return lang.jsonObject();
                            default:
                              throw new UnsupportedOperationException();
                          }
                        });
                      } else if (type.getKind() == ClassKind.JSON_ARRAY) {
                        return ExpressionModel.forNew(args -> {
                          switch (args.size()) {
                            case 0:
                              return lang.jsonArray();
                            default:
                              throw new UnsupportedOperationException();
                          }
                        });
                      } else if (type.getKind() == ClassKind.OPTIONS) {
                        return ExpressionModel.forNew(args -> lang.options(type));
                      } else {
                        return lang.classExpression(type);
                      }
                    }
                  } else {
                    ExpressionModel alias = context.getAlias(ident.sym);
                    if (alias != null) {
                      return alias;
                    } else {
                      return ExpressionModel.render(node.getName().toString());
                    }
                  }
                }

                @Override
                public CodeModel visitNewClass(NewClassTree node, VisitContext visitContext) {
                  ExpressionModel identifier = scan(node.getIdentifier(), visitContext);
                  List<ExpressionModel> arguments = node.getArguments().stream().map(arg -> scan(arg, visitContext)).collect(Collectors.toList());
                  return identifier.onNew(arguments);
                }

                @Override
                public CodeModel visitParenthesized(ParenthesizedTree node, VisitContext visitContext) {
                  ExpressionModel expression = scan(node.getExpression(), visitContext);
                  return ExpressionModel.forParenthesized(expression);
                }

                @Override
                public ExpressionModel visitMemberSelect(MemberSelectTree node, VisitContext p) {
                  ExpressionModel expression = scan(node.getExpression(), p);
                  return expression.onMemberSelect(node.getIdentifier().toString());
                }

                @Override
                public ExpressionModel visitMethodInvocation(MethodInvocationTree node, VisitContext p) {
                  ExpressionModel methodSelect = scan(node.getMethodSelect(), p);
                  List<ExpressionModel> arguments = node.getArguments().stream().map(argument -> scan(argument, p)).collect(Collectors.toList());
                  return methodSelect.onMethodInvocation(arguments);
                }

                @Override
                public StatementModel visitBlock(BlockTree node, VisitContext p) {
                  List<StatementModel> statements = node.getStatements().stream().map((statement) -> scan(statement, p)).collect(Collectors.toList());
                  return StatementModel.block(statements);
                }

                @Override
                public ExpressionModel visitLambdaExpression(LambdaExpressionTree node, VisitContext p) {
                  List<String> parameterNames = node.getParameters().stream().map(parameter -> parameter.getName().toString()).collect(Collectors.toList());
                  List<TypeInfo> parameterTypes = node.getParameters().stream().
                      map(parameter -> factory.create(((JCTree.JCVariableDecl) parameter).type)).
                      collect(Collectors.toList());
                  int size = parameterNames.size();
                  if (size > 0) {
                    JCTree.JCVariableDecl last = (JCTree.JCVariableDecl) node.getParameters().get(size - 1);
                    if (last.vartype instanceof JCTree.JCTypeApply) {
                      JCTree.JCTypeApply typeApply = (JCTree.JCTypeApply) last.vartype;
                      if (typeApply.clazz instanceof JCTree.JCFieldAccess) {
                        JCTree.JCFieldAccess clazz = (JCTree.JCFieldAccess) typeApply.clazz;
                        Symbol.ClassSymbol sym = (Symbol.ClassSymbol) clazz.sym;
                        TypeInfo type = factory.create(sym.type);
                        if (type.getKind() == ClassKind.ASYNC_RESULT) {
                          ExpressionModel result = lang.asyncResult(last.name.toString());
                          CodeModel body = scan(node.getBody(), p.putAlias(last.sym, result));
                          return lang.asyncResultHandler(node.getBodyKind(), last.name.toString(), body);
                        }
                      }
                    }
                  }
                  CodeModel body = scan(node.getBody(), p);
                  return lang.lambda(node.getBodyKind(), parameterTypes, parameterNames, body);
                }

                @Override
                public CodeModel visitMethod(MethodTree node, VisitContext p) {
                  return scan(node.getBody(), p);
                }
              };
              CodeModel src = visitor.scan(path, new VisitContext());
              CodeWriter writer = new CodeWriter(lang);
              src.render(writer);
              result.put(rootElt.toString().replace('.', '/') + '.' + lang.getExtension(), writer.getBuffer().toString());
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
