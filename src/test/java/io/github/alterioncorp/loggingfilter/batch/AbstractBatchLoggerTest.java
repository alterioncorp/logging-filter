package io.github.alterioncorp.loggingfilter.batch;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.JmxUtils;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;

public class AbstractBatchLoggerTest {

	private class TestLogger extends AbstractBatchLogger<Object> {

		private TestLogger(PluginFactory pluginFactory, BatchQueue<Object> queue, Configuration config, BatchPlugin<Object> plugin) {
			super(pluginFactory, queue, config, plugin);
		}

		@Override
		public void logBatch(List<Object[]> dataToLog) throws Exception {
		}

		@Override
		protected void onInit(Configuration configuration) {
		}

		@Override
		protected void onDestroy() {
		}

		@Override
		protected BatchPlugin<Object> createPlugin(PluginFactory pluginFactory, Configuration configuration) {
			return plugin;
		}
	}

	private PluginFactory pluginFactory;
	private BatchQueue<Object> queue;
	private Configuration config;
	private BatchPlugin<Object> plugin;
	private AbstractBatchLogger<Object> logger;
	private AbstractBatchLogger<Object> spy;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		pluginFactory = Mockito.mock(PluginFactory.class);
		queue = Mockito.mock(BatchQueue.class);
		config = Mockito.mock(Configuration.class);
		plugin = Mockito.mock(BatchPlugin.class);
		logger = new TestLogger(pluginFactory, queue, config, plugin);
		spy = Mockito.spy(logger);
	}

	@Test
	public void testInit() {

		spy.init(config);

		Mockito.verify(spy).onInit(config);
		Mockito.verify(spy).createPlugin(pluginFactory, config);
		Mockito.verify(plugin).init(config);

		Mockito.verify(queue).setLogger(spy);
		Mockito.verify(queue).setPlugin(plugin);
		Mockito.verify(queue).init(config);
	}

	@Test
	public void testDestroy() {

		spy.destroy();

		Mockito.verify(spy).onDestroy();
		Mockito.verify(queue).destroy();
		Mockito.verify(plugin).destroy();
	}

	@Test
	public void testLogRequest() {

		RequestInfo requestInfo = new RequestInfo();
		logger.logRequest(requestInfo);

		Mockito.verify(queue).queueRequest(requestInfo);
		Mockito.verify(plugin).onLogRequestStart(requestInfo);
		Mockito.verify(plugin).onLogRequestEnd(requestInfo);
	}

	@Test
	public void testLogResponse() {

		ResponseInfo responseInfo = new ResponseInfo();
		logger.logResponse(responseInfo);

		Mockito.verify(queue).queueResponse(responseInfo);
		Mockito.verify(plugin).onLogResponseStart(responseInfo);
		Mockito.verify(plugin).onLogResponseEnd(responseInfo);
	}

	@Test
	public void testEnabled() {

		assertTrue(logger.isEnabled());

		logger.setEnabled(false);
		assertFalse(logger.isEnabled());
	}

	@Test
	public void testGetMBean() {
		assertSame(logger, logger.getMBean());
	}

	@Test
	public void testGetBeanName() {

		final String instanceName = "/test/filter1";

		Mockito
			.when(config.getInstanceName())
			.thenReturn(instanceName);

		String expectedBeanName = JmxUtils.BEAN_PREFIX +
				":type=" + logger.getClass().getSimpleName() +
				",instance=" + instanceName;

		assertEquals(expectedBeanName, logger.getBeanName());
	}
}
