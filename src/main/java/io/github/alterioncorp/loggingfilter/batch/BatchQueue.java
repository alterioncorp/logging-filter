package io.github.alterioncorp.loggingfilter.batch;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

/**
 * Accumulates request/response pairs and periodically flushes them to a {@link BatchLogger}.
 *
 * @param <T> the type of each logged element
 */
public interface BatchQueue<T> extends Configurable {

	/**
	 * Stores the request data for later pairing with the response on the same thread.
	 *
	 * @param requestInfo captured request data
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	void queueRequest(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Pairs the response with the previously queued request from this thread and enqueues the record.
	 *
	 * @param responseInfo captured response data
	 * @param request the HTTP request
	 * @param response the HTTP response
	 */
	void queueResponse(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Sets the plugin used to extract values from each request/response pair.
	 *
	 * @param plugin the plugin to use
	 */
	void setPlugin(BatchPlugin<T> plugin);

	/**
	 * Sets the logger that receives batches of accumulated records.
	 *
	 * @param logger the logger to use
	 */
	void setLogger(BatchLogger<T> logger);
}
