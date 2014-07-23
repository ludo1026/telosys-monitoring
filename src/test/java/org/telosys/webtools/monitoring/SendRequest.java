package org.telosys.webtools.monitoring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.telosys.webtools.monitoring.bean.Request;

public class SendRequest implements Runnable {
	
	private final CountDownLatch startSignal;
	private final CountDownLatch doneSignal;
	
	private final int numSender;
	private final RequestsMonitor requestsMonitor;
	private final Counter counter;
	private final Random random;
	private final int nbRequests;
	private final int delay;
	private List<Request> requests = new ArrayList<Request>();
	
	public SendRequest(int numSender, CountDownLatch startSignal, CountDownLatch doneSignal, 
						RequestsMonitor requestsMonitor, Counter counter, Random random, int nbRequests, int delay) {
		this.numSender = numSender;
		this.startSignal = startSignal;
	    this.doneSignal = doneSignal;
	    this.requestsMonitor = requestsMonitor;
		this.counter = counter;
		this.random = random;
		this.nbRequests = nbRequests;
		this.delay = delay;
	}
	
	@Override
	public void run() {
		System.out.println(numSender + " - run - wait");
		try {
			startSignal.await();
		} catch (InterruptedException e) {
			return;
		}
		System.out.println(numSender + " - run - go");
		for(int i=0; i<nbRequests; i++) {
			if(delay > 0) {
				int sleep = random.nextInt(delay);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					return;
				}
			}
			HttpServletRequest httpRequest = nextHttpServletRequest();
			Request request = requestsMonitor.createRequest(httpRequest, new Date().getTime(), 0);
			requests.add(request);
			
			// doFilter
			try {
				// System.out.println(numSender + " - doFilter - "+request);
				requestsMonitor.doFilter(httpRequest, mock(HttpServletResponse.class), mock(FilterChain.class));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ServletException e) {
				throw new RuntimeException(e);
			}
		}
		System.out.println(numSender + " - run - end");
		doneSignal.countDown();
	}
	
	public HttpServletRequest nextHttpServletRequest() {
		int i = counter.increment();
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getServletPath()).thenReturn("/test"+i);
		when(httpRequest.getRequestURL()).thenReturn(new StringBuffer("http://request"+i));
		when(httpRequest.getQueryString()).thenReturn("query"+i);
		return httpRequest;
	}
	
}