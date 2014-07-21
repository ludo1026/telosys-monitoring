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
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter for Http Requests Monitor
 * 
 */
public class RequestsMonitor implements Filter {

	protected final static int DEFAULT_DURATION_THRESHOLD  = 1000 ; // 1 second 
	protected final static int DEFAULT_LOG_SIZE            =  100 ;
	
	protected int     durationThreshold     = DEFAULT_DURATION_THRESHOLD ; 
	protected String  reportingReqPath      = "/monitor" ; 
	protected int     logSize               = DEFAULT_LOG_SIZE ;
	protected boolean traceFlag             = false ;
	
	protected String initializationDate     = "???" ; 
	protected long   countAllRequest        = 0 ; 
	protected long   countLongTimeRequests  = 0 ; 
	
	protected CircularStack logLines = new CircularStack(DEFAULT_LOG_SIZE);
	
	protected String ipAddress;
	protected String hostname;
	
    /**
     * Default constructor. 
     */
    public RequestsMonitor() {
    }

    protected final void trace(String msg) {
    	if ( traceFlag ) {
    		System.out.println("[TRACE] : " + msg );
    	}    	
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
		if ( pathInfo != null && pathInfo.startsWith(reportingReqPath) ) {
			reporting( (HttpServletResponse) response );
		}
		else {
			countAllRequest++ ;
			final long startTime = getTime();
			
			try {
				//--- Chain (nothing to stop here)
				chain.doFilter(request, response);
				
			} finally {
				final long elapsedTime = getTime() - startTime;

				if ( elapsedTime > durationThreshold ) {
					countLongTimeRequests++ ;
					logRequest(httpRequest, startTime, elapsedTime);
				}
			}
		}
	}

	protected final void logRequest(HttpServletRequest httpRequest, long startTime, long elapsedTime ) {
		
		
		final String sStartTime = format( new Date(startTime) ) ;
		
		final StringBuilder sb = new StringBuilder();
		sb.append( sStartTime );
		sb.append(" [ ");
		sb.append(countLongTimeRequests);
		sb.append(" / ");
		sb.append(countAllRequest);
		sb.append(" ] ");
		sb.append(elapsedTime);
		sb.append(" ms : ");
		sb.append(httpRequest.getRequestURL() );
		String queryString = httpRequest.getQueryString() ;
		if ( queryString != null ) {
			sb.append("?"+queryString );
		}
		String line = sb.toString();
		
		logLine( line );
	}
	
	protected final void logLine( String line ) {
		trace( "Logging line : " + line );
		logLines.push(line);
	}
	
	/**
	 * Reports the current status in plain text
	 *   
	 * @param response
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
			out.println(" ");
			
			out.println("Initialization date/time : " + initializationDate );
			out.println("Total requests count     : " + countAllRequest);
			out.println("Long time requests count : " + countLongTimeRequests );
			out.println(" ");
			
			List<String> lines = logLines.getAllAscendant(); 
			out.println("" + lines.size() + " last long time requests : " );
			for ( String line : lines ) {
				out.println(line);
			}
			out.close();
		} catch (IOException e) {
			throw new RuntimeException("RequestMonitor error : cannot get writer");
		}
	}

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
