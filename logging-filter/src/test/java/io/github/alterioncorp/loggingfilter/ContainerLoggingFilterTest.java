package io.github.alterioncorp.loggingfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;

import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;

public class ContainerLoggingFilterTest {

	private static final String PROP_REQUEST_INFO =
			ContainerLoggingFilter.class.getName() + ".requestInfo";

	private PluginFactory pluginFactory;
	private MBeanServer mBeanServer;
	private Properties systemProperties;
	private ClientIpResolver clientIpResolver;
	private InfoLogger logger;
	private InfoLoggerMXBean mBean;
	private ContainerLoggingFilter filter;
	private ContainerRequestContext requestContext;
	private ContainerResponseContext responseContext;
	private UriInfo uriInfo;

	@BeforeEach
	public void before() throws Exception {

		pluginFactory = Mockito.mock(PluginFactory.class);
		mBeanServer = Mockito.mock(MBeanServer.class);
		systemProperties = new Properties();
		clientIpResolver = Mockito.mock(ClientIpResolver.class);
		logger = Mockito.mock(InfoLogger.class);
		mBean = Mockito.mock(InfoLoggerMXBean.class);

		Mockito.when(logger.isEnabled()).thenReturn(true);
		Mockito.when(logger.getMBean()).thenReturn(mBean);
		Mockito.when(mBean.getBeanName()).thenReturn(
				"io.github.alterioncorp.loggingfilter:type=TestLogger,instance=test");

		filter = new ContainerLoggingFilter(pluginFactory, mBeanServer, systemProperties,
				clientIpResolver, Arrays.asList(logger));

		requestContext = Mockito.mock(ContainerRequestContext.class);
		responseContext = Mockito.mock(ContainerResponseContext.class);
		uriInfo = Mockito.mock(UriInfo.class);

		MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
		MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();

		Mockito.when(requestContext.getHeaders()).thenReturn(headers);
		Mockito.when(requestContext.getUriInfo()).thenReturn(uriInfo);
		Mockito.when(requestContext.getMethod()).thenReturn("GET");
		Mockito.when(uriInfo.getPath()).thenReturn("/test/path");
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParams);

		filter.init();
	}

	@Test
	public void testInit_LoggerEnabled() {
		assertTrue(logger.isEnabled());
	}

	@Test
	public void testFilter_Request_LogsCalled() {

		Mockito.when(clientIpResolver.getClientIp(Mockito.any(), Mockito.isNull()))
			.thenReturn("1.2.3.4");

		filter.filter(requestContext);

		Mockito.verify(logger).logRequest(Mockito.any(RequestInfo.class));
	}

	@Test
	public void testFilter_Request_SetsRequestInfoProperty() {

		Mockito.when(clientIpResolver.getClientIp(Mockito.any(), Mockito.isNull()))
			.thenReturn("1.2.3.4");

		filter.filter(requestContext);

		Mockito.verify(requestContext).setProperty(
				Mockito.eq(PROP_REQUEST_INFO),
				Mockito.any(RequestInfo.class));
	}

	@Test
	public void testFilter_Response_LogsCalled() {

		Mockito.when(clientIpResolver.getClientIp(Mockito.any(), Mockito.isNull()))
			.thenReturn("1.2.3.4");
		Mockito.when(responseContext.getStatus()).thenReturn(200);

		filter.filter(requestContext);

		ArgumentCaptor<RequestInfo> captor = ArgumentCaptor.forClass(RequestInfo.class);
		Mockito.verify(requestContext).setProperty(Mockito.eq(PROP_REQUEST_INFO), captor.capture());
		RequestInfo requestInfo = captor.getValue();

		Mockito.when(requestContext.getProperty(PROP_REQUEST_INFO)).thenReturn(requestInfo);

		filter.filter(requestContext, responseContext);

		Mockito.verify(logger).logResponse(Mockito.any(ResponseInfo.class));
	}

	@Test
	public void testFilter_Response_AbortedRequest_NoLog() {

		// requestContext has no stored RequestInfo (aborted before request phase)
		Mockito.when(requestContext.getProperty(PROP_REQUEST_INFO)).thenReturn(null);

		filter.filter(requestContext, responseContext);

		Mockito.verify(logger, Mockito.never()).logResponse(Mockito.any());
	}

	@Test
	public void testFilter_Request_DisabledLogger_NotCalled() {

		Mockito.when(logger.isEnabled()).thenReturn(false);

		filter.filter(requestContext);

		Mockito.verify(logger, Mockito.never()).logRequest(Mockito.any());
	}

	@Test
	public void testFilter_ParamMasking() {

		systemProperties.setProperty(ContainerLoggingFilter.PROPERTY_PARAM_NAMES_TO_HIDE, "password");
		filter = new ContainerLoggingFilter(pluginFactory, mBeanServer, systemProperties,
				clientIpResolver, Arrays.asList(logger));
		filter.init();

		assertTrue(filter.getParamNamesToHide().contains("password"));
	}

	@Test
	public void testDestroy_BeforeInit_NoException() {

		ContainerLoggingFilter uninitializedFilter = new ContainerLoggingFilter(
				pluginFactory, mBeanServer, systemProperties, clientIpResolver, null);

		assertDoesNotThrow(() -> uninitializedFilter.destroy());
	}

	@Test
	public void testResolveRemoteAddress_NoResolverRegistered_PassesNull() {

		// remoteAddressResolvers is never injected (stays null, as in a plain unit test)
		filter.filter(requestContext);

		Mockito.verify(clientIpResolver).getClientIp(Mockito.any(), Mockito.isNull());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testResolveRemoteAddress_Unsatisfied_PassesNull() {

		Instance<RemoteAddressResolver> resolvers = Mockito.mock(Instance.class);
		Mockito.when(resolvers.isUnsatisfied()).thenReturn(true);
		filter.setRemoteAddressResolvers(resolvers);

		filter.filter(requestContext);

		Mockito.verify(clientIpResolver).getClientIp(Mockito.any(), Mockito.isNull());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testResolveRemoteAddress_Ambiguous_PassesNull() {

		Instance<RemoteAddressResolver> resolvers = Mockito.mock(Instance.class);
		Mockito.when(resolvers.isUnsatisfied()).thenReturn(false);
		Mockito.when(resolvers.isAmbiguous()).thenReturn(true);
		filter.setRemoteAddressResolvers(resolvers);

		filter.filter(requestContext);

		Mockito.verify(clientIpResolver).getClientIp(Mockito.any(), Mockito.isNull());
		Mockito.verify(resolvers, Mockito.never()).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testResolveRemoteAddress_ResolverThrows_PassesNull() {

		Instance<RemoteAddressResolver> resolvers = Mockito.mock(Instance.class);
		RemoteAddressResolver resolver = Mockito.mock(RemoteAddressResolver.class);
		Mockito.when(resolvers.isUnsatisfied()).thenReturn(false);
		Mockito.when(resolvers.isAmbiguous()).thenReturn(false);
		Mockito.when(resolvers.get()).thenReturn(resolver);
		Mockito.when(resolver.getRemoteAddress()).thenThrow(new RuntimeException("boom"));
		filter.setRemoteAddressResolvers(resolvers);

		filter.filter(requestContext);

		Mockito.verify(clientIpResolver).getClientIp(Mockito.any(), Mockito.isNull());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testResolveRemoteAddress_ResolverSucceeds_PassesValue() {

		Instance<RemoteAddressResolver> resolvers = Mockito.mock(Instance.class);
		RemoteAddressResolver resolver = Mockito.mock(RemoteAddressResolver.class);
		Mockito.when(resolvers.isUnsatisfied()).thenReturn(false);
		Mockito.when(resolvers.isAmbiguous()).thenReturn(false);
		Mockito.when(resolvers.get()).thenReturn(resolver);
		Mockito.when(resolver.getRemoteAddress()).thenReturn("9.9.9.9");
		filter.setRemoteAddressResolvers(resolvers);

		filter.filter(requestContext);

		Mockito.verify(clientIpResolver).getClientIp(Mockito.any(), Mockito.eq("9.9.9.9"));
	}

	@Test
	public void testFilter_AttributesFromMdc_AppliedToRequestAndResponse() {

		systemProperties.setProperty(AttributeCollector.PARAM_FROM_MDC, "tenantId");
		filter = new ContainerLoggingFilter(pluginFactory, mBeanServer, systemProperties,
				clientIpResolver, Arrays.asList(logger));
		filter.init();

		Mockito.when(clientIpResolver.getClientIp(Mockito.any(), Mockito.isNull()))
			.thenReturn("1.2.3.4");
		Mockito.when(responseContext.getStatus()).thenReturn(200);

		MDC.put("tenantId", "acme");
		try {
			filter.filter(requestContext);

			ArgumentCaptor<RequestInfo> requestCaptor = ArgumentCaptor.forClass(RequestInfo.class);
			Mockito.verify(logger).logRequest(requestCaptor.capture());
			assertEquals("acme", requestCaptor.getValue().getAttribute("tenantId"));

			Mockito.when(requestContext.getProperty(PROP_REQUEST_INFO)).thenReturn(requestCaptor.getValue());

			filter.filter(requestContext, responseContext);

			ArgumentCaptor<ResponseInfo> responseCaptor = ArgumentCaptor.forClass(ResponseInfo.class);
			Mockito.verify(logger).logResponse(responseCaptor.capture());
			assertEquals("acme", responseCaptor.getValue().getAttribute("tenantId"));
		}
		finally {
			MDC.clear();
		}
	}
}
