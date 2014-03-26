/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opendrac.nsi.client;

import com.sun.net.httpserver.BasicAuthenticator;

public class TestBasicAuthenticator extends BasicAuthenticator{

	public TestBasicAuthenticator(String realm) {
		super(realm);
	}

	@Override
	public boolean checkCredentials(String username, String password) {
		if (username.equals("nsidemo") && password.equals("RioPlug-Fest2011!")){
			return true;
		}
		return false;
	}

}
