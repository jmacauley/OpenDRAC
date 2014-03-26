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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4J File appender that rolls the logs every night, compresses them and
 * keeps the last X days worth of logs. If the file exceeds a maximum size, it
 * is rolled and compressed right away, these extra rolled files do not count
 * towards extra days of logs. ie. you will really keep the last 14 days of
 * logs, no matter how often we rolled due to size The log4J
 * DailyRollingFileAppender could not be extended, and did not limit the number
 * of rolled files! The log4J RollingFileAppender understands that you would
 * want to limit the number of backups, but only rolls files based on a size
 * limit, not a date limit.
 * 
 * @author pitman
 */
public final class CompressedDailyRollingFileAppender extends FileAppender {
	static class Midnight extends TimerTask {
		private final CompressedDailyRollingFileAppender cdrfa;

		Midnight(CompressedDailyRollingFileAppender ap) {
			cdrfa = ap;
		}

		@Override
		public void run() {
			try {
				// if the file is open rollIt.
				LogLog
				    .debug("****************************************************************");
				LogLog
				    .debug("CompressedDailyRollingFileAppender: Midnight timer task fired for "
				        + cdrfa.getName());
				if (cdrfa.qw != null) {
					cdrfa.rollIfRequired();
					LogLog
					    .debug("CompressedDailyRollingFileAppender: Midnight timer task: rolled log file.");
				}
				else {
					LogLog
					    .debug("CompressedDailyRollingFileAppender: Midnight file closed.");
				}
			}
			catch (Exception t) {
				cdrfa.error("Midnight Roll over failed on " + cdrfa.getName(), t);
			}
		}
	}

	private static final int GZIP_BUFFER_SIZE = 2048;

	// Pass true to our timer so it is a daemon thread and won't
	// otherwise stop the JVM from terminating...
	private static final Timer MIDNIGHT_ROLL_TIMER = new Timer(
	    "CompressedDailyRollingFileAppender Midnight timer", true);

	// Roll files over every minute in test mode.
	private static final boolean TESTING_MODE = Boolean
	    .getBoolean("equinox.testLogs");

	// These can be set/get via methods below.
	private String baseFileName;
	private boolean beenActivated;
	private String datePattern = "'.'yyyy-MM-dd";
	private int maxBackups = 14;

	/*
	 * If the current log file grows larger than this max, roll and compress it
	 * right away, don't want to keep this puppy around. If the size is <=0 never
	 * compress based on size.
	 */
	private long maxFileSize = OptionConverter.toFileSize("200MB",
	    200 * 1024 * 1024);

	// Working variables.
	private long nextRoll = System.currentTimeMillis() - 1;
	private final Date now = new Date();
	private String rollDirectory = "archive";
	private String scheduledFilename;
	private SimpleDateFormat sdf;

	/**
	 * CompressedDailyRollingFileAppender
	 */
	public CompressedDailyRollingFileAppender() {
		LogLog.debug("CompressedDailyRollingFileAppender null ctr called");
	}


	/**
	 * CompressedDailyRollingFileAppender
	 */
	public CompressedDailyRollingFileAppender(Layout layout, String filename,
	    boolean append) throws IOException {
		super(layout, filename, append);

		LogLog.debug("CompressedDailyRollingFileAppender ctr-b called");
	}
	

	/**
	 * activateOptions:
	 */
	@Override
	public void activateOptions() {
		LogLog
		    .debug("CompressedDailyRollingFileAppender activateOptions start for "
		        + fileName);
		now.setTime(System.currentTimeMillis());
		if (TESTING_MODE) {
			// Roll every minute or 1K when in testing mode.
			datePattern = "'.'yyyy-MM-dd-HH-mm";
			maxFileSize = 1024;
		}
		sdf = new SimpleDateFormat(datePattern, Locale.getDefault());

		File f = new File(new File(fileName).getParent(), rollDirectory);
		if (!f.isDirectory()) {
			if (f.mkdirs()) {
				LogLog
				    .debug("CompressedDailyRollingFileAppender created roll directory "
				        + f);
			}
			else {
				LogLog
				    .debug("CompressedDailyRollingFileAppender unable to create roll directory "
				        + f);
			}
		}

		nextRoll = getNextRollTime(now);

		setMidnight();

		// If an old file exists in the last time period, roll
		// it over now before we start logging again on top of it.
		// Don't roll the file over if it is 0 bytes.
		File old = new File(baseFileName);
		if (old.isFile() && old.length() > 0) {
			Date last = new Date(old.lastModified());
			if (!sdf.format(last).equals(sdf.format(now))) {
				scheduledFilename = computeScheduledFileName(last);
				LogLog
				    .debug("CompressedDailyRollingFileAppender: Initial roll over to: "
				        + scheduledFilename);
				roll();
			}
		}

		super.activateOptions();

		// It just don't make sense to truncate log files every time
		// we start running if we want to roll the entire days logs.
		if (!super.getAppend()) {
			super.setAppend(true);
		}

		if (fileName != null && scheduledFilename == null) {
			scheduledFilename = computeScheduledFileName(now);
		}

		// Calling roll (above) will delete old logs, but we may
		// not have called it so try again.
		deleteOldLogs();

		beenActivated = true;
		LogLog
		    .debug("CompressedDailyRollingFileAppender activateOptions called nextRoll="
		        + nextRoll);
	}

	/**
	 * append: Top level method used by the appender to write a log. We override
	 * this method to (re)open the output file if required. Append calls subAppend
	 * to write the log for real.
	 */
	@Override
	public void append(LoggingEvent event) {
		/*
		 * We've deferred the file open until the first time we attempt to write a
		 * log. This way we don't open file names just because a logger was
		 * configured, but never used.
		 */
		if (!beenActivated) {
			error("Attempted to append a log before active was called.",
			    new Exception(""));
			return;
		}
		reopenFile();
		super.append(event);
	}

	/**
	 * getDatePattern
	 */
	public String getDatePattern() {
		LogLog.debug("CompressedDailyRollingFileAppender getDatePattern:");
		return datePattern;
	}

	/**
	 * getMaxBackups
	 */
	public String getMaxBackups() {
		LogLog.debug("CompressedDailyRollingFileAppender getMaxBackups");
		return Integer.toString(maxBackups);
	}

	/**
	 * @return Returns the maxFileSize.
	 */
	public String getMaxFileSize() {
		LogLog.debug("CompressedDailyRollingFileAppender getMaxFileSize");
		return Long.toString(maxFileSize);
	}

	/**
	 * getRollDirectory
	 */
	public String getRollDirectory() {
		LogLog.debug("CompressedDailyRollingFileAppender getRollDirectory called");
		return rollDirectory;
	}

	/**
	 * setDatePattern
	 */
	public void setDatePattern(String pattern) {
		LogLog
		    .debug("CompressedDailyRollingFileAppender setDatePattern:" + pattern);
		datePattern = pattern;
	}

	/**
	 * setFile
	 */
	@Override
	public void setFile(String file) {
		LogLog.debug("CompressedDailyRollingFileAppender setFile:" + file);
		this.setFile(file, true, false, 0);
	}

	/**
	 * setFile
	 * <p>
	 * Synchronized because the FileAppender.class setFile is synchronized. Just
	 * to be safe.
	 */
	@Override
	public synchronized void setFile(String fileName, boolean append,
	    boolean bufferedIO, int bufferSize) {
		LogLog.debug("CompressedDailyRollingFileAppender setFile2 file=:"
		    + fileName + " append=" + append + " bufferedIo=" + bufferedIO
		    + " bufferSize=" + bufferSize);

		// It does not make sense to have immediate flush and bufferedIO.
		if (bufferedIO) {
			LogLog
			    .debug("Using buffered IO! Not a good idea if we crash, last logs are lost");
			setImmediateFlush(false);
		}
		else {
			setImmediateFlush(true);
		}

		reset();
		// Writer fw = createWriter(new FileOutputStream(fileName, append));
		// if (bufferedIO)
		// {
		// fw = new BufferedWriter(fw, bufferSize);
		// }
		// this.setQWForFiles(fw);
		// writeHeader();
		this.fileName = fileName.trim();
		this.baseFileName = fileName.trim();
		this.fileAppend = append;
		this.bufferedIO = bufferedIO;
		this.bufferSize = bufferSize;
	}

	/**
	 * setMaxBackups
	 */
	public void setMaxBackups(String backups) {
		LogLog.debug("CompressedDailyRollingFileAppender setMaxBackups:" + backups);
		maxBackups = Integer.parseInt(backups.trim());
	}

	/**
	 * setMaxFileSize: zero or less disables size based rolling.
	 */
	public void setMaxFileSize(String value) {
		LogLog.debug("CompressedDailyRollingFileAppender setMaxFileSize:" + value);
		maxFileSize = OptionConverter.toFileSize(value, 200 * 1024 * 1024);
	}

	/**
	 * setRollDirectory: Sub-directory to roll logs into. Must be under the
	 * current log directory.
	 */
	public void setRollDirectory(String dir) {
		LogLog.debug("CompressedDailyRollingFileAppender setRollDirectory " + dir
		    + " called");
		rollDirectory = dir;
	}

	/**
	 * error: Record an error via the debug logs and via the errorHandler. The
	 * errorHandler will only print the first error encountered to STDERR and all
	 * others will be ignored. This should be enough however, to let us know
	 * something went wrong.
	 */
	protected void error(String msg, Throwable t) {
		LogLog.error("CompressedDailyRollingFileAppender: ERROR: " + msg
		    + " fileName=" + fileName, t);
		/*
		 * Silly class only accepts Exception, put our Throwable into an exception.
		 */
		errorHandler.error("CompressedDailyRollingFileAppender: " + msg
		    + " fileName=" + fileName,
		    new Exception("Cause contains throwable", t), 0);
	}

	/**
	 * reset: Mark the writer null so we'll reopen it next time we log.
	 */
	@Override
	protected void reset() {
		LogLog.debug("CompressedDailyRollingFileAppender reset called " + fileName);
		// Calling reset resets the fileName variable, which will cause us problems
		// later.... so we reload it after calling super.reset()...
		super.reset();
		fileName = baseFileName;
		qw = null;
	}

	/**
	 * rollIfRequired: roll the log file if required. Called once for every log
	 * written, and via the midnight timer to roll at midnight.
	 * <p>
	 * Synchronized as this can be called on the timer thread at midnight as well
	 * as from subappend on another thread!
	 */
	protected synchronized void rollIfRequired() {
		long m = System.currentTimeMillis();
		if (m >= nextRoll) {
			LogLog
			    .debug("CompressedDailyRollingFileAppender rollIfRequired Roll Required!!! "
			        + fileName);
			now.setTime(m);
			nextRoll = getNextRollTime(now);
			roll();
		}
		else if (qw != null && maxFileSize > 0
		    && ((CountingQuietWriter) qw).getCount() >= maxFileSize) {
			// File based rolling is enabled and we have gotten too big. Roll and
			// compress the file.
			rollOnSize();
		}
	}

	@Override
	protected void setQWForFiles(Writer writer) {
		this.qw = new CountingQuietWriter(writer, errorHandler);
		if (fileAppend) {
			((CountingQuietWriter) qw).setCount(new File(fileName).length());
		}
	}

	/**
	 * subAppend: Here we do some work! Logs are generated and passed to us here.
	 * We want this to be efficient code as it will get called lots.
	 */
	@Override
	protected synchronized void subAppend(LoggingEvent event) {
		rollIfRequired();
		super.subAppend(event);
	}

	/**
	 * computeScheduledFileName: If the file name is /somepath/somefile.log then
	 * we want to roll it over to a file name that looks like
	 * /somepath/<rollDirectory>/somefile.<date>.log.gz so we insert the date
	 * before the last period in the string, or at the end of the file name, if no
	 * period is found. If rollDirectory is null, the current location is used to
	 * roll the file into. The ".gz" is added when the file is compressed (not
	 * here)
	 */
	private String computeScheduledFileName(Date currentTime) {
		LogLog
		    .debug("CompressedDailyRollingFileAppender computeScheduledFileName called "
		        + fileName);

		String p = new File(fileName).getParent();
		String f = new File(fileName).getName();

		try {
			if (rollDirectory != null) {
				p = new File(p, rollDirectory).getCanonicalPath();
			}

			int pos;
			pos = f.lastIndexOf('.');
			if (pos < 0) {
				return new File(p, f + sdf.format(currentTime)).getCanonicalPath();
			}

			return new File(p, f.substring(0, pos) + sdf.format(currentTime) + '.'
			    + f.substring(pos + 1, f.length())).getCanonicalPath();
		}
		catch (Exception e) {
			// Oh oh. Now what?
			error("Constructing new file name", e);
			return null;
		}
	}

	/**
	 * computeSizedRollFileName: If the file name is /somepath/somefile.log then
	 * we want to roll it over to a file name that looks like
	 * /somepath/<rollDirectory>/somefile.<date>.<sizePos>.log.gz so we insert the
	 * date before the last period in the string, or at the end of the file name,
	 * if no period is found. We then search for the next unused size index and
	 * append that. If rollDirectory is null, the current location is used to roll
	 * the file into. The ".gz" is added when the file is compressed (not here)
	 */
	private String computeSizedRollFileName(Date currentTime) {
		LogLog
		    .debug("CompressedDailyRollingFileAppender computeSizedRollFileName called "
		        + fileName);

		String p = new File(fileName).getParent();
		String f = new File(fileName).getName();
		String possibleFileName;
		String off = "";
		int sizeOffset = 0;

		try {
			if (rollDirectory != null) {
				p = new File(p, rollDirectory).getCanonicalPath();
			}

			int pos = f.lastIndexOf('.');

			// Keep making up file names until we find the next one that does not
			// exist.
			while (true) {
				if (pos < 0) {
					possibleFileName = new File(p, f + sdf.format(currentTime))
					    .getCanonicalPath() + off;
				}
				else {
					possibleFileName = new File(p, f.substring(0, pos)
					    + sdf.format(currentTime) + off + '.'
					    + f.substring(pos + 1, f.length())).getCanonicalPath();
				}

				// Add the .gz on before checking if it exists, as we are looking for
				// the compressed file.
				if (new File(possibleFileName + ".gz").exists()) {
					sizeOffset++;
					off = "." + (sizeOffset < 10 ? "0" + sizeOffset : "" + sizeOffset);
				}
				else {
					return possibleFileName;
				}
			}
		}
		catch (Exception e) {
			// Oh oh. Now what?
			error("Constructing new file name", e);
			return null;
		}
	}

	/**
	 * deleteOldLogs: Make sure we don't have more than maxBackup compressed log
	 * files sitting around. The backup files should be sitting in the
	 * rollDirectory... Given a base file name /somepath/somefile.log look in
	 * /somepath/<rollDirectory/ for files matching somefile(.*).log.gz
	 */
	private void deleteOldLogs() {
		LogLog.debug("CompressedDailyRollingFileAppender deleteOldLogs called "
		    + fileName);
		File f = new File(baseFileName);
		String simpleFileName = f.getName();
		simpleFileName = simpleFileName.substring(0,
		    simpleFileName.lastIndexOf('.'));
		f = f.getParentFile();
		f = new File(f, rollDirectory);

		LogLog.debug("CompressedDailyRollingFileAppender: deleteOldLogs "
		    + baseFileName + ":  Searching for existing files in: " + f
		    + " using simpleName=" + simpleFileName);
		String[] files = f.list();

		if (files != null) {
			Date d;

			/*
			 * Match simple rolled files as well as when we have multiple rolls per
			 * day because the file size limit was exceeded ie
			 * ./somefile<date>.<index>.log.gz . This means we can have multiple files
			 * per day so build a map of lists. With the map ordered by date and the
			 * list containing all files for that date.
			 */
			Pattern p = Pattern.compile("^" + simpleFileName
			    + "(\\.[^.]*)(\\.{0,1}[^.]*)\\.log\\.gz$");
			Map<Date, List<File>> tm = new TreeMap<Date, List<File>>();

			for (String element : files) {
				// LogLog.debug("CompressedDailyRollingFileAppender: Examinging file " +
				// files[i] +
				// " Against " + p.pattern());
				Matcher m = p.matcher(element);
				if (m.matches()) {
					// LogLog.debug("CompressedDailyRollingFileAppender: " + files[i] +
					// " MATCHES !!!!!!!!! " + m.group(1));
					d = sdf.parse(m.group(1), new ParsePosition(0));
					if (d != null) {
						/*
						 * We parsed it, so we assume we've got a valid file name, record
						 * and sort it so we can later discard the oldest unwanted entries.
						 */
						List<File> l = tm.get(d);
						if (l == null) {
							l = new ArrayList<File>();
							l.add(new File(f, element));
							tm.put(d, l);
						}
						else {
							l.add(new File(f, element));
						}
					}
					else {
						LogLog
						    .debug("CompressedDailyRollingFileAppender: deleteOldLogs: Failed to parse date from filename "
						        + element + " date portion=<" + m.group(1) + ">");
					}
				}
			}

			/*
			 * We should now have a tree map containing all the entries we found in
			 * the current directory sorted by date. If we have more than maxBackup
			 * entries we just need to loop off the extras. The oldest entries will be
			 * at the beginning of the list, so we just need to persevere the last
			 * maxBackup entries and delete anything that remains.
			 */
			LogLog
			    .debug("CompressedDailyRollingFileAppender: deleteOldLogs: Tree map of old files ="
			        + tm.toString());

			@SuppressWarnings("unchecked")
      List<File>[] foo = tm.values().toArray(new ArrayList[0]);
			LogLog.debug("CompressedDailyRollingFileAppender: deleteOldLogs: Found "
			    + foo.length + " days of logs.");
			if (foo.length > maxBackups) {
				// Got some to delete. Ignore the last maxBackup entries in the array,
				// then delete anything
				// left.
				for (int i = 0; i < foo.length - maxBackups; i++) {
					LogLog
					    .debug("CompressedDailyRollingFileAppender: deleteOldLogs: deleting old files <"
					        + foo[i] + ">");
					Iterator<File> it = foo[i].iterator();
					File fileToDelete;
					while (it.hasNext()) {
						fileToDelete = it.next();
						if (!fileToDelete.delete()) {
							error("deleteOldLogs: Unable to delete old file <" + fileToDelete
							    + ">", new Exception());
						}
					}
				}
			}
		}
	}

	/**
	 * getNextRollTime: Return the time the next file roll over should occur at
	 * (ie midnight)
	 */
	private long getNextRollTime(Date currentTime) {
		LogLog.debug("CompressedDailyRollingFileAppender getRollTime called for "
		    + fileName);
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(currentTime);
		if (TESTING_MODE) {
			// Roll every minute!
			gc.set(Calendar.SECOND, 0);
			gc.set(Calendar.MILLISECOND, 0);
			gc.add(Calendar.MINUTE, 1);
		}
		else {
			// Roll once a day at midnight.
			gc.set(Calendar.HOUR_OF_DAY, 0);
			gc.set(Calendar.MINUTE, 0);
			gc.set(Calendar.SECOND, 0);
			gc.set(Calendar.MILLISECOND, 0);
			gc.add(Calendar.DATE, 1);
		}
		return gc.getTime().getTime();
	}

	/**
	 * Given an existing file "from", create a new file to".gz" that is a
	 * compressed version of the from from file. The from file is deleted when
	 * complete.
	 */
	private void gzipCompressFile(String from, String to) throws Exception {
		// If the target file exits, delete (then try to create/overwrite)
		File target = new File(to + ".gz");
		if (target.exists()) {
			/*
			 * overwrite. Since our target file name includes the date and size offset
			 * it should never exist, or someone has been playing with us.
			 */
			target.delete();
		}

		/*
		 * Rename the from file to this directory with the name to before we
		 * compress it to to.gz.
		 */

		new File(from).renameTo(new File(to));
		File file = new File(to);

		// If the from file is zero bytes, don't roll, just delete it
		if (file.length() == 0) {
			LogLog
			    .debug("CompressedDailyRollingFileAppender gzipCompressFile file is zero bytes, not compressing");
			file.delete();
			return;
		}
		MyGZIPOutputStream gz = null;
		BufferedInputStream in = null;

		try {
			gz = new MyGZIPOutputStream(new FileOutputStream(target),
			    GZIP_BUFFER_SIZE);
			in = new BufferedInputStream(new FileInputStream(file), GZIP_BUFFER_SIZE);
			byte[] buffer = new byte[GZIP_BUFFER_SIZE];
			int howMany = 0;
			while (true) {
				howMany = in.read(buffer, 0, buffer.length);
				if (howMany == -1) {
					break;
				}
				gz.write(buffer, 0, howMany);
			}
		}
		finally {
			if (gz != null) {
				gz.close();
			}
			if (in != null) {
				in.close();
			}

			file.delete();
		}
	}

	/**
	 * We want to open the log file only when we use it or after we've rolled it.
	 */
	private void reopenFile() {
		if (qw == null) {
			LogLog.debug("CompressedDailyRollingFileAppender: lazy open of file "
			    + fileName);
			try {
				Writer fw = createWriter(new FileOutputStream(fileName, fileAppend));
				if (bufferedIO) {
					fw = new BufferedWriter(fw, bufferSize);
				}
				this.setQWForFiles(fw);
			}
			catch (IOException ioe) {
				error("lazy open of file " + fileName, ioe);
			}
		}
	}

	/**
	 * Roll the files over storing the file with a date extension and compress it.
	 * Then eliminate any extra files beyond our limit of maxBackups.
	 */
	private void roll() {
		LogLog.debug("CompressedDailyRollingFileAppender roll " + fileName);

		// only roll over if the new file is different
		String x = computeScheduledFileName(now);
		if (scheduledFilename.equals(x)) {
			error("Roll method invoked, but not required", null);
			return;
		}

		this.closeFile();

		rollAndCompressFile(fileName, scheduledFilename);
		deleteOldLogs();

		// new scheduled name
		scheduledFilename = computeScheduledFileName(now);
		/*
		 * This will also close the file. This is OK since multiple close operations
		 * are safe. Note we always set append to true. If append isn't true, then
		 * you won't roll everything once a day, only the last days output...
		 */
		this.setFile(baseFileName, true, false, 0);

		// Open the file again after a roll.
		reopenFile();
	}

	/**
	 * rollFile: Rename a to b safely then compress it.
	 */
	private void rollAndCompressFile(String from, String to) {
		LogLog.debug("CompressedDailyRollingFileAppender rollAndCompressFile from:"
		    + from + " to:" + to);

		try {
			gzipCompressFile(from, to);
		}
		catch (Exception e) {
			error("Failed to compress file <" + from + "> to <" + to
			    + "> during rollover", e);
		}
	}

	/**
	 * Roll the file cause it got too big. This is different than a time based
	 * roll as the target file names will differ
	 */
	private void rollOnSize() {
		LogLog.debug("CompressedDailyRollingFileAppender rollOnSize " + fileName
		    + " size=" + ((CountingQuietWriter) qw).getCount() + " maxsize="
		    + maxFileSize);

		// Close file.
		this.closeFile();

		// Compress it
		String sizeRollFilename = computeSizedRollFileName(now);
		LogLog
		    .debug("CompressedDailyRollingFileAppender rollOnSize compressing to "
		        + sizeRollFilename);

		rollAndCompressFile(fileName, sizeRollFilename);

		/*
		 * This will also close the file. This is OK since multiple close operations
		 * are safe. Note we always set append to true. If append isn't true, then
		 * you won't roll everything once a day, only the last days output...
		 */
		this.setFile(baseFileName, true, false, 0);

		// Open the file again after a roll.
		reopenFile();
	}

	private void setMidnight() {
		/*
		 * Create a timer thread that fires each night at midnight (or every minute
		 * if in test mode)...
		 */
		long freq = 1000 * 60 * 60 * 24;
		if (TESTING_MODE) {
			freq = 1000 * 60;
		}
		long start = getNextRollTime(new Date());

		MIDNIGHT_ROLL_TIMER.scheduleAtFixedRate(new Midnight(this),
		    new Date(start), freq);
		LogLog.debug("CompressedDailyRollingFileAppender: setMidnight " + fileName
		    + " now=" + new Date().toString() + " start="
		    + new Date(start).toString() + " freq=" + freq);
	}
}

/**
 * MyGZIPOutputStream: Extends the GZIPOutputStream and sets the compression
 * level to Deflater.BEST_COMPRESSION for better compression
 * 
 * @author pitman
 */
class MyGZIPOutputStream extends GZIPOutputStream {
	MyGZIPOutputStream(OutputStream out, int size) throws IOException {
		super(out, size);
		def.setLevel(Deflater.BEST_COMPRESSION);
	}
}
