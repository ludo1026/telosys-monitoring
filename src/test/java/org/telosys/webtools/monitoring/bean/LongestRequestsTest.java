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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LongestRequestsTest {
	
	@Test
	public void testAdd1() {
		LongestRequests longestRequests = new LongestRequests(3);
		List<Request> requests = new ArrayList<Request>();
		
		Request r1 = new Request();
		r1.setElapsedTime(10);
		r1.setRequestURL("url1");
		longestRequests.add(r1);
		requests = longestRequests.getAllDescending();
		assertEquals(1, requests.size());
		assertEquals(r1, requests.get(0));

		Request r1_2 = new Request();
		r1_2.setElapsedTime(8);
		r1_2.setRequestURL("url1");
		longestRequests.add(r1_2);
		requests = longestRequests.getAllDescending();
		assertEquals(1, requests.size());
		assertEquals(r1, requests.get(0));
		
		Request r2 = new Request();
		r2.setElapsedTime(12);
		r2.setRequestURL("url2");
		longestRequests.add(r2);
		requests = longestRequests.getAllDescending();
		assertEquals(2, requests.size());
		assertEquals(r2, requests.get(0));
		assertEquals(r1, requests.get(1));
		
		Request r3 = new Request();
		r3.setElapsedTime(14);
		r3.setRequestURL("url3");
		longestRequests.add(r3);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r3, requests.get(0));
		assertEquals(r2, requests.get(1));
		assertEquals(r1, requests.get(2));
		
		Request r4 = new Request();
		r4.setElapsedTime(16);
		r4.setRequestURL("url4");
		longestRequests.add(r4);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r4, requests.get(0));
		assertEquals(r3, requests.get(1));
		assertEquals(r2, requests.get(2));
		
		Request r5 = new Request();
		r5.setElapsedTime(18);
		r5.setRequestURL("url5");
		longestRequests.add(r5);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r5, requests.get(0));
		assertEquals(r4, requests.get(1));
		assertEquals(r3, requests.get(2));
		
		Request r5_2 = new Request();
		r5_2.setElapsedTime(20);
		r5_2.setRequestURL("url5");
		longestRequests.add(r5_2);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r5_2, requests.get(0));
		assertEquals(r4, requests.get(1));
		assertEquals(r3, requests.get(2));
		
		Request r4_2 = new Request();
		r4_2.setElapsedTime(22);
		r4_2.setRequestURL("url4");
		longestRequests.add(r4_2);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r4_2, requests.get(0));
		assertEquals(r5_2, requests.get(1));
		assertEquals(r3, requests.get(2));
		
		Request r3_2 = new Request();
		r3_2.setElapsedTime(24);
		r3_2.setRequestURL("url3");
		longestRequests.add(r3_2);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r3_2, requests.get(0));
		assertEquals(r4_2, requests.get(1));
		assertEquals(r5_2, requests.get(2));
		
		Request r3_3 = new Request();
		r3_3.setElapsedTime(23);
		r3_3.setRequestURL("url3");
		longestRequests.add(r3_3);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r3_2, requests.get(0));
		assertEquals(r4_2, requests.get(1));
		assertEquals(r5_2, requests.get(2));
		
		Request r2_2 = new Request();
		r2_2.setElapsedTime(2);
		r2_2.setRequestURL("url2");
		longestRequests.add(r2_2);
		requests = longestRequests.getAllDescending();
		assertEquals(3, requests.size());
		assertEquals(r3_2, requests.get(0));
		assertEquals(r4_2, requests.get(1));
		assertEquals(r5_2, requests.get(2));
	}
	
}
