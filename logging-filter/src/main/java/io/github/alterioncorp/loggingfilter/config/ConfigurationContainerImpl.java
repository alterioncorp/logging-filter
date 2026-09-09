package io.github.alterioncorp.loggingfilter.config;

import java.util.Properties;

/**
 * Implementation of {@link Configuration} for container (JAX-RS) based filters.
 * Reads configuration from system properties only.
 * {@link #getInstanceName()} reads {@code logging-filter.instance-name}, defaulting to
 * {@code "container-logging-filter"}.
 */
public final class ConfigurationContainerImpl implements Configuration {

	/** Configuration property for the filter instance name. */
	public static final String PARAM_INSTANCE_NAME = "logging-filter.instance-name";

	/** Default instance name when {@link #PARAM_INSTANCE_NAME} is not set. */
	public static final String DEFAULT_INSTANCE_NAME = "container-logging-filter";

	private final Properties systemProperties;

	/**
	 * Creates a new instance backed by the given system properties.
	 *
	 * @param systemProperties the system properties to read configuration from
	 */
	public ConfigurationContainerImpl(Properties systemProperties) {
		this.systemProperties = systemProperties;
	}

	@Override
	public String getConfigProperty(String propertyName) {
		return systemProperties.getProperty(propertyName);
	}

	@Override
	public String getInstanceName() {
		String name = systemProperties.getProperty(PARAM_INSTANCE_NAME);
		return name != null ? name : DEFAULT_INSTANCE_NAME;
	}
}
