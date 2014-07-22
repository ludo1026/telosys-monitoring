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
		circularStack.push(r1);
		List<Request> results = circularStack.getAllAscending();
		assertEquals(1, results.size());
		assertEquals(r1, results.get(0));
		
		Request r2 = new Request();
		circularStack.push(r2);
		results = circularStack.getAllAscending();
		assertEquals(2, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		
		Request r3 = new Request();
		circularStack.push(r3);
		results = circularStack.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r1, results.get(0));
		assertEquals(r2, results.get(1));
		assertEquals(r3, results.get(2));
		
		Request r4 = new Request();
		circularStack.push(r4);
		results = circularStack.getAllAscending();
		assertEquals(3, results.size());
		assertEquals(r2, results.get(0));
		assertEquals(r3, results.get(1));
		assertEquals(r4, results.get(2));

		Request r5 = new Request();
		circularStack.push(r5);
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
		circularStack.push(r1);
		List<Request> results = circularStack.getAllDescending();
		assertEquals(1, results.size());
		assertEquals(r1, results.get(0));
		
		Request r2 = new Request();
		circularStack.push(r2);
		results = circularStack.getAllDescending();
		assertEquals(2, results.size());
		assertEquals(r2, results.get(0));
		assertEquals(r1, results.get(1));
		
		Request r3 = new Request();
		circularStack.push(r3);
		results = circularStack.getAllDescending();
		assertEquals(3, results.size());
		assertEquals(r3, results.get(0));
		assertEquals(r2, results.get(1));
		assertEquals(r1, results.get(2));
		
		Request r4 = new Request();
		circularStack.push(r4);
		results = circularStack.getAllDescending();
		assertEquals(3, results.size());
		assertEquals(r4, results.get(0));
		assertEquals(r3, results.get(1));
		assertEquals(r2, results.get(2));

		Request r5 = new Request();
		circularStack.push(r5);
		results = circularStack.getAllDescending();
		assertEquals(3, results.size());
		assertEquals(r5, results.get(0));
		assertEquals(r4, results.get(1));
		assertEquals(r3, results.get(2));
		
		
	}
	
}
