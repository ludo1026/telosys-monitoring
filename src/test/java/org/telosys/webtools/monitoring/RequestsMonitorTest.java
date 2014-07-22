package org.telosys.webtools.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.telosys.webtools.monitoring.bean.CircularStack;
import org.telosys.webtools.monitoring.bean.Request;

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
		assertEquals(RequestsMonitor.DEFAULT_LONGUEST_SIZE, requestsMonitor.longuestSize);
		assertEquals("/monitor", requestsMonitor.reportingReqPath);
		assertFalse(requestsMonitor.traceFlag);
		assertEquals("10.11.12.13", requestsMonitor.ipAddress);
		assertEquals("hostname", requestsMonitor.hostname);
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
		when(filterConfig.getInitParameter("longuestsize")).thenReturn("500");
		when(filterConfig.getInitParameter("reporting")).thenReturn("/monitoring2");
		when(filterConfig.getInitParameter("trace")).thenReturn("true");
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		assertEquals(200, requestsMonitor.durationThreshold);
		assertEquals(300, requestsMonitor.logSize);
		assertEquals(400, requestsMonitor.topTenSize);
		assertEquals(500, requestsMonitor.longuestSize);
		assertEquals("/monitoring2", requestsMonitor.reportingReqPath);
		assertTrue(requestsMonitor.traceFlag);
		assertEquals("10.11.12.13", requestsMonitor.ipAddress);
		assertEquals("hostname", requestsMonitor.hostname);
	}
	
	@Test
	public void testDoFilter() throws IOException, ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());
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
		
		List<Request> requests = requestsMonitor.logLines.getAllAscendant();
		assertEquals("1970/01/01 01:00:00 - 500 ms - http://request1.url?query1", requests.get(0).toString());
		assertEquals("1970/01/01 01:00:01 - 500 ms - http://request2.url?query2", requests.get(1).toString());
		
		requests = requestsMonitor.topRequests.getAllAscending();
		assertEquals("1970/01/01 01:00:00 - 500 ms - http://request1.url?query1", requests.get(0).toString());
		assertEquals("1970/01/01 01:00:01 - 500 ms - http://request2.url?query2", requests.get(1).toString());
		
		requests = requestsMonitor.longuestRequests.getAllDescendants();
		assertEquals("1970/01/01 01:00:01 - 500 ms - http://request2.url?query2", requests.get(0).toString());
		assertEquals("1970/01/01 01:00:00 - 500 ms - http://request1.url?query1", requests.get(1).toString());
	}
	
	@Test
	public void testReporting() throws IOException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());
		
		requestsMonitor.logLines = mock(CircularStack.class);
		List<Request> lines = new ArrayList<Request>();
		when(requestsMonitor.logLines.getAllAscendant()).thenReturn(lines);
		Request r1 = new Request();
		lines.add(r1);
		r1.setStartTime(11000);
		r1.setElapsedTime(12);
		r1.setPathInfo("pathInfo1");
		r1.setQueryString("queryString1");
		r1.setRequestURL("requestURL1");
		r1.setServletPath("servletPath1");
		Request r2 = new Request();
		lines.add(r2);
		r2.setStartTime(21000);
		r2.setElapsedTime(22);
		r2.setPathInfo("pathInfo2");
		r2.setQueryString("queryString2");
		r2.setRequestURL("requestURL2");
		r2.setServletPath("servletPath2");
		
		HttpServletResponse response = mock(HttpServletResponse.class);
		PrintWriter out = mock(PrintWriter.class);
		when(response.getWriter()).thenReturn(out);
		
		// When
		requestsMonitor.reporting(response);
		
		// Then
		verify(response).setContentType("text/plain");
		verify(response).setHeader("Pragma", "no-cache");
		verify(response).setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		verify(response).setDateHeader ("Expires", 0);
		
		verify(out).println("IP address : " + requestsMonitor.ipAddress);
		verify(out).println("Hostname : " + requestsMonitor.hostname );
		verify(out).println("Duration threshold : " + requestsMonitor.durationThreshold );
		verify(out).println("Log in memory size : " + requestsMonitor.logSize + " lines");
		verify(out).println("Top longuest requests in memory size : " + requestsMonitor.topTenSize + " lines");
		verify(out).println("Initialization date/time : " + requestsMonitor.initializationDate );
		verify(out).println("Total requests count     : " + requestsMonitor.countAllRequest);
		verify(out).println("Long time requests count : " + requestsMonitor.countLongTimeRequests );
		verify(out).println("" + lines.size() + " last long time requests : " );
		verify(out).println("1970/01/01 01:00:11 - 12 ms - requestURL1?queryString1");
		verify(out).println("1970/01/01 01:00:21 - 22 ms - requestURL2?queryString2");
		verify(out).println("Top " + requestsMonitor.topRequests.getAllDescending().size() + " of last long time requests : " );
		verify(out).println(requestsMonitor.topRequests.getAllDescending().size() + " longuest requests : " );
	}
	
}
