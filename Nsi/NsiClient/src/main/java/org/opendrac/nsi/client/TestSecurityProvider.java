/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opendrac.nsi.client;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

/**
 * A security provider that ignores security. Mostly I want a trust manger that ignores certificate chain
 * errors so that self signed certificates can be used without error or requiring that the "Client" trust the
 * servers certificate.
 *
 * @author pitman
 */
public final class TestSecurityProvider
    extends Provider
{
    /**
     * A very trusting trust manager that accepts anything, used for setting up a TLS session when we don't
     * have client/server certificates and or validation code in place.
     */

    public static class VeryTrustingTrustManager
        implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] ax509certificate, String s)
            throws CertificateException
        {
            // Log.debug("VeryTrustingTrustManager checkClientTrusted invoked with " +
            // Arrays.toString(ax509certificate) + " "
            // + s);
            // Sure, looks ok to me... I trust you!
        }

        @Override
        public void checkServerTrusted(X509Certificate[] ax509certificate, String s)
            throws CertificateException
        {
            // Log.debug("VeryTrustingTrustManager checkServerTrusted invoked with " +
            // Arrays.toString(ax509certificate) + " "
            // + s);
            // Sure, looks ok to me... I trust you!
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }
    }

    public static class VeryTrustingTrustManagerFactorySPI
        extends TrustManagerFactorySpi
    {
        public static String getName()
        {
            return "VeryTrustingTrustManagerFactory";
        }

        @Override
        public TrustManager[] engineGetTrustManagers()
        {
            // Log.debug("engineGetTrustManagers invoked");
            return new TrustManager[]
            { new VeryTrustingTrustManager() };
        }

        @Override
        public void engineInit(KeyStore arg0)
            throws KeyStoreException
        {
            // Log.debug("engineInit invoked with " + arg0);
            // ignore the keystore, after all we are very trusting...
        }

        @Override
        public void engineInit(ManagerFactoryParameters arg0)
            throws InvalidAlgorithmParameterException
        {
            // ignore the factory parameters, after all we are very trusting...
            // Log.debug("engineInit invoked with " + arg0);
        }
    }

    private static final long serialVersionUID = 1L;

    private TestSecurityProvider()
    {
        super("TestSecurityProvider", 1, "A security provider that ignores security checks.");

        // Log.debug("TestSecurityProvider invoked");
        AccessController.doPrivileged(new PrivilegedAction<Object>()
        {
            public Object run()
            {
                put("TrustManagerFactory." + VeryTrustingTrustManagerFactorySPI.getName(),
                    VeryTrustingTrustManagerFactorySPI.class.getName());
                return null;
            }
        });
    }

    /**
     * Register the lame provider. Then any regular SSL/TLS traffic will use this provider. Must be called
     * before SSL/TLS is used.
     */
    public static void registerProvider()
    {
        if (Security.getProvider("TestSecurityProvider") == null)
        {
            // Log.info("Registering lame security provider!");
            Security.insertProviderAt(new TestSecurityProvider(), 1);
            Security.setProperty("ssl.TrustManagerFactory.algorithm", VeryTrustingTrustManagerFactorySPI.getName());
        }
    }
}
