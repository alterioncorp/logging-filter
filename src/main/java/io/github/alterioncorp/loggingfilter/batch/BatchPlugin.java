package io.github.alterioncorp.loggingfilter.batch;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.plugins.PluginLifecycle;

/**
 * Super interface for all plugins.
 * 
 * @author alitovsky
 *
 */
public interface BatchPlugin<T> extends PluginLifecycle, Configurable {

	/**
	 * Return the number of elements to log
	 * 
	 * @return the number of elements to log
	 */
	int getElementCount();
	
	/**
	 * Return the values for.
	 * 
	 * @param requestInfo the requestInfo
	 * @param responseInfo the responseInfo
	 * @param request the request
	 * @param response the response
	 * @return the values to log
	 */
	T[] getValues(
			RequestInfo requestInfo, ResponseInfo responseInfo,
			HttpServletRequest request, HttpServletResponse response);
}
