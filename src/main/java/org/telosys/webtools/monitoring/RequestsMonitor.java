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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.telosys.webtools.monitoring.bean.CircularStack;
import org.telosys.webtools.monitoring.bean.LongestRequests;
import org.telosys.webtools.monitoring.bean.Request;
import org.telosys.webtools.monitoring.bean.TopRequests;

/**
 * Servlet Filter for Http Requests Monitor
 */
public class RequestsMonitor implements Filter {

	/** Action */
	protected final static String ATTRIBUTE_NAME_ACTION = "action";
	/** Action : Clean all logs */
	protected final static String ATTRIBUTE_VALUE_ACTION_CLEAR = "clear";
	/** Action : Reset values as defined in the web.xml */
	protected final static String ATTRIBUTE_VALUE_ACTION_RESET = "reset";
	
	/** Execution time threshold */
	protected final static String ATTRIBUTE_NAME_DURATION_THRESHOLD = "duration";
	/** Number of last stored requests */
	protected final static String ATTRIBUTE_NAME_LOG_SIZE           = "log_size" ;
	/** Number of top longest requests */
	protected final static String ATTRIBUTE_NAME_BY_TIME_SIZE       = "by_time_size" ;
	/** Number of longest requests */
	protected final static String ATTRIBUTE_NAME_BY_URL_SIZE       = "by_url_size" ;
	/** Indicates if information are displayed in the output console of the server */
	protected final static String ATTRIBUTE_NAME_TRACE_FLAG         = "trace" ;
	
	/** Execution time threshold */
	protected final static int DEFAULT_DURATION_THRESHOLD  = 1000 ; // 1 second
	/** Number of last stored requests */
	protected final static int DEFAULT_LOG_SIZE            =  100 ;
	/** Number of top longest requests */
	protected final static int DEFAULT_TOP_TEN_SIZE        =  10 ;
	/** Number of longest requests */
	protected final static int DEFAULT_LONGEST_SIZE        =  10 ;
	
	/** Execution time threshold */
	protected int     durationThreshold     = DEFAULT_DURATION_THRESHOLD ;
	/** Number of last stored requests */
	protected int     logSize               = DEFAULT_LOG_SIZE ;
	/** Number of top longest requests */
	protected int     topTenSize            = DEFAULT_TOP_TEN_SIZE ;
	/** Number of longest requests */
	protected int     longestSize          = DEFAULT_LONGEST_SIZE ;

	/** URL path to the monitor reporting */
	protected String  reportingReqPath      = "/monitor" ;
	/** Indicates if information are displayed in the output console of the server */
	protected boolean traceFlag             = false ;
	
	/** Initialization date */
	protected String initializationDate     = "???" ;
	/** Count all requests */
	protected long   countAllRequest        = 0 ; 
	/** Count longest requests */
	protected long   countLongTimeRequests  = 0 ; 
	
	/** Last stored requests */
	protected CircularStack logLines = new CircularStack(DEFAULT_LOG_SIZE);
	/** Top longest requests */
	protected TopRequests topRequests = new TopRequests(DEFAULT_TOP_TEN_SIZE);
	/** Longest requests */
	protected LongestRequests longestRequests = new LongestRequests(DEFAULT_LONGEST_SIZE);
	
	/** IP address */
	protected String ipAddress;
	/** Host name */
	protected String hostname;
	
    /**
     * Default constructor. 
     */
    public RequestsMonitor() {
    }

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		
		//--- Parameter : duration threshold
		durationThreshold = parseInt( filterConfig.getInitParameter("duration"), DEFAULT_DURATION_THRESHOLD );

		//--- Parameter : memory log size 
		logSize = parseInt( filterConfig.getInitParameter("logsize"), DEFAULT_LOG_SIZE );
		logLines = new CircularStack(logSize);

		//--- Parameter : memory top ten size 
		topTenSize = parseInt( filterConfig.getInitParameter("toptensize"), DEFAULT_TOP_TEN_SIZE );
		topRequests = new TopRequests(topTenSize);

		//--- Parameter : memory longest requests size 
		longestSize = parseInt( filterConfig.getInitParameter("longestsize"), DEFAULT_LONGEST_SIZE );
		longestRequests = new LongestRequests(longestSize);

		//--- Parameter : status report URI
		String reportingParam = filterConfig.getInitParameter("reporting");
		if ( reportingParam != null ) {
			reportingReqPath = reportingParam ;
		}
		
		//--- Parameter : trace
		String traceParam = filterConfig.getInitParameter("trace");
		if ( traceParam != null ) {
			traceFlag = traceParam.equalsIgnoreCase("true") ;
		}
		
		initializationDate = format( new Date() );
		trace ("MONITOR INITIALIZED. durationThreshold = " + durationThreshold + ", reportingReqPath = " + reportingReqPath );
		
		InetAddress adrLocale = getLocalHost();
		if(adrLocale == null) {
			ipAddress = "unknown";
			hostname = "unknwon";
		} else {
			ipAddress = adrLocale.getHostAddress();
			hostname = adrLocale.getHostName();
		}
	}
	
	/**
	 * Return IP address and hostname.
	 * @return IP address and hostname
	 */
	protected InetAddress getLocalHost() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			return null;
		}
	}
	
	/**
	 * @return current time in milliseconds
	 *
	 */
	protected long getTime() {
		// Uses System.nanoTime() if necessary (precision ++)
		return System.currentTimeMillis();
	}

	/**
	 * Convert String value to Integer.
	 * @param s String value
	 * @param defaultValue Default Integer value if the conversion fails
	 * @return Integer value
	 */
	protected int parseInt(String s, int defaultValue) {
		int v = defaultValue ;
		if ( s != null ) {
			try {
				v = Integer.parseInt( s ) ;
			} catch (NumberFormatException e) {
				v = defaultValue ;
			}
		}
		return v ;
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request ;
		
		trace ("REQUEST RECEIVED : " + httpRequest.getRequestURL() );
		trace ("getServletPath : " + httpRequest.getServletPath() + " getPathInfo : " + httpRequest.getPathInfo() );

		String pathInfo = httpRequest.getServletPath() ;
		if( pathInfo != null && pathInfo.startsWith(reportingReqPath) ) {
			// Monitoring
			dispatch( httpRequest, (HttpServletResponse) response );
		}
		else {
			// Standard "doFilter" method
			doFilterStandard(httpRequest, response, chain);
		}
	}
	
	/**
	 * Standard "doFilter" method with HTTP request statistics.
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param chain Filter chain
	 * @throws IOException Error
	 * @throws ServletException Error
	 */
	protected void doFilterStandard(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request ;
		incrementCountAllRequest();
		final long startTime = getTime();
		try {
			//--- Chain (nothing to stop here)
			chain.doFilter(request, response);
			
		} finally {
			final long elapsedTime = getTime() - startTime;
			if ( elapsedTime > durationThreshold ) {
				incrementCountLongTimeRequests();
				logRequest(httpRequest, startTime, elapsedTime);
			}
		}
	}
	
	/**
	 * Increment count all requests.
	 */
	protected synchronized void incrementCountAllRequest() {
		countAllRequest++;
	}

	/**
	 * Increment count all long time requests.
	 */
	protected synchronized void incrementCountLongTimeRequests() {
		countLongTimeRequests++;
	}

	/**
	 * Create Request object and stores this request.
	 * @param httpRequest HTTP request
	 * @param startTime Start date
	 * @param elapsedTime Execution time
	 */
	protected final void logRequest(HttpServletRequest httpRequest, long startTime, long elapsedTime ) {
		Request request = createRequest(httpRequest, startTime, elapsedTime);
		
		this.logLines.add(request);
		this.topRequests.add(request);
		this.longestRequests.add(request);
		
		trace(request);
	}

	/**
	 * Create request
	 * @param httpRequest HTTP request
	 * @param startTime Start date
	 * @param elapsedTime Request execution time
	 * @return
	 */
	protected Request createRequest(HttpServletRequest httpRequest, long startTime, long elapsedTime) {
		Request request = new Request();
		request.setElapsedTime(elapsedTime);
		request.setStartTime(startTime);
		request.setPathInfo(httpRequest.getPathInfo());
		request.setQueryString(httpRequest.getQueryString());
		request.setRequestURL(httpRequest.getRequestURL().toString());
		request.setServletPath(httpRequest.getServletPath());
		request.setCountAllRequest(countAllRequest);
		request.setCountLongTimeRequests(countLongTimeRequests);
		return request;
	}
	
	/**
	 * Command for reporting.
	 */
	protected void dispatch(HttpServletRequest request, HttpServletResponse response) {
		Map<String,String> params = getParameters(request);
		action(params);
		reporting(response);
	}
	
	/**
	 * Parse URL query string to get parameters.
	 * @param request Request
	 * @return Map of parameters
	 */
	protected Map<String, String> getParameters(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		
		String query = request.getQueryString();
		if(query == null) {
			return params;
		}
		
		String[] querySplitteds = query.split("&");
		for(String querySplitted : querySplitteds) {
			int posEquals = querySplitted.indexOf('=');
			if(posEquals == -1 || posEquals + 1 >= querySplitted.length()) {
				continue;
			}
			String key = querySplitted.substring(0, posEquals);
			String value = querySplitted.substring(posEquals + 1);
			params.put(key, value);
		}
		
		return params;
	}
	
	/**
	 * Actions on monitoring.
	 * @param params Parameters
	 */
	protected void action(Map<String,String> params) {
		
		//--- Parameter : clean all logs
		if(params.get(ATTRIBUTE_NAME_ACTION) != null) {
			if(ATTRIBUTE_VALUE_ACTION_CLEAR.equals(params.get(ATTRIBUTE_NAME_ACTION))) {
				logLines = new CircularStack(logLines, logSize);
				topRequests = new TopRequests(topRequests, topTenSize);
				longestRequests = new LongestRequests(longestRequests, longestSize);
			}
			if(ATTRIBUTE_VALUE_ACTION_RESET.equals(params.get(ATTRIBUTE_NAME_ACTION))) {
				// reset
			}
		}
		
		//--- Parameter : request duration threshold
		if(params.get(ATTRIBUTE_NAME_DURATION_THRESHOLD) != null) {
			durationThreshold = parseInt( params.get(ATTRIBUTE_NAME_DURATION_THRESHOLD), durationThreshold );
		}
		
		//--- Parameter : memory log size 
		if(params.get(ATTRIBUTE_NAME_LOG_SIZE) != null) {
			int logSizeNew = parseInt( params.get(ATTRIBUTE_NAME_LOG_SIZE), logSize );
			if(logSizeNew != logSize) {
				this.logSize = logSizeNew;
				logLines = new CircularStack(logLines, logSize);
			}
		}

		//--- Parameter : memory top ten size 
		if(params.get(ATTRIBUTE_NAME_BY_TIME_SIZE) != null) {
			int topTenSizeNew = parseInt( params.get(ATTRIBUTE_NAME_BY_TIME_SIZE), topTenSize );
			if(topTenSizeNew != topTenSize) {
				this.topTenSize = topTenSizeNew;
				topRequests = new TopRequests(topTenSize);
			}
		}

		//--- Parameter : memory longest requests size 
		if(params.get(ATTRIBUTE_NAME_BY_URL_SIZE) != null) {
			int longestSizeNew = parseInt( params.get(ATTRIBUTE_NAME_BY_URL_SIZE), longestSize );
			if(longestSizeNew != longestSize) {
				this.longestSize = longestSizeNew;
				longestRequests = new LongestRequests(longestRequests, longestSize);
			}
		}
		
		//--- Parameter : trace
		if(params.get(ATTRIBUTE_NAME_TRACE_FLAG) != null) {
			String traceParam = params.get(ATTRIBUTE_NAME_TRACE_FLAG);
			traceFlag = "true".equalsIgnoreCase(traceParam) ;
		}
	}
	
	/**
	 * Reports the current status in plain text
	 * @param response HTTP response
	 */
	protected final void reporting (HttpServletResponse response) {
		response.setContentType("text/plain");
		
		//--- Prevent caching
		response.setHeader("Pragma", "no-cache"); // Set standard HTTP/1.0 no-cache header.
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // Set standard HTTP/1.1 no-cache header.
		response.setDateHeader ("Expires", 0); // Prevents caching on proxies
		
		final String date = format( new Date() ) ;
		PrintWriter out;
		try {
			out = response.getWriter();

			out.println("Requests monitoring status (" + date + ") ");
			out.println("IP address : " + ipAddress);
			out.println("Hostname : " + hostname );
			out.println(" ");
			
			out.println("Duration threshold : " + durationThreshold );
			out.println("Log in memory size : " + logSize + " lines" );	
			out.println("Top longest requests in memory size : " + topTenSize + " lines" );	
			out.println(" ");
			
			out.println("Initialization date/time : " + initializationDate );
			out.println("Total requests count     : " + countAllRequest);
			out.println("Long time requests count : " + countLongTimeRequests );
			out.println(" ");
			
			List<Request> lines = logLines.getAllAscending(); 
			out.println("Last longest requests : " );
			for ( Request line : lines ) {
				out.println(line.toString());
			}
			
			List<Request> requests = topRequests.getAllDescending(); 
			out.println(" ");
			out.println("Top longest requests : " );
			for ( Request request : requests ) {
				out.println(request.toStringWithoutCounting());
			}
			
			requests = longestRequests.getAllDescending(); 
			out.println(" ");
			out.println("Longest requests : " );
			for ( Request request : requests ) {
				out.println(request.toStringWithoutCounting());
			}
			
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("RequestMonitor error : cannot get writer");
		}
	}

	/**
	 * Log the request in the output console.
	 * @param request Request.
	 */
    protected final void trace(Request request) {
    	if ( traceFlag ) {
    		trace( "Logging line : " + request);
    	}
    }

    /**
     * Log the message in the output console.
     * @param msg Message
     */
    protected final void trace(String msg) {
    	if ( traceFlag ) {
    		System.out.println("[TRACE] : " + msg );
    	}    	
    }
    
    /**
     * Convert Date to String value.
     * @param date Date
     * @return String value
     */
	protected final String format ( Date date ) {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return dateFormat.format( date ) ;
	}
	
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

}
