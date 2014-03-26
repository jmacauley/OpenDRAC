/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opendrac.nsi.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 *
 * @author hacksaw
 */

public class TestHostnameVerifier implements HostnameVerifier {
	public boolean verify(String arg0, SSLSession arg1) {
		return true;
	}
}