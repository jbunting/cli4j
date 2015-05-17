package io.bunting.cli4j;

import java.io.Console;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * TODO: Document this class
 */
class ConsoleCliAdapter implements CliAdapter {
  private final Console console;

  ConsoleCliAdapter(Console console) {
    this.console = console;
  }

  @Override
  public PrintWriter writer() {
    return console.writer();
  }

  @Override
  public String readLine(String fmt, Object... args) {
    return console.readLine(fmt, args);
  }

  @Override
  public char[] readPassword() {
    return console.readPassword();
  }

  @Override
  public CliAdapter printf(String format, Object... args) {
    console.printf(format, args);
    return this;
  }

  @Override
  public char[] readPassword(String fmt, Object... args) {
    return console.readPassword(fmt, args);
  }

  @Override
  public void flush() {
    console.flush();
  }

  @Override
  public Reader reader() {
    return console.reader();
  }

  @Override
  public String readLine() {
    return console.readLine();
  }
}
