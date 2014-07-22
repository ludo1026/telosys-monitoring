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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Top longest requests with duplication.
 */
public class TopRequests {
	
	/** Array of stored requests */
	private final Request[] requests;
	/** Position and value of the shortest stored request in the requests array */ 
	private ValuePosition minimum;
	/** Indicates if the requests array is completed */
	private boolean completed = false;
	
	/**
	 * Contains position and value of a request in the requests array.
	 */
	private static class ValuePosition {
		public Long value = null;
		public Integer position = null;
	}
	
	/**
	 * Constructor.
	 * @param size Number of requests in the array.
	 */
	public TopRequests(int size) {
		if(size <= 0) {
			throw new IllegalStateException("size of top requests list must be greater than 0");
		}
		requests = new Request[size];
	}
	
	/**
	 * Add new request.
	 * @param request Request
	 */
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
	
	/**
	 * Returns value and position of the shortest stored request.
	 * @return Value and position
	 */
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

	/**
	 * Return all stored requests by ascending
	 * @return requests
	 */
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

	/**
	 * Return all stored requests by descending
	 * @return requests
	 */
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
