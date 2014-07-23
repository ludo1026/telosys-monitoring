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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.junit.Test;
import org.telosys.webtools.monitoring.bean.CircularStack;
import org.telosys.webtools.monitoring.bean.LongestRequests;
import org.telosys.webtools.monitoring.bean.TopRequests;

public class RequestsMonitorMultiThreadTest {
	
	@Test
	public void initDefaults() throws ServletException, InterruptedException {
		System.out.println("Test - Begin");
		
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		requestsMonitor.durationThreshold = -999;
		requestsMonitor.logSize = 100000;
		requestsMonitor.logLines = new CircularStack(requestsMonitor.logSize);
		requestsMonitor.topTenSize = 100000;
		requestsMonitor.topRequests = new TopRequests(requestsMonitor.topTenSize);
		requestsMonitor.longestSize = 100000;
		requestsMonitor.longestRequests = new LongestRequests(requestsMonitor.longestSize);
		requestsMonitor.traceFlag = false;
		
		int nbRequestsBySender = 50;
		int nbThreads = 1000;
		int delayRequestSending = 0;
		int delayAction = 2;
		
		Counter counter = new Counter();
		Random random = new Random();
		
		CountDownLatch startSignal = new CountDownLatch(1);
	    CountDownLatch doneSignal = new CountDownLatch(nbThreads);
		
	    List<Thread> threads = new ArrayList<Thread>();
		List<SendRequest> sendRequests = new ArrayList<SendRequest>();
		for(int i=0; i<nbThreads; i++) {
			SendRequest sendRequest = 
					new SendRequest(i, startSignal, doneSignal, requestsMonitor, counter, random, nbRequestsBySender, delayRequestSending);
			sendRequests.add(sendRequest);
			Thread thread = new Thread(sendRequest);
			threads.add(thread);
		}
		
		// Run
		for(Thread thread : threads) {
			thread.start();
		}
		startSignal.countDown();
		
		// Random actions
		while(doneSignal.getCount() > 0) {
			Thread.sleep(delayAction);
			randomActions(requestsMonitor, random);
		}
		doneSignal.await();
		
		System.out.println("Test - End");
		
		System.out.println("countAllRequest: " + requestsMonitor.countAllRequest);
		System.out.println("countLongTimeRequests: " + requestsMonitor.countLongTimeRequests);
		System.out.println("logLines: " + requestsMonitor.logLines.getAllAscending().size());
		System.out.println("by_time: " + requestsMonitor.topRequests.getAllDescending().size());
		System.out.println("by_url : " + requestsMonitor.longestRequests.getAllDescending().size());
	}
	
	public void randomActions(RequestsMonitor requestsMonitor, Random random) {
		if(random.nextInt(50) == 25) {
			// reset
			requestsMonitor.action(getParams("action", "reset"));
		}
		if(random.nextInt(50) == 25) {
			// clear
			requestsMonitor.action(getParams("action", "clear"));
		}
		// log size
		requestsMonitor.action(getParams(RequestsMonitor.ATTRIBUTE_NAME_LOG_SIZE, ""+(random.nextInt(150)+1)));
		// by time size
		requestsMonitor.action(getParams(RequestsMonitor.ATTRIBUTE_NAME_BY_TIME_SIZE, ""+(random.nextInt(150)+1)));
		// by url size
		requestsMonitor.action(getParams(RequestsMonitor.ATTRIBUTE_NAME_BY_URL_SIZE, ""+(random.nextInt(150)+1)));
	}
	
	private Map<String,String> getParams(String key, String name) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(key, name);
		return params;
	}
	
}
