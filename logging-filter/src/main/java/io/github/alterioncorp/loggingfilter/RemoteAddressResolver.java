package io.github.alterioncorp.loggingfilter;

/**
 * Optional SPI for resolving the client socket peer address in runtime-specific ways.
 *
 * <p>Implement this interface as a CDI bean to provide a real client IP when
 * {@code X-Forwarded-For} is absent. When no bean is registered the
 * {@link ContainerLoggingFilter} falls back to {@code null}.
 *
 * <p>The standard Quarkus/Vert.x implementation is provided by the
 * {@code logging-filter-vertx} module.
 *
 * @see ContainerLoggingFilter
 */
public interface RemoteAddressResolver {

	/**
	 * Returns the client socket peer address, or {@code null} if unavailable.
	 *
	 * @return the remote address string, or {@code null}
	 */
	String getRemoteAddress();
}
