package org.opendrac.automationtool;

import static org.opendrac.automationtool.ConfigLoader.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CertificateLoader {

	private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
	private ConfigLoader configLoader = new ConfigLoader();

	public void updateKeyStore(String configFile) throws Exception {

		String passwordToKeystore = readPasswordToKeyStore();

		KeyStore keyStore = null;
		if (configFile == null || configFile.isEmpty()) {
			configFile = System.getProperty("basedir") + "/"
			    + DEFAULT_CONFIG_FILE.replace("/", File.separator);
		}
		configLoader.loadConfig(configFile);
		String host = configLoader.getHost();
		int port = configLoader.getPort();

		File file = new File(System.getProperty("javax.net.ssl.trustStore"));
		if (file.isFile() == false) {
			char fileSeparatorChar = File.separatorChar;
			File dir = new File(System.getProperty("java.home") + fileSeparatorChar
			    + "lib" + fileSeparatorChar + "security");
			file = new File(dir, "jssecacerts");
			if (file.isFile() == false) {
				file = new File(dir, "cacerts");
			}
		}
		System.out.println("Loading KeyStore " + file + "...");
		try {
			keyStore = loadKeyStore(file, passwordToKeystore);
		}
		catch (IOException ioe) {
			passwordToKeystore = reReadPasswordToKeyStore(file);
			keyStore = loadKeyStore(file, passwordToKeystore);
		}

		TrustManagerFactory trustManagerFactory = TrustManagerFactory
		    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
		SavingTrustManager trustManager = getSavingTrustManager(
		    trustManagerFactory, keyStore);

		SSLContext context = getSSLContext(trustManager);
		context.init(null, new TrustManager[] { trustManager }, null);
		SSLSocketFactory factory = context.getSocketFactory();

		SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
		socket.setSoTimeout(10000);
		try {
			System.out.println("Start SSL handshake...");
			socket.startHandshake();
			socket.close();
			System.out.println();
			System.out.println("Certificate already trusted");
		}
		catch (SSLException e) {
			System.out.println();
		}

		X509Certificate[] certificateChain = trustManager.getCertificateChain();
		if (certificateChain == null) {
			System.out.println("Could not retrieve server certificate chain");
			return;
		}

		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < certificateChain.length; i++) {
			X509Certificate cert = certificateChain[i];
			System.out.println(" " + (i + 1) + " Subject " + cert.getSubjectDN());
			System.out.println("   Issuer  " + cert.getIssuerDN());
			sha1.update(cert.getEncoded());
			System.out.println("   sha1    " + toHexString(sha1.digest()));
			md5.update(cert.getEncoded());
			System.out.println("   md5     " + toHexString(md5.digest()));
			System.out.println();
		}

		// System.out.println("Enter certificate sequence number to add to trusted keystore or \"quit\" to exit the program: [1]");
		int sequenceNumber = 0;// readSequenceNumber();

		X509Certificate certificate = certificateChain[sequenceNumber];
		// assertValidCertificate(certificate);
		String alias = host + "-" + (sequenceNumber + 1);
		keyStore.setCertificateEntry(alias, certificate);

		OutputStream out = new FileOutputStream(file);
		keyStore.store(out, passwordToKeystore.toCharArray());
		out.close();

		System.out.println();
		System.out.println(certificate);
		System.out.println();
		System.out
		    .println("Added certificate to keystore 'jssecacerts' using alias: '"
		        + alias + "'");

		// force to reload the keystore
		keyStore = loadKeyStore(file, passwordToKeystore);
		trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
		    .getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		trustManager = getSavingTrustManager(trustManagerFactory, keyStore);
		context = getSSLContext(trustManager);
	}

	private KeyStore loadKeyStore(File file, String passwordToKeystore)
	    throws KeyStoreException, IOException, NoSuchAlgorithmException,
	    CertificateException {
		InputStream inputStream = new FileInputStream(file);
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(inputStream, passwordToKeystore.toCharArray());
		inputStream.close();
		return keyStore;
	}

	private String reReadPasswordToKeyStore(File file) throws KeyStoreException,
	    NoSuchAlgorithmException, CertificateException {
		try {
			String passwordToKeystore = reReadPasswordEntry();
			InputStream inputStream = new FileInputStream(file);
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(inputStream, passwordToKeystore.toCharArray());
			inputStream.close();
			return passwordToKeystore;
		}
		catch (IOException ioe) {
			return reReadPasswordToKeyStore(file);
		}
	}

	private String reReadPasswordEntry() throws IOException {
		System.out
		    .print("Password not correct, retry or type \"quit\" to exit program: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String answer = reader.readLine().trim().toLowerCase();
		if (answer.equals("quit")) {
			System.exit(0);
		}
		return answer;
	}

	private SSLContext getSSLContext(SavingTrustManager trustManager)
	    throws NoSuchAlgorithmException, KeyStoreException,
	    KeyManagementException {
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { trustManager }, null);
		SSLContext.setDefault(context);
		return context;
	}

	private SavingTrustManager getSavingTrustManager(
	    TrustManagerFactory trustManagerFactory, KeyStore keyStore)
	    throws KeyStoreException {
		trustManagerFactory.init(keyStore);
		X509TrustManager defaultTrustManager = (X509TrustManager) trustManagerFactory
		    .getTrustManagers()[0];
		SavingTrustManager trustManager = new SavingTrustManager(
		    defaultTrustManager);
		return trustManager;
	}

	private static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 3);
		for (int b : bytes) {
			b &= 0xff;
			sb.append(HEX_DIGITS[b >> 4]);
			sb.append(HEX_DIGITS[b & 15]);
			sb.append(' ');
		}
		return sb.toString();
	}

	private String readPasswordToKeyStore() throws IOException {
		System.out
		    .print("Type password to keystore (default password =\"changeit\"): ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		return reader.readLine().trim().toLowerCase();
	}

	private static class SavingTrustManager implements X509TrustManager {
		private final X509TrustManager trustManager;
		private X509Certificate[] certificateChain;

		SavingTrustManager(X509TrustManager trustManager) {
			this.trustManager = trustManager;
		}

		public X509Certificate[] getAcceptedIssuers() {
			throw new UnsupportedOperationException();
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
		    throws CertificateException {
			throw new UnsupportedOperationException();
		}

		X509Certificate[] getCertificateChain() {
			return certificateChain;
		}

		public void checkServerTrusted(X509Certificate[] certificateChain,
		    String authType) throws CertificateException {
			this.certificateChain = certificateChain;
			trustManager.checkServerTrusted(certificateChain, authType);
		}
	}
}
