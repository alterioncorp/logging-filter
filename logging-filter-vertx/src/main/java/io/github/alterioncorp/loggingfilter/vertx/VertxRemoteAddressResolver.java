package io.github.alterioncorp.loggingfilter.vertx;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;

import io.github.alterioncorp.loggingfilter.RemoteAddressResolver;
import io.vertx.core.http.HttpServerRequest;

/**
 * {@link RemoteAddressResolver} implementation for Quarkus / Vert.x deployments.
 *
 * <p>Reads the client socket peer address directly from the Vert.x
 * {@link HttpServerRequest} injected via JAX-RS {@code @Context}. This gives a
 * real remote address even when {@code X-Forwarded-For} is absent, making it
 * suitable for deployments where the Vert.x server accepts connections directly
 * (no reverse proxy in front).
 *
 * <p>This bean is auto-discovered when the {@code logging-filter-vertx} JAR is
 * on the classpath (via {@code META-INF/beans.xml}).
 *
 * @see RemoteAddressResolver
 */
@RequestScoped
public class VertxRemoteAddressResolver implements RemoteAddressResolver {

	@Context
	private HttpServerRequest request;

	/**
	 * Creates a new instance. CDI-managed; use injection rather than direct construction.
	 */
	public VertxRemoteAddressResolver() {
	}

	@Override
	public String getRemoteAddress() {
		if (request == null || request.remoteAddress() == null) {
			return null;
		}
		return request.remoteAddress().hostAddress();
	}
}
