[![Build Status](https://travis-ci.org/jbunting/cli4j.svg)](https://travis-ci.org/jbunting/cli4j) [![Coverage Status](https://coveralls.io/repos/jbunting/cli4j/badge.svg)](https://coveralls.io/r/jbunting/cli4j)
cli4j
-----

A CLI command framework for Java. Heavily inspired by [Click](click) and [JAX-RS](jaxrs). Based on parsing from 
[JCommander](jcommander) and console interaction from [JLine](jline).

 [click]: http://click.pocoo.org/4/
 [jaxrs]: https://jax-rs-spec.java.net/
 [jcommander]: http://jcommander.org/
 [jline]: http://jline.github.io/jline2/
                             
The goal is to make it simple and easy for Java developers to write multi-command style shell tools.

I'll also note that the style and content of this file borrows heavily from [Click](click) -- at least for now.

## Provides

 * annotation based command definition, similar to JAX-RS
 * annotation based option/argument parsing vi [JCommander](jcommander)
 * automatic help page generation
 * arbitrarily deep nesting of commands
 * lazy instantiation of command backing classes to reduce startup time
 
## Simple?

Here's an example of what a command would look like:

```java
public class HelloCommand {
  public static class HelloArgs {
    @Parameter(names = "--count", description = "Number of greetings.")
    private int count = 1;

    @Parameter(names = "--name", required = true, description = "The person to greet.")
    private String name;
  }

  @Command(name = "hello")
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
```
 
## Next Steps

1. Remove JCommander from public API.
2. Move from reflection-based inspection to annotation processor based.
3. Modify Hello to define @Parameter directly on method parameters.
4. Allow for overloading of methods to indicate optional parameters.
5. Develop benchmarking.
6. Add output controls. (color, formatting, etc)
7. Improve testing process.
8. Make using jline in commands simple.
9. Make using jcurses in commands simple -- maybe add a jaxrs-like framework for it.
