/**
 * Copyright (C) 2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.DynamicParameter;
import io.bunting.cli4j.parser.IParameterValidator;
import io.bunting.cli4j.parser.IParameterValidator2;
import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.ParameterDescription;
import io.bunting.cli4j.parser.ParameterException;
import io.bunting.cli4j.parser.Parameters;
import io.bunting.cli4j.parser.args.*;
import io.bunting.cli4j.parser.args.ArgsEnum.ChoiceType;
import io.bunting.cli4j.parser.command.CommandAdd;
import io.bunting.cli4j.parser.command.CommandCommit;
import io.bunting.cli4j.parser.command.CommandMain;
import io.bunting.cli4j.parser.internal.Lists;
import io.bunting.cli4j.parser.internal.Maps;

import org.junit.Assert;
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll;
//import org.testng.annotations.DataProvider;


import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ResourceBundle;

public class JCommanderTest extends Specification {
  def "simple args"() {
    when: "args parsed"
      Args1 args = new Args1();
      String[] argv = [ "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4",
              "-date", "2011-10-26", "-groups", "unit", "a", "b", "c" ];
      new JCommander(args, argv);
    then: "values are parsed properly"
      args.debug
      2 == args.verbose
      "unit" == args.groups
      ["a", "b", "c"] == args.parameters
      1.2f == args.floa
      1.3d == args.doub
      new BigDecimal("1.4") == args.bigd
      new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-26") == args.date
  }

  public String[][] alternateNamesListArgs() {
    return [
        ["--servers", "1", "-s", "2", "--servers", "3"],
        ["-s", "1", "-s", "2", "--servers", "3"],
        ["--servers", "1", "--servers", "2", "-s", "3"],
        ["-s", "1", "--servers", "2", "-s", "3"],
        ["-s", "1", "-s", "2", "--servers", "3"],
    ];
  }

  /**
   *  Confirm that List<?> parameters with alternate names return the correct
   * List regardless of how the arguments are specified
   */

  def "testAlternateNamesForListArguments"() {
    when: "args parsed"
      AlternateNamesForListArgs args = new AlternateNamesForListArgs();

      new JCommander(args, argv);
    then: "values are as expected"
      [argv[1], argv[3], argv[5]] == args.serverNames
    where:
      argv << alternateNamesListArgs()
  }


  /**
   * Make sure that if there are args with multiple names (e.g. "-log" and "-verbose"),
   * the usage will only display it once.
   */
  def "repeatedArgs"() {
    when: "args parsed"
      Args1 args = new Args1();
      String[] argv = [ "-log", "2" ];
      JCommander jc = new JCommander(args, argv);
    then: "values are as expected"
      Assert.assertEquals(jc.getParameters().size(), 8);
  }

  /**
   * Not specifying a required option should throw an exception.
   */
  public void requiredFields1Fail() {
    when: "args parsed"
      Args1 args = new Args1();
      String[] argv = [ "-debug" ];
      new JCommander(args, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  /**
   * Getting the description of a nonexistent command should throw an exception.
   */
  def "nonexistentCommandShouldThrow"() {
    when: 'args parsed"'
      String[] argv = { };
      JCommander jc = new JCommander(new Object(), argv);
      jc.getCommandDescription("foo");
    then: "error thrown"
      thrown(ParameterException)
  }

  /**
   * Required options with multiple names should work with all names.
   */
  def "multipleNames"() {
    when: "args parsed"
      Args1 args = new Args1();
      String[] argv = [ option, "2" ];
      new JCommander(args, argv);
    then: "values are correct"
      2 == args.verbose
    where:
      option << [ "-log", "-verbose" ]
  }

  @Unroll
  def "i18n1 #{locale}"() {
    given: "the resource bundle"
      ResourceBundle bundle = locale != null ? ResourceBundle.getBundle(bundleName, locale)
          : null;
    when: "args parsed"
      ArgsI18N1 i18n = new ArgsI18N1();
      String[] argv = [ "-host", "localhost" ];
      JCommander jc = new JCommander(i18n, bundle, argv);
      ParameterDescription pd = jc.getParameters().get(0);
    then: "values are as expected"
      expectedString == pd.getDescription()
    where:
      bundleName | locale | expectedString
      "MessageBundle" | null | "Host"
      "MessageBundle" | new Locale("en", "US") | "Host"
      "MessageBundle" | new Locale("fr", "FR") | "Hôte"
  }

  def "i18n2"() {
    when: "args parsed"
      String[] argv = [ "-host", "localhost" ];
      Locale.setDefault(new Locale("fr", "FR"));
      JCommander jc = new JCommander(i18n, argv);
      ParameterDescription pd = jc.getParameters().get(0);
    then: "values match"
      "Hôte" == pd.getDescription()
    where:
      i18n << [ new ArgsI18N2(), new ArgsI18N2New() ]
  }

  def "noParseConstructor"() {
    expect: "no error when parsed via method"
      JCommander jCommander = new JCommander(new ArgsMainParameter1());
      jCommander.usage(new StringBuilder());
      // Before fix, this parse would throw an exception, because it calls createDescription, which
      // was already called by usage(), and can only be called once.
      jCommander.parse();
  }

  /**
   * Test a use case where there are required parameters, but you still want
   * to interrogate the options which are specified.
   */
  def "usageWithRequiredArgsAndResourceBundle"() {
    when: "args parsed"
      ArgsHelp argsHelp = new ArgsHelp();
      JCommander jc = new JCommander([argsHelp, new ArgsRequired()],
          ResourceBundle.getBundle("MessageBundle"));
      // Should be able to display usage without triggering validation
      jc.usage(new StringBuilder());
      jc.parse("-h");
    then: "error thrown"
      def e = thrown(ParameterException)
      e.getMessage().contains("are required")
    and: "help is true"
      argsHelp.help
  }

  def "multiObjects"() {
    when: "args parsed"
      ArgsMaster m = new ArgsMaster();
      ArgsSlave s = new ArgsSlave();
      String[] argv = [ "-master", "master", "-slave", "slave" ];
      new JCommander([ m , s ], argv);
    then: "values are correct"
      "master" == m.master
      "slave" == s.slave
  }

  def "multiObjectsWithDuplicatesFail"() {
    when: "args parsed"
    ArgsMaster m = new ArgsMaster();
    ArgsSlave s = new ArgsSlaveBogus();
    String[] argv = [ "-master", "master", "-slave", "slave" ];
    new JCommander([ m , s ], argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "arityString"() {
    when: "args parsed"
      ArgsArityString args = new ArgsArityString();
      String[] argv = [ "-pairs", "pair0", "pair1", "rest" ];
      new JCommander(args, argv);
    then: "values match"
      ["pair0", "pair1"] == args.pairs
      ["rest"] == args.rest
  }

  def "arity2Fail"() {
    when: "args parsed"
      ArgsArityString args = new ArgsArityString();
      String[] argv = [ "-pairs", "pair0" ];
      new JCommander(args, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "multipleUnparsedFail"() {
    when: "args parsed"
      ArgsMultipleUnparsed args = new ArgsMultipleUnparsed();
      String[] argv = { };
      new JCommander(args, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "privateArgs"() {
    when: "args parsed"
      ArgsPrivate args = new ArgsPrivate();
      new JCommander(args, "-verbose", "3");
    then: "values match"
      3 == args.getVerbose()
  }

  def "converterArgs"() {
    when: "args parsed"
      ArgsConverter args = new ArgsConverter();
      String fileName = "a";
      new JCommander(args, "-file", "/tmp/" + fileName,
        "-listStrings", "Tuesday,Thursday",
        "-listInts", "-1,8",
        "-listBigDecimals", "-11.52,100.12");
    then: "values match"
      fileName == args.file.getName()
      ["Tuesday", "Thursday"] == args.listStrings
      [-1, 8] == args.listInts
      [new BigDecimal("-11.52"), new BigDecimal("100.12")] == args.listBigDecimals
  }

  def "booleanArity1"() {
    when: "args parsed"
      ArgsBooleanArity args = new ArgsBooleanArity();
      new JCommander(args, params as String[]);
    then: "values match"
      expected == args.debug
    where:
      params | expected
      []   | false
      ["-debug", "true"] | true
  }

  def "booleanArity0"() {
    when: "args parsed"
      ArgsBooleanArity0 args = new ArgsBooleanArity0();
      new JCommander(args, params as String[]);
    then: "values match"
      expected == args.debug
    where:
      params | expected
      []   | false
      ["-debug"] | true
  }

  def "badParameterShouldThrowParameter1Exception"() {
    when: "args parsed"
      Args1 args = new Args1();
      String[] argv = [ "-log", "foo" ];
      new JCommander(args, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "badParameterShouldThrowParameter2Exception"() {
    when: "args parsed"
      Args1 args = new Args1();
      String[] argv = [ "-long", "foo" ];
      new JCommander(args, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "listParameters"() {
    when: "args parsed"
    Args2 a = new Args2();
    String[] argv = ["-log", "2", "-groups", "unit", "a", "b", "c", "-host", "host2"];
    new JCommander(a, argv);
    then: "values match"
      2 == a.verbose
      "unit" == a.groups
      ["host2"] == a.hosts
      ["a", "b", "c"] == a.parameters
  }

  def "separatorEqual"() {
    when: "args parsed"
      SeparatorEqual s = new SeparatorEqual();
      String[] argv = [ "-log=3", "--longoption=10" ];
      new JCommander(s, argv);
    then: "values match"
      3 == s.log
      10 == s.longOption
  }

  def "separatorColon"() {
    when: "args parsed"
      SeparatorColon s = new SeparatorColon();
      String[] argv = [ "-verbose:true" ];
      new JCommander(s, argv);
    then: "values match"
      s.verbose
  }

  def "separatorBoth"() {
    when: "args parsed"
      SeparatorColon s = new SeparatorColon();
      SeparatorEqual s2 = new SeparatorEqual();
      String[] argv = [ "-verbose:true", "-log=3" ];
      new JCommander([ s, s2 ], argv);
    then: "values match"
      s.verbose
      3 == s2.log
  }

  def "separatorMixed1"() {
    when: "args parsed"
      SeparatorMixed s = new SeparatorMixed();
      String[] argv = [ "-long:1", "-level=42" ];
      new JCommander(s, argv);
    then: "values match"
      1l == s.l
      42 == s.level
  }

  def "slashParameters"() {
    when: "args parsed"
      SlashSeparator a = new SlashSeparator();
      String[] argv = [ "/verbose", "/file", "/tmp/a" ];
      new JCommander(a, argv);
    then: "values match"
      a.verbose
      "/tmp/a" == a.file
  }

  def "inheritance"() {
    when: "args parsed"
      ArgsInherited args = new ArgsInherited();
      String[] argv = [ "-log", "3", "-child", "2" ];
      new JCommander(args, argv);
    then: "values match"
      2 == args.child
      3 == args.log
  }

  def "negativeNumber"() {
    when: "args parsed"
      Args1 a = new Args1();
      String[] argv = [ "-verbose", "-3" ];
      new JCommander(a, argv);
    then: "values match"
      -3 == a.verbose
  }

  def "requiredMainParameters"() {
    when: "args parsed"
      ArgsRequired a = new ArgsRequired();
      String[] argv = [];
      new JCommander(a, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "usageShouldNotChange"() {
    when: "args parsed"
      JCommander jc = new JCommander(new Args1(), "-log", "1");
      StringBuilder sb = new StringBuilder();
      jc.usage(sb);
      String expected = sb.toString();
      jc = new JCommander(new Args1(), "-debug", "-log", "2", "-long", "5");
      sb = new StringBuilder();
      jc.usage(sb);
      String actual = sb.toString();
    then: "values match"
      expected == actual
  }

  private void verifyCommandOrdering(String[] commandNames, Object[] commands) {
    when: "commands configured"
      CommandMain cm = new CommandMain();
      JCommander jc = new JCommander(cm);

      for (int i = 0; i < commands.length; i++) {
        jc.addCommand(commandNames[i], commands[i]);
      }
    then: "they're all there"
      jc.getCommands().keySet().collect() == commandNames
    where:
      commandNames      | commands
      ["add", "commit"] | [ new CommandAdd(), new CommandCommit() ]
      ["commit", "add"] | [ new CommandCommit(), new CommandAdd() ]
  }

  public static Object[][] f() {
    return [
      [ 3, 5, 1 ],
      [ 3, 8, 1 ],
      [ 3, 12, 2 ],
      [ 8, 12, 2 ],
      [ 9, 10, 1 ],
    ];
  }

  public void arity1Fail() {
    when: "args parsed"
      final Arity1 arguments = new Arity1();
      final JCommander jCommander = new JCommander(arguments);
      final String[] commands = {
        "-inspect"
      };
      jCommander.parse(commands);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "arity1Success1"() {
    when: "args parsed"
      final Arity1 arguments = new Arity1();
      final JCommander jCommander = new JCommander(arguments);
      final String[] commands = [ "-inspect", "true" ]
      jCommander.parse(commands);
    then: "values match"
      arguments.inspect
  }

  def "arity1Success2"() {
    when: "args parsed"
      final Arity1 arguments = new Arity1();
      final JCommander jCommander = new JCommander(arguments);
      final String[] commands = [ "-inspect", "false" ]
      jCommander.parse(commands);
    then: "values match"
      !arguments.inspect
  }

  @Parameters(commandDescription = "Help for the given commands.")
  public static class Help {
      public static final String NAME = "help";

      @Parameter(description = "List of commands.")
      public List<String> commands=new ArrayList<String>();
  }

  def "wrongMainTypeShouldThrow"() {
    when: "args parsed"
      JCommander jc = new JCommander(new ArgsRequiredWrongMain());
      jc.parse("f1", "f2");
    then: "error thrown"
      thrown(ParameterException)
  }

  def "oom"() {
    expect: "it doesn't OOM"
      JCommander jc = new JCommander(new ArgsOutOfMemory());
      jc.usage(new StringBuilder());
  }

  def "getParametersShouldNotNpe"() {
    expect: "no NPE"
      JCommander jc = new JCommander(new Args1());
      List<ParameterDescription> parameters = jc.getParameters();
  }

  def "validationShouldWork1"() {
    when: "args parsed"
      ArgsValidate1 a = new ArgsValidate1();
      JCommander jc = new JCommander(a);
      jc.parse("-age", "2 ");
    then: "values match"
      2 == a.age
  }

  def "validationShouldWorkWithDefaultValues"() {
    when: "args parsed"
      ArgsValidate2 a = new ArgsValidate2();
      new JCommander(a);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "validationShouldWork2"() {
    when: "args parsed"
      ArgsValidate1 a = new ArgsValidate1();
      JCommander jc = new JCommander(a);
      jc.parse("-age", "-2 ");
    then: "error thrown"
      thrown(ParameterException)
  }

//  public void atFileCanContainEmptyLines() throws IOException {
//    File f = File.createTempFile("JCommander", null);
//    f.deleteOnExit();
//    FileWriter fw = new FileWriter(f);
//    fw.write("-log\n");
//    fw.write("\n");
//    fw.write("2\n");
//    fw.close();
//    new JCommander(new Args1(), "@" + f.getAbsolutePath());
//  }
//
//  public void handleEqualSigns() {
//    ArgsEquals a = new ArgsEquals();
//    JCommander jc = new JCommander(a);
//    jc.parse(new String[] { "-args=a=b,b=c" });
//    Assert.assertEquals(a.args, "a=b,b=c");
//  }
//
//  @SuppressWarnings("serial")
//  public void handleSets() {
//    ArgsWithSet a = new ArgsWithSet();
//    new JCommander(a, new String[] { "-s", "3,1,2" });
//    Assert.assertEquals(a.set, new TreeSet<Integer>() {{ add(1); add(2); add(3); }});
//  }
//
//  private static final List<String> V = Arrays.asList("a", "b", "c", "d");
//
//  @DataProvider
//  public Object[][] variable() {
//    return new Object[][] {
//        new Object[] { 0, V.subList(0, 0), V },
//        new Object[] { 1, V.subList(0, 1), V.subList(1, 4) },
//        new Object[] { 2, V.subList(0, 2), V.subList(2, 4) },
//        new Object[] { 3, V.subList(0, 3), V.subList(3, 4) },
//        new Object[] { 4, V.subList(0, 4), V.subList(4, 4) },
//    };
//  }
//
//  @Test(dataProvider = "variable")
//  public void variableArity(int count, List<String> var, List<String> main) {
//    VariableArity va = new VariableArity(count);
//    new JCommander(va).parse("-variable", "a", "b", "c", "d");
//    Assert.assertEquals(var, va.var);
//    Assert.assertEquals(main, va.main);
//  }
//
//  public void enumArgs() {
//    ArgsEnum args = new ArgsEnum();
//    String[] argv = { "-choice", "ONE", "-choices", "ONE", "Two" };
//    JCommander jc = new JCommander(args, argv);
//
//    Assert.assertEquals(args.choice, ArgsEnum.ChoiceType.ONE);
//
//    List<ChoiceType> expected = Arrays.asList(ChoiceType.ONE, ChoiceType.Two);
//    Assert.assertEquals(expected, args.choices);
//    Assert.assertEquals(jc.getParameters().get(0).getDescription(),
//        "Options: " + EnumSet.allOf((Class<? extends Enum>) ArgsEnum.ChoiceType.class));
//
//  }
//
//  public void enumArgsCaseInsensitive() {
//      ArgsEnum args = new ArgsEnum();
//      String[] argv = { "-choice", "one"};
//      JCommander jc = new JCommander(args, argv);
//
//      Assert.assertEquals(args.choice, ArgsEnum.ChoiceType.ONE);
//  }
//
//  @Test(expectedExceptions = ParameterException.class)
//  public void enumArgsFail() {
//    ArgsEnum args = new ArgsEnum();
//    String[] argv = { "-choice", "A" };
//    new JCommander(args, argv);
//  }
//
//  public void testListAndSplitters() {
//    ArgsList al = new ArgsList();
//    JCommander j = new JCommander(al);
//    j.parse("-groups", "a,b", "-ints", "41,42", "-hp", "localhost:1000;example.com:1001",
//        "-hp2", "localhost:1000,example.com:1001", "-uppercase", "ab,cd");
//    Assert.assertEquals(al.groups.get(0), "a");
//    Assert.assertEquals(al.groups.get(1), "b");
//    Assert.assertEquals(al.ints.get(0).intValue(), 41);
//    Assert.assertEquals(al.ints.get(1).intValue(), 42);
//    Assert.assertEquals(al.hostPorts.get(0).host, "localhost");
//    Assert.assertEquals(al.hostPorts.get(0).port.intValue(), 1000);
//    Assert.assertEquals(al.hostPorts.get(1).host, "example.com");
//    Assert.assertEquals(al.hostPorts.get(1).port.intValue(), 1001);
//    Assert.assertEquals(al.hp2.get(1).host, "example.com");
//    Assert.assertEquals(al.hp2.get(1).port.intValue(), 1001);
//    Assert.assertEquals(al.uppercase.get(0), "AB");
//    Assert.assertEquals(al.uppercase.get(1), "CD");
//  }
//
//  @Test(expectedExceptions = ParameterException.class)
//  public void shouldThrowIfUnknownOption() {
//    class A {
//      @Parameter(names = "-long")
//      public long l;
//    }
//    A a = new A();
//    new JCommander(a).parse("-lon", "32");
//  }
//
//  @Test(expectedExceptions = ParameterException.class)
//  public void mainParameterShouldBeValidate() {
//    class V implements IParameterValidator {
//
//      @Override
//      public void validate(String name, String value) throws ParameterException {
//        Assert.assertEquals("a", value);
//      }
//    }
//
//    class A {
//      @Parameter(validateWith = V.class)
//      public List<String> m;
//    }
//
//    A a = new A();
//    new JCommander(a).parse("b");
//  }

  @Parameters(commandNames = [ "--configure" ])
  public static class ConfigureArgs {
  }

  public static class BaseArgs {
    @Parameter(names = [ "-h", "--help" ], description = "Show this help screen")
    private boolean help = false;

    @Parameter(names = [ "--version", "-version" ], description = "Show the program version")
    private boolean version;
  }

//  public void commandsWithSamePrefixAsOptionsShouldWork() {
//    BaseArgs a = new BaseArgs();
//    ConfigureArgs conf = new ConfigureArgs();
//    JCommander jc = new JCommander(a);
//    jc.addCommand(conf);
//    jc.parse("--configure");
//  }
//
//  // Tests:
//  // required unparsed parameter
//  @Test(enabled = false,
//      description = "For some reason, this test still asks the password on stdin")
//  public void askedRequiredPassword() {
//    class A {
//        @Parameter(names = { "--password", "-p" }, description = "Private key password",
//            password = true, required = true)
//        public String password;
//
//        @Parameter(names = { "--port", "-o" }, description = "Port to bind server to",
//            required = true)
//        public int port;
//    }
//    A a = new A();
//    InputStream stdin = System.in;
//    try {
//      System.setIn(new ByteArrayInputStream("password".getBytes()));
//      new JCommander(a,new String[]{"--port", "7","--password"});
//      Assert.assertEquals(a.port, 7);
//      Assert.assertEquals(a.password, "password");
//    } finally {
//      System.setIn(stdin);
//    }
//  }
//
//  public void dynamicParameters() {
//    class Command {
//      @DynamicParameter(names = {"-P"}, description = "Additional command parameters")
//      private Map<String, String> params = Maps.newHashMap();
//    }
//    JCommander commander = new JCommander();
//    Command c = new Command();
//    commander.addCommand("command", c);
//    commander.parse(new String[] { "command", "-Pparam='name=value'" });
//    Assert.assertEquals(c.params.get("param"), "'name=value'");
//  }
//
//  public void exeParser() {
//      class Params {
//        @Parameter( names= "-i")
//        private String inputFile;
//      }
//
//      String args[] = { "-i", "" };
//      Params p = new Params();
//      new JCommander(p, args);
//  }
//
//  public void multiVariableArityList() {
//    class Params {
//      @Parameter(names = "-paramA", description = "ParamA", variableArity = true)
//      private List<String> paramA = Lists.newArrayList();
//
//      @Parameter(names = "-paramB", description = "ParamB", variableArity = true)
//      private List<String> paramB = Lists.newArrayList();
//    }
//
//    {
//      String args[] = { "-paramA", "a1", "a2", "-paramB", "b1", "b2", "b3" };
//      Params p = new Params();
//      new JCommander(p, args).parse();
//      Assert.assertEquals(p.paramA, Arrays.asList(new String[] { "a1", "a2" }));
//      Assert.assertEquals(p.paramB, Arrays.asList(new String[] { "b1", "b2", "b3" }));
//    }
//
//    {
//      String args[] = { "-paramA", "a1", "a2", "-paramB", "b1", "-paramA", "a3" };
//      Params p = new Params();
//      new JCommander(p, args).parse();
//      Assert.assertEquals(p.paramA, Arrays.asList(new String[] { "a1", "a2", "a3" }));
//      Assert.assertEquals(p.paramB, Arrays.asList(new String[] { "b1" }));
//    }
//  }
//
//  @Test(enabled = false,
//      description = "Need to double check that the command description is i18n'ed in the usage")
//  public void commandKey() {
//    @Parameters(resourceBundle = "MessageBundle", commandDescriptionKey = "command")
//    class Args {
//      @Parameter(names="-myoption", descriptionKey="myoption")
//      private boolean option;
//    }
//    JCommander j = new JCommander();
//    Args a = new Args();
//    j.addCommand("comm", a);
//    j.usage();
//  }
//
//  public void tmp() {
//    class A {
//      @Parameter(names = "-b")
//      public String b;
//    }
//    new JCommander(new A()).parse("");
//  }
//
//  public void unknownOptionWithDifferentPrefix() {
//    @Parameters(optionPrefixes = "/")
//    class SlashSeparator {
//
//     @Parameter(names = "/verbose")
//     public boolean verbose = false;
//
//     @Parameter(names = "/file")
//     public String file;
//    }
//    SlashSeparator ss = new SlashSeparator();
//    try {
//      new JCommander(ss).parse("/notAParam");
//    } catch (ParameterException ex) {
//      boolean result = ex.getMessage().contains("Unknown option");
//      Assert.assertTrue(result);
//    }
//  }
//
//  public void equalSeparator() {
//    @Parameters(separators = "=", commandDescription = "My command")
//    class MyClass {
//
//       @Parameter(names = { "-p", "--param" }, required = true, description = "param desc...")
//       private String param;
//    }
//    MyClass c = new MyClass();
//    String expected = "\"hello\"world";
//    new JCommander(c).parse("--param=" + expected);
//    Assert.assertEquals(expected, c.param);
//  }
//
//  public void simpleArgsSetter() throws ParseException {
//    Args1Setter args = new Args1Setter();
//    String[] argv = { "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4",
//            "-date", "2011-10-26", "-groups", "unit", "a", "b", "c" };
//    new JCommander(args, argv);
//
//    Assert.assertTrue(args.debug);
//    Assert.assertEquals(args.verbose.intValue(), 2);
//    Assert.assertEquals(args.groups, "unit");
//    Assert.assertEquals(args.parameters, Arrays.asList("a", "b", "c"));
//    Assert.assertEquals(args.floa, 1.2f, 0.1f);
//    Assert.assertEquals(args.doub, 1.3f, 0.1f);
//    Assert.assertEquals(args.bigd, new BigDecimal("1.4"));
//    Assert.assertEquals(args.date, new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-26"));
//  }
//
//  public void verifyHelp() {
//    class Arg {
//      @Parameter(names = "--help", help = true)
//      public boolean help = false;
//
//      @Parameter(names = "file", required = true)
//      public String file;
//    }
//    Arg arg = new Arg();
//    String[] argv = { "--help" };
//    new JCommander(arg, argv);
//
//    Assert.assertTrue(arg.help);
//  }
//
//  public void helpTest() {
//    class Arg {
//      @Parameter(names = { "?", "-help", "--help" }, description = "Shows help", help = true)
//      private boolean help = false;
//    }
//    Arg arg = new Arg();
//    JCommander jc = new JCommander(arg);
//    jc.parse(new String[] { "-help" });
////    System.out.println("helpTest:" + arg.help);
//  }
//
//  @Test(enabled = false, description = "Should only be enable once multiple parameters are allowed")
//  public void duplicateParameterNames() {
//    class ArgBase {
//      @Parameter(names = { "-host" })
//      protected String host;
//    }
//
//    class Arg1 extends ArgBase {}
//    Arg1 arg1 = new Arg1();
//
//    class Arg2 extends ArgBase {}
//    Arg2 arg2 = new Arg2();
//
//    JCommander jc = new JCommander(new Object[] { arg1, arg2});
//    jc.parse(new String[] { "-host", "foo" });
//    Assert.assertEquals(arg1.host, "foo");
//    Assert.assertEquals(arg2.host, "foo");
//  }
//
//  public void parameterWithOneDoubleQuote() {
//    @Parameters(separators = "=")
//    class Arg {
//      @Parameter(names = { "-p", "--param" })
//      private String param;
//    }
//    JCommander jc = new JCommander(new MyClass());
//    jc.parse("-p=\"");
//  }
//
//  public void emptyStringAsDefault() {
//    class Arg {
//      @Parameter(names = "-x")
//      String s = "";
//    }
//    Arg a = new Arg();
//    StringBuilder sb = new StringBuilder();
//    new JCommander(a).usage(sb);
//    Assert.assertTrue(sb.toString().contains("Default: <empty string>"));
//  }
//
//  public void spaces() {
//    class Arg {
//      @Parameter(names = "-rule", description = "rule")
//      private List<String> rules = new ArrayList<String>();
//    }
//    Arg a = new Arg();
//    new JCommander(a, "-rule", "some test");
//    Assert.assertEquals(a.rules, Arrays.asList("some test"));
//  }
//
//  static class V2 implements IParameterValidator2 {
//    final static List<String> names =  Lists.newArrayList();
//    static boolean validateCalled = false;
//
//    @Override
//    public void validate(String name, String value) throws ParameterException {
//      validateCalled = true;
//    }
//
//    @Override
//    public void validate(String name, String value, ParameterDescription pd)
//        throws ParameterException {
//      names.addAll(Arrays.asList(pd.getParameter().names()));
//    }
//  }
//
//  public void validator2() {
//    class Arg {
//      @Parameter(names = { "-h", "--host" }, validateWith = V2.class)
//      String host;
//    }
//    Arg a = new Arg();
//    V2.names.clear();
//    V2.validateCalled = false;
//    JCommander jc = new JCommander(a, "--host", "h");
//    jc.setAcceptUnknownOptions(true);
//    Assert.assertEquals(V2.names, Arrays.asList(new String[] { "-h", "--host" }));
//    Assert.assertTrue(V2.validateCalled);
//  }
//
//  public void usageCommandsUnderUsage() {
//    class Arg {
//    }
//    @Parameters(commandDescription = "command a")
//    class ArgCommandA {
//      @Parameter(description = "command a parameters")
//      List<String> parameters;
//    }
//    @Parameters(commandDescription = "command b")
//    class ArgCommandB {
//      @Parameter(description = "command b parameters")
//      List<String> parameters;
//    }
//
//    Arg a = new Arg();
//
//    JCommander c = new JCommander(a);
//    c.addCommand("a", new ArgCommandA());
//    c.addCommand("b", new ArgCommandB());
//
//    StringBuilder sb = new StringBuilder();
//    c.usage(sb);
//    Assert.assertTrue(sb.toString().contains("[command options]\n  Commands:"));
//  }
//
//  public void usageWithEmpytLine() {
//    class Arg {
//    }
//    @Parameters(commandDescription = "command a")
//    class ArgCommandA {
//      @Parameter(description = "command a parameters")
//      List<String> parameters;
//    }
//    @Parameters(commandDescription = "command b")
//    class ArgCommandB {
//      @Parameter(description = "command b parameters")
//      List<String> parameters;
//    }
//
//    Arg a = new Arg();
//
//    JCommander c = new JCommander(a);
//    c.addCommand("a", new ArgCommandA());
//    c.addCommand("b", new ArgCommandB());
//
//    StringBuilder sb = new StringBuilder();
//    c.usage(sb);
//    Assert.assertTrue(sb.toString().contains("command a parameters\n\n    b"));
//  }
//
//  public void partialValidation() {
//    class Arg {
//      @Parameter(names = { "-h", "--host" })
//      String host;
//    }
//    Arg a = new Arg();
//    JCommander jc = new JCommander();
//    jc.setAcceptUnknownOptions(true);
//    jc.addObject(a);
//    jc.parse("-a", "foo", "-h", "host");
//    Assert.assertEquals(a.host, "host");
//    Assert.assertEquals(jc.getUnknownOptions(), Lists.newArrayList("-a", "foo"));
//  }
//
//  /**
//   * GITHUB-137.
//   */
//  public void listArgShouldBeCleared() {
//    class Args {
//      @Parameter(description = "[endpoint]")
//      public List<String> endpoint = Lists.newArrayList("prod");
//    }
//    Args a = new Args();
//    new JCommander(a, new String[] { "dev" });
//    Assert.assertEquals(a.endpoint, Lists.newArrayList("dev"));
//  }
//
//  public void dashDashParameter() {
//    class Arguments {
//        @Parameter(names = { "-name" })
//        public String name;
//        @Parameter
//        public List<String> mainParameters;
//    }
//
//    Arguments a = new Arguments();
//    new JCommander(a, new String[] {
//        "-name", "theName", "--", "param1", "param2"}
//    );
//    Assert.assertEquals(a.name, "theName");
//    Assert.assertEquals(a.mainParameters.size(), 2);
//    Assert.assertEquals(a.mainParameters.get(0), "param1");
//    Assert.assertEquals(a.mainParameters.get(1), "param2");
//  }
//
//  public void dashDashParameter2() {
//    class Arguments {
//        @Parameter(names = { "-name" })
//        public String name;
//        @Parameter
//        public List<String> mainParameters;
//    }
//
//    Arguments a = new Arguments();
//    new JCommander(a, new String[] {
//        "param1", "param2", "--", "param3", "-name", "theName"}
//    );
//    Assert.assertNull(a.name);
//    Assert.assertEquals(a.mainParameters.size(), 5);
//    Assert.assertEquals(a.mainParameters.get(0), "param1");
//    Assert.assertEquals(a.mainParameters.get(1), "param2");
//    Assert.assertEquals(a.mainParameters.get(2), "param3");
//    Assert.assertEquals(a.mainParameters.get(3), "-name");
//    Assert.assertEquals(a.mainParameters.get(4), "theName");
//  }
//
//  public void access() {
//    class Arguments {
//      private int bar;
//
//      @Parameter(names = "-bar")
//      private void setBar(int value) {
//        bar = value;
//      }
//    }
//    try {
//      Arguments a = new Arguments();
//      new JCommander(a, new String[] { "-bar", "1" });
//    } catch(ParameterException ex) {
//      Assert.assertTrue(ex.getMessage().contains("invoke"));
//    }
//  }
//
//  @Test(enabled = false)
//  public static void main(String[] args) throws Exception {
//    new JCommanderTest().access();
////    class A {
////      @Parameter(names = "-short", required = true)
////      List<String> parameters;
////
////      @Parameter(names = "-long", required = true)
////      public long l;
////    }
////    A a = new A();
////    new JCommander(a).parse();
////    System.out.println(a.l);
////    System.out.println(a.parameters);
////    ArgsList al = new ArgsList();
////    JCommander j = new JCommander(al);
////    j.setColumnSize(40);
////    j.usage();
////    new JCommanderTest().testListAndSplitters();
////    new JCommanderTest().converterArgs();
//  }

  // Tests:
  // required unparsed parameter
}
