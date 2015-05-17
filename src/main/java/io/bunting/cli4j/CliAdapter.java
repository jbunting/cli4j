package io.bunting.cli4j;

import java.io.PrintWriter;
import java.io.Reader;

/**
 * An adapter for the Cli Environment.
 */
public interface CliAdapter {
  PrintWriter writer();

  String readLine(String fmt, Object... args);

  char[] readPassword();

  CliAdapter printf(String format, Object... args);

  char[] readPassword(String fmt, Object... args);

  void flush();

  Reader reader();

  String readLine();
}
