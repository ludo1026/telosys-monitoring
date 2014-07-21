package org.telosys.webtools.monitoring.bean;

import java.util.Comparator;

public class RequestComparator implements Comparator<Request> {

	// true : natural order
	// false: reverse order
	private final boolean orderAscendant;
	
	public RequestComparator() {
		this.orderAscendant = true;
	}
	
	public RequestComparator(boolean orderAscendant) {
		this.orderAscendant = orderAscendant;
	}
	
	@Override
	public int compare(Request r1, Request r2) {
		int compareAscendant = compareAscendant(r1, r2);
		if(orderAscendant) {
			return compareAscendant;
		} else {
			return -compareAscendant;
		}
	}
	
	private int compareAscendant(Request r1, Request r2) {
		if(r1 == null) {
			if(r2 == null) {
				return 0;
			}
			return -1;
		}
		if(r2 == null) {
			return 1;
		}
		if(r1.getElapsedTime() == r2.getElapsedTime()) {
			return 0;
		}
		if(r1.getElapsedTime() > r2.getElapsedTime()) {
			return 1;
		} else {
			return -1;
		}
	}
	
}
