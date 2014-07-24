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

	/** Numéro de version */
	protected final static String VERSION = "v1.8";
	
	/** Action */
	protected final static String ATTRIBUTE_NAME_ACTION = "action";
	/** Action : Clean all logs */
	protected final static String ATTRIBUTE_VALUE_ACTION_CLEAR = "clear";
	/** Action : Reset values as defined in the web.xml */
	protected final static String ATTRIBUTE_VALUE_ACTION_RESET = "reset";
	/** Action : Stop monitoring */
	protected final static String ATTRIBUTE_VALUE_ACTION_STOP = "stop";
	/** Action : Start monitoring */
	protected final static String ATTRIBUTE_VALUE_ACTION_START = "start";
	
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
	/** Indicates if the filter is activated */
	protected boolean activated             = true ;
	
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
	
	/** Init values from web.xml configuration. */
	protected InitValues initValues;
	
	/**
	 * Init values.
	 */
	protected static class InitValues {
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
		/** Indicates if the filter is activated */
		protected boolean activated             = true ;
		
		/** Count all requests */
		protected long   countAllRequest        = 0 ; 
		/** Count longest requests */
		protected long   countLongTimeRequests  = 0 ; 
	}
	
    /**
     * Default constructor. 
     */
    public RequestsMonitor() {
    }

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		initValues(filterConfig);
		reset();
		if(reportingReqPath == null || "".equals(reportingReqPath.trim())) {
			throw new ServletException("URL path of the report page is not defined. Please verify in the web.xml the value of the init-param 'reporting' which must not be empty.");
		}
	}
	
	/**
	 * Save init values from web.xml configuration.
	 * @param filterConfig Filter configuration
	 * @throws ServletException Error
	 */
	protected void initValues(FilterConfig filterConfig) throws ServletException {
		this.initValues = new InitValues();

		//--- Parameter : activated
		String activatedParam = filterConfig.getInitParameter("activated");
		if ( activatedParam != null ) {
			this.initValues.activated = activatedParam.equalsIgnoreCase("true") ;
		}
		
		//--- Parameter : duration threshold
		this.initValues.durationThreshold = parseInt( filterConfig.getInitParameter("duration"), DEFAULT_DURATION_THRESHOLD );

		//--- Parameter : memory log size 
		this.initValues.logSize = parseInt( filterConfig.getInitParameter("logsize"), DEFAULT_LOG_SIZE );
		
		//--- Parameter : memory top ten size 
		this.initValues.topTenSize = parseInt( filterConfig.getInitParameter("toptensize"), DEFAULT_TOP_TEN_SIZE );
		
		//--- Parameter : memory longest requests size 
		this.initValues.longestSize = parseInt( filterConfig.getInitParameter("longestsize"), DEFAULT_LONGEST_SIZE );
		
		//--- Parameter : status report URI
		String reportingParam = filterConfig.getInitParameter("reporting");
		if ( reportingParam != null ) {
			this.initValues.reportingReqPath = reportingParam ;
		}
		
		//--- Parameter : trace
		String traceParam = filterConfig.getInitParameter("trace");
		if ( traceParam != null ) {
			this.initValues.traceFlag = traceParam.equalsIgnoreCase("true") ;
		}
		
		InetAddress adrLocale = getLocalHost();
		if(adrLocale == null) {
			ipAddress = "unknown";
			hostname = "unknwon";
		} else {
			ipAddress = adrLocale.getHostAddress();
			hostname = adrLocale.getHostName();
		}
	}
	
	protected void reset() {
		//--- Parameter : duration threshold
		durationThreshold = this.initValues.durationThreshold;

		//--- Parameter : activated
		activated = this.initValues.activated;
		
		//--- Parameter : memory log size 
		logSize = this.initValues.logSize;
		logLines = new CircularStack(logSize);

		//--- Parameter : memory top ten size 
		topTenSize = this.initValues.topTenSize;
		topRequests = new TopRequests(topTenSize);

		//--- Parameter : memory longest requests size 
		longestSize = this.initValues.longestSize;
		longestRequests = new LongestRequests(longestSize);

		//--- Parameter : status report URI
		reportingReqPath = this.initValues.reportingReqPath;
		
		//--- Parameter : trace
		traceFlag = this.initValues.traceFlag;
		
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
	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// Check if this request is to display the report page
		boolean isRequestForReportPage = false;
		try {
			isRequestForReportPage = isRequestForReportPage(servletRequest);
		} catch(Throwable throwable) {
			manageError(throwable);
		}
		
		if( isRequestForReportPage ) {
			// Report page
			dispatch( (HttpServletRequest) servletRequest, (HttpServletResponse) response );
		}
		else {
			
			// Check if monitoring is activated
			if(activated == false) {
				
				// "doFilter" method without request monitoring
				doFilterWithoutMonitoring(servletRequest, response, chain);
				
			} else {
				
				// "doFilter" method with request monitoring
				doFilterWithMonitoring(servletRequest, response, chain);
				
			}
		}
	}

	/**
	 * "doFilter" method without request monitoring.
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param chain Filter chain
	 * @throws IOException Error
	 * @throws ServletException Error
	 */
	protected void doFilterWithoutMonitoring(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		//--- Chain (nothing to stop here)
		chain.doFilter(request, response);
		
	}
	
	/**
	 * "doFilter" method with request monitoring.
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param chain Filter chain
	 * @throws IOException Error
	 * @throws ServletException Error
	 */
	protected void doFilterWithMonitoring(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		long startTime = 0;

		try {
			incrementCountAllRequest();
			startTime = getTime();
		} catch(Throwable throwable) {
			manageError(throwable);
		}
		
		try {
			
			//--- Chain (nothing to stop here)
			chain.doFilter(request, response);
			
		} finally {
			try {
				final long elapsedTime = getTime() - startTime;
				if ( elapsedTime > durationThreshold ) {
					incrementCountLongTimeRequests();
					logRequest(request, startTime, elapsedTime);
				}
			} catch(Throwable throwable) {
				manageError(throwable);
			}
		}
	}
	
	/**
	 * Manage the exception.
	 * @param throwable Error
	 */
	protected void manageError(Throwable throwable) {
		if(throwable == null) {
			return;
		}
		try {
			System.err.println("Error during monitoring : "+throwable.getClass().getName()+" : "+throwable.getMessage());
			throwable.printStackTrace(System.err);
		} catch(Throwable throwable2) {
			// ignore this error
		}
	}

	/**
	 * Indicates if the request is to access to the report page.
	 * @param httpRequest HTTP request.
	 * @return boolean.
	 */
	protected boolean isRequestForReportPage(ServletRequest servletRequest) {
		if(!(servletRequest instanceof HttpServletRequest)) {
			return false;
		}
		if(reportingReqPath == null || "".equals(reportingReqPath.trim())) {
			return false;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
		String pathInfo = httpRequest.getServletPath();
		return (pathInfo != null && pathInfo.startsWith(reportingReqPath));
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
	protected final void logRequest(ServletRequest servletRequest, long startTime, long elapsedTime ) {
		Request request = createRequest(servletRequest, startTime, elapsedTime);
		
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
	protected Request createRequest(ServletRequest servletRequest, long startTime, long elapsedTime) {
		Request request = new Request();
		request.setElapsedTime(elapsedTime);
		request.setStartTime(startTime);
		if(servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
			request.setPathInfo(httpServletRequest.getPathInfo());
			request.setQueryString(httpServletRequest.getQueryString());
			request.setRequestURL(httpServletRequest.getRequestURL().toString());
			request.setServletPath(httpServletRequest.getServletPath());
		} else {
			request.setPathInfo("");
			request.setQueryString("");
			request.setRequestURL("");
			request.setServletPath("");
		}
		request.setCountAllRequest(countAllRequest);
		request.setCountLongTimeRequests(countLongTimeRequests);
		return request;
	}
	
	/**
	 * Command for reporting.
	 */
	protected void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Map<String,String> params = getParameters(httpServletRequest);
		boolean isMakingAction = action(params);
		if(isMakingAction) {
			// Redirection to the default reporting url
			String redirectURL = httpServletRequest.getRequestURL().toString();
			try {
				httpServletResponse.sendRedirect(redirectURL);
			} catch (IOException e) {
				manageError(e);
			}
		} else {
			// Report page
			reporting(httpServletResponse);
		}
	}
	
	/**
	 * Parse URL query string to get parameters.
	 * @param request Request
	 * @return Map of parameters
	 */
	protected Map<String, String> getParameters(HttpServletRequest httpServletRequest) {
		Map<String, String> params = new HashMap<String, String>();
		
		String query = httpServletRequest.getQueryString();
		if(query == null) {
			return params;
		}
		
		String[] querySplitteds = query.split("&");
		for(String querySplitted : querySplitteds) {
			if(querySplitted == null || "".equals(querySplitted.trim())) {
				continue;
			}
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
	protected boolean action(Map<String,String> params) {
		
		boolean isMakingAction = false;
		
		//--- Parameter : clean all logs
		if(params.get(ATTRIBUTE_NAME_ACTION) != null) {
			if(ATTRIBUTE_VALUE_ACTION_CLEAR.equals(params.get(ATTRIBUTE_NAME_ACTION))) {
				isMakingAction = true;
				logLines = new CircularStack(this.logSize);
				topRequests = new TopRequests(this.topTenSize);
				longestRequests = new LongestRequests(this.longestSize);
			}
			if(ATTRIBUTE_VALUE_ACTION_RESET.equals(params.get(ATTRIBUTE_NAME_ACTION))) {
				isMakingAction = true;
				reset();
			}
			if(ATTRIBUTE_VALUE_ACTION_STOP.equals(params.get(ATTRIBUTE_NAME_ACTION))) {
				isMakingAction = true;
				this.activated = false;
			}
			if(ATTRIBUTE_VALUE_ACTION_START.equals(params.get(ATTRIBUTE_NAME_ACTION))) {
				isMakingAction = true;
				this.activated = true;
			}
		}
		
		//--- Parameter : request duration threshold
		if(params.get(ATTRIBUTE_NAME_DURATION_THRESHOLD) != null) {
			isMakingAction = true;
			durationThreshold = parseInt( params.get(ATTRIBUTE_NAME_DURATION_THRESHOLD), durationThreshold );
		}
		
		//--- Parameter : memory log size 
		if(params.get(ATTRIBUTE_NAME_LOG_SIZE) != null) {
			isMakingAction = true;
			int logSizeNew = parseInt( params.get(ATTRIBUTE_NAME_LOG_SIZE), logSize );
			if(logSizeNew > 0 && logSizeNew != logSize) {
				this.logSize = logSizeNew;
				logLines = new CircularStack(logLines, logSize);
			}
		}

		//--- Parameter : memory top ten size 
		if(params.get(ATTRIBUTE_NAME_BY_TIME_SIZE) != null) {
			isMakingAction = true;
			int topTenSizeNew = parseInt( params.get(ATTRIBUTE_NAME_BY_TIME_SIZE), topTenSize );
			if(topTenSizeNew > 0 && topTenSizeNew != topTenSize) {
				this.topTenSize = topTenSizeNew;
				topRequests = new TopRequests(topRequests, topTenSize);
			}
		}

		//--- Parameter : memory longest requests size 
		if(params.get(ATTRIBUTE_NAME_BY_URL_SIZE) != null) {
			isMakingAction = true;
			int longestSizeNew = parseInt( params.get(ATTRIBUTE_NAME_BY_URL_SIZE), longestSize );
			if(longestSizeNew > 0 && longestSizeNew != longestSize) {
				this.longestSize = longestSizeNew;
				longestRequests = new LongestRequests(longestRequests, longestSize);
			}
		}
		
		//--- Parameter : trace
		if(params.get(ATTRIBUTE_NAME_TRACE_FLAG) != null) {
			isMakingAction = true;
			String traceParam = params.get(ATTRIBUTE_NAME_TRACE_FLAG);
			traceFlag = "true".equalsIgnoreCase(traceParam) ;
		}
		
		return isMakingAction;
	}

	/**
	 * Reports the current status in plain text
	 * @param response HTTP response
	 */
	protected final void reporting (HttpServletResponse response) {
		reportingHtml2(response);
	}
	
	/**
	 * Reports the current status in plain text
	 * @param response HTTP response
	 */
	protected final void reportingBrut (HttpServletResponse response) {
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
			out.println("Top requests by time : " + topTenSize + " lines" );	
			out.println("Top requests by URL : " + longestSize + " lines" );	
			out.println(" ");
			
			out.println("Initialization date/time : " + initializationDate );
			out.println("Total requests count     : " + countAllRequest);
			out.println("Long time requests count : " + countLongTimeRequests );
			out.println(" ");
			
			List<Request> requests = logLines.getAllAscending(); 
			out.println("Last longest requests : " );
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toString());
				}
			}
			
			requests = topRequests.getAllDescending(); 
			out.println(" ");
			out.println("Top requests by time : " );
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toStringWithoutCounting());
				}
			}
			
			requests = longestRequests.getAllDescending(); 
			out.println(" ");
			out.println("Top requests by URL : " );
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toStringWithoutCounting());
				}
			}
			
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("RequestMonitor error : cannot get writer");
		}
	}
	
	/**
	 * Add action bar Javascript.
	 * @param out Output
	 */
	protected final void addActionBarJS(PrintWriter out) {
		out.println("<script>");
		out.println("function doRefresh(){document.location=document.location;}");
		out.println("function doAction(action){document.location=document.location+'?action='+action;}");
		out.println("function doParam(key,value){if(key==null||key==''||value==null||value==''){return;}document.location=document.location+'?'+key+'='+value;}");
		out.println("</script>");
	}
	
	/**
	 * Add action bar.
	 * @param out Output
	 */
	protected final void addActionBar(PrintWriter out) {
		out.println("<div class='actionbar'>");
		out.println("<div class='content'>");
		out.println("<input type='button' value='Refresh' onclick='doRefresh()'/>");
		out.println(" | ");
		out.println("<select id='key'>");
		out.println("<option value=''></option>");
		out.println("<option value='"+ATTRIBUTE_NAME_DURATION_THRESHOLD+"'>Duration threshold</option>");
		out.println("<option value='"+ATTRIBUTE_NAME_LOG_SIZE+"'>Log size</option>");
		out.println("<option value='"+ATTRIBUTE_NAME_BY_TIME_SIZE+"'>Top requests by Time size</option>");
		out.println("<option value='"+ATTRIBUTE_NAME_BY_URL_SIZE+"'>Top requests by URL size</option>");
		out.println("</select>");
		out.println("<input type='text' id='value' value=''/>");
		out.println("<input type='button' value='Modify' onclick='doParam(document.getElementById(\"key\").value,document.getElementById(\"value\").value)'/>");
		out.println(" | ");
		out.println("<input type='button' value='Clear logs' onclick='doAction(\"clear\")'/>");
		out.println(" | ");
		out.println("<input type='button' value='Reset' onclick='doAction(\"reset\")'/>");
		out.println(" | ");
		if(activated) {
			out.println("<input type='button' value=' Stop ' onclick='doAction(\""+ATTRIBUTE_VALUE_ACTION_STOP+"\")'/>");
		} else {
			out.println("<input type='button' value=' Start ' onclick='doAction(\""+ATTRIBUTE_VALUE_ACTION_START+"\")'/>");
		}
		out.println("</div>");
		out.println("</div>");
	}

	/**
	 * Reports the current status in plain text
	 * @param response HTTP response
	 */
	protected final void reportingHtml (HttpServletResponse response) {
		response.setContentType("text/html");
		
		//--- Prevent caching
		response.setHeader("Pragma", "no-cache"); // Set standard HTTP/1.0 no-cache header.
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // Set standard HTTP/1.1 no-cache header.
		response.setDateHeader ("Expires", 0); // Prevents caching on proxies
		
		final String date = format( new Date() ) ;
		PrintWriter out;
		try {
			out = response.getWriter();
			
			out.println("<html>");
			out.println("<head>");
			
			addActionBar(out);
			
			out.println("<pre>");
			out.println("Requests monitoring status (" + date + ")");
			out.println("IP address : " + ipAddress);
			out.println("Hostname : " + hostname );
			out.println(" ");
			
			out.println("Duration threshold : " + durationThreshold );
			out.println("Log in memory size : " + logSize + " lines" );	
			out.println("Top requests by time : " + topTenSize + " lines" );	
			out.println("Top requests by URL : " + longestSize + " lines" );	
			out.println(" ");
			
			out.println("Initialization date/time : " + initializationDate );
			out.println("Total requests count     : " + countAllRequest);
			out.println("Long time requests count : " + countLongTimeRequests );
			out.println(" ");
			
			List<Request> requests = logLines.getAllAscending(); 
			out.println("Last longest requests : " );
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toString());
				}
			}
			
			requests = topRequests.getAllDescending(); 
			out.println(" ");
			out.println("Top requests by time : " );
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toStringWithoutCounting());
				}
			}
			
			requests = longestRequests.getAllDescending(); 
			out.println(" ");
			out.println("Top requests by URL : " );
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toStringWithoutCounting());
				}
			}
			
			out.println("</pre>");

			out.println("</body>");
			out.println("</html>");
			
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("RequestMonitor error : cannot get writer");
		}
	}

	/**
	 * Reports the current status in plain text
	 * @param response HTTP response
	 */
	protected final void reportingHtml2 (HttpServletResponse response) {
		response.setContentType("text/html");
		
		//--- Prevent caching
		response.setHeader("Pragma", "no-cache"); // Set standard HTTP/1.0 no-cache header.
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate"); // Set standard HTTP/1.1 no-cache header.
		response.setDateHeader ("Expires", 0); // Prevents caching on proxies
		
		final String date = format( new Date() ) ;
		PrintWriter out;
		try {
			out = response.getWriter();

			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html>");
			out.println("<head>");
			out.println("<style>");
			out.println("body{font-family:monospace;font-size:13px;margin:0;padding:0}");
			out.println("div{margin:10px 0}");
			out.println(".title{width:100%;min-width:800px;margin:0;background-color:#bfbfbf;border-bottom:1px solid #8f8f8f;} .title h1{width:800px;margin:0;padding:20px 0 0 20px;} .title .version{width:800px;margin:0;padding:2px 0 2px 20px;font-size:13px;color:#4f4f4f;font-weight:bold;}");
			out.println(".actionbar{width:100%;min-width:800px;margin:0;background-color:#f0f0f0;border-bottom:1px solid #bfbfbf;border-top:1px solid #ffffff;} .actionbar .content{width:800px;margin:0;padding:5px;}");
			out.println(".main{width:800px;margin:0;}");
			out.println(".started{color:#009900;font-weight:bold;} .stopped{color:#990000;font-weight:bold;}");
			out.println("h2{font-size:15px;}");
			out.println("</style>");
			out.println("</head>");
			out.println("<body>");

			out.println("<div class='title'><h1>Telosys Monitoring</h1><div class='version'>"+VERSION+"</div></div>");

			addActionBar(out);
			
			out.println("<div class='main'>");

			out.println("<div>");
			out.println("Requests monitoring status (" + date + ") : ");
			if(activated) {
				out.println("<span class='started'>[Started]</span>");
			} else {
				out.println("<span class='stopped'>[Stopped]</span>");
			}
			out.println("<h2>Host</h2>" );
			out.println("<ul>");
			out.println("<li>IP address : " + ipAddress + "</li>");
			out.println("<li>Hostname : " + hostname + "</li>");
			out.println("</ul>");
			out.println("</div>");
			
			out.println("<div>");
			out.println("<h2>Configuration</h2>" );
			out.println("<ul>");
			out.println("<li>Duration threshold : " + durationThreshold + " ms</li>");
			out.println("<li>Log in memory size : " + logSize + " lines</li>" );	
			out.println("<li>Top requests by time : " + topTenSize + " lines</li>" );	
			out.println("<li>Top requests by URL : " + longestSize + " lines</li>" );	
			out.println("</ul></div>");
			
			out.println("<div>");
			out.println("<h2>Monitoring</h2>" );
			out.println("<ul>");
			out.println("<li>Initialization date/time : " + initializationDate + "</li>");
			out.println("<li>Total requests count     : " + countAllRequest + "</li>");
			out.println("<li>Long time requests count : " + countLongTimeRequests + "</li>");
			out.println("</ul></div>");
			
			out.println("<div>");
			List<Request> requests = logLines.getAllAscending(); 
			out.println("<h2>Last longest requests</h2>" );
			out.println("<pre>");
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toString());
				}
			}
			out.println("</pre>");
			out.println("</div>");
			
			out.println("<div>");
			requests = topRequests.getAllDescending(); 
			out.println(" ");
			out.println("<h2>Top requests by time</h2>" );
			out.println("<pre>");
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toStringWithoutCounting());
				}
			}
			out.println("</pre>");
			out.println("</div>");
			
			out.println("<div>");
			requests = longestRequests.getAllDescending(); 
			out.println("<h2>Top requests by URL</h2>" );
			out.println("<pre>");
			for ( Request request : requests ) {
				if(request != null) {
					out.println(request.toStringWithoutCounting());
				}
			}
			out.println("</pre>");
			out.println("</div>");
			
			out.println("</div>");
			
			addActionBarJS(out);
			
			out.println("</body>");
			out.println("</html>");
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
