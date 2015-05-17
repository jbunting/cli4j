package io.bunting.cli4j.parser.dynamic;

import io.bunting.cli4j.parser.JCommander;
import io.bunting.cli4j.parser.ParameterException;
import io.bunting.cli4j.parser.internal.Maps;
import org.junit.Assert;
import org.junit.Test;


public class DynamicParameterTest {

  @Test(expected = ParameterException.class)
  public void nonMapShouldThrow() {
    new JCommander(new DSimpleBad()).parse("-D", "a=b", "-D", "c=d");
  }

  @Test(expected = ParameterException.class)
  public void wrongSeparatorShouldThrow() {
    DSimple ds = new DSimple();
    new JCommander(ds).parse("-D", "a:b", "-D", "c=d");
  }

  private void simple(String... parameters) {
    DSimple ds = new DSimple();
    new JCommander(ds).parse(parameters);
    Assert.assertEquals(ds.params, Maps.newHashMap("a", "b", "c", "d"));
  }

  @Test
  public void simpleWithSpaces() {
    simple("-D", "a=b", "-D", "c=d");
  }

  @Test
  public void simpleWithoutSpaces() {
    simple("-Da=b", "-Dc=d");
  }

  @Test
  public void usage() {
    DSimple ds = new DSimple();
    new JCommander(ds).usage(new StringBuilder());
  }

  @Test
  public void differentAssignment() {
    DSimple ds = new DSimple();
    new JCommander(ds).parse("-D", "a=b", "-A", "c@d");
    Assert.assertEquals(ds.params, Maps.newHashMap("a", "b"));
    Assert.assertEquals(ds.params2, Maps.newHashMap("c", "d"));
  }

  public static void main(String[] args) {
    DynamicParameterTest dpt = new DynamicParameterTest();
    dpt.simpleWithSpaces();
//    dpt.nonMapShouldThrow();
//    dpt.wrongSeparatorShouldThrow();
//    dpt.differentAssignment();
//    dpt.arity0();
//    dpt.usage();
  }
}
