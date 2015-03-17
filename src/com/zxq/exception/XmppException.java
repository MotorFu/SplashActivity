package com.zxq.exception;

public class XmppException extends Exception {
	private static final long serialVersionUID = 1L;

	public XmppException(String message) {
		super(message);
	}

	public XmppException(String message, Throwable cause) {
		super(message, cause);
	}
}
