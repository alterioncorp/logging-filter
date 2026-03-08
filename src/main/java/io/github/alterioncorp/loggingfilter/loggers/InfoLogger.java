package io.github.alterioncorp.loggingfilter.loggers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;

/**
 * Interface for loggers of request and response data.
 * 
 *
 */
public interface InfoLogger extends Configurable {
	
	/**
	 * Logs information about an incoming request.
	 *
	 * @param requestInfo the captured request data
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	void logRequest(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Logs information about an outgoing response.
	 *
	 * @param responseInfo the captured response data
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	void logResponse(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * Whether this logger is currently enabled or not.
	 * 
	 * @return whether the logger is currently enabled
	 */
	boolean isEnabled();
	
	/**
	 * Returns an MBean instance to manage this logger.
	 * 
	 * @return an MBean instance
	 */
	InfoLoggerMXBean getMBean();
}
