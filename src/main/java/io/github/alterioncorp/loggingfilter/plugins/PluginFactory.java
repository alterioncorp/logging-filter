package io.github.alterioncorp.loggingfilter.plugins;

import io.github.alterioncorp.loggingfilter.config.Configurable;

/**
 * Factory for instantiating plugin instances by class name.
 */
public interface PluginFactory {

	/**
	 * Instantiates and returns a plugin of the given type using its fully-qualified class name.
	 *
	 * @param <T> the plugin type
	 * @param type the expected plugin interface or superclass
	 * @param name the fully-qualified class name of the plugin implementation
	 * @return the instantiated plugin
	 * @throws IllegalArgumentException if the class cannot be found or instantiated
	 */
	<T extends Configurable> T getPlugin(Class<T> type, String name);
}
