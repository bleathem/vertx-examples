package io.vertx.examples;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CodeModel {

  private final Exception where;

  public CodeModel() {
    this.where = new UnsupportedOperationException();
  }

  public String render(Lang lang) {
    CodeWriter writer = new CodeWriter(lang);
    render(writer);
    return writer.getBuffer().toString();
  }

  public void render(CodeWriter writer) {
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
