package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.IParameterValidator;
import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.ParameterException;
import io.bunting.cli4j.parser.Parameters;
import org.junit.Test;


public class ValidatePropertiesWhenParsingTest {
  @Test
  public void f()
      throws Exception {

    io.bunting.cli4j.parser.JCommander cmd = new JCommander();

    cmd.addCommand("a", new A());
//    cmd.addCommand("b", new B());

    cmd.parse(new String[] { "a", "-path", "myPathToHappiness" });
  }

  public static class MyPathValidator implements IParameterValidator {

    public void validate(String name, String value) throws ParameterException {
      throw new RuntimeException("I shouldn't be called for command A!");
    }
  }

  @Parameters
  public static class A {

    @Parameter(names = "-path")
    private String path = "W";
  }

  @Parameters
  public static class B {

    @Parameter(names = "-path", validateWith = MyPathValidator.class)
    private String path = "W";
  }

  public static void main(String[] args) throws Exception {
    new ValidatePropertiesWhenParsingTest().f();
  }
}
