package io.github.alterioncorp.loggingfilter.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.BaseInfo;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

/**
 * Default plugin for {@link io.github.alterioncorp.loggingfilter.loggers.Slf4JLoggerImpl}.
 * 
 * The 2 entries will contain the following elements:
 * <ol>
 * 	<li>Request IP</li>
 * 	<li>Response Code (on the response entry only, the request entry will contain '---' instead)</li>
 * 	<li>Method: GET, POST, PUT</li>
 * 	<li>Request URI with parameters (passwords will be masked)</li>
 * 	<li>Session ID (if the logging-filter.slf4j.show-session-id=true in init-params)</li>
 * </ol>
 * 
 * The following system-properties or filter init-params customize the behavior of this handler:
 * <ul>
 * 	<li>logging-filter.slf4j.show-session-id (default=false): when true, the Session ID will be logged, or 'no-session' if no Session is established.</li>
 * </ul>
 * 
 *
 */
public class Slf4JLoggerPluginDefaultImpl implements Slf4JLoggerPlugin {

	/** Configuration property name that controls whether session IDs are included in log entries. */
	protected static final String PARAM_SHOW_SESSION_ID = "logging-filter.slf4j.show-session-id";

	/** Placeholder used in request entries where the response code is not yet known. */
	protected static final String NO_RESPONSE_CODE = "---";

	/** Value logged when no HTTP session exists at the time of the request or response. */
	protected static final String NO_SESSION = "no-session";

	/**
	 * Creates a new instance.
	 */
	public Slf4JLoggerPluginDefaultImpl() {
	}

	private boolean showSessionId;

	@Override
	public void init(Configuration config) {
		String paramShowSessionId = config.getConfigProperty(PARAM_SHOW_SESSION_ID);
		this.showSessionId = Boolean.parseBoolean(paramShowSessionId);
	}

	@Override
	public void destroy() {
	}

	@Override
	public List<Object> getRequestValuesToLog(RequestInfo info, HttpServletRequest request,
			HttpServletResponse response) {

		List<Object> valuesToLog = new ArrayList<>(Arrays.asList(
    			info.getClientNameOrAddress(),
    			NO_RESPONSE_CODE,
    			info.getMethod(),
    			getPathWithParams(info)));
    	
    	if (showSessionId) {
    		valuesToLog.add(getSessionId(info));
    	}
    	
    	return valuesToLog;
	}

	@Override
	public List<Object> getResponseValuesToLog(ResponseInfo info, HttpServletRequest request,
			HttpServletResponse response) {
		
    	List<Object> valuesToLog = new ArrayList<>(Arrays.asList(
    			info.getClientNameOrAddress(),
    			info.getResponseCode(),
    			info.getMethod(),
    			getPathWithParams(info)));

    	if (showSessionId) {
    		valuesToLog.add(getSessionId(info));
    	}
    	
    	return valuesToLog;
	}

	/**
	 * Returns whether session IDs are included in log entries.
	 *
	 * @return {@code true} if session IDs are logged
	 */
	public boolean isShowSessionId() {
		return showSessionId;
	}

	/**
	 * Sets whether session IDs are included in log entries.
	 *
	 * @param showSessionId {@code true} to include session IDs in log entries
	 */
	protected void setShowSessionId(boolean showSessionId) {
		this.showSessionId = showSessionId;
	}

	private static String getPathWithParams(BaseInfo info) {
		
		String pathWithParams = info.getPath();
		
		if (info.getParamsAsString() != null && info.getParamsAsString().length() > 0) {
			pathWithParams = pathWithParams + "?" + info.getParamsAsString();
		}
		
		return pathWithParams;
	}
	
	private static String getSessionId(BaseInfo info) {
		return info.getSessionId() == null ? NO_SESSION : info.getSessionId();
	}
}
