package io.github.alterioncorp.loggingfilter.config;

import java.util.Properties;

import jakarta.servlet.FilterConfig;

/**
 * Default implementation of {@link Configuration} that resolves properties from
 * system properties first, falling back to servlet filter init-params.
 */
public final class ConfigurationImpl implements Configuration {

	private final FilterConfig filterConfig;
	private final Properties systemProperties;

	/**
	 * Creates a new instance backed by the given filter config and system properties.
	 *
	 * @param filterConfig the servlet filter configuration
	 * @param systemProperties the system properties to check first
	 */
	public ConfigurationImpl(FilterConfig filterConfig, Properties systemProperties) {
		this.filterConfig = filterConfig;
		this.systemProperties = systemProperties;
	}

	@Override
	public String getConfigProperty(String propertyName) {
		
		String propertyValue = systemProperties.getProperty(propertyName);
		
		if (propertyValue == null) {
			propertyValue = filterConfig.getInitParameter(propertyName);
		}
		
		return propertyValue;
	}

	@Override
	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

}
