package io.github.alterioncorp.loggingfilter.config;

import java.util.Properties;

import jakarta.servlet.FilterConfig;

/**
 * Default implementation of {@link Configuration} for servlet-based filters.
 * Resolves properties from system properties first, falling back to servlet filter init-params.
 *
 * @deprecated Renamed to {@link ConfigurationDefaultImpl}.
 */
@Deprecated
public final class ConfigurationImpl extends ConfigurationDefaultImpl {

	/**
	 * Creates a new instance backed by the given filter config and system properties.
	 *
	 * @param filterConfig the servlet filter configuration
	 * @param systemProperties the system properties to check first
	 */
	public ConfigurationImpl(FilterConfig filterConfig, Properties systemProperties) {
		super(filterConfig, systemProperties);
	}
}
