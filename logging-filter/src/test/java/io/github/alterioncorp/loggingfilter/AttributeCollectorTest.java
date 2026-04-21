package io.github.alterioncorp.loggingfilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;

public class AttributeCollectorTest {

	private Configuration config;

	@BeforeEach
	public void before() {
		config = Mockito.mock(Configuration.class);
	}

	@AfterEach
	public void after() {
		MDC.clear();
	}

	@Test
	public void testFromMdc_Present() {

		MDC.put("tenantId", "acme");

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_MDC))
			.thenReturn("tenantId");

		AttributeCollector collector = new AttributeCollector(config);

		RequestInfo info = new RequestInfo();
		collector.apply(info, null);

		assertEquals("acme", info.getAttribute("tenantId"));
	}

	@Test
	public void testFromMdc_Absent() {

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_MDC))
			.thenReturn("tenantId");

		AttributeCollector collector = new AttributeCollector(config);

		RequestInfo info = new RequestInfo();
		collector.apply(info, null);

		assertNull(info.getAttribute("tenantId"));
	}

	@Test
	public void testFromHeader_CaseInsensitive() {

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_HEADER))
			.thenReturn("tenant:X-Tenant-Id");

		AttributeCollector collector = new AttributeCollector(config);

		Map<String, List<String>> headers = new HashMap<>();
		headers.put("x-tenant-id", Arrays.asList("acme-corp"));

		RequestInfo info = new RequestInfo();
		collector.apply(info, headers);

		assertEquals("acme-corp", info.getAttribute("tenant"));
	}

	@Test
	public void testFromHeader_Shorthand() {

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_HEADER))
			.thenReturn("X-Tenant-Id");

		AttributeCollector collector = new AttributeCollector(config);

		Map<String, List<String>> headers = new HashMap<>();
		headers.put("X-Tenant-Id", Arrays.asList("acme-corp"));

		RequestInfo info = new RequestInfo();
		collector.apply(info, headers);

		assertEquals("acme-corp", info.getAttribute("X-Tenant-Id"));
	}

	@Test
	public void testFromHeader_NullHeaders() {

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_HEADER))
			.thenReturn("tenant:X-Tenant-Id");

		AttributeCollector collector = new AttributeCollector(config);

		RequestInfo info = new RequestInfo();
		collector.apply(info, null);

		assertNull(info.getAttribute("tenant"));
	}

	@Test
	public void testFromSysprop_Present() {

		final String propName = "test.attributecollector.sysprop";
		System.setProperty(propName, "node-1");

		try {
			Mockito
				.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_SYSPROP))
				.thenReturn("nodeId:" + propName);

			AttributeCollector collector = new AttributeCollector(config);

			RequestInfo info = new RequestInfo();
			collector.apply(info, null);

			assertEquals("node-1", info.getAttribute("nodeId"));
		}
		finally {
			System.clearProperty(propName);
		}
	}

	@Test
	public void testFromSysprop_Absent() {

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_SYSPROP))
			.thenReturn("nodeId:does.not.exist.prop");

		AttributeCollector collector = new AttributeCollector(config);

		RequestInfo info = new RequestInfo();
		collector.apply(info, null);

		assertNull(info.getAttribute("nodeId"));
	}

	@Test
	public void testNullConfig_NoException() {

		AttributeCollector collector = new AttributeCollector(config);

		RequestInfo info = new RequestInfo();
		assertDoesNotThrow(() -> collector.apply(info, null));
	}

	@Test
	public void testMultipleSources() {

		MDC.put("tenant", "mdc-tenant");

		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_MDC))
			.thenReturn("tenant");
		Mockito
			.when(config.getConfigProperty(AttributeCollector.PARAM_FROM_HEADER))
			.thenReturn("nodeId:X-Node-Id");

		AttributeCollector collector = new AttributeCollector(config);

		Map<String, List<String>> headers = new HashMap<>();
		headers.put("X-Node-Id", Arrays.asList("node-42"));

		RequestInfo info = new RequestInfo();
		collector.apply(info, headers);

		assertEquals("mdc-tenant", info.getAttribute("tenant"));
		assertEquals("node-42", info.getAttribute("nodeId"));
	}
}
