package io.bunting.cli4j.parser;

import io.bunting.cli4j.parser.IStringConverter;

import java.util.SortedSet;
import java.util.TreeSet;

public class SetConverter implements IStringConverter<SortedSet<Integer>> {
 
  public SortedSet<Integer> convert(String value) {
    SortedSet<Integer> set = new TreeSet<Integer>();
    String[] values = value.split(",");
    for (String num : values) {
      set.add(Integer.parseInt(num));
    }
    return set;
  }
}
