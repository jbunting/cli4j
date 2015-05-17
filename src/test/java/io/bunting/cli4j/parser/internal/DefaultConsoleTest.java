package io.bunting.cli4j.parser.internal;

import io.bunting.cli4j.parser.internal.Console;
import io.bunting.cli4j.parser.internal.DefaultConsole;
import org.junit.Assert;
import org.junit.Test;


import java.io.IOException;
import java.io.InputStream;

public class DefaultConsoleTest {
  @Test
  public void readPasswordCanBeCalledMultipleTimes() {
    final InputStream inBackup = System.in;
    try {
      final StringInputStream in = new StringInputStream();
      System.setIn(in);
      final Console console = new DefaultConsole();

      in.setData("password1\n");
      char[] password = console.readPassword(false);
      Assert.assertArrayEquals(password, "password1".toCharArray());
      Assert.assertFalse("System.in stream shouldn't be closed", in.isClosedCalled());

      in.setData("password2\n");
      password = console.readPassword(false);
      Assert.assertArrayEquals(password, "password2".toCharArray());
      Assert.assertFalse("System.in stream shouldn't be closed", in.isClosedCalled());
    } finally {
      System.setIn(inBackup);
    }
  }

  private static class StringInputStream extends InputStream {
    private byte[] data = new byte[0];
    private int offset = 0;
    private boolean closedCalled;

    StringInputStream() {
      super();
    }

    void setData(final String strData) {
      data = strData.getBytes();
      offset = 0;
    }

    boolean isClosedCalled() {
      return closedCalled;
    }

    @Override
    public int read() throws IOException {
      if (offset >= data.length) {
        return -1;
      }
      return 0xFFFF & data[offset++];
    }

    @Override
    public void close() throws IOException {
      closedCalled = true;
      super.close();
    }
  }
}
