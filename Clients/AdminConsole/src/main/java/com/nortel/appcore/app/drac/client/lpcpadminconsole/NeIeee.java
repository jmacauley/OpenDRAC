package com.nortel.appcore.app.drac.client.lpcpadminconsole;

public class NeIeee implements Comparable<NeIeee> {
	String nename;
	String neieee;

	public NeIeee(String name, String ieee) {
		this.nename = name;
		this.neieee = ieee;
	}

	@Override
	public int compareTo(NeIeee neIeee) {
		if (neIeee != null) {
			return nename.compareToIgnoreCase(neIeee.toString());
		}
		return 0;
	}

	public String getIeee() {
		return this.neieee;
	}

	public String getName() {
		return this.nename;
	}

	@Override
	public String toString() {
		return getName();
	}

}
