package io.vertx.examples;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Helper {

  static String unwrapQuotedString(String s) {
    int len = s.length();
    if (len > 2 && s.charAt(0) == '"' && s.charAt(len - 1) == '"') {
      return s.substring(1, len - 1);
    } else {
      throw new IllegalArgumentException("Illegal quoted string " + s);
    }
  }
}
