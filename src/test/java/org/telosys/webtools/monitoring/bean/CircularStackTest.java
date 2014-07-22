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

public class CircularStackTest {
	
	@Test
	public void testAscendant() {
		CircularStack circularStack = new CircularStack(3);
		assertEquals(0, circularStack.getAllAscending().size());
		
		Request r1 = new Request();
		circularStack.add(r1);
		List<Request> results = circularStack.getAllAscending();
		assertEquals(1, results.size());
		assertEquals(r1, results.get(0));
		
		Request r2 = new Request();
		circularStack.add(r2);
		results = circularStack.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		
		Request r3 = new Request();
		circularStack.add(r3);
		results = circularStack.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		assertEquals(r3, results.get(2));
		
		Request r4 = new Request();
		circularStack.add(r4);
		results = circularStack.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r2, results.get(0));
		assertEquals(r3, results.get(1));
		assertEquals(r4, results.get(2));

		Request r5 = new Request();
		circularStack.add(r5);
		results = circularStack.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r3, results.get(0));
		assertEquals(r4, results.get(1));
		assertEquals(r5, results.get(2));
		
	}

	@Test
	public void testDescendant() {
		CircularStack circularStack = new CircularStack(3);
		assertEquals(0, circularStack.getAllDescending().size());
		
		Request r1 = new Request();
		circularStack.add(r1);
		List<Request> results = circularStack.getAllDescending();
		assertEquals(1, results.size());
		assertEquals(r1, results.get(0));
		
		Request r2 = new Request();
		circularStack.add(r2);
		results = circularStack.getAllDescending();
		assertEquals(2, results.size());
		assertEquals(r2, results.get(0));
		assertEquals(r1, results.get(1));
		
		Request r3 = new Request();
		circularStack.add(r3);
		results = circularStack.getAllDescending();
		assertEquals(3, results.size());
		assertEquals(r3, results.get(0));
		assertEquals(r2, results.get(1));
		assertEquals(r1, results.get(2));
		
		Request r4 = new Request();
		circularStack.add(r4);
		results = circularStack.getAllDescending();
		assertEquals(3, results.size());
		assertEquals(r4, results.get(0));
		assertEquals(r3, results.get(1));
		assertEquals(r2, results.get(2));

		Request r5 = new Request();
		circularStack.add(r5);
		results = circularStack.getAllDescending();
		assertEquals(3, results.size());
		assertEquals(r5, results.get(0));
		assertEquals(r4, results.get(1));
		assertEquals(r3, results.get(2));
		
		
	}
	
}
