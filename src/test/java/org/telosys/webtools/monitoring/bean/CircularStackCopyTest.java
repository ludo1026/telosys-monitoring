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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.telosys.webtools.monitoring.bean.CircularStack;

public class CircularStackCopyTest {
	
	@Test
	public void testResizePlus1() {
		CircularStack circularStack = new CircularStack(3);
		CircularStack circularStack2 = new CircularStack(circularStack, circularStack.size+1);
		assertEquals(0, circularStack2.getAllAscending().size());
		
		Request r1 = new Request();
		r1.setCountLongTimeRequests(1);
		circularStack.add(r1);
		circularStack2 = new CircularStack(circularStack, circularStack.size+1);
		List<Request> results = circularStack2.getAllAscending();
		assertEquals(1, results.size());
		assertEquals(r1, results.get(0));
		
		Request r2 = new Request();
		r2.setCountLongTimeRequests(2);
		circularStack.add(r2);
		circularStack2 = new CircularStack(circularStack, circularStack.size+1);
		results = circularStack2.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		
		Request r3 = new Request();
		r3.setCountLongTimeRequests(3);
		circularStack.add(r3);
		circularStack2 = new CircularStack(circularStack, circularStack.size+1);
		results = circularStack2.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		assertEquals(r3, results.get(2));
		
		Request r4 = new Request();
		r4.setCountLongTimeRequests(4);
		circularStack.add(r4);
		circularStack2 = new CircularStack(circularStack, circularStack.size+1);
		results = circularStack2.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r2, results.get(0));
		assertEquals(r3, results.get(1));
		assertEquals(r4, results.get(2));

		Request r5 = new Request();
		r5.setCountLongTimeRequests(5);
		circularStack.add(r5);
		circularStack2 = new CircularStack(circularStack, circularStack.size+1);
		results = circularStack2.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r3, results.get(0));
		assertEquals(r4, results.get(1));
		assertEquals(r5, results.get(2));
	}

	@Test
	public void testResizeMoins1() {
		CircularStack circularStack = new CircularStack(3);
		CircularStack circularStack2;
		
		Request r1 = new Request();
		r1.setCountLongTimeRequests(1);
		circularStack.add(r1);
		circularStack2 = new CircularStack(circularStack, circularStack.size-1);
		List<Request> results = circularStack2.getAllAscending();
		assertEquals(1, results.size());
		assertEquals(r1, results.get(0));
		
		Request r2 = new Request();
		r2.setCountLongTimeRequests(2);
		circularStack.add(r2);
		circularStack2 = new CircularStack(circularStack, circularStack.size-1);
		results = circularStack2.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		
		Request r3 = new Request();
		r3.setCountLongTimeRequests(3);
		circularStack.add(r3);
		circularStack2 = new CircularStack(circularStack, circularStack.size-1);
		results = circularStack2.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		
		Request r4 = new Request();
		r4.setCountLongTimeRequests(4);
		circularStack.add(r4);
		circularStack2 = new CircularStack(circularStack, circularStack.size-1);
		results = circularStack2.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r2, results.get(0));
		assertEquals(r3, results.get(1));

		Request r5 = new Request();
		r5.setCountLongTimeRequests(5);
		circularStack.add(r5);
		circularStack2 = new CircularStack(circularStack, circularStack.size-1);
		results = circularStack2.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r3, results.get(0));
		assertEquals(r4, results.get(1));
	}
}
