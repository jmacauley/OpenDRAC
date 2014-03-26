package org.opendrac.security;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum InternalLoginHelper {
	INSTANCE;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory
	    .getLogger(InternalLoginHelper.class);

	public enum InternalLoginTokenType {
		LPCP(new File(System.getProperty("java.io.tmpdir")
		    + "/lpcp.token".replace("/", File.separator))), NEPROXY(new File(
		    System.getProperty("java.io.tmpdir")
		        + "/neproxy.token".replace("/", File.separator)));
		final File token;

		InternalLoginTokenType(final File token) {
			this.token = token;
			token.deleteOnExit();
		}
	}

	public InternalLoginToken getToken(final InternalLoginTokenType tokenType)
	    throws IOException {
		FileChannel readChannel = null;
		InternalLoginToken token = null;
		RandomAccessFile randomAccessFile = null;
		try {
		  randomAccessFile = new RandomAccessFile(tokenType.token, "r");
			readChannel = randomAccessFile.getChannel();
			// see javadoc CharBuffer#toString for LENGTH - 1 explanation
			final ByteBuffer readBuffer = readChannel.map(
			    FileChannel.MapMode.READ_ONLY, 0, InternalLoginToken.LENGTH - 1);
			token = new InternalLoginToken(readBuffer.asCharBuffer().toString());
		}
		finally {
			if (readChannel != null) {
				readChannel.close();
			}
			if(randomAccessFile !=null){
        randomAccessFile.close();
      }
		}
		return token;
	}

	public void setToken(final InternalLoginTokenType tokenType,
	    final InternalLoginToken token) throws IOException {
		FileChannel readWriteChannel = null;
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(tokenType.token, "rw");
      readWriteChannel = randomAccessFile
			    .getChannel();
			final ByteBuffer readWriteBuffer = readWriteChannel.map(
			    FileChannel.MapMode.READ_WRITE, 0, InternalLoginToken.LENGTH);
			readWriteBuffer.asCharBuffer().put(token.token);
		}
		finally {
			if (readWriteChannel != null) {
				readWriteChannel.close();
			}
			if(randomAccessFile !=null){
			  randomAccessFile.close();
			}
		}
	}
}
