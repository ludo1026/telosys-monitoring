package org.telosys.webtools.monitoring;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class CircularStackTest {
	
	@Test
	public void testAscendant() {
		CircularStack circularStack = new CircularStack(3);
		assertEquals(0, circularStack.getAllAscendant().size());
		
		circularStack.push("1");
		List<String> results = circularStack.getAllAscendant();
		assertEquals(1, results.size());
		assertEquals("1", results.get(0));
		
		circularStack.push("2");
		results = circularStack.getAllAscendant();
		assertEquals(2, results.size());
		assertEquals("1", results.get(0));
		assertEquals("2", results.get(1));
		
		circularStack.push("3");
		results = circularStack.getAllAscendant();
		assertEquals(3, results.size());
		assertEquals("1", results.get(0));
		assertEquals("2", results.get(1));
		assertEquals("3", results.get(2));
		
		circularStack.push("4");
		results = circularStack.getAllAscendant();
		assertEquals(3, results.size());
		assertEquals("2", results.get(0));
		assertEquals("3", results.get(1));
		assertEquals("4", results.get(2));

		circularStack.push("5");
		results = circularStack.getAllAscendant();
		assertEquals(3, results.size());
		assertEquals("3", results.get(0));
		assertEquals("4", results.get(1));
		assertEquals("5", results.get(2));
		
	}

	@Test
	public void testDescendant() {
		CircularStack circularStack = new CircularStack(3);
		assertEquals(0, circularStack.getAllDescendant().size());
		
		circularStack.push("1");
		List<String> results = circularStack.getAllDescendant();
		assertEquals(1, results.size());
		assertEquals("1", results.get(0));
		
		circularStack.push("2");
		results = circularStack.getAllDescendant();
		assertEquals(2, results.size());
		assertEquals("2", results.get(0));
		assertEquals("1", results.get(1));
		
		circularStack.push("3");
		results = circularStack.getAllDescendant();
		assertEquals(3, results.size());
		assertEquals("3", results.get(0));
		assertEquals("2", results.get(1));
		assertEquals("1", results.get(2));
		
		circularStack.push("4");
		results = circularStack.getAllDescendant();
		assertEquals(3, results.size());
		assertEquals("4", results.get(0));
		assertEquals("3", results.get(1));
		assertEquals("2", results.get(2));

		circularStack.push("5");
		results = circularStack.getAllDescendant();
		assertEquals(3, results.size());
		assertEquals("5", results.get(0));
		assertEquals("4", results.get(1));
		assertEquals("3", results.get(2));
		
		
	}
	
}
