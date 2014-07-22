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

import java.util.Comparator;

/**
 * Comparator which orders requests by their execution time.
 */
public class RequestComparator implements Comparator<Request> {

	/** Order : ascending or descending */
	// true : ascending : shortest to longest requests
	// false: descending : longest to shortest requests
	private final boolean orderAscendant;
	
	/**
	 * Constructor with ascending order.
	 */
	public RequestComparator() {
		this.orderAscendant = true;
	}
	
	/**
	 * Constructor.
	 * @param orderAscendant true:ascending, false:descending
	 */
	public RequestComparator(boolean orderAscendant) {
		this.orderAscendant = orderAscendant;
	}
	
	/**
	 * Compare two requests by their execution time.
	 * @param r1 Request 1
	 * @param r2 Request 2
	 */
	@Override
	public int compare(Request r1, Request r2) {
		int compareAscendant = compareAscendant(r1, r2);
		if(orderAscendant) {
			return compareAscendant;
		} else {
			return -compareAscendant;
		}
	}
	
	/**
	 * Compare two requests by their execution time.
	 * @param r1 Request 1
	 * @param r2 Request 2
	 */
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
