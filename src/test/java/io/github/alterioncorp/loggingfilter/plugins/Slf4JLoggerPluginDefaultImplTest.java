package io.github.alterioncorp.loggingfilter.plugins;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

public class Slf4JLoggerPluginDefaultImplTest {

	private Slf4JLoggerPluginDefaultImpl plugin;
	private Configuration config;
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
	
	@BeforeEach
	public void before() {
		plugin = new Slf4JLoggerPluginDefaultImpl();
		config = Mockito.mock(Configuration.class);
		httpServletRequest = Mockito.mock(HttpServletRequest.class);
		httpServletResponse = Mockito.mock(HttpServletResponse.class);
	}
	
	@Test
	public void testInit_ShowSessionIdNull() throws Exception {
		
		plugin.init(config);
		
		assertFalse(plugin.isShowSessionId());
	}
	
	@Test
	public void testInit_ShowSessionIdFalse() throws Exception {

		Mockito
			.when(config.getConfigProperty(Slf4JLoggerPluginDefaultImpl.PARAM_SHOW_SESSION_ID))
			.thenReturn("false");

		plugin.init(config);
		
		assertFalse(plugin.isShowSessionId());
	}
	
	@Test
	public void testInit_ShowSessionIdTrue() throws Exception {

		Mockito
			.when(config.getConfigProperty(Slf4JLoggerPluginDefaultImpl.PARAM_SHOW_SESSION_ID))
			.thenReturn("true");

		plugin.init(config);
		
		assertTrue(plugin.isShowSessionId());
	}

	@Test
	public void testLogRequest_InfoData_WithParams() {
		
		final String clientIp = "1.2.3.4";
		final String method = "GET";
		final String path = "/a/b/c";
		final String params = "a=b&c=d";
		
		RequestInfo info = new RequestInfo();
		info.setClientNameOrAddress(clientIp);
		info.setMethod(method);
		info.setPath(path);
		info.setParamsAsString(params);
		
		List<Object> data = plugin.getRequestValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(4, data.size());
		assertEquals(clientIp, data.get(0));
		assertEquals(Slf4JLoggerPluginDefaultImpl.NO_RESPONSE_CODE, data.get(1));
		assertEquals(method, data.get(2));
		assertEquals(path + "?" + params, data.get(3));
	}
	
	@Test
	public void testLogRequest_InfoData_NoParams() {
		
		final String clientIp = "1.2.3.4";
		final String method = "GET";
		final String path = "/a/b/c";
		
		RequestInfo info = new RequestInfo();
		info.setClientNameOrAddress(clientIp);
		info.setMethod(method);
		info.setPath(path);
		
		List<Object> data = plugin.getRequestValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(4, data.size());
		assertEquals(clientIp, data.get(0));
		assertEquals(Slf4JLoggerPluginDefaultImpl.NO_RESPONSE_CODE, data.get(1));
		assertEquals(method, data.get(2));
		assertEquals(path, data.get(3));
	}
	
	@Test
	public void testLogRequest_ShowSession_SessionNull() {

		plugin.setShowSessionId(true);

		RequestInfo info = new RequestInfo();
		
		List<Object> data = plugin.getRequestValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(5, data.size());
		assertEquals(Slf4JLoggerPluginDefaultImpl.NO_SESSION, data.get(4));
	}
	
	@Test
	public void testLogRequest_ShowSession_SessionNotNull() {

		plugin.setShowSessionId(true);

		final String sessionId = "abc123";

		RequestInfo info = new RequestInfo();
		info.setSessionId(sessionId);
		
		List<Object> data = plugin.getRequestValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(5, data.size());
		assertEquals(sessionId, data.get(4));
	}
	
	@Test
	public void testLogResponse_InfoData_WithParams() {
		
		final String clientIp = "1.2.3.4";
		final int responseCode = 200;
		final String method = "GET";
		final String path = "/a/b/c";
		final String params = "a=b&c=d";
		
		ResponseInfo info = new ResponseInfo();
		info.setClientNameOrAddress(clientIp);
		info.setMethod(method);
		info.setPath(path);
		info.setParamsAsString(params);
		info.setResponseCode(responseCode);
		
		List<Object> data = plugin.getResponseValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(4, data.size());
		assertEquals(clientIp, data.get(0));
		assertEquals(Integer.valueOf(responseCode), data.get(1));
		assertEquals(method, data.get(2));
		assertEquals(path + "?" + params, data.get(3));
	}
	
	@Test
	public void testLogResponse_InfoData_NoParams() {
		
		final String clientIp = "1.2.3.4";
		final int responseCode = 200;
		final String method = "GET";
		final String path = "/a/b/c";
		
		ResponseInfo info = new ResponseInfo();
		info.setClientNameOrAddress(clientIp);
		info.setMethod(method);
		info.setPath(path);
		info.setResponseCode(responseCode);
		
		List<Object> data = plugin.getResponseValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(4, data.size());
		assertEquals(clientIp, data.get(0));
		assertEquals(Integer.valueOf(responseCode), data.get(1));
		assertEquals(method, data.get(2));
		assertEquals(path, data.get(3));
	}
	
	@Test
	public void testLogResponse_ShowSession_SessionNull() {

		plugin.setShowSessionId(true);

		ResponseInfo info = new ResponseInfo();
		
		List<Object> data = plugin.getResponseValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(5, data.size());
		assertEquals(Slf4JLoggerPluginDefaultImpl.NO_SESSION, data.get(4));
	}
	
	@Test
	public void testLogResponse_ShowSession_SessionNotNull() {

		plugin.setShowSessionId(true);

		final String sessionId = "abc123";

		ResponseInfo info = new ResponseInfo();
		info.setSessionId(sessionId);
		
		List<Object> data = plugin.getResponseValuesToLog(info, httpServletRequest, httpServletResponse);
		
		assertEquals(5, data.size());
		assertEquals(sessionId, data.get(4));
	}
}
