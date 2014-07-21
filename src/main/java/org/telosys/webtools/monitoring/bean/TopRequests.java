package org.telosys.webtools.monitoring.bean;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TopRequests {
	
	private final Request[] requests;
	
	public TopRequests(int size) {
		if(size <= 0) {
			throw new IllegalStateException("size of top requests list must be greater than 0");
		}
		requests = new Request[size-1];
	}
	
	public void add(Request request) {
		int pos = 0;
		while(pos<requests.length
		   && requests[pos] != null
		   && requests[pos].getElapsedTime() >= request.getElapsedTime()) {
			pos++;
		}
		
		if(pos < requests.length) {
			if(requests[pos] == null) {
				requests[pos] = request;
			} else {
				for(int pos2 = pos; pos2<requests.length-1; pos2++) {
					requests[pos2+1] = requests[pos2];
				}
				requests[pos] = request;
			}
		}
	}
	
}
