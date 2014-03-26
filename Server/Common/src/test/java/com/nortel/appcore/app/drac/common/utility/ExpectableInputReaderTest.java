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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpectableInputReaderTest {
  private static final Logger log = LoggerFactory.getLogger(ExpectableInputReaderTest.class);

	static class StringInputStream extends InputStream

	{
		private final byte[] b;
		int offset = 0;

		public StringInputStream(String s) {
			b = s.getBytes();
		}

		@Override
		public int read() throws IOException {
			if (offset >= b.length) {
				return -1;
			}
			return b[offset++];

		}

	}

	@Test
	public void testExpectPatternLong() throws Exception {
		ExpectableInputReader e = new ExpectableInputReader(new StringInputStream(
		    "hellohellozzzip"), null, "number1");
		e.setDaemon(true);
		e.start();
		Pattern p = Pattern.compile("he.*o");
		String res;
		res = e.expect(p, 10 * 1000, true);
		log.debug("Got " + res);
		res = e.expect(Pattern.compile("ip"), 10 * 1000, true, 0);
		log.debug("Got " + res);
	}

	@Test
	public void testExpectStringLong() throws Exception {
		ExpectableInputReader e = new ExpectableInputReader(new StringInputStream(
		    "hellohello"), null, "number 2");
		e.setDaemon(true);
		e.start();
		e.expect("hello", 10 * 1000, true);
		e.expect("hello", 10 * 1000, true);
	}

}
