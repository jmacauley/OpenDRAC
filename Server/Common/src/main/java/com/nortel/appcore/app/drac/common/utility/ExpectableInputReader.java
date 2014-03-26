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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread/class that sucks data in from a input stream and buffers it
 * (forever). The caller can then use a pattern to examine the buffer and return
 * when the pattern is matched or a time out or end of file occurs. Once a match
 * is found, data in the buffer from the start of the buffer to the end of the
 * match is removed. Mimics the expect program...
 *
 * @author pitman
 * @since June 15, 2010
 */

public final class ExpectableInputReader extends Thread {
  private final org.slf4j.Logger Log = LoggerFactory.getLogger(getClass());
	private final BufferedReader br;
	private StringBuilder cbuf = new StringBuilder(1024);
	private final Object lock = new Object();
	private boolean eof = false;
	private final Logger log;

	public ExpectableInputReader(InputStream stream, Logger l, String title) {
		super();
		log = l;
		br = new BufferedReader(new InputStreamReader(stream));
		setName("ExpectableInputReader for " + title);
	}

	public void clearBuffer() {
		synchronized (lock) {
			cbuf.setLength(0);
		}
	}

	public void close() {
		try {
			if (br != null) {
				br.close();
			}
		}
		catch (IOException e) {
			Log.error("Error: ", e);
		}
		clearBuffer();
	}

	public String expect(Pattern p, long timeout, boolean clearOnMatch)
	    throws Exception {
		return expect(p, timeout, clearOnMatch, 0);
	}

	/**
	 * Look in the input buffer for the given pattern 'p'. Wait at most 'timeout'
	 * before giving up. if 'clearOnMatch' remove the matching pattern from the
	 * buffer, otherwise leave it as is. Return the regular expression group
	 * 'groupToReturn' if successful, exception otherwise. Use 0 for groupToReturn
	 * to return the entire pattern.
	 */
	public String expect(Pattern p, long timeout, boolean clearOnMatch,
	    int groupToReturn) throws Exception {

		Log.debug("looking for regex pattern '" + p
		    + "' in buffer with timeout of " + timeout);

		long timeToDie = System.currentTimeMillis() + timeout;
		boolean seenEofFlag = false;
		while (!seenEofFlag) {
			synchronized (lock) {
				if (eof) {
					/*
					 * if we have encountered an eof then no more will be added to the
					 * buffer, try to match one (more) time then give up
					 */
					seenEofFlag = true;
				}

				Log.debug("scanning buffer of " + cbuf.length()
				    + " bytes for regex match " + p);

				Matcher m = p.matcher(cbuf);
				if (m.find()) {
					Log.debug("A match was found!");
					String match = m.group(groupToReturn);
					Log.debug("ExpectableInputReader found a match of " + match + " in buffer " + cbuf);
					// found a match. clear everything in the buffer up to the match
					if (clearOnMatch) {
						cbuf = new StringBuilder(cbuf.substring(m.end()));
						cbuf.ensureCapacity(1024);
						Log.debug("ExpectableInputReader buffer after match " + cbuf);
					}

					return match;
				}
				Log.debug("No match was found for: " + p.toString());

				/*
				 * no match (yet) wait until the timeout occurs or we get waken up
				 * because more data was added to the buffer.
				 */
				long timeLeft = timeToDie - System.currentTimeMillis();

                Log.debug("Time left to wait: " + timeLeft);

				if (timeLeft <= 0) {
					throw new Exception("timeout waiting for regex pattern '"
					    + p.toString() + "' buffer " + cbuf);
				}

				lock.wait(timeLeft);
				// either new data was added or EOF/error occurred and EOF is true.
			}
		}

		throw new Exception(
		    "Encountered end of file while waiting for regex pattern '"
		        + p.toString() + "'");
	}

	/**
	 * Block and wait for the given string pattern to occur in the input stream
	 * buffer, or end of file on the input stream, or time out occurs. Return when
	 * the pattern is found, or throw an exception if not. If true is returned,
	 * the input buffer is thrown out (cleared) and later calls to expect will
	 * only be able so scan or see data received after the buffer was cleared. If
	 * the buffer contains multiple occurrences of the same pattern, only the
	 * first occurrence will be found and the buffer cleared
	 */
	public void expect(String simpleString, long timeout, boolean clearOnMatch)
	    throws Exception {
		Log.debug("looking for '" + simpleString + "' in buffer with timeout of "
		    + timeout);
		long timeToDie = System.currentTimeMillis() + timeout;

		boolean seenEofFlag = false;
		while (!seenEofFlag) {
			synchronized (lock) {
				if (eof) {
					/*
					 * if we have encountered an eof then no more will be added to the
					 * buffer, try to match one (more) time then give up
					 */
					seenEofFlag = true;
				}

				int index = cbuf.indexOf(simpleString);
				if (index != -1) {
					// Log.debug("ExpectableInputReader found a match of " + simpleString
					// + " in buffer " +
					// cbuf);
					if (clearOnMatch) {
						// found a match. clear everything in the buffer up to the match
						cbuf = new StringBuilder(cbuf.substring(index
						    + simpleString.length()));
						cbuf.ensureCapacity(1024);
						// Log.debug("ExpectableInputReader buffer after match " + cbuf);
					}
					return;
				}

				/*
				 * no match (yet) wait until the timeout occurs or we get waken up
				 * because more data was added to the buffer.
				 */
				long timeLeft = timeToDie - System.currentTimeMillis();
				if (timeLeft < 0) {
					throw new Exception("timeout waiting for input '" + simpleString
					    + "' from buffer " + cbuf);
				}

				lock.wait(timeLeft);
				// either new data was added or EOF/error occurred and EOF is true.
			}
		}

		throw new Exception("Encountered end of file while waiting for input '"
		    + simpleString + "'  buffer " + cbuf);
	}

	public boolean isEof() {
		return eof;
	}

	/**
	 * This thread will keep reading data from the given input stream and
	 * buffering it until EOF.
	 */
	@Override
	public void run() {
		try {
			Log.debug("ExpectableInputReader running");
			char b[] = new char[8 * 1024];
			int size;
			try {
				while (!eof) {
					size = br.read(b);
					if (size == -1) {
						Log.debug("ExpectableInputReader hit EOF");
						if (log != null) {
							log.debug("\n\n***** EOF ENCOUNTERED at " + new Date()
							    + " *****\n\n");
						}
						eof = true;
						synchronized (lock) {
							// no more data.
							lock.notifyAll();
						}
						break;
					}

					if (log != null) {

					}

					int len = 0;
					synchronized (lock) {
						cbuf.append(b, 0, size);
						len = cbuf.length();
						// new data added.
						if (br.ready()) {
							/*
							 * We have just read some data, acquired the lock (meaning no one
							 * is using it or they are wait()ing ) and our reader is telling
							 * us that more data remains to be read. Don't notify the waiting
							 * thread (if one exists) add the data to the buffer and go read
							 * some more, because the ready() method guarantees that their is
							 * more data waiting to be read so we'll spin around another time
							 * for sure.
							 */
							Log.debug("ExpectableInputReader more data reamains to be read, not notifiing readers yet.");
						}
						else {
							lock.notifyAll();
						}
					}

					Log.debug("ExpectableInputReader added " + size + " bytes to buffer "
					    + len + " bytes");
				}
			}
			catch (IOException e) {
				Log.error("Exception reading input stream, treating as EOF.", e);
				eof = true;
				synchronized (lock) {
					// no more data, treat as eof.
					lock.notifyAll();
				}
			}
		}
		catch (Exception t) {
			Log.error("ExpectableInputReader thread", t);
		}
	}
}