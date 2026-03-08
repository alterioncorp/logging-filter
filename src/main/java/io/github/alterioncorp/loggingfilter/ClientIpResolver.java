package io.github.alterioncorp.loggingfilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Determines the IP that made this request.
 * 
 *
 */
interface ClientIpResolver {

	/**
	 * Returns the IP that made this request.
	 * 
	 * @param request the request to get the IP from
	 * @return the client's IP
	 */
	String getClientIP(HttpServletRequest request);
}
