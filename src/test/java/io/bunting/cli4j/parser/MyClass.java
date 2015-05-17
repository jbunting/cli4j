package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.IParameterValidator;
import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.ParameterException;
import io.bunting.cli4j.parser.Parameters;
import org.junit.Assert;


@Parameters(separators = "=")
public class MyClass {

  @Parameter(names = { "-p", "--param" }, validateWith = MyValidator.class)
  private String param;

  public static void main(String[] args) {
    JCommander jCommander = new JCommander(new MyClass());
    jCommander.parse("--param=value");
  }

  public static class MyValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
      Assert.assertEquals(value, "\"");
    }
  }

}
