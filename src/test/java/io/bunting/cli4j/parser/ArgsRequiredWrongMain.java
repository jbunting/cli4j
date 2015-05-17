package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.Parameter;

public class ArgsRequiredWrongMain {
  @Parameter(required = true)
  public String[] file;
}
