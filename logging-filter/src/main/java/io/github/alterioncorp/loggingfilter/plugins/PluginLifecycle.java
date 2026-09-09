package io.github.alterioncorp.loggingfilter.plugins;

import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;

/**
 * Lifecycle hooks called around each request and response logging event.
 */
public interface PluginLifecycle {

	/**
	 * Hook for subclasses - called at the beginning of {@link InfoLogger#logRequest(RequestInfo)}
	 *
	 * @param requestInfo the requestInfo
	 */
	default void onLogRequestStart(RequestInfo requestInfo) {
	}

	/**
	 * Hook for subclasses - called at the end of {@link InfoLogger#logRequest(RequestInfo)}
	 *
	 * @param requestInfo the requestInfo
	 */
	default void onLogRequestEnd(RequestInfo requestInfo) {
	}

	/**
	 * Hook for subclasses - called at the beginning of {@link InfoLogger#logResponse(ResponseInfo)}
	 *
	 * @param responseInfo the responseInfo
	 */
	default void onLogResponseStart(ResponseInfo responseInfo) {
	}

	/**
	 * Hook for subclasses - called at the end of {@link InfoLogger#logResponse(ResponseInfo)}
	 *
	 * @param responseInfo the responseInfo
	 */
	default void onLogResponseEnd(ResponseInfo responseInfo) {
	}
}
