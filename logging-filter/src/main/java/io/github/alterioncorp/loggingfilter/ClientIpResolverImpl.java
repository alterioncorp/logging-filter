package io.github.alterioncorp.loggingfilter;

import java.util.List;
import java.util.Map;

/**
 * Uses the following search-pattern to determine the IP that made this request:
 * <ol>
 * 	<li>X-Forwarded-For header (case-insensitive lookup, first value, first comma-separated entry)</li>
 * 	<li>remoteAddress fallback</li>
 * </ol>
 *
 */
class ClientIpResolverImpl implements ClientIpResolver {

	static final String X_FORWARDED_FOR = "X-Forwarded-For";

	@Override
	public String getClientIp(Map<String, List<String>> headers, String remoteAddress) {

		if (headers != null) {
			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				if (X_FORWARDED_FOR.equalsIgnoreCase(entry.getKey())) {
					List<String> values = entry.getValue();
					if (values != null && !values.isEmpty()) {
						String first = values.get(0);
						if (first != null) {
							String[] parts = first.split(",");
							return parts[0].trim();
						}
					}
					break;
				}
			}
		}

		return remoteAddress;
	}
}
