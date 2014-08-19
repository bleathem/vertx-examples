package io.vertx.examples;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ModuleTest extends ConversionTestBase {

  public static Object buffer;

  @Test
  public void testModule() throws Exception {
    runAll("module/Module", () -> {
      Assert.assertEquals("the_buffer", buffer);
    });
  }
}
