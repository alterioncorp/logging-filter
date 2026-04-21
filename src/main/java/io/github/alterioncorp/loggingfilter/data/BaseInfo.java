package io.github.alterioncorp.loggingfilter.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class holding common fields shared between request and response data.
 */
public abstract class BaseInfo {

	/**
	 * Creates a new instance.
	 */
	protected BaseInfo() {
	}

	private String clientNameOrAddress;
	private String serverNameOrAddress;
	private String method;
	private String path;
	private String paramsAsString;
	private String sessionId;
	private Map<String, List<String>> headers = new HashMap<>();
	private Map<String, String> attributes = new HashMap<>();

	/**
	 * Returns the client IP address or hostname.
	 *
	 * @return the client IP address or hostname
	 */
	public final String getClientNameOrAddress() {
		return clientNameOrAddress;
	}

	/**
	 * Sets the client IP address or hostname.
	 *
	 * @param clientHostOrIp the client IP address or hostname
	 */
	public final void setClientNameOrAddress(String clientHostOrIp) {
		this.clientNameOrAddress = clientHostOrIp;
	}

	/**
	 * Returns the server IP address or hostname.
	 *
	 * @return the server IP address or hostname
	 */
	public final String getServerNameOrAddress() {
		return serverNameOrAddress;
	}

	/**
	 * Sets the server IP address or hostname.
	 *
	 * @param serverHostOrIp the server IP address or hostname
	 */
	public final void setServerNameOrAddress(String serverHostOrIp) {
		this.serverNameOrAddress = serverHostOrIp;
	}

	/**
	 * Returns the HTTP method (e.g. GET, POST).
	 *
	 * @return the HTTP method
	 */
	public final String getMethod() {
		return method;
	}

	/**
	 * Sets the HTTP method.
	 *
	 * @param method the HTTP method
	 */
	public final void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Returns the request URI path.
	 *
	 * @return the request URI path
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * Sets the request URI path.
	 *
	 * @param path the request URI path
	 */
	public final void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns the URL-encoded query parameters.
	 *
	 * @return the URL-encoded query parameters
	 */
	public final String getParamsAsString() {
		return paramsAsString;
	}

	/**
	 * Sets the URL-encoded query parameters.
	 *
	 * @param paramsAsString the URL-encoded query parameters
	 */
	public final void setParamsAsString(String paramsAsString) {
		this.paramsAsString = paramsAsString;
	}

	/**
	 * Returns the HTTP session ID, or {@code null} if no session exists.
	 *
	 * @return the HTTP session ID, or {@code null} if no session exists
	 */
	public final String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the HTTP session ID.
	 *
	 * @param sessionId the HTTP session ID
	 */
	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Returns the HTTP headers associated with this request or response.
	 *
	 * @return the headers map
	 */
	public final Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * Sets the HTTP headers.
	 *
	 * @param headers the headers map
	 */
	public final void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers != null ? headers : new HashMap<>();
	}

	/**
	 * Returns an unmodifiable view of the attributes map.
	 *
	 * @return the attributes map
	 */
	public final Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	/**
	 * Sets a single attribute value.
	 *
	 * @param key the attribute name
	 * @param value the attribute value
	 */
	public final void setAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	/**
	 * Returns the value of the attribute with the given name.
	 *
	 * @param key the attribute name
	 * @return the attribute value, or {@code null} if not set
	 */
	public final String getAttribute(String key) {
		return attributes.get(key);
	}
}
