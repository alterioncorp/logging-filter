package io.github.alterioncorp.loggingfilter.plugins;

import io.github.alterioncorp.loggingfilter.config.Configurable;

public interface PluginFactory {

	<T extends Configurable> T getPlugin(Class<T> type, String name) throws IllegalArgumentException;	
}
