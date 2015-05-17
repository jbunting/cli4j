package io.bunting.cli4j.parser.args;

import io.bunting.cli4j.parser.Parameter;

public class Arity1
{
  @Parameter(arity = 1, names = "-inspect", description = "", required = false)
  public boolean inspect;
}
