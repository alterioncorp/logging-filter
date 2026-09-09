package io.github.alterioncorp.loggingfilter.loggers;

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
	 */
	void logRequest(RequestInfo requestInfo);

	/**
	 * Logs information about an outgoing response.
	 *
	 * @param responseInfo the captured response data
	 */
	void logResponse(ResponseInfo responseInfo);

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
