package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.MissingCommandException;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.Parameters;

import org.junit.Assert;
import org.junit.Test
import spock.lang.Specification;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CmdTest extends Specification {

    @Parameters(commandNames = "--cmd-one")
    public static class CmdOne {
    }

    @Parameters(commandNames = "--cmd-two")
    class CmdTwo {
        @Parameter
        List<String> params = new LinkedList<String>();
    }

    public String parseArgs(boolean withDefault, String[] args) {
        JCommander jc = new JCommander();
        jc.addCommand(new CmdOne());
        jc.addCommand(new CmdTwo());

        if (withDefault) {
            // First check if a command was given, when not prepend default
            // command (--cmd-two")
            // In version up to 1.23 JCommander throws an Exception in this
            // line,
            // which might be incorrect, at least its not reasonable if the
            // method
            // is named "WithoutValidation".
            jc.parseWithoutValidation(args);
            if (jc.getParsedCommand() == null) {
                LinkedList<String> newArgs = new LinkedList<String>();
                newArgs.add("--cmd-two");
                newArgs.addAll(Arrays.asList(args));
                jc.parse(newArgs.toArray(new String[0]));
            }
        } else {
            jc.parse(args);
        }
        return jc.getParsedCommand();
    }

    public Object[][] testData() {
        return [
                [ "--cmd-one", false, [ "--cmd-one" ] ],
                [ "--cmd-two", false, [ "--cmd-two" ] ],
                [ "--cmd-two", false, [ "--cmd-two", "param1", "param2" ] ],
                // This is the relevant test case to test default commands
                [ "--cmd-two", true, [ "param1", "param2" ] ] ];
    }

    public void testArgsWithoutDefaultCmd(String expected, String[] args) {
        expect: "args parsed successfully"
            expected == parseArgs(false, args)
        where:
          [expected, _, args] << testData().findAll {!it[1]}

    }

    public void testArgsWithoutDefaultCmdFail(String expected, String[] args) {
        when: "args parsed"
          parseArgs(false, args);
        then: "error thrown"
            thrown(MissingCommandException)
        where:
          [expected, _, args] << testData().findAll {it[1]}
    }

    // We do not expect a MissingCommandException!
    public void testArgsWithDefaultCmd(String expected, String[] args) {
        expect: "commands are parsed"
            expected == parseArgs(true, args)
        where:
          [expected, _, args] << testData()
    }

}
