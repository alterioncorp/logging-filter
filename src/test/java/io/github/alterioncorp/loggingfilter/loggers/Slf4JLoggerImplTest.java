package io.github.alterioncorp.loggingfilter.loggers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
		public List<Object> getRequestValuesToLog(RequestInfo info, HttpServletRequest request,
				HttpServletResponse response) {
			return null;
		}

		@Override
		public List<Object> getResponseValuesToLog(ResponseInfo info, HttpServletRequest request,
				HttpServletResponse response) {
			return null;
		}
		
	}

	private Logger loggerRequest;
	private Logger loggerResponse;
	private PluginFactory pluginFactory;
	private Slf4JLoggerPlugin plugin;
	private Slf4JLoggerImpl infoLogger;
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private Configuration config;

	@BeforeEach
	public void before() {
		
		loggerRequest = Mockito.mock(Logger.class);
		loggerResponse = Mockito.mock(Logger.class);
		pluginFactory = Mockito.mock(PluginFactory.class);
		plugin = Mockito.mock(Slf4JLoggerPlugin.class);
		infoLogger = new Slf4JLoggerImpl(loggerRequest, loggerResponse, pluginFactory, plugin);

		httpServletRequest = Mockito.mock(HttpServletRequest.class);
		httpServletResponse = Mockito.mock(HttpServletResponse.class);
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
			.when(plugin.getRequestValuesToLog(info, httpServletRequest, httpServletResponse))
			.thenReturn(Arrays.asList(value1, value2));
		
		infoLogger.logRequest(info, httpServletRequest, httpServletResponse);
		
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
			.when(plugin.getResponseValuesToLog(info, httpServletRequest, httpServletResponse))
			.thenReturn(Arrays.asList(value1, value2));
		
		infoLogger.logResponse(info, httpServletRequest, httpServletResponse);
		
		List<String> data = captureLogData(loggerResponse);
		assertEquals(2, data.size());
		assertEquals(value1, data.get(0));
		assertEquals(value2, data.get(1));
	}
		
	@Test
	public void testLogRequest_RequestHeaders() throws Exception {
				
		Mockito
			.when(httpServletRequest.getHeaderNames())
			.thenReturn(Collections.enumeration(Arrays.asList("h1", "h2")));
		
		Mockito
			.when(httpServletRequest.getHeaders("h1"))
			.thenReturn(Collections.enumeration(Arrays.asList("v11", "v12")));

		Mockito
			.when(httpServletRequest.getHeaders("h2"))
			.thenReturn(Collections.enumeration(Arrays.asList("v21")));

		Mockito
			.when(loggerRequest.isDebugEnabled())
			.thenReturn(true);

		infoLogger.logRequest(new RequestInfo(), httpServletRequest, httpServletResponse);
		
		ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(loggerRequest, Mockito.times(1)).debug(logCaptor.capture());

		StringBuilder expected = new StringBuilder();
		expected.append("request headers:\n");
		expected.append("h1=v11,v12\n");
		expected.append("h2=v21\n");

		assertEquals(expected.toString(), logCaptor.getValue());
	}
	
	@Test
	public void testLogResponse_ResponseHeaders() throws Exception {
				
		Mockito
			.when(httpServletResponse.getHeaderNames())
			.thenReturn(Arrays.asList("h1", "h2"));
		
		Mockito
			.when(httpServletResponse.getHeaders("h1"))
			.thenReturn(Arrays.asList("v11", "v12"));

		Mockito
			.when(httpServletResponse.getHeaders("h2"))
			.thenReturn(Arrays.asList("v21"));

		Mockito
			.when(loggerResponse.isDebugEnabled())
			.thenReturn(true);

		infoLogger.logResponse(new ResponseInfo(), httpServletRequest, httpServletResponse);
		
		ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(loggerResponse, Mockito.times(1)).debug(logCaptor.capture());

		StringBuilder expected = new StringBuilder();
		expected.append("response headers:\n");
		expected.append("h1=v11,v12\n");
		expected.append("h2=v21\n");

		assertEquals(expected.toString(), logCaptor.getValue());
	}
	
	@Test
	public void testGetBeanName() {

		final String contextPath = "/test";
		final String filterName = "filter1";
		
		FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
		ServletContext servletContext = Mockito.mock(ServletContext.class);
		
		Mockito
			.when(config.getFilterConfig())
			.thenReturn(filterConfig);
		
		Mockito
			.when(filterConfig.getFilterName())
			.thenReturn(filterName);
		
		Mockito
			.when(filterConfig.getServletContext())
			.thenReturn(servletContext);
		
		Mockito
			.when(servletContext.getContextPath())
			.thenReturn(contextPath);
		
		infoLogger.setConfiguration(config);
		
		String expectedBeanName = JmxUtils.BEAN_PREFIX +
				":type=" + infoLogger.getClass().getSimpleName() +
				",context=" + contextPath +
				",filter=" + filterName;
		
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
