package io.vertx.examples;

import com.sun.tools.javac.code.Symbol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class VisitContext {

  private final Map<Symbol, ExpressionBuilder> aliases;

  public VisitContext() {
    aliases = Collections.emptyMap();
  }

  private VisitContext(Map<Symbol, ExpressionBuilder> aliases) {
    this.aliases = aliases;
  }

  public VisitContext putAlias(Symbol symbol, ExpressionBuilder builder) {
    HashMap<Symbol, ExpressionBuilder> clone = new HashMap<>(aliases);
    clone.put(symbol, builder);
    return new VisitContext(clone);
  }

  public ExpressionBuilder getAlias(Symbol symbol) {
    return aliases.get(symbol);
  }

}
