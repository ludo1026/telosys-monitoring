/**
 *  Copyright (C) 2008-2014  Telosys project org. ( http://www.telosys.org/ )
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.telosys.webtools.monitoring.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Longest requests without duplication.
 */
public class LongestRequests {
	
	/** Stored requests by their URL. */
	private Map<String, Request> requestsByTimes = new HashMap<String, Request>();
	/** Number of stored requests. */
	private final int size;
	/** URL of the request with the minimum execution time. */
	private String minURL = null;
	/** Minimum execution time. */
	private Long minTime = null;
	
	/**
	 * Constructor.
	 * @param size Numbers of requests.
	 */
	public LongestRequests(int size) {
		this.size = size;
	}
	
	/**
	 * The new request is added if the execution time is longest than the existing requests.
	 * If the request already exists, its execution time is updated.  
	 * @param request Request.
	 */
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
	
	/**
	 * Calculate the minimum execution of stored requests.
	 */
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
	
	/**
	 * Returns requests by descending execution time.
	 * @return requests
	 */
	public List<Request> getAllDescending() {
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
