package io.github.alterioncorp.loggingfilter.plugins;

import io.github.alterioncorp.loggingfilter.config.Configurable;

/**
 * Reflection-based implementation of {@link PluginFactory} that instantiates plugins
 * by loading their class and invoking the no-arg constructor.
 */
public final class PluginFactoryImpl implements PluginFactory {

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Configurable> T getPlugin(Class<T> type, String name) {
		try {
			Class<?> implType = Class.forName(name);
			return (T)implType.getConstructor().newInstance();
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
