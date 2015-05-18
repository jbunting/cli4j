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

package io.bunting.cli4j.parser

import io.bunting.cli4j.parser.args.*;
import io.bunting.cli4j.parser.args.ArgsEnum.ChoiceType;
import io.bunting.cli4j.parser.command.CommandAdd;
import io.bunting.cli4j.parser.command.CommandCommit;
import io.bunting.cli4j.parser.command.CommandMain;
import io.bunting.cli4j.parser.internal.Lists;
import io.bunting.cli4j.parser.internal.Maps;

import org.junit.Assert
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll;
//import org.testng.annotations.DataProvider;


import java.io.*
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ResourceBundle

import static spock.util.matcher.HamcrestMatchers.closeTo;

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

  def "verifyCommandOrdering"() {
    when: "commands configured"
      CommandMain cm = new CommandMain();
      JCommander jc = new JCommander(cm);

      for (int i = 0; i < commands.size(); i++) {
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

  def "arity1Fail"() {
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

  def "atFileCanContainEmptyLines"() throws IOException {
    expect: "doesn't fail"
      File f = File.createTempFile("JCommander", null);
      f.deleteOnExit();
      FileWriter fw = new FileWriter(f);
      fw.write("-log\n");
      fw.write("\n");
      fw.write("2\n");
      fw.close();
      new JCommander(new Args1(), "@" + f.getAbsolutePath());
  }

  def "handleEqualSigns"() {
    when: "args parsed"
      ArgsEquals a = new ArgsEquals();
      JCommander jc = new JCommander(a);
      jc.parse("-args=a=b,b=c");
    then: "values match"
      "a=b,b=c" == a.args
  }

  @SuppressWarnings("serial")
  def "handleSets"() {
    when: "args parsed"
      ArgsWithSet a = new ArgsWithSet();
      new JCommander(a, "-s", "3,1,2" );
    then: "values match"
      [1, 2, 3] as Set == a.set
  }

  private static final List<String> V = ["a", "b", "c", "d"];

  public Object[][] variable() {
    return [
        [ 0, V.subList(0, 0), V ],
        [ 1, V.subList(0, 1), V.subList(1, 4) ],
        [ 2, V.subList(0, 2), V.subList(2, 4) ],
        [ 3, V.subList(0, 3), V.subList(3, 4) ],
        [ 4, V.subList(0, 4), V.subList(4, 4) ],
    ];
  }

  def "variableArity"() {
    when: "args parsed"
      VariableArity va = new VariableArity(count);
      new JCommander(va).parse("-variable", "a", "b", "c", "d");
    then: "values match"
      Assert.assertEquals(var, va.var);
      Assert.assertEquals(main, va.main);
    where:
      [count, var, main] << variable()
  }

  def "enumArgs"() {
    when: "args parsed"
      ArgsEnum args = new ArgsEnum();
      String[] argv = [ "-choice", "ONE", "-choices", "ONE", "Two" ];
      JCommander jc = new JCommander(args, argv);
    then: "values match"
      ChoiceType.ONE == args.choice
      [ChoiceType.ONE, ChoiceType.Two] == args.choices;
  }


  def "enumArgsCaseInsensitive"() {
    when: "args parsed"
      ArgsEnum args = new ArgsEnum();
      String[] argv = [ "-choice", "one"];
      JCommander jc = new JCommander(args, argv);
    then: "values match"
      ChoiceType.ONE == args.choice
  }

  def "enumArgsFail"() {
    when: "args parsed"
    ArgsEnum args = new ArgsEnum();
    String[] argv = [ "-choice", "A" ];
    new JCommander(args, argv);
    then: "error thrown"
      thrown(ParameterException)
  }

  def "testListAndSplitters"() {
    when: "args parsed"
      ArgsList al = new ArgsList();
      JCommander j = new JCommander(al);
      j.parse("-groups", "a,b", "-ints", "41,42", "-hp", "localhost:1000;example.com:1001",
          "-hp2", "localhost:1000,example.com:1001", "-uppercase", "ab,cd");
    then: "values match"
      ["a", "b"] == al.groups
      [41, 42] == al.ints
      [new HostPort(host:"localhost", port:1000), new HostPort(host:"example.com", port:1001)] == al.hostPorts
      [new HostPort(host:"localhost", port:1000), new HostPort(host:"example.com", port:1001)] == al.hp2
      ["AB", "CD"] == al.uppercase
  }

  class shouldThrowIfUnknownA {
    @Parameter(names = "-long")
    public long l;
  }
  def "shouldThrowIfUnknownOption"() {
    when: "args parsed"
      def a = new shouldThrowIfUnknownA();
      new JCommander(a).parse("-lon", "32");
    then: "error thrown"
      thrown(ParameterException)
  }

  class mainParameterV implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      Assert.assertEquals("a", value);
    }
  }

  class mainParameterA {
    @Parameter(validateWith = mainParameterV.class)
    public List<String> m;
  }

  def "mainParameterShouldBeValidate"() {
    when: "args parsed"
      def a = new mainParameterA();
      new JCommander(a).parse("b");
    then: "error thrown"
      thrown(ParameterException)

  }

  @Parameters(commandNames = [ "--configure" ])
  public static class ConfigureArgs {
  }

  public static class BaseArgs {
    @Parameter(names = [ "-h", "--help" ], description = "Show this help screen")
    private boolean help = false;

    @Parameter(names = [ "--version", "-version" ], description = "Show the program version")
    private boolean version;
  }

  def "commandsWithSamePrefixAsOptionsShouldWork"() {
    expect: "it work"
      BaseArgs a = new BaseArgs();
      ConfigureArgs conf = new ConfigureArgs();
      JCommander jc = new JCommander(a);
      jc.addCommand(conf);
      jc.parse("--configure");
  }

  // Tests:
  // required unparsed parameter
  class askedRequiredPasswordA {
    @Parameter(names = [ "--password", "-p" ], description = "Private key password",
            password = true, required = true)
    public String password;

    @Parameter(names = [ "--port", "-o" ], description = "Port to bind server to",
            required = true)
    public int port;
  }
  @Ignore
  def "askedRequiredPassword"() {
    def a = new askedRequiredPasswordA();
    InputStream stdin = System.in;
    try {
      System.setIn(new ByteArrayInputStream("password".getBytes()));
      new JCommander(a,"--port", "7","--password");
      Assert.assertEquals(a.port, 7);
      Assert.assertEquals(a.password, "password");
    } finally {
      System.setIn(stdin);
    }
  }

  class dynamicParametersCommand {
    @DynamicParameter(names = ["-P"], description = "Additional command parameters")
    private Map<String, String> params = Maps.newHashMap();
  }
  def "dynamicParameters"() {
    when: "args parsed"
      JCommander commander = new JCommander();
      def c = new dynamicParametersCommand();
      commander.addCommand("command", c);
      commander.parse( "command", "-Pparam='name=value'" );
    then: "values match"
      [param: "'name=value'"] == c.params
  }

  class exeParserParams {
    @Parameter( names= "-i")
    private String inputFile;
  }
  def "exeParser"() {
    expect: "it work"
      String[] args = [ "-i", "" ];
      def p = new exeParserParams();
      new JCommander(p, args);
  }

  class multiVariableParams {
    @Parameter(names = "-paramA", description = "ParamA", variableArity = true)
    private List<String> paramA = Lists.newArrayList();

    @Parameter(names = "-paramB", description = "ParamB", variableArity = true)
    private List<String> paramB = Lists.newArrayList();
  }
  def "multiVariableArityList"() {
    when: "args parsed"
      String[] args = [ "-paramA", "a1", "a2", "-paramB", "b1", "b2", "b3" ];
      def p = new multiVariableParams();
      new JCommander(p, args).parse();
    then: "values match"
      ["a1", "a2"] == p.paramA
      ["b1", "b2", "b3"] == p.paramB
    when: "more args parsed"
      String[] moreArgs = [ "-paramA", "a1", "a2", "-paramB", "b1", "-paramA", "a3" ];
      def moreP = new multiVariableParams();
      new JCommander(moreP, moreArgs).parse();
    then: "more values match"
      ["a1", "a2", "a3"] == moreP.paramA
      ["b1"] == moreP.paramB
  }

  @Parameters(resourceBundle = "MessageBundle", commandDescriptionKey = "command")
  class commandKeyArgs {
    @Parameter(names="-myoption", descriptionKey="myoption")
    private boolean option;
  }
  @Ignore("no resource bundles")
  def "commandKey"() {
    expect: "it work"
      JCommander j = new JCommander();
      def a = new commandKeyArgs();
      j.addCommand("comm", a);
      j.usage();
  }

  class tmpA {
    @Parameter(names = "-b")
    public String b;
  }
  def "tmp"() {
    expect: "it work"
      new JCommander(new tmpA()).parse("");
  }

  @Parameters(optionPrefixes = "/")
  class SlashSeparator {

    @Parameter(names = "/verbose")
    public boolean verbose = false;

    @Parameter(names = "/file")
    public String file;
  }
  def "unknownOptionWithDifferentPrefix"() {
    when: "args parsed"
      SlashSeparator ss = new SlashSeparator();
      new JCommander(ss).parse("/notAParam");
    then: "error thrown and has proper text"
      def e = thrown(ParameterException)
      e.getMessage().contains("Unknown option")
  }

  @Parameters(separators = "=", commandDescription = "My command")
  class equalSeparatorMyClass {

    @Parameter(names = [ "-p", "--param" ], required = true, description = "param desc...")
    private String param;
  }
  def "equalSeparator"() {
    when: "args parsed"
      def c = new equalSeparatorMyClass();
      String expected = "\"hello\"world";
      new JCommander(c).parse("--param=" + expected);
    then: "values match"
      expected == c.param
  }

  def "simpleArgsSetter"() throws ParseException {
    when: "args parsed"
      Args1Setter args = new Args1Setter();
      String[] argv = [ "-debug", "-log", "2", "-float", "1.2", "-double", "1.3", "-bigdecimal", "1.4",
              "-date", "2011-10-26", "-groups", "unit", "a", "b", "c" ];
      new JCommander(args, argv);
    then: "values match"
      args.debug
      2 == args.verbose
      "unit" == args.groups
      ["a", "b", "c" ] == args.parameters
      1.2f closeTo(args.floa, 0.0001f)
      1.3f closeTo(args.doub, 0.0001f)
      new BigDecimal("1.4") == args.bigd
      new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-26") == args.date
  }

  class verifyHelpArg {
    @Parameter(names = "--help", help = true)
    public boolean help = false;

    @Parameter(names = "file", required = true)
    public String file;
  }
  def "verifyHelp"() {
    when: "args parsed"
      def arg = new verifyHelpArg();
      String[] argv = [ "--help" ];
      new JCommander(arg, argv);
    then: "values match"
      arg.help
  }

  class helpTestArg {
    @Parameter(names = [ "?", "-help", "--help" ], description = "Shows help", help = true)
    private boolean help = false;
  }
  def "helpTest"() {
    expect: "it work"
      def arg = new helpTestArg();
      JCommander jc = new JCommander(arg);
      jc.parse("-help");
  }


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
  @Parameters(separators = "=")
  class OneDoubleQuoteArg {
    @Parameter(names = [ "-p", "--param" ])
    private String param;
  }
  def "parameterWithOneDoubleQuote"() {
    expect: "it work"
      JCommander jc = new JCommander(new OneDoubleQuoteArg());
      jc.parse("-p=\"");
  }


  class EmptyStringAsDefaultArg {
    @Parameter(names = "-x")
    String s = "";
  }
  def "emptyStringAsDefault"() {
    when: "args parsed"
      EmptyStringAsDefaultArg a = new EmptyStringAsDefaultArg();
      StringBuilder sb = new StringBuilder();
      new JCommander(a).usage(sb);
    then: "values match"
    sb.toString().contains("Default: <empty string>")
  }

  class SpacesArg {
    @Parameter(names = "-rule", description = "rule")
    private List<String> rules = new ArrayList<String>();
  }
  def "spaces"() {
    when: "args parsed"
      SpacesArg a = new SpacesArg();
      new JCommander(a, "-rule", "some test");
    then: "values match"
      ["some test"] == a.rules
  }

  static class V2 implements IParameterValidator2 {
    final static List<String> names =  Lists.newArrayList();
    static boolean validateCalled = false;

    @Override
    public void validate(String name, String value) throws ParameterException {
      validateCalled = true;
    }

    @Override
    public void validate(String name, String value, ParameterDescription pd)
        throws ParameterException {
      names.addAll(Arrays.asList(pd.getParameter().names()));
    }
  }

  class Validator2Arg {
    @Parameter(names = [ "-h", "--host" ], validateWith = V2.class)
    String host;
  }
  public void validator2() {
    when: "args parsed"
      Validator2Arg a = new Validator2Arg();
      V2.names.clear();
      V2.validateCalled = false;
      JCommander jc = new JCommander(a, "--host", "h");
      jc.setAcceptUnknownOptions(true);
    then: "values match"
      ["-h", "--host"] == V2.names
      V2.validateCalled
  }

  class UsageCommandsUnderUsageArg {
  }
  @Parameters(commandDescription = "command a")
  class UsageCommandsArgCommandA {
    @Parameter(description = "command a parameters")
    List<String> parameters;
  }
  @Parameters(commandDescription = "command b")
  class UsageCommandsArgCommandB {
    @Parameter(description = "command b parameters")
    List<String> parameters;
  }
  def "usageCommandsUnderUsage"() {

    when: "commands added"
      UsageCommandsUnderUsageArg a = new UsageCommandsUnderUsageArg();

      JCommander c = new JCommander(a);
      c.addCommand("a", new UsageCommandsArgCommandA());
      c.addCommand("b", new UsageCommandsArgCommandB());

    then: "usage has proper text"
      StringBuilder sb = new StringBuilder();
      c.usage(sb);
      sb.toString().contains("[command options]\n  Commands:");
  }

  class UsageWithEmptyLineArg {
  }
  @Parameters(commandDescription = "command a")
  class ArgCommandA {
    @Parameter(description = "command a parameters")
    List<String> parameters;
  }
  @Parameters(commandDescription = "command b")
  class ArgCommandB {
    @Parameter(description = "command b parameters")
    List<String> parameters;
  }
  def "usageWithEmpytLine"() {

    when: "commands added"
      UsageWithEmptyLineArg a = new UsageWithEmptyLineArg();

      JCommander c = new JCommander(a);
      c.addCommand("a", new ArgCommandA());
      c.addCommand("b", new ArgCommandB());

    then: "usage has proper text"
      StringBuilder sb = new StringBuilder();
      c.usage(sb);
      sb.toString().contains("command a parameters\n\n    b");
  }


  class PartialValidationArg {
    @Parameter(names = [ "-h", "--host" ])
    String host;
  }
  public void partialValidation() {
    when: "args parsed"
      PartialValidationArg a = new PartialValidationArg();
      JCommander jc = new JCommander();
      jc.setAcceptUnknownOptions(true);
      jc.addObject(a);
      jc.parse("-a", "foo", "-h", "host");
    then: "values match"
      "host" == a.host
      ["-a", "foo"] == jc.getUnknownOptions()
  }

  /**
   * GITHUB-137.
   */
  class ListArgShouldBeClearedArgs {
    @Parameter(description = "[endpoint]")
    public List<String> endpoint = Lists.newArrayList("prod");
  }
  def "listArgShouldBeCleared"() {
    when: "args parsed"
      ListArgShouldBeClearedArgs a = new ListArgShouldBeClearedArgs();
      new JCommander(a,  "dev" );
      then: "values match"
        ["dev"] == a.endpoint
  }

  class DashDashParameterArguments {
    @Parameter(names = [ "-name" ])
    public String name;
    @Parameter
    public List<String> mainParameters;
  }
  def "dashDashParameter"() {
    when: "args parsed"
      DashDashParameterArguments a = new DashDashParameterArguments();
      new JCommander(a, "-name", "theName", "--", "param1", "param2");
    then: "values match"
      "theName" == a.name
      ["param1", "param2"] == a.mainParameters
  }

  class DashDashParameter2Arguments {
    @Parameter(names = [ "-name" ])
    public String name;
    @Parameter
    public List<String> mainParameters;
  }
  public void dashDashParameter2() {
    when: "args parsed"
      DashDashParameter2Arguments a = new DashDashParameter2Arguments();
      new JCommander(a, "param1", "param2", "--", "param3", "-name", "theName");
    then: "values match"
      null == a.name
      ["param1", "param2", "param3", "-name", "theName"] == a.mainParameters
  }

  class AccessArguments {
    private int bar;

    @Parameter(names = "-bar")
    private void setBar(int value) {
      bar = value;
    }
  }
  public void access() {
    when: "args parsed"
      AccessArguments a = new AccessArguments();
      new JCommander(a, "-bar", "1");
    then: "error thrown"
      def e = thrown(ParameterException)
      e.getMessage().contains("invoke")
  }

  public static void main(String[] args) throws Exception {
    new JCommanderTest().access();
//    class A {
//      @Parameter(names = "-short", required = true)
//      List<String> parameters;
//
//      @Parameter(names = "-long", required = true)
//      public long l;
//    }
//    A a = new A();
//    new JCommander(a).parse();
//    System.out.println(a.l);
//    System.out.println(a.parameters);
//    ArgsList al = new ArgsList();
//    JCommander j = new JCommander(al);
//    j.setColumnSize(40);
//    j.usage();
//    new JCommanderTest().testListAndSplitters();
//    new JCommanderTest().converterArgs();
  }

  // Tests:
  // required unparsed parameter
}
