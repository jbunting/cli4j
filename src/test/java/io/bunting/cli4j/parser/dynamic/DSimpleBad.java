package io.bunting.cli4j.parser.dynamic;

import io.bunting.cli4j.parser.DynamicParameter;

import java.util.List;

public class DSimpleBad {

  @DynamicParameter(names = "-D")
  public List<String> params;
}
