package io.github.alterioncorp.loggingfilter.plugins;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.loggers.Slf4JLoggerImpl;

/**
 * Plugin to customize the behavior of {@link Slf4JLoggerImpl}.
 * Converts request and response info to a list of data to log.
 * 
 * To add custom data, you should extend {@link Slf4JLoggerPluginDefaultImpl}.
 * 
 * @author alitovsky
 * @see Slf4JLoggerPluginDefaultImpl
 * @see Slf4JLoggerImpl
 *
 */
public interface Slf4JLoggerPlugin extends Configurable {

	/**
	 * Converts request data to a list of values to log.
	 * 
	 * @param info the requestInfo
	 * @param request the request
	 * @param response the response
	 * @return the list of data to log
	 */
	List<Object> getRequestValuesToLog(RequestInfo info, HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * Converts response data to a list of values to log.
	 * 
	 * @param info the requestInfo
	 * @param request the request
	 * @param response the response
	 * @return the list of data to log
	 */
	List<Object> getResponseValuesToLog(ResponseInfo info, HttpServletRequest request, HttpServletResponse response);
}
