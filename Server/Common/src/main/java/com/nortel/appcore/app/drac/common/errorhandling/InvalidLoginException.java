package com.nortel.appcore.app.drac.common.errorhandling;

import java.util.Locale;

public class InvalidLoginException extends DracException {

	private static final long serialVersionUID = -5398510109456349046L;

	public InvalidLoginException(int errorCode, Object[] args) {
		super(errorCode, args);
	}

	public InvalidLoginException(int errorCode, Object[] args,
			Exception exception) {
		super(errorCode, args, exception);
	}

	public InvalidLoginException(Locale locale, ResourceKey key, Object[] args,
			Exception exception) {
		super(locale, key, args, exception);
	}

	public InvalidLoginException(ResourceKey key, Object[] args, Exception exception) {
		super(key, args, exception);
	}
}
