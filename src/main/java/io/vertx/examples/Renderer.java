package io.vertx.examples;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Renderer implements Appendable {

  private final Lang lang;
  private int indent = 0;
  private boolean first = true;
  private StringBuilder buffer = new StringBuilder();

  public Renderer(Lang lang) {
    this.lang = lang;
  }

  public Lang getLang() {
    return lang;
  }

  public void indent() {
    indent += 2;
  }

  public void unindent() {
    if (indent < 2) {
      throw new IllegalStateException();
    }
    indent -= 2;
  }

  public StringBuilder getBuffer() {
    return buffer;
  }

  @Override
  public Renderer append(CharSequence csq) {
    return append(csq, 0, csq.length());
  }

  @Override
  public Renderer append(CharSequence csq, int start, int end) {
    while (start < end) {
      append(csq.charAt(start++));
    }
    return this;
  }

  @Override
  public Renderer append(char c) {
    if (c == '\n') {
      first = true;
    } else if (first) {
      first = false;
      for (int i = 0;i < indent;i++) {
        buffer.append(' ');
      }
    }
    buffer.append(c);
    return this;
  }
}
