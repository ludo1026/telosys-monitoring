package org.telosys.webtools.monitoring.bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TopRequests {
	
	private final Request[] requests;
	private ValuePosition minimum;
	private boolean completed = false;
	
	private static class ValuePosition {
		public Long value = null;
		public Integer position = null;
	}
	
	public TopRequests(int size) {
		if(size <= 0) {
			throw new IllegalStateException("size of top requests list must be greater than 0");
		}
		requests = new Request[size];
	}
	
	public int getSize() {
		return requests.length;
	}
	
	public synchronized void add(Request request) {
		if(minimum == null) {
			requests[0] = request;
			minimum = getMinimum();
		} else {
			if(minimum.position >= 0 && minimum.position < requests.length) {
				if(minimum.value == null) {
					requests[minimum.position] = request;
					minimum = getMinimum();
				}
				else if(minimum.value <= request.getElapsedTime()) {
					requests[minimum.position] = request;
					minimum = getMinimum();
				}
			}
		}
	}
	
	protected ValuePosition getMinimum() {
		ValuePosition minimum = new ValuePosition();
		for(int pos=0; pos<requests.length; pos++) {
			if(requests[pos] == null) {
				minimum.position = pos;
				minimum.value = null;
				completed = false;
				break;
			}
			if(minimum.value == null || requests[pos].getElapsedTime() < minimum.value) {
				minimum.position = pos;
				minimum.value = requests[pos].getElapsedTime();
			}
		}
		if(minimum.position == null) {
			completed = true;
		}
		return minimum;
	}

	public List<Request> getAllAscending() {
		List<Request> all = new ArrayList<Request>();
		for(Request request : requests) {
			if(request != null) {
				all.add(request);
			}
		}
		all.sort(new RequestComparator());
		return all;
	}

	public List<Request> getAllDescending() {
		List<Request> all = new ArrayList<Request>();
		for(Request request : requests) {
			if(request != null) {
				all.add(request);
			}
		}
		all.sort(new RequestComparator(false));
		return all;
	}
	
}
