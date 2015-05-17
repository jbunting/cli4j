package io.bunting.cli4j.parser.args;

import io.bunting.cli4j.parser.HostPort;
import io.bunting.cli4j.parser.HostPortConverter;
import io.bunting.cli4j.parser.IStringConverter;
import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.converters.IParameterSplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgsList {
  @Parameter(names = "-groups", description = "Comma-separated list of group names to be run")
  public List<String> groups;

  @Parameter(names = "-ints")
  public List<Integer> ints;

  @Parameter(names = "-hp", converter = HostPortConverter.class, splitter = SemiColonSplitter.class)
  public List<HostPort> hostPorts;

  @Parameter(names = "-hp2", converter = HostPortConverter.class)
  public List<HostPort> hp2;

  @Parameter(names = "-uppercase", listConverter = UppercaseConverter.class)
  public List<String> uppercase;

  public static class UppercaseConverter implements IStringConverter<List<String>> {
    public List<String> convert(String value) {
      List<String> result = new ArrayList<>();
      String[] s = value.split(",");
      for (String p : s) {
        result.add(p.toUpperCase());
      }
      return result;
    }
  }

  public static class SemiColonSplitter implements IParameterSplitter {

    public List<String> split(String value) {
      return Arrays.asList(value.split(";"));
    }
    
  }

}
