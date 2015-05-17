package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.ParameterException;
import io.bunting.cli4j.parser.validators.PositiveInteger;
import org.junit.Test;


public class PositiveIntegerTest {

  @Test
  public void validateTest() {
    class Arg {
      @Parameter(names = { "-p", "--port" }, description = "Shows help", validateWith = PositiveInteger.class)
      private int port = 0;
    }
    Arg arg = new Arg();
    JCommander jc = new JCommander(arg);
    jc.parse(new String[] { "-p", "8080" });

  }

  @Test(expected = io.bunting.cli4j.parser.ParameterException.class)
  public void validateTest2() {
    class Arg {
      @Parameter(names = { "-p", "--port" }, description = "Shows help", validateWith = PositiveInteger.class)
      private int port = 0;
    }
    Arg arg = new Arg();
    JCommander jc = new JCommander(arg);
    jc.parse(new String[] { "-p", "" });
  }

  @Test(expected = io.bunting.cli4j.parser.ParameterException.class)
  public void validateTest3() {
    class Arg {
      @Parameter(names = { "-p", "--port" }, description = "Shows help", validateWith = PositiveInteger.class)
      private int port = 0;
    }
    Arg arg = new Arg();
    JCommander jc = new JCommander(arg);
    jc.parse(new String[] { "-p", "-1" });
  }

  @Test(expected = io.bunting.cli4j.parser.ParameterException.class)
  public void validateTest4() {
    class Arg {
      @Parameter(names = { "-p", "--port" }, description = "Port Number", validateWith = PositiveInteger.class)
      private int port = 0;
    }
    Arg arg = new Arg();
    JCommander jc = new JCommander(arg);
    jc.parse(new String[] { "-p", "abc" });
  }

  @Test(expected = ParameterException.class)
  public void validateTest5() {
    class Arg {
      @Parameter(names = { "-p", "--port" }, description = "Port Number", validateWith = PositiveInteger.class)
      private int port = 0;
    }

    Arg arg = new Arg();
    JCommander jc = new JCommander(arg);
    jc.parse(new String[] { "--port", " " });
  }
}
