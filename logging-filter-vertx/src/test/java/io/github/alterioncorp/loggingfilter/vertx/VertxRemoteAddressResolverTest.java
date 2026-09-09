package io.github.alterioncorp.loggingfilter.vertx;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;

public class VertxRemoteAddressResolverTest {

	@Test
	public void testGetRemoteAddress_HappyPath() {

		SocketAddress socketAddress = Mockito.mock(SocketAddress.class);
		Mockito.when(socketAddress.hostAddress()).thenReturn("10.0.0.1");

		HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
		Mockito.when(request.remoteAddress()).thenReturn(socketAddress);

		VertxRemoteAddressResolver resolver = new VertxRemoteAddressResolver();
		injectRequest(resolver, request);

		assertEquals("10.0.0.1", resolver.getRemoteAddress());
	}

	@Test
	public void testGetRemoteAddress_NullRequest() {

		VertxRemoteAddressResolver resolver = new VertxRemoteAddressResolver();
		// request field stays null (no injection)

		assertNull(resolver.getRemoteAddress());
	}

	@Test
	public void testGetRemoteAddress_NullRemoteAddress() {

		HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
		Mockito.when(request.remoteAddress()).thenReturn(null);

		VertxRemoteAddressResolver resolver = new VertxRemoteAddressResolver();
		injectRequest(resolver, request);

		assertNull(resolver.getRemoteAddress());
	}

	private static void injectRequest(VertxRemoteAddressResolver resolver, HttpServerRequest request) {
		try {
			java.lang.reflect.Field field = VertxRemoteAddressResolver.class.getDeclaredField("request");
			field.setAccessible(true);
			field.set(resolver, request);
		}
		catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
