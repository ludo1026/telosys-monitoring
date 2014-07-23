package org.telosys.webtools.monitoring;

public class Counter {
	private Integer count = 0;
	public synchronized Integer increment() {
		return count++;
	}
}
