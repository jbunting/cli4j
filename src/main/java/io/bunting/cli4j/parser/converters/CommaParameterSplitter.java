package io.bunting.cli4j.parser.converters;

import io.bunting.cli4j.parser.converters.IParameterSplitter;

import java.util.Arrays;
import java.util.List;

public class CommaParameterSplitter implements IParameterSplitter {

  public List<String> split(String value) {
    return Arrays.asList(value.split(","));
  }

}
