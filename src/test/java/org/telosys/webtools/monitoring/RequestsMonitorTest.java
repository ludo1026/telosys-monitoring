package org.telosys.webtools.monitoring;

import static junit.framework.Assert.assertEquals;
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

public class RequestsMonitorTest {
	
	@Test
	public void initDefaults() throws ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());

		InetAddress adrLocale = mock(InetAddress.class);
		when(requestsMonitor.getLocalHost()).thenReturn(adrLocale);
		when(adrLocale.getAddress()).thenReturn("10.11.12.13".getBytes());
		when(adrLocale.getHostName()).thenReturn("hostname");
		
		FilterConfig filterConfig = mock(FilterConfig.class);
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		requestsMonitor.durationThreshold = RequestsMonitor.DEFAULT_DURATION_THRESHOLD;
		requestsMonitor.logSize = RequestsMonitor.DEFAULT_LOG_SIZE;
		requestsMonitor.reportingReqPath = "/monitor";
		requestsMonitor.traceFlag = false;
		requestsMonitor.ipAddress = "10.11.12.13";
		requestsMonitor.hostname = "hostname";
	}
	
	@Test
	public void initCustomized() throws ServletException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());

		InetAddress adrLocale = mock(InetAddress.class);
		when(requestsMonitor.getLocalHost()).thenReturn(adrLocale);
		when(adrLocale.getAddress()).thenReturn("10.11.12.13".getBytes());
		when(adrLocale.getHostName()).thenReturn("hostname");
		
		FilterConfig filterConfig = mock(FilterConfig.class);
		when(filterConfig.getInitParameter("duration")).thenReturn("200");
		when(filterConfig.getInitParameter("logsize")).thenReturn("300");
		when(filterConfig.getInitParameter("reporting")).thenReturn("monitoring2");
		when(filterConfig.getInitParameter("trace")).thenReturn("true");
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		requestsMonitor.durationThreshold = 200;
		requestsMonitor.logSize = 300;
		requestsMonitor.reportingReqPath = "/monitor2";
		requestsMonitor.traceFlag = true;
		requestsMonitor.ipAddress = "10.11.12.13";
		requestsMonitor.hostname = "hostname";
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
		
		List<String> lines = requestsMonitor.logLines.getAllAscendant();
		assertEquals("1970/01/01 01:00:00 [ 1 / 1 ] 500 ms : http://request1.url?query1", lines.get(0));
		assertEquals("1970/01/01 01:00:01 [ 2 / 2 ] 500 ms : http://request2.url?query2", lines.get(1));
	}
	
	@Test
	public void testReporting() throws IOException {
		// Given
		RequestsMonitor requestsMonitor = spy(new RequestsMonitor());
		
		requestsMonitor.logLines = mock(CircularStack.class);
		List<String> lines = new ArrayList<String>();
		when(requestsMonitor.logLines.getAllAscendant()).thenReturn(lines);
		lines.add("line 1");
		lines.add("line 2");
		
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
		verify(out).println("Initialization date/time : " + requestsMonitor.initializationDate );
		verify(out).println("Total requests count     : " + requestsMonitor.countAllRequest);
		verify(out).println("Long time requests count : " + requestsMonitor.countLongTimeRequests );
		verify(out).println("" + lines.size() + " last long time requests : " );
		verify(out).println("line 1");
		verify(out).println("line 2");
	}
	
}
