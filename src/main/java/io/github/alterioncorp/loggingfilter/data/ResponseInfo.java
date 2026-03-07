package io.github.alterioncorp.loggingfilter.data;

import java.util.Date;

/**
 * Holds data captured at the time a response is sent.
 */
public class ResponseInfo extends BaseInfo {

	private Date endTimestamp;
	private int responseCode;

	/** @return the timestamp when the response was sent */
	public final Date getEndTimestamp() {
		return endTimestamp;
	}

	/** @param endTimestamp the timestamp when the response was sent */
	public final void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/** @return the HTTP response status code */
	public final int getResponseCode() {
		return responseCode;
	}

	/** @param responseCode the HTTP response status code */
	public final void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
}
