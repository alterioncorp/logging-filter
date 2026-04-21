package io.github.alterioncorp.loggingfilter.batch;

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
	 */
	void queueRequest(RequestInfo requestInfo);

	/**
	 * Pairs the response with the previously queued request from this thread and enqueues the record.
	 *
	 * @param responseInfo captured response data
	 */
	void queueResponse(ResponseInfo responseInfo);

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
