package io.github.alterioncorp.loggingfilter.loggers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.JmxUtils;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;
import io.github.alterioncorp.loggingfilter.plugins.Slf4JLoggerPlugin;
import io.github.alterioncorp.loggingfilter.plugins.Slf4JLoggerPluginDefaultImpl;

public class Slf4JLoggerImplTest {

	public static class TestPlugin implements Slf4JLoggerPlugin {

		@Override
		public void init(Configuration config) {
		}

		@Override
		public void destroy() {
		}

		@Override
		public List<Object> getRequestValuesToLog(RequestInfo info) {
			return null;
		}

		@Override
		public List<Object> getResponseValuesToLog(ResponseInfo info) {
			return null;
		}
	}

	private Logger loggerRequest;
	private Logger loggerResponse;
	private PluginFactory pluginFactory;
	private Slf4JLoggerPlugin plugin;
	private Slf4JLoggerImpl infoLogger;
	private Configuration config;

	@BeforeEach
	public void before() {

		loggerRequest = Mockito.mock(Logger.class);
		loggerResponse = Mockito.mock(Logger.class);
		pluginFactory = Mockito.mock(PluginFactory.class);
		plugin = Mockito.mock(Slf4JLoggerPlugin.class);
		infoLogger = new Slf4JLoggerImpl(loggerRequest, loggerResponse, pluginFactory, plugin);

		config = Mockito.mock(Configuration.class);

		Mockito
			.when(pluginFactory.getPlugin(Mockito.eq(Slf4JLoggerPlugin.class), Mockito.anyString()))
			.thenReturn(plugin);
	}

	@Test
	public void testInit_PluginSet() {

		Mockito
			.when(config.getConfigProperty(Slf4JLoggerImpl.PARAM_PLUGIN))
			.thenReturn(Slf4JLoggerPluginDefaultImpl.class.getName());

		infoLogger.init(config);

		Mockito.verify(pluginFactory, Mockito.never()).getPlugin(Mockito.eq(Slf4JLoggerPlugin.class), Mockito.anyString());
	}

	@Test
	public void testInit_PluginNotSet_DefaultPlugin() {

		infoLogger = new Slf4JLoggerImpl(loggerRequest, loggerResponse, pluginFactory, null);

		infoLogger.init(config);

		Mockito.verify(pluginFactory).getPlugin(Slf4JLoggerPlugin.class, Slf4JLoggerPluginDefaultImpl.class.getName());
	}

	@Test
	public void testInit_PluginNotSet_CustomPlugin() {

		infoLogger = new Slf4JLoggerImpl(loggerRequest, loggerResponse, pluginFactory, null);

		final String pluginName = "test123";

		Mockito
			.when(config.getConfigProperty(Slf4JLoggerImpl.PARAM_PLUGIN))
			.thenReturn(pluginName);

		infoLogger.init(config);

		Mockito.verify(pluginFactory).getPlugin(Slf4JLoggerPlugin.class, pluginName);
	}

	@Test
	public void testInit_PluginInit() {

		infoLogger.init(config);

		Mockito.verify(plugin).init(config);
	}

	@Test
	public void testDestroy_PluginDestroy() {

		infoLogger.destroy();

		Mockito.verify(plugin).destroy();
	}

	@Test
	public void testLogRequest() {

		final String value1 = "a1";
		final String value2 = "b2";

		RequestInfo info = new RequestInfo();

		Mockito
			.when(plugin.getRequestValuesToLog(info))
			.thenReturn(Arrays.asList(value1, value2));

		infoLogger.logRequest(info);

		List<String> data = captureLogData(loggerRequest);
		assertEquals(2, data.size());
		assertEquals(value1, data.get(0));
		assertEquals(value2, data.get(1));
	}

	@Test
	public void testLogResponse() {

		final String value1 = "a1";
		final String value2 = "b2";

		ResponseInfo info = new ResponseInfo();

		Mockito
			.when(plugin.getResponseValuesToLog(info))
			.thenReturn(Arrays.asList(value1, value2));

		infoLogger.logResponse(info);

		List<String> data = captureLogData(loggerResponse);
		assertEquals(2, data.size());
		assertEquals(value1, data.get(0));
		assertEquals(value2, data.get(1));
	}

	@Test
	public void testLogRequest_RequestHeaders() throws Exception {

		Map<String, List<String>> headers = new HashMap<>();
		headers.put("h1", Arrays.asList("v11", "v12"));
		headers.put("h2", Arrays.asList("v21"));

		RequestInfo info = new RequestInfo();
		info.setHeaders(headers);

		Mockito
			.when(loggerRequest.isDebugEnabled())
			.thenReturn(true);

		infoLogger.logRequest(info);

		ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(loggerRequest, Mockito.times(1)).debug(logCaptor.capture());

		String debugOutput = logCaptor.getValue();
		assertTrue(debugOutput.startsWith("request headers:\n"));
		assertTrue(debugOutput.contains("h1=v11,v12\n"));
		assertTrue(debugOutput.contains("h2=v21\n"));
	}

	@Test
	public void testLogResponse_ResponseHeaders() throws Exception {

		Map<String, List<String>> headers = new HashMap<>();
		headers.put("h1", Arrays.asList("v11", "v12"));
		headers.put("h2", Arrays.asList("v21"));

		ResponseInfo info = new ResponseInfo();
		info.setHeaders(headers);

		Mockito
			.when(loggerResponse.isDebugEnabled())
			.thenReturn(true);

		infoLogger.logResponse(info);

		ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(loggerResponse, Mockito.times(1)).debug(logCaptor.capture());

		String debugOutput = logCaptor.getValue();
		assertTrue(debugOutput.startsWith("response headers:\n"));
		assertTrue(debugOutput.contains("h1=v11,v12\n"));
		assertTrue(debugOutput.contains("h2=v21\n"));
	}

	@Test
	public void testGetBeanName() {

		final String instanceName = "/test/filter1";

		Mockito
			.when(config.getInstanceName())
			.thenReturn(instanceName);

		infoLogger.setConfiguration(config);

		String expectedBeanName = JmxUtils.BEAN_PREFIX +
				":type=" + infoLogger.getClass().getSimpleName() +
				",instance=" + instanceName;

		assertEquals(expectedBeanName, infoLogger.getBeanName());
	}

	@Test
	public void testEnabled() {

		assertTrue(infoLogger.isEnabled());

		infoLogger.setEnabled(false);
		assertFalse(infoLogger.isEnabled());
	}

	@Test
	public void testGetMBean() {
		assertSame(infoLogger, infoLogger.getMBean());
	}

	private static List<String> captureLogData(Logger logger) {

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

		Mockito.verify(logger).info(captor.capture());

		String logData = captor.getValue();

		return Arrays.asList(logData.split("\t"));
	}
}
