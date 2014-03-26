/**
 * <pre>
 * The owner of the original code is Ciena Corporation.
 *
 * Portions created by the original owner are Copyright (C) 2004-2010
 * the original owner. All Rights Reserved.
 *
 * Portions created by other contributors are Copyright (C) the contributor.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   (Contributors insert name & email here)
 *
 * This file is part of DRAC (Dynamic Resource Allocation Controller).
 *
 * DRAC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DRAC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 */

package com.nortel.appcore.app.drac.sshclisimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Force10SwitchCliParser: reads standard input and responds to basic force 10
 * CLi commands in a way that simulates a real switch.
 *
 * @author pitman
 */
class Force10SwitchCliParser extends Thread {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final BufferedReader in;
  private final OutputStream out;
  // private final OutputStream err;
  private final ExitCallback callback;
  private final String prompt;
  private final String dir;

  // "\r\fAsd001A_F25S1T#";
  Force10SwitchCliParser(InputStream input, OutputStream output,
      OutputStream error, ExitCallback cb, String commandPrompt,
      String directoryPath) {
    in = new BufferedReader(new InputStreamReader(input));
    out = output;
    // err = error;
    callback = cb;
    prompt = "\r\n" + commandPrompt;
    dir = directoryPath;
    setName("Force10SwitchCliParser ");
  }

  @Override
  public void run() {
    try {

      log.info("CLI running");
      writePrompt();

      while (true) {
        String line = in.readLine();

        if (line == null) {
          if (callback != null) {
            callback.onExit(0, "client EOF encountered");
          }
          break;
        }

        // local echo
        out.write(line.getBytes());
        out.write("\r\n".getBytes());
        out.flush();

        if ("".equals(line)) {
          writePrompt();
          continue;
        }

        if ("exit".equalsIgnoreCase(line)) {
          if (callback != null) {
            callback.onExit(0, "client exited");
          }
          break;
        }
        else if ("terminal length 0".equals(line)) {
          // ok done we default to this anyway.
        }
        else if (line.startsWith("show")) {
          try {
            processShowCommands(line);
          }
          catch (Exception e) {
            log.error("Error processing command " + line, e);
          }
        }
        else {
          log.info("Unexpected CLI command, ignoring input '" + line + "'.");
        }

        writePrompt();
      }
    }
    catch (Exception e) {

      if (e.getClass().isAssignableFrom(IOException.class) && e.toString().contains("Pipe closed")) {
        log.info("Expected IOException caused by termination and Pipe closed");
      }
      else {
        log.error("Error: ", e);
      }

      if (callback != null) {
        callback.onExit(-1, "Exception in simulator " + e.getMessage());
      }
    }
  }

  private void processShowCommands(String cmd) throws IOException {

    StringBuilder res = new StringBuilder("/");
    res.append(dir);
    res.append("/show");

    StringTokenizer st = new StringTokenizer(cmd);
    st.nextToken();
    String verb = null;
    while (st.hasMoreTokens()) {
        verb = st.nextToken();
        res.append(" ");
        res.append(verb);
    }

    res.append(".txt");

    log.info("Opening resource file: " + res);

    InputStream i = Force10SwitchCliParser.class.getResourceAsStream(res.toString());

    if (i == null) {
      out.write("                     ^\n\r% Error: Invalid input at \"^\" marker.\n\r"
          .getBytes());
      out.flush();
      log.info("Could not find response file for command line " + cmd
          + " verb " + verb + " resource " + res);
      return;
    }

    try {
      byte[] buf = new byte[8 * 1024];
      int len = i.read(buf);
      while (len > 0) {
        out.write(buf, 0, len);
        len = i.read(buf);
      }
      out.flush();
    }
    finally {
      i.close();
    }

    log.info("Processed command " + cmd);

  }

  private void writePrompt() throws IOException {
    out.write(prompt.getBytes());
    out.flush();
  }
}