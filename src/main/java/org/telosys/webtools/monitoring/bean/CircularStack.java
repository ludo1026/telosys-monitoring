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
import java.util.Arrays;
import java.util.List;

/**
 * Circular stack contains the last long requests : old requests are replaced by the new requests.
 */
public class CircularStack {
	
	/** Number of stored requests. */
	protected final int size;
	/** Array of stored requests */
	protected Request[] arrays;
	/** Position of the next request in the array */ 
	protected int nextIndex = 0;
	/** Indicates if the array is completed */
	protected boolean completed = false;
	
	/**
	 * Constructor.
	 * @param size Number of stored requests
	 */
	public CircularStack(int size) {
		this.size = size;
		this.arrays = new Request[size];
	}

	/**
	 * Copy constructor.
	 * @param size Number of stored requests
	 */
	public CircularStack(CircularStack circularStack, int size) {	
		this.size = size;
		this.arrays = new Request[size];
		int pos = 0;
		for(Request request : circularStack.getAllAscending()) {
			if(pos >= size) {
				break;
			}
			this.arrays[pos] = request;
			pos++;
		}
		if(pos >= size) {
			this.completed = true;
			this.nextIndex = 0;
		} else {
			this.nextIndex = pos;
		}
	}

	/**
	 * Add new request in the stack.
	 * @param request Request.
	 */
	public synchronized void add(Request request) {
		int index = getNextIndice();
		this.arrays[index] = request;
	}
	
	/**
	 * Next position in the requests array.
	 * @return position
	 */
	private int getNextIndice() {
		int index = this.nextIndex;
		this.nextIndex++;
		if(this.nextIndex >= this.size) {
			this.nextIndex = 0;
			completed = true;
		}
		return index;
	}
	
	/**
	 * Returns all requests by ascending.
	 * @return requests
	 */
	public synchronized List<Request> getAllAscending() {
		List<Request> results = new ArrayList<Request>();
		int index = this.nextIndex;
		boolean completed = this.completed;
		if(completed) {
			for(int i=index; i<this.size; i++) {
				results.add(this.arrays[i]);
			}
		}
		for(int i=0; i<index; i++) {
			results.add(this.arrays[i]);
		}
		return results;
	}
	
	/**
	 * Returns all requests by descending.
	 * @return requests
	 */
	public synchronized List<Request> getAllDescending() {
		List<Request> results = new ArrayList<Request>();
		int index = this.nextIndex;
		boolean completed = this.completed;
		for(int i=index-1; i>=0; i--) {
			results.add(this.arrays[i]);
		}
		if(completed) {
			for(int i=this.size-1; i>=index; i--) {
				results.add(this.arrays[i]);
			}
		}
		return results;
	}

}
