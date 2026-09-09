package io.github.alterioncorp.loggingfilter.data;

import java.util.Date;

/**
 * Holds data captured at the time a response is sent.
 */
public class ResponseInfo extends BaseInfo {

	/**
	 * Creates a new instance.
	 */
	public ResponseInfo() {
	}

	private Date endTimestamp;
	private int responseCode;

	/**
	 * Returns the timestamp when the response was sent.
	 *
	 * @return the timestamp when the response was sent
	 */
	public final Date getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * Sets the timestamp when the response was sent.
	 *
	 * @param endTimestamp the timestamp when the response was sent
	 */
	public final void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the HTTP response status code.
	 *
	 * @return the HTTP response status code
	 */
	public final int getResponseCode() {
		return responseCode;
	}

	/**
	 * Sets the HTTP response status code.
	 *
	 * @param responseCode the HTTP response status code
	 */
	public final void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
}
