package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.JCommanderTest.BaseArgs;
import io.bunting.cli4j.parser.JCommanderTest.ConfigureArgs;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.ParameterException;
import org.junit.Assert;
import org.junit.Test;


public class FinderTest {
  @Test
  public void caseInsensitiveOption() {
    class Arg {
  
      @Parameter(names = { "-p", "--param" })
      private String param;
    }
    Arg a = new Arg();
    JCommander jc = new JCommander(a);
    jc.setCaseSensitiveOptions(false);
    jc.parse(new String[] { "--PARAM", "foo" });
    Assert.assertEquals(a.param, "foo");
  }

  @Test
  public void caseInsensitiveCommand() {
    BaseArgs a = new BaseArgs();
    ConfigureArgs conf = new ConfigureArgs();
    JCommander jc = new JCommander(a);
    jc.addCommand(conf);
    jc.setCaseSensitiveOptions(false);
//    jc.setCaseSensitiveCommands(false);
    jc.parse("--CONFIGURE");
    String command = jc.getParsedCommand();
    Assert.assertEquals(command, "--configure");
  }

  @Test
  public void abbreviatedOptions() {
    class Arg {
      @Parameter(names = { "-p", "--param" })
      private String param;
    }
    Arg a = new Arg();
    JCommander jc = new JCommander(a);
    jc.setAllowAbbreviatedOptions(true);
    jc.parse(new String[] { "--par", "foo" });
    Assert.assertEquals(a.param, "foo");
  }

  @Test
  public void abbreviatedOptionsCaseInsensitive() {
    class Arg {
      @Parameter(names = { "-p", "--param" })
      private String param;
    }
    Arg a = new Arg();
    JCommander jc = new JCommander(a);
    jc.setCaseSensitiveOptions(false);
    jc.setAllowAbbreviatedOptions(true);
    jc.parse(new String[] { "--PAR", "foo" });
    Assert.assertEquals(a.param, "foo");
  }

  @Test(expected= io.bunting.cli4j.parser.ParameterException.class)
  public void ambiguousAbbreviatedOptions() {
    class Arg {
      @Parameter(names = { "--param" })
      private String param;
      @Parameter(names = { "--parb" })
      private String parb;
    }
    Arg a = new Arg();
    JCommander jc = new JCommander(a);
    jc.setAllowAbbreviatedOptions(true);
    jc.parse(new String[] { "--par", "foo" });
    Assert.assertEquals(a.param, "foo");
  }

  @Test(expected = ParameterException.class)
  public void ambiguousAbbreviatedOptionsCaseInsensitive() {
    class Arg {
      @Parameter(names = { "--param" })
      private String param;
      @Parameter(names = { "--parb" })
      private String parb;
    }
    Arg a = new Arg();
    JCommander jc = new JCommander(a);
    jc.setCaseSensitiveOptions(false);
    jc.setAllowAbbreviatedOptions(true);
    jc.parse(new String[] { "--PAR", "foo" });
    Assert.assertEquals(a.param, "foo");
  }

  public static void main(String[] args) throws Exception {
    new FinderTest().ambiguousAbbreviatedOptionsCaseInsensitive();
  }

}
