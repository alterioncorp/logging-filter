package io.github.alterioncorp.loggingfilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientIpResolverImplTest {

	private ClientIpResolverImpl clientIpResolver;

	@BeforeEach
	public void before() {
		clientIpResolver = new ClientIpResolverImpl();
	}

	@Test
	public void testGetClientIp_XForwarderFor_NotSet() {
		String remoteIPIn = "1.2.3.4";
		String remoteIPOut = clientIpResolver.getClientIp(Collections.emptyMap(), remoteIPIn);
		assertEquals(remoteIPIn, remoteIPOut);
	}

	@Test
	public void testGetClientIp_XForwarderFor_SingleHeaderValue() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded = "5.6.7.8";
		Map<String, List<String>> headers = new HashMap<>();
		headers.put(ClientIpResolverImpl.X_FORWARDED_FOR, Arrays.asList(remoteIPForwarded));
		String remoteIPOut = clientIpResolver.getClientIp(headers, remoteIPInOrig);
		assertEquals(remoteIPForwarded, remoteIPOut);
	}

	@Test
	public void testGetClientIp_XForwarderFor_MultipleHeaderValues() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded1 = "5.6.7.8";
		String remoteIPForwarded2 = "0.0.0.0";
		Map<String, List<String>> headers = new HashMap<>();
		headers.put(ClientIpResolverImpl.X_FORWARDED_FOR, Arrays.asList(remoteIPForwarded1, remoteIPForwarded2));
		String remoteIPOut = clientIpResolver.getClientIp(headers, remoteIPInOrig);
		assertEquals(remoteIPForwarded1, remoteIPOut);
	}

	@Test
	public void testGetClientIp_XForwarderFor_SingleHeaderValueWithMultipleAddresses() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded1 = "5.6.7.8";
		String remoteIPForwarded2 = "0.0.0.0";
		Map<String, List<String>> headers = new HashMap<>();
		headers.put(ClientIpResolverImpl.X_FORWARDED_FOR, Arrays.asList(remoteIPForwarded1 + "," + remoteIPForwarded2));
		String remoteIPOut = clientIpResolver.getClientIp(headers, remoteIPInOrig);
		assertEquals(remoteIPForwarded1, remoteIPOut);
	}

	@Test
	public void testGetClientIp_XForwarderFor_CaseInsensitive() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded = "5.6.7.8";
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("x-forwarded-for", Arrays.asList(remoteIPForwarded));
		String remoteIPOut = clientIpResolver.getClientIp(headers, remoteIPInOrig);
		assertEquals(remoteIPForwarded, remoteIPOut);
	}

	@Test
	public void testGetClientIp_NullHeaders() {
		String remoteIPIn = "1.2.3.4";
		String remoteIPOut = clientIpResolver.getClientIp(null, remoteIPIn);
		assertEquals(remoteIPIn, remoteIPOut);
	}
}
