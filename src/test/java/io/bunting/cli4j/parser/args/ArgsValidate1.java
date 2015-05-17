package io.bunting.cli4j.parser.args;

import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.validators.PositiveInteger;

public class ArgsValidate1 {

  @Parameter(names = "-age", validateWith = PositiveInteger.class)
  public Integer age;
}
