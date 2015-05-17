package io.bunting.cli4j.parser.dynamic;

import io.bunting.cli4j.parser.DynamicParameter;

import java.util.HashMap;
import java.util.Map;

public class DSimple {

  @DynamicParameter(names = "-D", description = "Dynamic parameters go here")
  public Map<String, String> params = new HashMap<>();

  @DynamicParameter(names = "-A", assignment = "@")
  public Map<String, String> params2 = new HashMap<>();
}
