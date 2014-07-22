package org.telosys.webtools.monitoring.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LonguestRequests {
	
	private Map<String, Request> requestsByTimes = new HashMap<String, Request>();
	private final int size;
	private String minURL = null;
	private Long minTime = null;
	
	public LonguestRequests(int size) {
		this.size = size;
	}
	
	public void add(Request request) {
		if(minTime == null) {
			requestsByTimes.put(request.getURL(), request);
			calculateMinimum();
		} else {
			if(requestsByTimes.containsKey(request.getURL())) {
				Request requestStored = requestsByTimes.get(request.getURL());
				if(requestStored.getElapsedTime() < request.getElapsedTime()) {
					requestsByTimes.put(request.getURL(), request);
					calculateMinimum();
				}
			} else {
				if(requestsByTimes.size() < size) {
					requestsByTimes.put(request.getURL(), request);
					calculateMinimum();
				} else {
					if(minTime < request.getElapsedTime()) {
						requestsByTimes.remove(minURL);
						requestsByTimes.put(request.getURL(), request);
						calculateMinimum();
					}
				}
			}
		}
	}
	
	public void calculateMinimum() {
		String minURL = null;
		Long minTime = null;
		for(Request request : requestsByTimes.values()) {
			if(minTime == null || minTime > request.getElapsedTime()) {
				minTime = request.getElapsedTime();
				minURL = request.getURL();
			}
		}
		this.minURL = minURL;
		this.minTime = minTime;
	}
	
	public List<Request> getAllDescendants() {
		List<Request> requests = new ArrayList<Request>();
		for(Request request : requestsByTimes.values()) {
			if(request != null) {
				requests.add(request);
			}
		}
		requests.sort(new RequestComparator(false));
		return requests;
	}
	
}
