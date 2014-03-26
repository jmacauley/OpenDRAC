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

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/**
 * A security provider that ignores security. Mostly I want a trust manger that
 * ignores certificate chain errors so that self signed certificates can be used
 * without error or requiring that the "Client" trust the servers certificate.
 * 
 * @author pitman
 */
public final class AllTrustingSecurityProvider extends Provider { // NO_UCD
	/**
	 * A very trusting trust manager that accepts anything, used for setting up a
	 * TLS session when we don't have client/server certificates and or validation
	 * code in place.
	 */

	public static class VeryTrustingTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] ax509certificate, String s)
		    throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] ax509certificate, String s)
		    throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	public static class VeryTrustingTrustManagerFactorySPI extends
	    TrustManagerFactorySpi {
		public static String getName() {
			return "VeryTrustingTrustManagerFactory";
		}

		@Override
		public TrustManager[] engineGetTrustManagers() {
			// 
			return new TrustManager[] { new VeryTrustingTrustManager() };
		}

		@Override
		public void engineInit(KeyStore arg0) throws KeyStoreException {
			// 
			// ignore the keystore, after all we are very trusting...
		}

		@Override
		public void engineInit(ManagerFactoryParameters arg0)
		    throws InvalidAlgorithmParameterException {
			// ignore the factory parameters, after all we are very trusting...
			// 
		}
	}

	private static final long serialVersionUID = 1L;

	private AllTrustingSecurityProvider() {
		super("AllTrustingSecurityProvider", 1,
		    "A security provider that ignores security checks.");

		// 
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				put("TrustManagerFactory."
				    + VeryTrustingTrustManagerFactorySPI.getName(),
				    VeryTrustingTrustManagerFactorySPI.class.getName());
				return null;
			}
		});
	}

	/**
	 * Register the lame provider. Then any regular SSL/TLS traffic will use this
	 * provider. Must be called before SSL/TLS is used.
	 */
	public static void registerProvider() {
		if (Security.getProvider("AllTrustingSecurityProvider") == null) {
			Security.insertProviderAt(new AllTrustingSecurityProvider(), 1);
			Security.setProperty("ssl.TrustManagerFactory.algorithm",
			    VeryTrustingTrustManagerFactorySPI.getName());
		}
	}
}
