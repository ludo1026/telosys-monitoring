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

public class RequestsMonitor2Test {
	
	@Test
	public void initDefaults() throws ServletException, InterruptedException {
		System.out.println("Test - Begin");
		
		RequestsMonitor requestsMonitor = new RequestsMonitor();
		
		int nbRequestsBySender = 2000;
		int nbThreads = 10;
		int delayRequestSending = 10;
		int delayAction = 5;
		
		Counter counter = new Counter();
		Random random = new Random();
		
		CountDownLatch startSignal = new CountDownLatch(1);
	    CountDownLatch doneSignal = new CountDownLatch(nbThreads);
		
	    List<Thread> threads = new ArrayList<Thread>();
		List<SendRequest> sendRequests = new ArrayList<SendRequest>();
		for(int i=0; i<nbThreads; i++) {
			SendRequest sendRequest = 
					new SendRequest(startSignal, doneSignal, requestsMonitor, counter, random, nbRequestsBySender, delayRequestSending);
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
			randomActions(requestsMonitor);
		}
		
		doneSignal.await();
		
		System.out.println("Test - End");
	}
	
	public void randomActions(RequestsMonitor requestsMonitor) {
		System.out.println("Action : clear");
		requestsMonitor.action(getParams("action", "clear"));
	}
	
	private Map<String,String> getParams(String key, String name) {
		Map<String,String> params = new HashMap<String, String>();
		params.put(key, name);
		return params;
	}
	
}
