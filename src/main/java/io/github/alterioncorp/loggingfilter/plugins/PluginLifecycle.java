package io.github.alterioncorp.loggingfilter.plugins;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;

/**
 * Lifecycle hooks called around each request and response logging event.
 */
public interface PluginLifecycle {
	
	/**
	 * Hook for subclasses - called at the beginning of {@link InfoLogger#logRequest(RequestInfo, HttpServletRequest, HttpServletResponse)}
	 * 
	 * @param requestInfo the requestInfo
	 * @param request the request
	 * @param response the response
	 */
	default void onLogRequestStart(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response) {
	}
	
	/**
	 * Hook for subclasses - called at the end of {@link InfoLogger#logRequest(RequestInfo, HttpServletRequest, HttpServletResponse)}
	 * 
	 * @param requestInfo the requestInfo
	 * @param request the request
	 * @param response the response
	 */
	default void onLogRequestEnd(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response) {
	}
	
	/**
	 * Hook for subclasses - called at the beginning of {@link InfoLogger#logResponse(ResponseInfo, HttpServletRequest, HttpServletResponse)}
	 *
	 * @param responseInfo the responseInfo
	 * @param request the request
	 * @param response the response
	 */
	default void onLogResponseStart(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response) {
	}

	/**
	 * Hook for subclasses - called at the end of {@link InfoLogger#logResponse(ResponseInfo, HttpServletRequest, HttpServletResponse)}
	 *
	 * @param responseInfo the responseInfo
	 * @param request the request
	 * @param response the response
	 */
	default void onLogResponseEnd(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response) {
	}
}
