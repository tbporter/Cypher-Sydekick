package com.github.tbporter.cypher_sydekick.nfc;

/**
 * Exception class for exceptions thrown by NFCManager.
 * 
 * @author Alex
 * 
 */
public class NFCManagerException extends Exception {

	public NFCManagerException() {
	}

	public NFCManagerException(String detailMessage) {
		super(detailMessage);
	}

	public NFCManagerException(Throwable throwable) {
		super(throwable);
	}

	public NFCManagerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
