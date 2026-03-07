package io.github.alterioncorp.loggingfilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.github.alterioncorp.loggingfilter.config.ConfigurationImpl;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;
import io.github.alterioncorp.loggingfilter.loggers.Slf4JLoggerImpl;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;

public class LoggingFilterTest {
	
	private static class Infos {
		
		private final RequestInfo requestInfo;
		private final ResponseInfo responseInfo;
		
		private Infos(RequestInfo requestInfo, ResponseInfo responseInfo) {
			this.requestInfo = requestInfo;
			this.responseInfo = responseInfo;
		}
	}

private static final String MBEAN_NAME = "com.follett.test:name=test1";
	
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	private HttpSession httpSession;
	private ClientIpResolver clientIpResolver;
	private FilterChain filterChain;
	private FilterConfig filterConfig;
	private Properties properties;
	private LoggingFilter filter;
	private InfoLogger infoLogger;
	private PluginFactory pluginFactory;
	private MBeanServer mBeanServer;
	private InfoLoggerMXBean mBean;
	
	@BeforeEach
	public void before() {
		
		httpServletRequest = Mockito.mock(HttpServletRequest.class);
		httpServletResponse = Mockito.mock(HttpServletResponse.class);
		httpSession = Mockito.mock(HttpSession.class);
		clientIpResolver = Mockito.mock(ClientIpResolver.class);
		filterChain = Mockito.mock(FilterChain.class);
		filterConfig = Mockito.mock(FilterConfig.class);
		properties = new Properties();
		infoLogger = Mockito.mock(InfoLogger.class);
		pluginFactory = Mockito.mock(PluginFactory.class);
		mBeanServer = Mockito.mock(MBeanServer.class);
		filter = new LoggingFilter(clientIpResolver, pluginFactory, properties, Arrays.asList(infoLogger), mBeanServer);
		
		Mockito.when(pluginFactory.getPlugin(Mockito.eq(InfoLogger.class), Mockito.anyString())).thenReturn(infoLogger);
		
		Mockito.when(infoLogger.isEnabled()).thenReturn(true);
		
		mBean = Mockito.mock(InfoLoggerMXBean.class);
		Mockito.when(mBean.getBeanName()).thenReturn(MBEAN_NAME);
		Mockito.when(infoLogger.getMBean()).thenReturn(mBean);
	}
	
	@Test
	public void testInit_LoggersNotSet() throws Exception {
		
		filter = new LoggingFilter(clientIpResolver, pluginFactory, properties, null, mBeanServer);
		
		filter.init(filterConfig);

		Mockito.verify(pluginFactory).getPlugin(InfoLogger.class, Slf4JLoggerImpl.class.getName());
	}
	
	@Test
	public void testInit_LoggersSet() throws Exception {

		final String logger1Name = "test1";
		final String logger2Name = "test2";
		
		properties.setProperty(LoggingFilter.PROPERTY_LOGGERS, logger1Name + ", " + logger2Name);
		
		filter = new LoggingFilter(clientIpResolver, pluginFactory, properties, null, mBeanServer);
		
		filter.init(filterConfig);

		Mockito.verify(pluginFactory).getPlugin(InfoLogger.class, logger1Name);
		Mockito.verify(pluginFactory).getPlugin(InfoLogger.class, logger2Name);
	}
		
	@Test
	public void testInit_ParamNamesToHideNull() throws Exception {
		
		filter.init(filterConfig);
		
		assertNotNull(filter.getParamNamesToHide());
		assertTrue(filter.getParamNamesToHide().isEmpty());
	}
	
	@Test
	public void testInit_ParamNamesToHideNotNull() throws Exception {
		
		Mockito
			.when(filterConfig.getInitParameter(LoggingFilter.PROPERTY_PARAM_NAMES_TO_HIDE))
			.thenReturn("abc, def");
			
		filter.init(filterConfig);
		
		assertNotNull(filter.getParamNamesToHide());
		assertEquals(2, filter.getParamNamesToHide().size());
		assertTrue(filter.getParamNamesToHide().contains("abc"));
		assertTrue(filter.getParamNamesToHide().contains("def"));
	}
	
	@Test
	public void testInit_ServerName() throws Exception {

		final String serverName = "test123";
		
		properties.setProperty(LoggingFilter.PROPERTY_SERVER_NAME, serverName);
			
		filter.init(filterConfig);
		
		assertEquals(serverName, filter.getServerName());
	}
	
	@Test
	public void testInit_Loggers() throws Exception {
		
		filter.init(filterConfig);
		
		Mockito.verify(infoLogger).init(Mockito.any(ConfigurationImpl.class));
	}
	
	@Test
	public void testInit_RegisterWithJmx() throws Exception {
		
		filter.init(filterConfig);
		
		Mockito.verify(mBeanServer).registerMBean(mBean, new ObjectName(MBEAN_NAME));
	}
	
	@Test
	public void testInit_RegisterWithJmxOnlyAfterSuccessfulInit() throws Exception {

		final String logger1Id = "logger1";
		final String logger2Id = "logger2";

		InfoLogger logger1 = Mockito.mock(InfoLogger.class);
		InfoLogger logger2 = Mockito.mock(InfoLogger.class);

		Mockito
			.when(filterConfig.getInitParameter(LoggingFilter.PROPERTY_LOGGERS))
			.thenReturn(String.join(", ", logger1Id, logger2Id));

		Mockito
			.when(pluginFactory.getPlugin(InfoLogger.class, logger1Id))
			.thenReturn(logger1);

		Mockito
			.when(pluginFactory.getPlugin(InfoLogger.class, logger2Id))
			.thenReturn(logger2);

		Mockito
			.doThrow(new IllegalStateException("test"))
			.when(logger2).init(Mockito.any());

		filter = new LoggingFilter(clientIpResolver, pluginFactory, properties, null, mBeanServer);
		assertThrows(IllegalStateException.class, () -> filter.init(filterConfig));
		Mockito.verify(logger1).init(Mockito.any());
		Mockito.verify(logger2).init(Mockito.any());
		Mockito.verify(mBeanServer, Mockito.never()).registerMBean(Mockito.any(), Mockito.any());
	}
	
	@Test
	public void testDestroy_Loggers() throws Exception {
		
		filter.destroy();
		
		Mockito.verify(infoLogger).destroy();
	}
	
	@Test
	public void testDestroy_DeegisterWithJmx() throws Exception {
		
		filter.destroy();
		
		Mockito.verify(mBeanServer).unregisterMBean(new ObjectName(MBEAN_NAME));
	}
	
	@Test
	public void testDoFilter() throws Exception {

		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final String paramName = "abc";
		final String paramValue1 = "def";
		final String paramValue2 = "ghi";
		final int responseStatus = 200;
		
		HashMap<String, String[]> params = new HashMap<>();
		params.put(paramName, new String[] {paramValue1, paramValue2});

		Infos infos = this.doFilter(requestIP, method, uri, params, responseStatus);
		
		assertNotNull(infos.requestInfo.getStartTimestamp());
		assertEquals(requestIP, infos.requestInfo.getClientNameOrAddress());
		assertNull(infos.requestInfo.getServerNameOrAddress());
		assertEquals(method, infos.requestInfo.getMethod());
		assertEquals(uri, infos.requestInfo.getPath());
		assertNotNull(infos.requestInfo.getParamsAsString());
		assertTrue(infos.requestInfo.getParamsAsString().contains(paramName + "=" + paramValue1));
		assertTrue(infos.requestInfo.getParamsAsString().contains(paramName + "=" + paramValue2));
		
		assertNotNull(infos.responseInfo.getEndTimestamp());
		assertEquals(requestIP, infos.responseInfo.getClientNameOrAddress());
		assertNull(infos.responseInfo.getServerNameOrAddress());
		assertEquals(method, infos.responseInfo.getMethod());
		assertEquals(uri, infos.responseInfo.getPath());
		assertEquals(responseStatus, infos.responseInfo.getResponseCode());
		assertNotNull(infos.responseInfo.getParamsAsString());
		assertTrue(infos.responseInfo.getParamsAsString().contains(paramName + "=" + paramValue1));
		assertTrue(infos.responseInfo.getParamsAsString().contains(paramName + "=" + paramValue2));
	}
	
	@Test
	public void testDoFilter_NoSession() throws Exception {

		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final int responseStatus = 200;
		
		Infos infos = this.doFilter(requestIP, method, uri, new HashMap<>(), responseStatus);
		
		assertNull(infos.requestInfo.getSessionId());
		assertNull(infos.responseInfo.getSessionId());
	}
	
	@Test
	public void testDoFilter_WithSession() throws Exception {

		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final int responseStatus = 200;
		final String sessionId = "test123";
		
		Mockito.when(httpSession.getId()).thenReturn(sessionId);
		Mockito.when(httpServletRequest.getSession(Mockito.anyBoolean())).thenReturn(httpSession);
		Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
		
		Infos infos = this.doFilter(requestIP, method, uri, new HashMap<>(), responseStatus);

		assertEquals(sessionId, infos.requestInfo.getSessionId());
		assertEquals(sessionId, infos.responseInfo.getSessionId());
	}
	
	@Test
	public void testDoFilter_WithSession_OnResponse() throws Exception {

		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final int responseStatus = 200;
		final String sessionId = "test123";
		
		Mockito.when(httpSession.getId()).thenReturn(sessionId);
		Mockito.when(httpServletRequest.getSession(Mockito.anyBoolean())).thenReturn(null, httpSession);
		Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
		
		Infos infos = this.doFilter(requestIP, method, uri, new HashMap<>(), responseStatus);

		assertEquals(null, infos.requestInfo.getSessionId());
		assertEquals(sessionId, infos.responseInfo.getSessionId());
	}
	
	@Test
	public void testDoFilter_ParamMasking() throws Exception {

		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final String paramName = "myPassword";
		final String paramValue = "abcdef";
		final int responseStatus = 200;
		
		filter.getParamNamesToHide().add(paramName);
		
		HashMap<String, String[]> params = new HashMap<>();
		params.put(paramName, new String[] {paramValue});
		
		Infos infos = this.doFilter(requestIP, method, uri, params, responseStatus);
		
		assertFalse(infos.requestInfo.getParamsAsString().contains(paramName + "=" + paramValue));
		assertTrue(infos.requestInfo.getParamsAsString().contains(paramName + "=" + LoggingFilter.HIDDEN_PARAM_VALUE));

		assertFalse(infos.responseInfo.getParamsAsString().contains(paramName + "=" + paramValue));
		assertTrue(infos.responseInfo.getParamsAsString().contains(paramName + "=" + LoggingFilter.HIDDEN_PARAM_VALUE));
	}
	
	@Test
	public void testDoFilter_ServerName() throws Exception {

		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final int responseStatus = 200;
		final String serverName = "test123";
		
		Mockito.when(httpServletRequest.getSession(Mockito.anyBoolean())).thenReturn(httpSession);
		Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);

		filter.setServerName(serverName);
		
		Infos infos = this.doFilter(requestIP, method, uri, new HashMap<>(), responseStatus);

		assertEquals(serverName, infos.requestInfo.getServerNameOrAddress());
		assertEquals(serverName, infos.responseInfo.getServerNameOrAddress());
	}
	
	@Test
	public void testDoFilter_ChainException() throws Exception {

		Mockito
			.doThrow(new ServletException("just testing"))
			.when(filterChain).doFilter(
					Mockito.any(ServletRequest.class), Mockito.any(ServletResponse.class));
		
		final String requestIP = "1.2.3.4";
		final String method = "GET";
		final String uri = "/hello/world";
		final int responseStatus = 200;
		
		HashMap<String, String[]> params = new HashMap<>();

		this.doFilter(requestIP, method, uri, params, responseStatus);	
	}
	
	@Test
	public void testDoFilter_LoggerDisabled() throws Exception {

		Mockito.when(infoLogger.isEnabled()).thenReturn(false);
		
		filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		
		Mockito.verify(infoLogger, Mockito.never()).logRequest(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.verify(infoLogger, Mockito.never()).logResponse(Mockito.any(), Mockito.any(), Mockito.any());
	}
	
	private Infos doFilter(String requestIP, String method, String uri, Map<String, String[]> params, int responseStatus) throws Exception {
		
		Mockito.when(clientIpResolver.getClientIP(Mockito.any())).thenReturn(requestIP);
		Mockito.when(httpServletRequest.getMethod()).thenReturn(method);
		Mockito.when(httpServletRequest.getRequestURI()).thenReturn(uri);
		Mockito.when(httpServletRequest.getParameterMap()).thenReturn(params);
		Mockito.when(httpServletResponse.getStatus()).thenReturn(responseStatus);
		
		RequestInfo requestInfo;
		ResponseInfo responseInfo;			

		try {
			filter.doFilter(httpServletRequest, httpServletResponse, filterChain);
		}
		catch (Exception e) {
		}
		finally {
		
			ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
			ArgumentCaptor<ResponseInfo> responseCaptor = ArgumentCaptor.forClass(ResponseInfo.class);

			Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
			Mockito.verify(infoLogger).logRequest(requestCaptor.capture(), Mockito.eq(httpServletRequest), Mockito.eq(httpServletResponse));
			Mockito.verify(infoLogger).logResponse(responseCaptor.capture(), Mockito.eq(httpServletRequest), Mockito.eq(httpServletResponse));
			
			requestInfo = requestCaptor.getValue();
			responseInfo = responseCaptor.getValue();			
		}
		
		return new Infos(requestInfo, responseInfo);
	}
}
