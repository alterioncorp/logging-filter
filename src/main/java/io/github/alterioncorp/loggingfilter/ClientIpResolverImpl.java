package io.github.alterioncorp.loggingfilter;

import java.util.Enumeration;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Uses the following search-pattern to determine the IP that made this request:
 * <ol>
 * 	<li>X-Forwarded-For header</li>
 * 	<li>{@link HttpServletRequest#getRemoteAddr()}</li>
 * </ol>
 * 
 *
 */
class ClientIpResolverImpl implements ClientIpResolver {
	
	static final String X_FORWARDED_FOR = "X-Forwarded-For";

	@Override
	public String getClientIP(HttpServletRequest request) {
		
		String requestIP = null;
		
		if (request != null) {
			// first try to get a forward IP from the proxy
			Enumeration<String> forwardIPs = request.getHeaders(X_FORWARDED_FOR);
			if (forwardIPs != null && forwardIPs.hasMoreElements()) {
				// we care about the first one
				requestIP = forwardIPs.nextElement();
			}
			
			// if the header contains multiple IP's separated by a comma
			if (requestIP != null) {
				// we care about the first one
				String[] requestIPs = requestIP.split(",");
				requestIP = requestIPs[0].trim();
			}
			
			// if not
			if (requestIP == null) {
				// use the remoteAddr from request
				requestIP = request.getRemoteAddr();
			}
			
		}
		
		return requestIP;
	}

}
