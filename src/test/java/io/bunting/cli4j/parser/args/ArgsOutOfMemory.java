package io.bunting.cli4j.parser.args;

import io.bunting.cli4j.parser.Parameter;

public class ArgsOutOfMemory
{
  @Parameter(names = { "-p", "--pattern"  },
      description = "pattern used by 'tail'. See http://logback.qos.ch/manual/layouts.html#ClassicPatternLayout and http://logback.qos.ch/manual/layouts.html#AccessPatternLayout")
  public String pattern;

  @Parameter(names = "-q", description = "Filler arg")
  public String filler;
}
