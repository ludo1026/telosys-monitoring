package org.telosys.webtools.monitoring.bean;

import java.util.ArrayList;
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
		requests = new Request[size];
	}
	
	public int getSize() {
		return requests.length;
	}
	
	public void add(Request request) {
		if(mustBeInsert(request)) {
			int posMinimum = getMinimumPosition();
			requests[posMinimum] = request;
		}
	}
	
	protected int getMinimumPosition() {
		int posMinimum = -1;
		long minimum = -1;
		for(int pos=0; pos<requests.length; pos++) {
			if(requests[pos] == null) {
				posMinimum = pos;
				break;
			}
			if(requests[pos].getElapsedTime() < minimum || minimum == -1) {
				posMinimum = pos;
				minimum = requests[pos].getElapsedTime();
			}
		}
		return posMinimum;
	}
	
	protected boolean mustBeInsert(Request request) {
		for(int pos=0; pos<requests.length; pos++) {
			if(requests[pos] == null) {
				return true;
			}
			if(request.getElapsedTime() > requests[pos].getElapsedTime()) {
				return true;
			}
		}
		return false;
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
