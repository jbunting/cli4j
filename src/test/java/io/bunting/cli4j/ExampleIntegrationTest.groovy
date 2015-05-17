package io.bunting.cli4j

import io.bunting.cli4j.example.HelloCommand
import spock.lang.Specification

/**
 * TODO: Document this class
 */
class ExampleIntegrationTest extends Specification {
  def "test hello command"() {
    given: "a console"
      CliAdapter cliAdapter = Mock(CliAdapter)
    and: "a cli4j"
      Cli4J cli4J = new Cli4J(cliAdapter)
      cli4J.addResource(HelloCommand.class)
    when: "i invoke a command"
      cli4J.execute("hello --name Fred".split("\\s+"))
    then: "i got output"
      1 * cliAdapter.printf("Hello %s!%n", "Fred")
    when: "i invoke a command with count 5"
      cli4J.execute("hello --name Fred --count 5".split("\\s+"))
    then: "i got the output 5 times"
      5 * cliAdapter.printf("Hello %s!%n", "Fred")
  }
}
