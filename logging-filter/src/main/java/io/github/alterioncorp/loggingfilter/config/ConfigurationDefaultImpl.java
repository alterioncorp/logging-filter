package io.github.alterioncorp.loggingfilter.config;

import java.util.Properties;

import jakarta.servlet.FilterConfig;

/**
 * Default implementation of {@link Configuration} for servlet-based filters.
 * Resolves properties from system properties first, falling back to servlet filter init-params.
 * {@link #getInstanceName()} returns {@code contextPath + "/" + filterName}.
 */
public class ConfigurationDefaultImpl implements Configuration {

	private final FilterConfig filterConfig;
	private final Properties systemProperties;

	/**
	 * Creates a new instance backed by the given filter config and system properties.
	 *
	 * @param filterConfig the servlet filter configuration
	 * @param systemProperties the system properties to check first
	 */
	public ConfigurationDefaultImpl(FilterConfig filterConfig, Properties systemProperties) {
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
	public String getInstanceName() {
		String contextPath = filterConfig.getServletContext().getContextPath();
		String filterName = filterConfig.getFilterName();
		if (contextPath == null) contextPath = "";
		if (filterName == null) filterName = "";
		return contextPath + "/" + filterName;
	}
}
