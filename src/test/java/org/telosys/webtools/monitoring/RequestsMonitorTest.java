package org.telosys.webtools.monitoring;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
		RequestsMonitor requestsMonitor = new RequestsMonitor();

		FilterConfig filterConfig = mock(FilterConfig.class);
		
		// When
		requestsMonitor.init(filterConfig);
		
		// Then
		requestsMonitor.durationThreshold = RequestsMonitor.DEFAULT_DURATION_THRESHOLD;
		requestsMonitor.logSize = RequestsMonitor.DEFAULT_LOG_SIZE;
		requestsMonitor.reportingReqPath = "/monitor";
		requestsMonitor.traceFlag = false;
	}
	
	@Test
	public void initCustomized() throws ServletException {
		// Given
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
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
	
}
