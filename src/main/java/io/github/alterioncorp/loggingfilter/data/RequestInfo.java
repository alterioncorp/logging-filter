package io.github.alterioncorp.loggingfilter.data;

import java.util.Date;

/**
 * Holds data captured at the time a request is received.
 */
public class RequestInfo extends BaseInfo {

	/**
	 * Creates a new instance.
	 */
	public RequestInfo() {
	}

	private Date startTimestamp;

	/**
	 * Returns the timestamp when the request was received.
	 *
	 * @return the timestamp when the request was received
	 */
	public final Date getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * Sets the timestamp when the request was received.
	 *
	 * @param startTimestamp the timestamp when the request was received
	 */
	public final void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
}
