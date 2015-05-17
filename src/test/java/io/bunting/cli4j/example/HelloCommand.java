package io.bunting.cli4j.example;

import com.beust.jcommander.Parameter;
import io.bunting.cli4j.Cli4J;
import io.bunting.cli4j.CliAdapter;
import io.bunting.cli4j.Command;

import java.io.Console;

/**
 * TODO: Document this class
 */
public class HelloCommand {
  public static class HelloArgs {
    @Parameter(names = "--count", description = "Number of greetings.")
    private int count = 1;

    @Parameter(names = "--name", required = true, description = "The person to greet.")
    private String name;
  }

  @Command(name = "hello", description = "Simple program that greets NAME for a total of COUNT times.")
  public void hello(final CliAdapter cliAdapter, final HelloArgs args) {
    for (int i = 0; i < args.count; i++) {
      cliAdapter.printf("Hello %s!%n", args.name);
    }
  }

  public static void main(final String ... args) {
    Cli4J cli = new Cli4J();
    cli.addResource(HelloCommand.class);

    cli.execute(args);
  }
}
