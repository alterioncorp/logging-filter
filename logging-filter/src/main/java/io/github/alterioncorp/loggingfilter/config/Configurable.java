package io.github.alterioncorp.loggingfilter.config;

/**
 * Lifecycle interface for configurable plugins.
 * 
 * @see Configuration
 */
public interface Configurable {

	/**
	 * Initializes this instance with the given configuration.
	 *
	 * @param config the configuration to initialize with
	 */
	void init(Configuration config);

	/**
	 * Releases any resources held by this instance.
	 */
	void destroy();
}
