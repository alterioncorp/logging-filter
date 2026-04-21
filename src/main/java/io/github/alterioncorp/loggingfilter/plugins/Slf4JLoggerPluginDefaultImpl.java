package io.github.alterioncorp.loggingfilter.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.BaseInfo;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

/**
 * Default plugin for {@link io.github.alterioncorp.loggingfilter.loggers.Slf4JLoggerImpl}.
 *
 * The 2 entries will contain the following elements:
 * <ol>
 * 	<li>Any prepended attribute values (configured via {@link #PARAM_PREPEND_ATTRIBUTES})</li>
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
 * 	<li>logging-filter.slf4j.prepend-attributes (default=none): comma-separated list of attribute names to prepend to each log line. Missing attributes are logged as '---'.</li>
 * </ul>
 *
 *
 */
public class Slf4JLoggerPluginDefaultImpl implements Slf4JLoggerPlugin {

	/** Configuration property name that controls whether session IDs are included in log entries. */
	protected static final String PARAM_SHOW_SESSION_ID = "logging-filter.slf4j.show-session-id";

	/** Configuration property name for attribute names to prepend to each log line. */
	public static final String PARAM_PREPEND_ATTRIBUTES = "logging-filter.slf4j.prepend-attributes";

	/** Placeholder used in request entries where the response code is not yet known. */
	protected static final String NO_RESPONSE_CODE = "---";

	/** Value logged when no HTTP session exists at the time of the request or response. */
	protected static final String NO_SESSION = "no-session";

	/** Placeholder used when a configured prepend attribute is missing from the info object. */
	protected static final String FILLER = "---";

	/**
	 * Creates a new instance.
	 */
	public Slf4JLoggerPluginDefaultImpl() {
	}

	private boolean showSessionId;
	private List<String> prependAttributes = Collections.emptyList();

	@Override
	public void init(Configuration config) {
		String paramShowSessionId = config.getConfigProperty(PARAM_SHOW_SESSION_ID);
		this.showSessionId = Boolean.parseBoolean(paramShowSessionId);

		String prependAttrValue = config.getConfigProperty(PARAM_PREPEND_ATTRIBUTES);
		if (prependAttrValue != null && !prependAttrValue.trim().isEmpty()) {
			List<String> attrs = new ArrayList<>();
			for (String a : prependAttrValue.split(",")) {
				String trimmed = a.trim();
				if (!trimmed.isEmpty()) attrs.add(trimmed);
			}
			this.prependAttributes = attrs;
		} else {
			this.prependAttributes = Collections.emptyList();
		}
	}

	@Override
	public void destroy() {
	}

	@Override
	public List<Object> getRequestValuesToLog(RequestInfo info) {

		List<Object> valuesToLog = new ArrayList<>();

		for (String attrName : prependAttributes) {
			String attrValue = info.getAttribute(attrName);
			valuesToLog.add(attrValue != null ? attrValue : FILLER);
		}

		valuesToLog.addAll(Arrays.asList(
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
	public List<Object> getResponseValuesToLog(ResponseInfo info) {

		List<Object> valuesToLog = new ArrayList<>();

		for (String attrName : prependAttributes) {
			String attrValue = info.getAttribute(attrName);
			valuesToLog.add(attrValue != null ? attrValue : FILLER);
		}

		valuesToLog.addAll(Arrays.asList(
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

	/**
	 * Returns the list of attribute names prepended to each log line.
	 *
	 * @return the prepend-attributes list
	 */
	public List<String> getPrependAttributes() {
		return Collections.unmodifiableList(prependAttributes);
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
