package com.dhc.openglbasic;

public class ResponseHandler {
	private byte[] response = null;

	public synchronized boolean handleResponse(byte[] response) {
		this.response = response;
		this.notify();
		return true;
	}

	public synchronized void waitForResponse() {
		while (this.response == null) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}

		System.out.println(new String(this.response));
	}
}