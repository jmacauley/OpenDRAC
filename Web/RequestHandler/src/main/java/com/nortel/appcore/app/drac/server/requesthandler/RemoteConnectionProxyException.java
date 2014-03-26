package com.nortel.appcore.app.drac.server.requesthandler;

public class RemoteConnectionProxyException extends RequestHandlerException {

	private static final long serialVersionUID = 1L;

	public RemoteConnectionProxyException(int errorCode, Object[] args) {
		super(errorCode, args);
	}

	public RemoteConnectionProxyException(int errorCode, Object[] args,
			Exception t) {
		super(errorCode, args, t);
	}

}
