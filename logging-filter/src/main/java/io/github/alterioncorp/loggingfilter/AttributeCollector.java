package io.github.alterioncorp.loggingfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.BaseInfo;

/**
 * Collects attributes from MDC, request headers, and system properties and applies them to info objects.
 *
 * Configuration keys:
 * <ul>
 *   <li>{@code logging-filter.attributes.from-mdc} — comma-separated MDC key names</li>
 *   <li>{@code logging-filter.attributes.from-header} — comma-separated {@code attrName:HeaderName} pairs
 *       (or just {@code name} when attr name equals header name)</li>
 *   <li>{@code logging-filter.attributes.from-sysprop} — comma-separated {@code attrName:syspropName} pairs</li>
 * </ul>
 */
class AttributeCollector {

	static final String PARAM_FROM_MDC = "logging-filter.attributes.from-mdc";
	static final String PARAM_FROM_HEADER = "logging-filter.attributes.from-header";
	static final String PARAM_FROM_SYSPROP = "logging-filter.attributes.from-sysprop";

	private final List<String> mdcKeys;
	/** Each entry is [attrName, headerName]. */
	private final List<String[]> headerMappings;
	/** Each entry is [attrName, syspropName]. */
	private final List<String[]> syspropMappings;

	AttributeCollector(Configuration config) {
		this.mdcKeys = parseSimpleList(config.getConfigProperty(PARAM_FROM_MDC));
		this.headerMappings = parseMappingList(config.getConfigProperty(PARAM_FROM_HEADER));
		this.syspropMappings = parseMappingList(config.getConfigProperty(PARAM_FROM_SYSPROP));
	}

	/**
	 * Applies attribute values from MDC, headers, and system properties to the given info object.
	 *
	 * @param info    the info object to populate
	 * @param headers the request headers for header-sourced attributes (case-insensitive lookup)
	 */
	void apply(BaseInfo info, Map<String, List<String>> headers) {

		for (String key : mdcKeys) {
			String value = MDC.get(key);
			if (value != null) {
				info.setAttribute(key, value);
			}
		}

		for (String[] mapping : headerMappings) {
			String attrName = mapping[0];
			String headerName = mapping[1];
			String value = getHeaderValue(headers, headerName);
			if (value != null) {
				info.setAttribute(attrName, value);
			}
		}

		for (String[] mapping : syspropMappings) {
			String attrName = mapping[0];
			String syspropName = mapping[1];
			String value = System.getProperty(syspropName);
			if (value != null) {
				info.setAttribute(attrName, value);
			}
		}
	}

	private static String getHeaderValue(Map<String, List<String>> headers, String headerName) {
		if (headers == null) return null;
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			if (headerName.equalsIgnoreCase(entry.getKey())) {
				List<String> values = entry.getValue();
				return (values != null && !values.isEmpty()) ? values.get(0) : null;
			}
		}
		return null;
	}

	private static List<String> parseSimpleList(String value) {
		if (value == null || value.trim().isEmpty()) return Collections.emptyList();
		List<String> result = new ArrayList<>();
		for (String s : value.split(",")) {
			String trimmed = s.trim();
			if (!trimmed.isEmpty()) result.add(trimmed);
		}
		return result;
	}

	private static List<String[]> parseMappingList(String value) {
		if (value == null || value.trim().isEmpty()) return Collections.emptyList();
		List<String[]> result = new ArrayList<>();
		for (String entry : value.split(",")) {
			String trimmed = entry.trim();
			if (trimmed.isEmpty()) continue;
			int colon = trimmed.indexOf(':');
			if (colon >= 0) {
				result.add(new String[]{trimmed.substring(0, colon).trim(), trimmed.substring(colon + 1).trim()});
			} else {
				result.add(new String[]{trimmed, trimmed});
			}
		}
		return result;
	}

	List<String> getMdcKeys() { return Collections.unmodifiableList(mdcKeys); }
	List<String[]> getHeaderMappings() { return Collections.unmodifiableList(headerMappings); }
	List<String[]> getSyspropMappings() { return Collections.unmodifiableList(syspropMappings); }
}
