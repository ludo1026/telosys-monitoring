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
package org.telosys.webtools.monitoring.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * Request.
 */
public class Request implements Serializable {
	
	private static final long serialVersionUID = -2713511325247998819L;
	
	/** Start date */
	private long startTime;
	/** Execution time */
	private long elapsedTime;
	/** URL */
	private String requestURL;
	/** URL params */
	private String queryString;
	/** Servlet path */
	private String servletPath;
	/** URL Path */
	private String pathInfo;
	/** Counting all requests */
	private long countAllRequest;
	/** Counting all longest requests */
	private long countLongTimeRequests;

	/**
	 * Constructor
	 */
	public Request() {
	}
	
	/**
	 * Convert to String value.
	 * @return string value
	 */
	@Override
	public String toString() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final String sStartTime = dateFormat.format( new Date(getStartTime()) ) ;
		final StringBuilder sb = new StringBuilder();
		sb.append(sStartTime );
		sb.append(" - [ ");
		sb.append(countLongTimeRequests);
		sb.append(" / ");
		sb.append(countAllRequest);
		sb.append(" ] - ");
		sb.append(elapsedTime);
		sb.append(" ms - ");
		sb.append(requestURL );
		if ( queryString != null ) {
			sb.append("?"+queryString );
		}
		return sb.toString();
	}

	/**
	 * Convert to String value without counting information.
	 * @return string value
	 */
	public String toStringWithoutCounting() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final String sStartTime = dateFormat.format( new Date(getStartTime()) ) ;
		final StringBuilder sb = new StringBuilder();
		sb.append(sStartTime );
		sb.append(" - ");
		sb.append(elapsedTime);
		sb.append(" ms - ");
		sb.append(requestURL );
		if ( queryString != null ) {
			sb.append("?"+queryString );
		}
		return sb.toString();
	}
	
	/**
	 * Return complete URL of the request including URL parameters.
	 * @return Complete URL as String value
	 */
	public String getURL() {
		if ( queryString != null ) {
			return requestURL + "?" + queryString;
		} else {
			return requestURL;
		}
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public long getCountAllRequest() {
		return countAllRequest;
	}

	public void setCountAllRequest(long countAllRequest) {
		this.countAllRequest = countAllRequest;
	}

	public long getCountLongTimeRequests() {
		return countLongTimeRequests;
	}

	public void setCountLongTimeRequests(long countLongTimeRequests) {
		this.countLongTimeRequests = countLongTimeRequests;
	}

}
