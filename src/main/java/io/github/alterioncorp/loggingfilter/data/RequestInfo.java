package io.github.alterioncorp.loggingfilter.data;

import java.util.Date;

/**
 * Holds data captured at the time a request is received.
 */
public class RequestInfo extends BaseInfo {

	private Date startTimestamp;

	/** @return the timestamp when the request was received */
	public final Date getStartTimestamp() {
		return startTimestamp;
	}

	/** @param startTimestamp the timestamp when the request was received */
	public final void setStartTimestamp(Date startTimestamp) {
		this.startTimestamp = startTimestamp;
	}
}
