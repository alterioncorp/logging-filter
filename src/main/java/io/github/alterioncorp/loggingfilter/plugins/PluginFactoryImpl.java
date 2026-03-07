package io.github.alterioncorp.loggingfilter.plugins;

import io.github.alterioncorp.loggingfilter.config.Configurable;

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
