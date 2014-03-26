package org.opendrac.security;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InternalLoginToken implements Serializable {
  private static final long serialVersionUID = -5265884983144906281L;
  public static final int LENGTH = 1024;
  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());
  public final String token;

  // somehow equals on the token does not work
  public final boolean isValid;

  public InternalLoginToken() {
    final byte[] randomBytes = new byte[LENGTH];
    try {
      SecureRandom.getInstance("NativePRNG").nextBytes(randomBytes);
    }
    catch (NoSuchAlgorithmException e1) {
      try {
        SecureRandom.getInstance("SHA1PRNG").nextBytes(randomBytes);
      }
      catch (NoSuchAlgorithmException e2) {
        throw new RuntimeException("Unable to use NativePRNG or SHA1PRNG: ", e2);
      }
    }
    token = DigestUtils.sha512Hex(randomBytes);
    isValid = token.length() == LENGTH;
  }

  public InternalLoginToken(String token) {
    this.token = token;
    isValid = token.length() == LENGTH;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("InternalLoginToken [token=");
    builder.append(token);
    builder.append(", isValid=");
    builder.append(isValid);
    builder.append("]");
    return builder.toString();
  }

}
