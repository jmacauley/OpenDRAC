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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.protocol.tl1;

public final class TL1Util {
  private static final char QUOTE = '"';

  private static final char SPACE = 32;
  private static final char COLON = ':';
  private static final char SEMI_COLON = ';';
  private static final char SLASH_CHAR = '\\';
  private static final char COMMA = ',';
  private static final String SLASH_STRING = "\\";
  private static final String EMPTY_STRING = "\"\"";
  private static final String QUOTE_STRING = "\"";
  private static final String SLASH_QUOTE_STRING = SLASH_STRING + QUOTE_STRING;
  private static final String DOUBLE_SLASH_STRING = "\\\\";

  private TL1Util() {
  }

  /**
   * Insert the method's description here. Creation date: (2/1/02 5:54:21 PM)
   */

  /**
   * This method should not be of any use to anyone but the engine. Given a
   * quoted String ( ie not the stuff from the payload) get the unquoted String.
   * For INSTANCE, This might be used when cleaning up a TID from the NE.
   */

  // public static String getCleanedString(String input)
  // {
  // if (input == null)
  // {
  // return null;
  // }
  // if (!input.startsWith("\""))
  // {
  // // What! This isn't quoted!
  // //
  // return input;
  // }
  // if ("".equals(input))
  // {
  // return input;
  // }
  // char in[] = input.toCharArray();
  // int end = in.length;
  // if (in[end - 1] == '"')
  // {
  // end = in.length - 1;
  // }
  // final int realEnd = end;
  // StringBuilder buff = new StringBuilder(25);
  // char item;
  // int index = 1;
  // while (index < realEnd)
  // {
  // item = in[index];
  // if (item == '\\')
  // {
  // try
  // {
  // char next = in[index + 1];
  // if (next == '"')
  // {
  // buff.append('"');
  // index++;
  // }
  // else if (next == '\\')
  // {
  // buff.append('\\');
  // index++;
  // }
  // }
  // catch (IndexOutOfBoundsException e)
  // {
  // }
  // }
  // else
  // {
  // buff.append(item);
  // }
  // index++;
  // }
  // return buff.toString();
  // }
  /**
   * Given the input String return a quoted String if the input String contains
   * any characters that should be quoted. This method will not quote a String
   * if it doesn't need to be.
   */

  public static String getQuotedString(String input) {
    if (input == null) {
      return null;
    }

    if (input.length() == 0) {
      return EMPTY_STRING;
    }

    if (input.equals(SLASH_STRING)) {
      return SLASH_STRING;
    }

    char[] in = input.toCharArray();
    boolean outputShouldBeQuoted = false;
    boolean alreadyQuoted = false; // ifStringAlreadyQuoted(in);

    /*
     * if (alreadyQuoted) if (input.length() <= 2) return input;
     */

    StringBuilder buff = new StringBuilder(25);

    for (char toAppend : in) {
      if (!outputShouldBeQuoted) {
        if (toAppend == SPACE || toAppend == COLON || toAppend == SEMI_COLON
            || toAppend == COMMA) {
          outputShouldBeQuoted = true;
        }
      }

      if (toAppend == QUOTE) {
        /*
         * if (alreadyQuoted) { if ( index == 0 || index == in.length - 1)
         * buff.append(toAppend); else { buff.append(SLASH_QUOTE_STRING);
         * outputShouldBeQuoted = true; } } else
         */{
          buff.append(SLASH_QUOTE_STRING);
          outputShouldBeQuoted = true;
        }
      }
      else if (toAppend == SLASH_CHAR) {
        buff.append(DOUBLE_SLASH_STRING);
      }
      else {
        buff.append(toAppend);
      }
    }

    // // if not already quoted then quotes it out
    if (outputShouldBeQuoted && !alreadyQuoted) {
      buff.insert(0, QUOTE);
      buff.append(QUOTE);
    }
    return buff.toString();
  }

}
