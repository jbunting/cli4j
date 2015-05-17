package io.bunting.cli4j.parser.args;

import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.Parameters;

@Parameters(separators = "=")
public class ArgsEquals {

  @Parameter(names = "-args")
  public String args;
}
