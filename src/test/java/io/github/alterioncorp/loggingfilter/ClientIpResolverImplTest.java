package io.github.alterioncorp.loggingfilter;

import java.util.Arrays;
import java.util.Collections;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ClientIpResolverImplTest {

	private HttpServletRequest request;
	private ClientIpResolverImpl clientIpResolver;
	
	@BeforeEach
	public void before() {
		request = Mockito.mock(HttpServletRequest.class);
		clientIpResolver = new ClientIpResolverImpl();
	}
	
    @Test  		
	public void testGetClientIP_XForwarderFor_NotSet() {
		String remoteIPIn = "1.2.3.4";
		Mockito.when(request.getRemoteAddr()).thenReturn(remoteIPIn);
		String remoteIPOut = clientIpResolver.getClientIP(request);
		assertEquals(remoteIPIn, remoteIPOut);
	}
	
    @Test       
	public void testGetClientIP_XForwarderFor_SingleHeaderValue() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded = "5.6.7.8";
		Mockito.when(request.getRemoteAddr()).thenReturn(remoteIPInOrig);
		Mockito.when(request.getHeaders(ClientIpResolverImpl.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Arrays.asList(remoteIPForwarded)));
		String remoteIPOut = clientIpResolver.getClientIP(request);
		assertEquals(remoteIPForwarded, remoteIPOut);
	}
	
    @Test       
	public void testGetClientIP_XForwarderFor_MultipleHeaderValues() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded1 = "5.6.7.8";
		String remoteIPForwarded2 = "0.0.0.0";
		Mockito.when(request.getRemoteAddr()).thenReturn(remoteIPInOrig);
		Mockito.when(request.getHeaders(ClientIpResolverImpl.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Arrays.asList(remoteIPForwarded1, remoteIPForwarded2)));
		String remoteIPOut = clientIpResolver.getClientIP(request);
		assertEquals(remoteIPForwarded1, remoteIPOut);
	}
	
    @Test       
	public void testGetClientIP_XForwarderFor_SingleHeaderValueWithMultipleAddresses() {
		String remoteIPInOrig = "1.2.3.4";
		String remoteIPForwarded1 = "5.6.7.8";
		String remoteIPForwarded2 = "0.0.0.0";
		Mockito.when(request.getRemoteAddr()).thenReturn(remoteIPInOrig);
		Mockito.when(request.getHeaders(ClientIpResolverImpl.X_FORWARDED_FOR)).thenReturn(Collections.enumeration(Arrays.asList(remoteIPForwarded1 + "," + remoteIPForwarded2)));
		String remoteIPOut = clientIpResolver.getClientIP(request);
		assertEquals(remoteIPForwarded1, remoteIPOut);
	} 
}
