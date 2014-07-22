package org.telosys.webtools.monitoring.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public class Request implements Serializable {
	
	private static final long serialVersionUID = -2713511325247998819L;
	
	private long startTime;
	private long elapsedTime;
	private String requestURL;
	private String queryString;
	private String servletPath;
	private String pathInfo;

	public Request() {
	}
	
	public Request(HttpServletRequest httpRequest, long startTime, long elapsedTime) {
		this.startTime = startTime;
		this.elapsedTime = elapsedTime;
		this.requestURL = httpRequest.getRequestURL().toString();
		this.queryString = httpRequest.getQueryString();
		this.servletPath = httpRequest.getServletPath();
		this.pathInfo = httpRequest.getPathInfo();
	}

	@Override
	public String toString() {
		final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		final String sStartTime = dateFormat.format( new Date(getStartTime()) ) ;
		final StringBuilder sb = new StringBuilder();
		sb.append(sStartTime );
		sb.append(" - ");
		sb.append(getElapsedTime());
		sb.append(" ms - ");
		sb.append(getRequestURL() );
		String queryString = getQueryString() ;
		if ( queryString != null ) {
			sb.append("?"+queryString );
		}
		return sb.toString();
	}
	
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

}
