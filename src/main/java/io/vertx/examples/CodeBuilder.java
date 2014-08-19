package io.vertx.examples;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CodeBuilder {

  private final Exception where;

  public CodeBuilder() {
    this.where = new UnsupportedOperationException();
  }

  public String render(Lang lang) {
    Renderer renderer = new Renderer(lang);
    render(renderer);
    return renderer.getBuffer().toString();
  }

  public void render(Renderer renderer) {
    UnsupportedOperationException e = new UnsupportedOperationException(getClass().getName() + " has not implemented this method");
    e.initCause(where);
    throw e;
  }

  public void build(Lang lang) {
    UnsupportedOperationException e = new UnsupportedOperationException(getClass().getName() + " has not implemented this method");
    e.initCause(where);
    throw e;
  }
}
