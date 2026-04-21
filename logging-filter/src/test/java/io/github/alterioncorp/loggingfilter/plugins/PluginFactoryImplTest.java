package io.github.alterioncorp.loggingfilter.plugins;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.config.Configuration;

public class PluginFactoryImplTest {
	
	public static interface TestPlugin extends Configurable {
	}
	
	public static class TestPluginImpl implements TestPlugin {
		
		@Override
		public void init(Configuration config) {
		}

		@Override
		public void destroy() {
		}
	}

	private PluginFactoryImpl pluginFactory;
	
	@BeforeEach
	public void before() {
		pluginFactory = new PluginFactoryImpl();
	}
	
	@Test
	public void testGetPlugin_Success() {
		TestPlugin plugin = pluginFactory.getPlugin(TestPlugin.class, TestPluginImpl.class.getName());
		assertNotNull(plugin);
		assertEquals(TestPluginImpl.class, plugin.getClass());
	}
	
	@Test
	public void testGetPlugin_InvalidClassName() {
		assertThrows(IllegalArgumentException.class, () -> pluginFactory.getPlugin(TestPlugin.class, "com.blah.Blah"));
	}
}
