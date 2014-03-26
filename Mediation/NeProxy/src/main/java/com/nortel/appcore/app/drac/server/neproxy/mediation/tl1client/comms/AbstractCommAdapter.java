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

package com.nortel.appcore.app.drac.server.neproxy.mediation.tl1client.comms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CommAdapter is a common interface for communication primitives (eg. sockets,
 * serial ports, modems). It is the Target participant of an Adapter/ Wrapper
 * pattern (Design Patterns, Gamma et al., p.139). Adaptees (eg. SocketAdapter,
 * a concrete subclass) support the read behaviour implemented in CommAdapter.
 * CommAdapter Clients include Gateway and TL1Engine. </p> Adaptees also
 * encapsulate the data required to create their corresponding primitive. They
 * provide copy methods, to support applying preferences to default gateway
 * adapters. </p> CommAdapter supports a list of data (String or byte)
 * listeners. Data listeners may register with an adapter via the add and remove
 * methods; registered listeners will be notified of any data read. </p>
 * CommAdapter also supports a single ConnectionDropListener. This may be set
 * with the corresponding set method; the specified listener will be notified if
 * the connection is dropped. Currently, only a single drop listener is
 * supported; this design allows a single, central cleanup path and avoids
 * connection drop error message 'race conditions'. </p>
 * 
 * @see CommAdapterListener
 * @see SocketAdapter
 */
public abstract class AbstractCommAdapter implements Runnable {
  
  private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * Default read and write buffer size.
	 */
	protected static final int DEFAULT_BUFFER_SIZE = 8192; // 1024;
	/**
	 * State var to turn logging of commAdapter writes to port
	 */
	// public static final boolean logCommAdapter=false;
	/**
	 * A flag indicating whether or not a manual connection window should be
	 * opened after the adapter is connected. Currently used only by the modem
	 * adapter gui.
	 */
	public boolean needsManualConnection;

	/**
	 * The listener notified if the adapter detects its connection has dropped.
	 * Refer to the class comment for more details.
	 */
	private ConnectionDropListener dropListener;

	/**
	 * A flag indicating whether or not the receiver is still running/open.
	 */
	protected boolean isOpen;

	/**
	 * The buffer used to read available data.
	 */
	protected byte[] buffer;

	/**
	 * The amount of buffer the adapter should allocate for its buffered input
	 * read stream.
	 */
	protected int readBufferSize = 65536; // DEFAULT_BUFFER_SIZE;

	/**
	 * The number of characters the adapter should attempt to read on each read
	 * operation.
	 */
	protected int readSize = DEFAULT_BUFFER_SIZE;

	/**
	 * The priority of the adapter's read thread. This was originally set to
	 * Thread.NORM_PRIORITY + 1, but it turns out we get better throughput under
	 * heavily loaded conditions when left alone. This is probably because we read
	 * data in bigger chunks from the pipe.
	 */
	private int readThreadPriority = Thread.NORM_PRIORITY;

	/**
	 * The amount of buffer the adapter should allocate for its buffered output
	 * read stream.
	 */
	protected int writeBufferSize = DEFAULT_BUFFER_SIZE;

	/**
	 * The number of milliseconds to delay after writing a charater.
	 */
	// private int writeDelay;
	/**
	 * The adapter input stream.
	 */
	protected BufferedInputStream is;

	/**
	 * The adapter output stream.
	 */
	protected BufferedOutputStream os;

	/**
	 * A wrapper for a primitive data output stream.
	 */
	protected AbstractCommAdapterWriter writer;

	/**
	 * The read thread.
	 */
	private Thread readThread;

	/**
	 * Storage for the receiver's string data listeners - instances of
	 * CommAdapterListener.
	 */
	private final List<CommAdapterListener> listeners = new ArrayList<CommAdapterListener>(
	    3);

	/**
	 * Storage for the receiver's byte data listeners - instances of
	 * CommAdapterByteListener.
	 */
	// private final Vector<CommAdapterByteListener> byteListeners = new
	// Vector<CommAdapterByteListener>(3);
	/**
	 * constants used by threadState. RUNNING: either the Thread is currently
	 * running and reading data, or there is no Thread and readThread == null
	 * <p>
	 * ON_HOLD: The infinite loop of the read Thread is blocked in a wait()
	 * statement It will be woken up when the ZModem session is terminated
	 * PENDING_DEATH: The Thread has been instructed to terminate and is about to
	 * do so
	 */
	private static final int RUNNING = 0;
	private static final int ON_HOLD = 1;
	private static final int PENDING_DEATH = 2;

	/** internal variable used to run, hold, or stop the CommAdapter Thread */
	private int threadState = RUNNING;

	/** backdoor to allow more messages to stderr during debug session */
	public static final boolean commDebug = false;

	/**
	 * Create and return a new CommAdapter with default settings. The defaults
	 * should be acceptable for most circumstances. The read buffer size, read
	 * size, read delay and write delay may each be changed through their
	 * respective set method.
	 */
	protected AbstractCommAdapter() {
		// note that the writer 'bridge object' is always created
		setWriteDelay(0);
		buffer = new byte[readSize];
	}

	// /**
	// * This method is used to add a CommAdapater byte listener.
	// */
	// public synchronized void addCommAdapterByteListener(CommAdapterByteListener
	// listener)
	// {
	// byteListeners.addElement(listener);
	// }

	/**
	 * This method is used to add a CommAdapater string listener.
	 */
	public synchronized void addCommAdapterListener(CommAdapterListener listener) {
		listeners.add(listener);
	}

	/**
	 * Close the adapter's read and write streams. Concrete subclases handle any
	 * extra close behaviour they require.
	 */
	public abstract void close();

	/**
	 * Open a read and write stream to a network. Concrete subclasses provide any
	 * extra required behaviour in their open() method.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public synchronized void connect() throws CommAdapterException {

		if (!isOpen) {
			open();
			writer.connect(os);

			isOpen = true;
			startRead();
		}
	}

	/**
	 * Open a read and write stream to a network. Concrete subclasses provide any
	 * extra required behaviour in their open() method.
	 */
	public synchronized void connect(long lTimeoutMS) throws CommAdapterException {

		if (!isOpen) {
			open();
			writer.connect(os);

			isOpen = true;
			startRead();
		}
	}

	/**
	 * Close the adapter's read and write streams. Concrete subclasses implement
	 * any extra required behaviour in their close() method. </p> WARNING: Use
	 * caution when calling this method. It does not fire a connection dropped
	 * warning - it should only really be used by the same central object which
	 * manages the connection and connection drop processes.
	 */
	public synchronized void disconnect() {

		if (isOpen) {
			isOpen = false;
			stopRead();
			close();
		}
	}

	/**
	 * get rid of this adapter
	 */
	public synchronized void dispose() {
		stopRead();
		isOpen = false;
		dropListener = null;
		listeners.clear();
		// byteListeners.clear();
	}

	/**
	 * Return an input stream. Concrete subclasses wrap a specific communication
	 * primitive, which should have an input stream; subclasses should implement
	 * this method to create/return a reference to that stream.
	 */
	public BufferedInputStream getInputStream() {

		return is;
	}

	/**
	 * This method gets the member variable which is responsible for indicating
	 * that a manual connection session is either needed or not.
	 */
	public boolean getManualConnection() {
		return needsManualConnection;
	}

	/**
	 * Return an output stream. Concrete subclasses wrap a specific communication
	 * primitive, which should have an output stream; subclasses should implement
	 * this method to create/return a reference to that stream.
	 */
	public BufferedOutputStream getOutputStream() {

		return os;
	}

	/**
	 * Return true if the adapter's read and write streams are open and working,
	 * return false otherwise.
	 */
	public boolean isConnected() {

		return isOpen;
	}

	/**
	 * Notify each registered CommAdapterListener of the received data strings and
	 * notifies each registered CommAdapterByteListener of the received data
	 * bytes.
	 */
	public void notifyByteListeners2(byte[] data, int available) {

	}

	/**
	 * This method is used to remove a CommAdapater byte listener.
	 */
	// public synchronized void
	// removeCommAdapterByteListener(CommAdapterByteListener listener) {
	// public void removeCommAdapterByteListener(CommAdapterByteListener listener)
	// {
	// byteListeners.removeElement(listener);
	// }
	/**
	 * This method is used to remove a CommAdapater string listener.
	 */
	// public synchronized void removeCommAdapterListener(CommAdapterListener
	// listener) {
	public synchronized void removeCommAdapterListener(
	    CommAdapterListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method is used to temporarily unfreeze the main loop of the reading
	 * thread that was previously frozen by holdRead(). This method behaves nicely
	 * if it is called on a Thread that s not frozen. Useful when TL! most be
	 * stopped during ZModem transfer
	 */
	public synchronized void resumeRead() {
		if (commDebug) {
			
		}

		// prevent the case were were run() has not yet been called
		if (readThread == null) {
			return;
		}

		// resume if we are on hold only if we are already running (extra call
		// to resumeRead) then we have nothing to do if we are PENDING_DEATH,
		// then we should not resume anyway
		if (threadState != ON_HOLD) {
			return;
		}

		threadState = RUNNING;
		notify(); // wake up the wait() call in run()
	}

	/**
	 * After connection, repeatedly read from the adapter's input stream. Since
	 * read is a blocking call, this is not really polling. Notify all data
	 * listeners of all received data.
	 * <P>
	 * Dropped connection detection and notification is the responsibility of
	 * concrete subclasses.
	 * <P>
	 * The original version used the method stopRead which calls Thread.stop().
	 * That is a brutal way of stopping and is now depreciated in Java 2. It
	 * appears to produce a 'IOException: Broken Pipe' in the PipedReader used by
	 * the TL1 Engine to listen on the CommAdapter
	 * <P>
	 * stopRead was modified to no longer calls stop(), but send a interrupt()
	 * instead That breaks the infinite loop, null the Thread, and exits run().
	 * <P>
	 * To go back and forth between ZModem and TL1 modes this Thread used to be
	 * killed and replace by a new one. That breaks the Piped Reader and Writer
	 * used by the TL1 engine. This method was therefore modified to keep the
	 * original Thread alive for all the gateway session, and code was added to
	 * hold and resume it instead. We do this by setting (from another Thread) the
	 * threadState then we send an interrupt to wake up this Thread. The Thread
	 * can be permanently stopped by setting the threadState to PENDING_DEATH.
	 */
	@Override
	public void run() {
		threadState = RUNNING;

		int available = 0;

		do {
			/*
			 * the holdRead/stopRead interrupt exception should occur while in this
			 * block
			 */
			try

			{
				if (available > 0) {
					fireDataReceived(buffer, available);
				}

				if (isOpen) {
					available = read(buffer);
				}
			}
			finally {
				// with exception or not, we should check our flag everytime
				synchronized (this) {
					while (threadState == ON_HOLD) {
						if (commDebug) {
							
						}
						try {
							wait(); // wait until the CommThread is resumed
						}
						catch (InterruptedException ex) {
							/*
							 * if we get interrupted (possibly by a second call to holdRead()
							 * then just check if the hold still is still on
							 */
						}
					}
				}
			}

		}
		while (isOpen && available != -1 && threadState != PENDING_DEATH);

		

		/*
		 * we are typically here because stopRead() has interrupted us This is a our
		 * clue to die, NICELY stopRead() has already set readThread to null, we set
		 * it again here to cover the cases were we stopped for other reasons e.g.
		 * disconnection
		 */
		synchronized (this) {
			/*
			 * keep synchronized to make sure we do not remove it while some other
			 * synchronized block is executing
			 */
			readThread = null;
		}
	}

	/**
	 * Send a break sequence to the server.
	 * 
	 * @throws IOException
	 */
	// public abstract void sendBreak()
	// throws CommAdapterException, IOException;
	/**
	 * Set the write adapter's listener; the listener will be notified when data
	 * is written.
	 */
	public void setCommAdapterWriterListener(CommAdapterWriterListener listener) {

		if (writer != null) {
			writer.setCommAdapterWriterListener(listener);
		}
	}

	/**
	 * Set the adapter's connection drop listener; the listener will be notified
	 * if the connection drops.
	 */
	public void setConnectionDropListener(ConnectionDropListener listener) {

		dropListener = listener;
	}

	/**
	 * This method sets the member variable which is responsible for indicating
	 * that a manual connection session is either needed or not.
	 */
	public void setManualConnection(boolean bNeedsManualConnection) {
		needsManualConnection = bNeedsManualConnection;
	}

	/**
	 * The amount of buffer the adapter should allocate for its read stream. If
	 * the adapter is already open, the operation is ignored.
	 */
	public void setReadBufferSize(int bytes) {

		if (!isOpen) {
			readBufferSize = bytes;
		}
	}

	/**
	 * Set the number of bytes the adapter will read during each read operation.
	 * If the adapter is already open, the operation is ignored.
	 */
	public void setReadSize(int bytes) {

		if (!isOpen) {
			readSize = bytes;
		}
	}

	/**
	 * Set the priority of the adapter's read thread. Permissible values are
	 * between Thread.MIN_PRIORITY and Thread.MAX_PRIORITY. If the adapter is
	 * already open or the specified priority is out of the valid range, the
	 * operation is ignored.
	 */
	// public void setReadThreadPriority(int priority)
	// {
	//
	// if (!isOpen && priority >= Thread.MIN_PRIORITY && priority <=
	// Thread.MAX_PRIORITY)
	// {
	// readThreadPriority = priority;
	// if (readThread != null)
	// {
	// readThread.setPriority(priority);
	// }
	// }
	// }
	/**
	 * Set the millisecond delay a comm adapter waits between writing characters.
	 * If milliseconds is not a nonzero, positive int, the adapter will write
	 * commands as fast as possible (using a buffered stream). If the adapter is
	 * already open, the operation is ignored.
	 */
	public final void setWriteDelay(int milliseconds) {

		// writeDelay = milliseconds;

		if (!isOpen) {
			if (milliseconds > 0) {
				// writer = new CommAdapterDelayedWriter(milliseconds);
				throw new RuntimeException("Sorry, non-zerod delay not supported");
			}

			writer = new CommAdapterDefaultWriter();

		}
	}

	/**
	 * This method is used to start the reading thread It should be caled only
	 * once per gateway session It is expected to be called by the connect() mth
	 * of the product Gateway WARNING: This mth should no longer be used to
	 * stop/start the CommAdapter when switching TL1<-->ZModem. This breaks the
	 * pipe used to communicate with the engine. One should use holdRead and
	 * resumeRead instead.
	 */
	public synchronized void startRead() {
		if (commDebug) {
			
		}

		if (readThread != null) {
			return; // already started
		}

		readThread = new Thread(this, "TL1 Engine : CommAdapter Read Thread "
		    + toString());
		readThread.setPriority(readThreadPriority);
		readThread.setDaemon(true);
		readThread.start();
	}

	/**
	 * Return a string representing a summary of the adapter's connection
	 * configuration. For example, a socket adapter would return its host string.
	 */
	public abstract String summaryString();

	/**
	 * toggle the echo of some products
	 * 
	 * @throws IOException
	 */
	// public void toggleEcho()
	// throws IOException
	// {
	//
	// // vt320.echoOffInProgress();
	// char c = '\016'; // the ^N character
	// String s = new Character(c).toString();
	// String t = s + s + s + s + s + s + s + s + s + s;
	// write(t);
	// }
	/**
	 * Write the specified byte array to the receiver's underlying output stream.
	 * This method is synchronized because we want to make sure that two threads
	 * do not write at the same time, causing the commands to interleave.
	 * 
	 * @param command
	 *          String data to write to the CommAdapter
	 */
	// public void write(byte[] data)
	// throws IOException
	// {
	// write(new String(data));
	// }
	/**
	 * Write the specified string to the receiver's Writer. This method is
	 * synchronized because we want to make sure that two threads do not write at
	 * the same time, causing the commands to interleave.
	 * 
	 * @param command
	 *          String data to write to the CommAdapter
	 */
	public abstract void write(String command) throws IOException;

	// /**
	// * Copy the adapter's configuration information into the specified comm
	// adapter. Do NOT copy derived
	// * objects (ie. open sockets).
	// */
	// protected void copy(CommAdapter adapter)
	// {
	//
	// adapter.setReadBufferSize(readBufferSize);
	// adapter.setReadSize(readSize);
	// adapter.setReadThreadPriority(readThreadPriority);
	// adapter.setWriteDelay(writeDelay);
	// }

	/**
	 * Notify the adapter's ConnectionDropListener that the connection was
	 * dropped. Note that this method does not fire if the adapter was
	 * disconnected or was already closed.
	 */
	protected synchronized void fireConnectionDropped(
	    CommAdapterException exception) {

		// notify listeners only if we are open, ie. the first detection
		if (isOpen && dropListener != null) {
			dropListener.connectionDropped();
		}

		// always cleanup, just in case
		disconnect();
	}

	// public static void toggleCommAdapterWrite() {
	// logCommAdapter = ! logCommAdapter;
	// }

	/**
	 * Notify each registered CommAdapterListener of the received data strings and
	 * notifies each registered CommAdapterByteListener of the received data
	 * bytes.
	 */
	protected synchronized void fireDataReceived(byte[] data, int available) {
		String dataString = new String(data, 0, available);
		for (CommAdapterListener c : listeners) {
			c.received(dataString);
		}
	}

	/**
	 * Open the receiver's communication primitive (eg. socket). Concrete
	 * subclasses should catch any primitive exceptions and throw a
	 * ConnectionFailed exception with a detailed localized message. The method is
	 * also responsible for creating the basic input and output streams for the
	 * adapter.
	 */
	protected abstract void open() throws CommAdapterException;

	/**
	 * Read data from the adapter's input stream. Block until the data is
	 * available. If concrete implementations encounter an exception, it is their
	 * responsibility to fire a connection dropped notification.
	 */
	protected abstract int read(byte[] buffer);

	/**
	 * This method is used to stop the reading thread
	 * <P>
	 * This mth used to call Tread.stop() which kills the Thread right away and
	 * leaks many things. stop is deprecated in Java 2.
	 * <P>
	 */
	// protected synchronized void stopRead() {
	protected void stopRead() {
		if (commDebug) {
			
		}

		if (readThread == null) {
			return; // safe guard
		}

		readThread = null;
		threadState = PENDING_DEATH;
	}

}
