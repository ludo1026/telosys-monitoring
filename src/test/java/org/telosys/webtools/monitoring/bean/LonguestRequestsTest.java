package org.telosys.webtools.monitoring.bean;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class LonguestRequestsTest {
	
	@Test
	public void testAdd1() {
		LonguestRequests longuestRequests = new LonguestRequests(3);
		List<Request> requests = new ArrayList<Request>();
		
		Request r1 = new Request();
		r1.setElapsedTime(10);
		r1.setRequestURL("url1");
		longuestRequests.add(r1);
		requests = longuestRequests.getAllDescendants();
		assertEquals(1, requests.size());
		assertEquals(r1, requests.get(0));
		
		Request r2 = new Request();
		r2.setElapsedTime(12);
		r2.setRequestURL("url2");
		longuestRequests.add(r2);
		requests = longuestRequests.getAllDescendants();
		assertEquals(2, requests.size());
		assertEquals(r2, requests.get(0));
		assertEquals(r1, requests.get(1));
		
		Request r3 = new Request();
		r3.setElapsedTime(14);
		r3.setRequestURL("url3");
		longuestRequests.add(r3);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r3, requests.get(0));
		assertEquals(r2, requests.get(1));
		assertEquals(r1, requests.get(2));
		
		Request r4 = new Request();
		r4.setElapsedTime(16);
		r4.setRequestURL("url4");
		longuestRequests.add(r4);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r4, requests.get(0));
		assertEquals(r3, requests.get(1));
		assertEquals(r2, requests.get(2));
		
		Request r5 = new Request();
		r5.setElapsedTime(18);
		r5.setRequestURL("url5");
		longuestRequests.add(r5);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r5, requests.get(0));
		assertEquals(r4, requests.get(1));
		assertEquals(r3, requests.get(2));
		
		Request r5_2 = new Request();
		r5_2.setElapsedTime(20);
		r5_2.setRequestURL("url5");
		longuestRequests.add(r5_2);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r5_2, requests.get(0));
		assertEquals(r4, requests.get(1));
		assertEquals(r3, requests.get(2));
		
		Request r4_2 = new Request();
		r4_2.setElapsedTime(22);
		r4_2.setRequestURL("url4");
		longuestRequests.add(r4_2);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r4_2, requests.get(0));
		assertEquals(r5_2, requests.get(1));
		assertEquals(r3, requests.get(2));
		
		Request r3_2 = new Request();
		r3_2.setElapsedTime(24);
		r3_2.setRequestURL("url3");
		longuestRequests.add(r3_2);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r3_2, requests.get(0));
		assertEquals(r4_2, requests.get(1));
		assertEquals(r5_2, requests.get(2));
		
		Request r3_3 = new Request();
		r3_3.setElapsedTime(23);
		r3_3.setRequestURL("url3");
		longuestRequests.add(r3_3);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r3_2, requests.get(0));
		assertEquals(r4_2, requests.get(1));
		assertEquals(r5_2, requests.get(2));
		
		Request r2_2 = new Request();
		r2_2.setElapsedTime(2);
		r2_2.setRequestURL("url2");
		longuestRequests.add(r2_2);
		requests = longuestRequests.getAllDescendants();
		assertEquals(3, requests.size());
		assertEquals(r3_2, requests.get(0));
		assertEquals(r4_2, requests.get(1));
		assertEquals(r5_2, requests.get(2));
	}
	
}
