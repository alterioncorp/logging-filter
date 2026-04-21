package io.github.alterioncorp.loggingfilter;

import java.util.List;
import java.util.Map;

/**
 * Determines the IP that made this request.
 *
 */
interface ClientIpResolver {

	/**
	 * Returns the IP that made this request.
	 *
	 * @param headers the request headers (case-insensitive lookup expected)
	 * @param remoteAddress the remote address from the transport layer
	 * @return the client's IP
	 */
	String getClientIp(Map<String, List<String>> headers, String remoteAddress);
}
