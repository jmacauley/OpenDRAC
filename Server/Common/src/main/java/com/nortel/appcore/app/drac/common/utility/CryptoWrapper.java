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

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CryptoWrapper {

  INSTANCE;

  private final Logger log = LoggerFactory.getLogger(CryptoWrapper.class);
	/**
	 * A class that holds an encrypted string but basically tries to act like a
	 * regular string in every other regard. We were getting confused passing
	 * around strings not knowing if they were crypted or unencrypted, this class
	 * avoids that confusion. Strings that are encrypted should be carried around
	 * in this class only.
	 */
	public static class CryptedString implements Serializable {

		private static final long serialVersionUID = 1L;
		private final String cryptedString;

		public CryptedString(String s) {
			cryptedString = s;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof CryptedString)) {
				return false;
			}
			CryptedString other = (CryptedString) obj;
			if (cryptedString == null) {
				if (other.cryptedString != null) {
					return false;
				}
			}
			else if (!cryptedString.equals(other.cryptedString)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return cryptedString.hashCode();
		}

		@Override
		public String toString() {
			return cryptedString;
		}

	}

	private static final String TRANSFORMATION = "Blowfish";
	private SecretKeySpec keySpec;
	private Cipher cipher;

	private CryptoWrapper() {
		// Security.addProvider(provider);
		try {
      this.cipher = Cipher.getInstance(TRANSFORMATION);
      PBEKeySpec pbeKeySpec = new PBEKeySpec("sAu0214Toet".toCharArray());
      SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
      SecretKey secretKey = keyFac.generateSecret(pbeKeySpec);
      keySpec = new SecretKeySpec(secretKey.getEncoded(), TRANSFORMATION);
    }
    catch (NoSuchAlgorithmException e) {
      log.error("Error: ", e);
    }
    catch (NoSuchPaddingException e) {
      log.error("Error: ", e);
    }
    catch (InvalidKeySpecException e) {
      log.error("Error: ", e);
    }
	}

	public String decrypt(CryptedString cipherText) {
		if (cipherText == null) {
			return null;
		}

		try {
			synchronized (cipher) {
				cipher.init(Cipher.DECRYPT_MODE, keySpec);
				byte[] temp = cipher
				    .doFinal(hexStringToByteArray(cipherText.toString()));
				return new String(temp);
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		return null;
	}

	public CryptedString encrypt(String plainText) {
		if (plainText == null) {
			return null;
		}

		try {
			synchronized (cipher) {
				cipher.init(Cipher.ENCRYPT_MODE, keySpec);
				byte[] temp = cipher.doFinal(plainText.getBytes());
				return new CryptedString(byteArrayToHexString(temp));
			}
		}
		catch (Exception e) {
			log.error("Error: ", e);
		}
		return null;
	}

	private String byteArrayToHexString(byte[] bt) {
		StringBuilder hexStrBuf = new StringBuilder();
		for (byte element : bt) {
			int x = (element & 0xf0) >> 4;
			int y = element & 0x0f;
			String hexStr1 = Integer.toHexString(x);
			String hexStr2 = Integer.toHexString(y);
			char c1 = hexStr1.charAt(0);
			char c2 = hexStr2.charAt(0);
			hexStrBuf.append(c1);
			hexStrBuf.append(c2);
		}
		return hexStrBuf.toString();
	}

	private byte[] hexStringToByteArray(String hexStr) {
		int length = hexStr.length();
		int[] x = new int[length];
		byte[] bt = new byte[length / 2];
		for (int i = 0; i < length; i++) {
			x[i] = Character.getNumericValue(hexStr.charAt(i));
			// was Check for oddness that won't work for negative numbers
			if (i % 2 != 0) {
				int y = x[i - 1] * 16 + x[i];
				bt[(i - 1) / 2] = (byte) y;
			}
		}
		return bt;
	}

    public static void main(String [] args) {
        System.out.println(CryptoWrapper.INSTANCE.decrypt(
		        new CryptedString("292c2cdcb5f669a8")));
    }
}
