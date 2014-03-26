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

package com.nortel.appcore.app.drac.common.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pitman
 */

public final class StringParser {
  private static final Logger log = LoggerFactory.getLogger(StringParser.class);
	public static final Pattern COLONS_PATTERN = Pattern.compile("::");

	// Special characters in DRAC
	private static final String UNENCODED_COLON = ":";
	private static final Pattern UNENCODED_COLON_PATTERN = Pattern
	    .compile(UNENCODED_COLON);
	private static final String ENCODED_COLON = ".colon.";
	private static final Pattern ENCODED_COLON_PATTERN = Pattern
	    .compile(ENCODED_COLON);

	// Characters requiring escape for browser display (e.g. tree)
	private static final String UNESCAPED_APOS = "'";
	private static final Pattern UNESCAPED_APOS_PATTERN = Pattern
	    .compile(UNESCAPED_APOS);
	private static final String ESCAPED_APOS = "\\'";

	// Special characters in XML
	private static final String UNENCODED_AMP = "&";
	private static final Pattern UNENCODED_AMP_PATTERN = Pattern
	    .compile(UNENCODED_AMP);
	private static final String ENCODED_AMP = "&amp;";
	private static final Pattern ENCODED_AMP_PATTERN = Pattern
	    .compile(ENCODED_AMP);

	private static final String UNENCODED_GT = ">";
	private static final Pattern UNENCODED_GT_PATTERN = Pattern
	    .compile(UNENCODED_GT);
	private static final String ENCODED_GT = "&gt;";
	private static final Pattern ENCODED_GT_PATTERN = Pattern.compile(ENCODED_GT);

	private static final String UNENCODED_LT = "<";
	private static final Pattern UNENCODED_LT_PATTERN = Pattern
	    .compile(UNENCODED_LT);
	private static final String ENCODED_LT = "&lt;";
	private static final Pattern ENCODED_LT_PATTERN = Pattern.compile(ENCODED_LT);

	private static final String UNENCODED_APOS = "'";
	private static final Pattern UNENCODED_APOS_PATTERN = Pattern
	    .compile(UNENCODED_APOS);
	private static final String ENCODED_APOS = "&apos;";
	private static final Pattern ENCODED_APOS_PATTERN = Pattern
	    .compile(ENCODED_APOS);

	private static final String UNENCODED_QUOTE = "\"";
	private static final Pattern UNENCODED_QUOTE_PATTERN = Pattern
	    .compile(UNENCODED_QUOTE);
	private static final String ENCODED_QUOTE = "&quot;";
	private static final Pattern ENCODED_QUOTE_PATTERN = Pattern
	    .compile(ENCODED_QUOTE);

	private StringParser() {
		// all methods are static.
	}

	public static final String decodeForDRACSpecialChars(String input) {
		if (input != null && input.length() > 0) {
			input = ENCODED_COLON_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(UNENCODED_COLON));
		}

		return input;
	}

	public static final String decodeForXMLSpecialChars(String input) {
		if (input != null && input.length() > 0) {
			input = ENCODED_AMP_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(UNENCODED_AMP));

			input = ENCODED_GT_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(UNENCODED_GT));

			input = ENCODED_LT_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(UNENCODED_LT));

			input = ENCODED_APOS_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(UNENCODED_APOS));

			input = ENCODED_QUOTE_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(UNENCODED_QUOTE));
		}

		return input;
	}

	public static final String encodeForDRACSpecialChars(String input) {
		if (input != null && input.length() > 0) {
			input = UNENCODED_COLON_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ENCODED_COLON));
		}

		return input;
	}

	public static final String encodeForXMLSpecialChars(String input) {
		if (input != null && input.length() > 0) {
			input = UNENCODED_AMP_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ENCODED_AMP));

			input = UNENCODED_GT_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ENCODED_GT));

			input = UNENCODED_LT_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ENCODED_LT));

			input = UNENCODED_APOS_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ENCODED_APOS));

			input = UNENCODED_QUOTE_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ENCODED_QUOTE));
		}

		return input;
	}

	public static final String escapeForClient(String input) {
		if (input != null && input.length() > 0) {
			input = UNESCAPED_APOS_PATTERN.matcher(input).replaceAll(
			    Matcher.quoteReplacement(ESCAPED_APOS));
		}

		return input;
	}

	public static double parseDouble(String sValue) {
		double result = -1.0;
		try {
			result = Double.parseDouble(sValue);
		}
		catch (Exception e) {
			log.error("Could not parse as double: " + sValue);
		}
		return result;
	}

	/**
	 * Had to re-implement our own String.split(), because the performance from
	 * the built-in java version is really bad for splitting strings by single
	 * character delimiters. When reportRepetitions is set to true, split("a==b",
	 * '=', true) returns [a,,b], whereas split("a==b", '=', false) returns [a,b]
	 */
	public static List<String> split(String aString, char delimiter,
	    boolean reportRepetitions) {
		List<String> result = new ArrayList<String>();
		int begin = 0;
		for (int curIndex = 0; curIndex < aString.length(); curIndex++) {
			if (aString.charAt(curIndex) == delimiter) {
				if (curIndex == begin) {
					if (reportRepetitions) {
						result.add("");
					}
				}
				else {
					result.add(aString.substring(begin, curIndex));
				}
				begin = curIndex + 1;
			}
			else if (curIndex == aString.length() - 1) {
				result.add(aString.substring(begin));
			}
		}
		return result;
	}

}
