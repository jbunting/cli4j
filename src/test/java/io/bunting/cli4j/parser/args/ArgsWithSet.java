package io.bunting.cli4j.parser.args;

import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.SetConverter;

import java.util.SortedSet;

public class ArgsWithSet {
  @Parameter(names = "-s", converter = SetConverter.class)
  public SortedSet<Integer> set;
}
