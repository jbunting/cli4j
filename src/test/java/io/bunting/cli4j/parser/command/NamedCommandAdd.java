package io.bunting.cli4j.parser.command;

import io.bunting.cli4j.parser.Parameter;
import io.bunting.cli4j.parser.Parameters;

import java.util.List;

@Parameters(commandNames = "add", commandDescription = "Add file contents to the index")
public class NamedCommandAdd {

  @Parameter(description = "Patterns of files to be added")
  public List<String> patterns;

  @Parameter(names = "-i")
  public Boolean interactive = false;

}
