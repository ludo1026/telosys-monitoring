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
package org.telosys.webtools.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.telosys.webtools.monitoring.bean.CircularStack;
import org.telosys.webtools.monitoring.bean.LongestRequests;
import org.telosys.webtools.monitoring.bean.Request;
import org.telosys.webtools.monitoring.bean.TopRequests;

public class RequestsMonitorTest {
	
	@Test
	public void initDefaults() throws ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());

		InetAddress adrLocale = mock(InetAddress.class);
		doReturn(adrLocale).when(requestsMonitor).getLocalHost();
		when(adrLocale.getHostAddress()).thenReturn("10.11.12.13");
		when(adrLocale.getHostName()).thenReturn("hostname");
		
		FilterConfig filterConfig = mock(FilterConfig.class);
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		assertEquals(RequestsMonitor.DEFAULT_DURATION_THRESHOLD, requestsMonitor.durationThreshold);
		assertEquals(RequestsMonitor.DEFAULT_LOG_SIZE, requestsMonitor.logSize);
		assertEquals(RequestsMonitor.DEFAULT_TOP_TEN_SIZE, requestsMonitor.topTenSize);
		assertEquals(RequestsMonitor.DEFAULT_LONGEST_SIZE, requestsMonitor.longestSize);
		assertEquals("/monitor", requestsMonitor.reportingReqPath);
		assertFalse(requestsMonitor.traceFlag);
		assertEquals("10.11.12.13", requestsMonitor.ipAddress);
		assertEquals("hostname", requestsMonitor.hostname);
		assertTrue(requestsMonitor.activated);
	}
	
	@Test
	public void initCustomized() throws ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());

		InetAddress adrLocale = mock(InetAddress.class);
		doReturn(adrLocale).when(requestsMonitor).getLocalHost();
		when(adrLocale.getHostAddress()).thenReturn("10.11.12.13");
		when(adrLocale.getHostName()).thenReturn("hostname");
		
		FilterConfig filterConfig = mock(FilterConfig.class);
		when(filterConfig.getInitParameter("duration")).thenReturn("200");
		when(filterConfig.getInitParameter("logsize")).thenReturn("300");
		when(filterConfig.getInitParameter("toptensize")).thenReturn("400");
		when(filterConfig.getInitParameter("longestsize")).thenReturn("500");
		when(filterConfig.getInitParameter("reporting")).thenReturn("/monitoring2");
		when(filterConfig.getInitParameter("trace")).thenReturn("true");
		when(filterConfig.getInitParameter("activated")).thenReturn("true");
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		assertEquals(200, requestsMonitor.durationThreshold);
		assertEquals(300, requestsMonitor.logSize);
		assertEquals(400, requestsMonitor.topTenSize);
		assertEquals(500, requestsMonitor.longestSize);
		assertEquals("/monitoring2", requestsMonitor.reportingReqPath);
		assertTrue(requestsMonitor.traceFlag);
		assertEquals("10.11.12.13", requestsMonitor.ipAddress);
		assertEquals("hostname", requestsMonitor.hostname);
		assertTrue(requestsMonitor.activated);
	}

	@Test
	public void initCustomized2() throws ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());

		InetAddress adrLocale = mock(InetAddress.class);
		doReturn(adrLocale).when(requestsMonitor).getLocalHost();
		when(adrLocale.getHostAddress()).thenReturn("10.11.12.13");
		when(adrLocale.getHostName()).thenReturn("hostname");
		
		FilterConfig filterConfig = mock(FilterConfig.class);
		when(filterConfig.getInitParameter("trace")).thenReturn("false");
		when(filterConfig.getInitParameter("activated")).thenReturn("false");
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		assertFalse(requestsMonitor.traceFlag);
		assertFalse(requestsMonitor.activated);
	}

	@Test
	public void testGetParameters() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getQueryString()).thenReturn(
				RequestsMonitor.ATTRIBUTE_NAME_DURATION_THRESHOLD + "=201&"
				+ RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE + "=301&"
				+ RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE + "=401&"
				+ RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE + "=501&"
				+ RequestsMonitor.ATTRIBUTE_NAME_TRACE_FLAG + "=true&"
				+ RequestsMonitor.ATTRIBUTE_NAME_ACTION + "="+RequestsMonitor.ATTRIBUTE_VALUE_ACTION_CLEAR);
		
		// When
		Map<String,String> params = requestsMonitor.getParameters(request);
		
		// Then
		assertEquals("201", params.get(RequestsMonitor.ATTRIBUTE_NAME_DURATION_THRESHOLD));
		assertEquals("301", params.get(RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE));
		assertEquals("401", params.get(RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE));
		assertEquals("501", params.get(RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE));
		assertEquals("true", params.get(RequestsMonitor.ATTRIBUTE_NAME_TRACE_FLAG));
		assertEquals(RequestsMonitor.ATTRIBUTE_VALUE_ACTION_CLEAR, params.get(RequestsMonitor.ATTRIBUTE_NAME_ACTION));
	}

	@Test
	public void testGetParameters2() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getQueryString()).thenReturn(
				RequestsMonitor.ATTRIBUTE_NAME_DURATION_THRESHOLD + "=&"
				+ RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE + "=1&"
				+ RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE + "&"
				+ RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE + "&"
				+ RequestsMonitor.ATTRIBUTE_NAME_TRACE_FLAG + "=" + "&"
				+ RequestsMonitor.ATTRIBUTE_NAME_ACTION + "");
		
		// When
		Map<String,String> params = requestsMonitor.getParameters(request);
		
		// Then
		assertNull(params.get(RequestsMonitor.ATTRIBUTE_NAME_DURATION_THRESHOLD));
		assertEquals("1", params.get(RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE));
		assertNull(params.get(RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE));
		assertNull(params.get(RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE));
		assertNull(params.get(RequestsMonitor.ATTRIBUTE_NAME_TRACE_FLAG));
		assertNull(params.get(RequestsMonitor.ATTRIBUTE_NAME_ACTION));
	}
	
	@Test
	public void testAction1() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
		CircularStack logLines = requestsMonitor.logLines = mock(CircularStack.class);
		TopRequests topRequests = requestsMonitor.topRequests = mock(TopRequests.class);
		LongestRequests longestRequests = requestsMonitor.longestRequests = mock(LongestRequests.class);
		
		requestsMonitor.durationThreshold = 200;
		requestsMonitor.logSize = 300;
		requestsMonitor.topTenSize = 400;
		requestsMonitor.longestSize = 500;
		requestsMonitor.traceFlag = true;
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RequestsMonitor.ATTRIBUTE_NAME_DURATION_THRESHOLD, "201");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE, "301");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE, "401");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE, "501");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_TRACE_FLAG, "true");
		
		// When
		requestsMonitor.action(params);
		
		// Then
		assertEquals(201, requestsMonitor.durationThreshold);
		assertEquals(301, requestsMonitor.logSize);
		assertNotEquals(logLines, requestsMonitor.logLines);
		assertEquals(401, requestsMonitor.topTenSize);
		assertNotEquals(topRequests, requestsMonitor.topRequests);
		assertEquals(501, requestsMonitor.longestSize);
		assertNotEquals(longestRequests, requestsMonitor.longestRequests);
		assertTrue(requestsMonitor.traceFlag);

	}

	@Test
	public void testAction2() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
		CircularStack logLines = requestsMonitor.logLines = mock(CircularStack.class);
		TopRequests topRequests = requestsMonitor.topRequests = mock(TopRequests.class);
		LongestRequests longestRequests = requestsMonitor.longestRequests = mock(LongestRequests.class);
		
		requestsMonitor.durationThreshold = 200;
		requestsMonitor.logSize = 300;
		requestsMonitor.topTenSize = 400;
		requestsMonitor.longestSize = 500;
		requestsMonitor.traceFlag = true;
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RequestsMonitor.ATTRIBUTE_NAME_DURATION_THRESHOLD, "200");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE, "300");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE, "400");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE, "500");
		params.put(RequestsMonitor.ATTRIBUTE_NAME_TRACE_FLAG, "false");
		
		// When
		requestsMonitor.action(params);
		
		// Then
		assertEquals(200, requestsMonitor.durationThreshold);
		assertEquals(300, requestsMonitor.logSize);
		assertEquals(logLines, requestsMonitor.logLines);
		assertEquals(400, requestsMonitor.topTenSize);
		assertEquals(topRequests, requestsMonitor.topRequests);
		assertEquals(500, requestsMonitor.longestSize);
		assertEquals(longestRequests, requestsMonitor.longestRequests);
		assertFalse(requestsMonitor.traceFlag);
	}

	@Test
	public void testActionStop() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		requestsMonitor.activated = true;
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RequestsMonitor.ATTRIBUTE_NAME_ACTION, RequestsMonitor.ATTRIBUTE_VALUE_ACTION_STOP);
		
		// When
		requestsMonitor.action(params);
		
		// Then
		assertFalse(requestsMonitor.activated);
	}

	@Test
	public void testActionStart() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		requestsMonitor.activated = false;
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RequestsMonitor.ATTRIBUTE_NAME_ACTION, RequestsMonitor.ATTRIBUTE_VALUE_ACTION_START);
		
		// When
		requestsMonitor.action(params);
		
		// Then
		assertTrue(requestsMonitor.activated);
	}

	@Test
	public void testActionClean() {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
		CircularStack logLines = requestsMonitor.logLines = mock(CircularStack.class);
		TopRequests topRequests = requestsMonitor.topRequests = mock(TopRequests.class);
		LongestRequests longestRequests = requestsMonitor.longestRequests = mock(LongestRequests.class);
		
		requestsMonitor.logSize = 300;
		requestsMonitor.topTenSize = 400;
		requestsMonitor.longestSize = 500;
		
		Map<String,String> params = new HashMap<String,String>();
		params.put(RequestsMonitor.ATTRIBUTE_NAME_ACTION, RequestsMonitor.ATTRIBUTE_VALUE_ACTION_CLEAR);
		
		// When
		requestsMonitor.action(params);
		
		// Then
		assertNotEquals(logLines, requestsMonitor.logLines);
		assertNotEquals(topRequests, requestsMonitor.topRequests);
		assertNotEquals(longestRequests, requestsMonitor.longestRequests);
	}

	@Test
	public void testActionReset() throws ServletException {
		
		/**
		 * 1: filter initialization
		 */

		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());

		InetAddress adrLocale = mock(InetAddress.class);
		doReturn(adrLocale).when(requestsMonitor).getLocalHost();
		when(adrLocale.getHostAddress()).thenReturn("10.11.12.13");
		when(adrLocale.getHostName()).thenReturn("hostname");
		
		FilterConfig filterConfig = mock(FilterConfig.class);
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		assertEquals(RequestsMonitor.DEFAULT_DURATION_THRESHOLD, requestsMonitor.durationThreshold);
		assertEquals(RequestsMonitor.DEFAULT_LOG_SIZE, requestsMonitor.logSize);
		assertEquals(RequestsMonitor.DEFAULT_TOP_TEN_SIZE, requestsMonitor.topTenSize);
		assertEquals(RequestsMonitor.DEFAULT_LONGEST_SIZE, requestsMonitor.longestSize);
		assertEquals("/monitor", requestsMonitor.reportingReqPath);
		assertFalse(requestsMonitor.traceFlag);
		assertEquals("10.11.12.13", requestsMonitor.ipAddress);
		assertEquals("hostname", requestsMonitor.hostname);
		
		/**
		 * 2: filter update
		 */

		requestsMonitor.durationThreshold = 1;
		requestsMonitor.logSize = 2;
		requestsMonitor.topTenSize = 3;
		requestsMonitor.longestSize = 4;
		requestsMonitor.reportingReqPath = "test";
		requestsMonitor.traceFlag = true;
		requestsMonitor.ipAddress = "1.2.3.4";
		requestsMonitor.hostname = "test2";

		/**
		 * 3: filter reset
		 */
		
		// Given
		Map<String,String> params = new HashMap<String,String>();
		params.put(RequestsMonitor.ATTRIBUTE_NAME_ACTION, RequestsMonitor.ATTRIBUTE_VALUE_ACTION_RESET);
		
		// When
		requestsMonitor.action(params);
		
		// Then
		assertEquals(RequestsMonitor.DEFAULT_DURATION_THRESHOLD, requestsMonitor.durationThreshold);
		assertEquals(RequestsMonitor.DEFAULT_LOG_SIZE, requestsMonitor.logSize);
		assertEquals(RequestsMonitor.DEFAULT_TOP_TEN_SIZE, requestsMonitor.topTenSize);
		assertEquals(RequestsMonitor.DEFAULT_LONGEST_SIZE, requestsMonitor.longestSize);
		assertEquals("/monitor", requestsMonitor.reportingReqPath);
		assertFalse(requestsMonitor.traceFlag);
		assertEquals("10.11.12.13", requestsMonitor.ipAddress);
		assertEquals("hostname", requestsMonitor.hostname);
	}
	
	@Test
	public void testDoFilterActivated() throws IOException, ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());
		requestsMonitor.activated = true;
		requestsMonitor.reportingReqPath = "/monitoring";
		requestsMonitor.durationThreshold = -99999999;
		
		when(requestsMonitor.getTime()).thenAnswer(new Answer<Long>() {
			private long time = 0;
			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				time += 500;
				return time;
			}
		});
		
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		
		HttpServletRequest httpRequest1 = mock(HttpServletRequest.class);
		when(httpRequest1.getServletPath()).thenReturn("/test1");
		when(httpRequest1.getRequestURL()).thenReturn(new StringBuffer("http://request1.url"));
		when(httpRequest1.getQueryString()).thenReturn("query1");
		
		HttpServletRequest httpRequest2 = mock(HttpServletRequest.class);
		when(httpRequest2.getServletPath()).thenReturn("/test2");
		when(httpRequest2.getRequestURL()).thenReturn(new StringBuffer("http://request2.url"));
		when(httpRequest2.getQueryString()).thenReturn("query2");
		
		// When
		requestsMonitor.doFilter(httpRequest1, response, chain);
		requestsMonitor.doFilter(httpRequest2, response, chain);
		
		// Then
		verify(chain).doFilter(httpRequest1, response);
		verify(chain).doFilter(httpRequest2, response);
		
		List<Request> requests = requestsMonitor.logLines.getAllAscending();
		assertEquals("1970/01/01 01:00:00 - [ 1 / 1 ] - 500 ms - http://request1.url?query1", requests.get(0).toString());
		assertEquals("1970/01/01 01:00:01 - [ 2 / 2 ] - 500 ms - http://request2.url?query2", requests.get(1).toString());
		
		requests = requestsMonitor.topRequests.getAllAscending();
		assertEquals("1970/01/01 01:00:00 - [ 1 / 1 ] - 500 ms - http://request1.url?query1", requests.get(0).toString());
		assertEquals("1970/01/01 01:00:01 - [ 2 / 2 ] - 500 ms - http://request2.url?query2", requests.get(1).toString());
		
		requests = requestsMonitor.longestRequests.getAllDescending();
		assertEquals("1970/01/01 01:00:01 - [ 2 / 2 ] - 500 ms - http://request2.url?query2", requests.get(0).toString());
		assertEquals("1970/01/01 01:00:00 - [ 1 / 1 ] - 500 ms - http://request1.url?query1", requests.get(1).toString());
	}

	@Test
	public void testDoFilterDesactivated() throws IOException, ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());
		requestsMonitor.activated = false;
		requestsMonitor.reportingReqPath = "/monitoring";
		requestsMonitor.durationThreshold = -99999999;
		
		when(requestsMonitor.getTime()).thenAnswer(new Answer<Long>() {
			private long time = 0;
			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				time += 500;
				return time;
			}
		});
		
		HttpServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		
		HttpServletRequest httpRequest1 = mock(HttpServletRequest.class);
		when(httpRequest1.getServletPath()).thenReturn("/test1");
		when(httpRequest1.getRequestURL()).thenReturn(new StringBuffer("http://request1.url"));
		when(httpRequest1.getQueryString()).thenReturn("query1");
		
		HttpServletRequest httpRequest2 = mock(HttpServletRequest.class);
		when(httpRequest2.getServletPath()).thenReturn("/test2");
		when(httpRequest2.getRequestURL()).thenReturn(new StringBuffer("http://request2.url"));
		when(httpRequest2.getQueryString()).thenReturn("query2");
		
		// When
		requestsMonitor.doFilter(httpRequest1, response, chain);
		requestsMonitor.doFilter(httpRequest2, response, chain);
		
		// Then
		verify(chain).doFilter(httpRequest1, response);
		verify(chain).doFilter(httpRequest2, response);
		
		List<Request> requests = requestsMonitor.logLines.getAllAscending();
		assertEquals(0, requests.size());
		
		requests = requestsMonitor.topRequests.getAllAscending();
		assertEquals(0, requests.size());
		
		requests = requestsMonitor.longestRequests.getAllDescending();
		assertEquals(0, requests.size());
	}
	
	@Test
	public void testReportingBrut() throws IOException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());
		
		List<Request> lines = new ArrayList<Request>();
		Request r1 = new Request();
		lines.add(r1);
		r1.setStartTime(11000);
		r1.setElapsedTime(12);
		r1.setPathInfo("pathInfo1");
		r1.setQueryString("queryString1");
		r1.setRequestURL("requestURL1");
		r1.setServletPath("servletPath1");
		r1.setCountLongTimeRequests(1);
		r1.setCountAllRequest(5);
		Request r2 = new Request();
		lines.add(r2);
		r2.setStartTime(21000);
		r2.setElapsedTime(22);
		r2.setPathInfo("pathInfo2");
		r2.setQueryString("queryString2");
		r2.setRequestURL("requestURL2");
		r2.setServletPath("servletPath2");
		r2.setCountLongTimeRequests(2);
		r2.setCountAllRequest(10);
		
		requestsMonitor.logLines = mock(CircularStack.class);
		when(requestsMonitor.logLines.getAllAscending()).thenReturn(lines);
		
		requestsMonitor.topRequests = mock(TopRequests.class);
		when(requestsMonitor.topRequests.getAllDescending()).thenReturn(lines);
		
		requestsMonitor.longestRequests = mock(LongestRequests.class);
		when(requestsMonitor.longestRequests.getAllDescending()).thenReturn(lines);
		
		HttpServletResponse response = mock(HttpServletResponse.class);
		PrintWriter out = mock(PrintWriter.class);
		when(response.getWriter()).thenReturn(out);
		
		// When
		requestsMonitor.reportingBrut(response);
		
		// Then
		verify(response).setContentType("text/plain");
		verify(response).setHeader("Pragma", "no-cache");
		verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		verify(response).setDateHeader ("Expires", 0);
		
		verify(requestsMonitor.logLines).getAllAscending();
		verify(requestsMonitor.topRequests).getAllDescending();
		verify(requestsMonitor.longestRequests).getAllDescending();
		
		verify(out).println("IP address : " + requestsMonitor.ipAddress);
		verify(out).println("Hostname : " + requestsMonitor.hostname );
		verify(out).println("Duration threshold : " + requestsMonitor.durationThreshold );
		verify(out).println("Log in memory size : " + requestsMonitor.logSize + " lines");
		verify(out).println("Top requests by time : " + requestsMonitor.topTenSize + " lines");
		verify(out).println("Top requests by URL : " + requestsMonitor.longestSize + " lines");
		verify(out).println("Initialization date/time : " + requestsMonitor.initializationDate );
		verify(out).println("Total requests count     : " + requestsMonitor.countAllRequest);
		verify(out).println("Long time requests count : " + requestsMonitor.countLongTimeRequests );
		verify(out).println("Last longest requests : " );
		verify(out).println("1970/01/01 01:00:11 - [ 1 / 5 ] - 12 ms - requestURL1?queryString1");
		verify(out).println("1970/01/01 01:00:21 - [ 2 / 10 ] - 22 ms - requestURL2?queryString2");
		verify(out).println("Top requests by time : " );
		verify(out).println("Top requests by URL : " );
		verify(out, times(2)).println("1970/01/01 01:00:11 - 12 ms - requestURL1?queryString1");
		verify(out, times(2)).println("1970/01/01 01:00:21 - 22 ms - requestURL2?queryString2");
	}
	
}
