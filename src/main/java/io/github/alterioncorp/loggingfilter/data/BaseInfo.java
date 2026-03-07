package io.github.alterioncorp.loggingfilter.data;

/**
 * Base class holding common fields shared between request and response data.
 */
public abstract class BaseInfo {

	private String clientNameOrAddress;
	private String serverNameOrAddress;
	private String method;
	private String path;
	private String paramsAsString;
	private String sessionId;

	/** @return the client IP address or hostname */
	public final String getClientNameOrAddress() {
		return clientNameOrAddress;
	}
	/** @param clientHostOrIp the client IP address or hostname */
	public final void setClientNameOrAddress(String clientHostOrIp) {
		this.clientNameOrAddress = clientHostOrIp;
	}
	/** @return the server IP address or hostname */
	public final String getServerNameOrAddress() {
		return serverNameOrAddress;
	}
	/** @param serverHostOrIp the server IP address or hostname */
	public final void setServerNameOrAddress(String serverHostOrIp) {
		this.serverNameOrAddress = serverHostOrIp;
	}
	/** @return the HTTP method (e.g. GET, POST) */
	public final String getMethod() {
		return method;
	}
	/** @param method the HTTP method */
	public final void setMethod(String method) {
		this.method = method;
	}
	/** @return the request URI path */
	public final String getPath() {
		return path;
	}
	/** @param path the request URI path */
	public final void setPath(String path) {
		this.path = path;
	}
	/** @return the URL-encoded query parameters */
	public final String getParamsAsString() {
		return paramsAsString;
	}
	/** @param paramsAsString the URL-encoded query parameters */
	public final void setParamsAsString(String paramsAsString) {
		this.paramsAsString = paramsAsString;
	}
	/** @return the HTTP session ID, or {@code null} if no session exists */
	public final String getSessionId() {
		return sessionId;
	}
	/** @param sessionId the HTTP session ID */
	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
