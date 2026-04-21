# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules and run all tests
mvn package

# Build and test only the core module
mvn package -pl logging-filter

# Build and test only the Vert.x module (core must be installed first)
mvn package -pl logging-filter-vertx -am

# Run tests only (all modules)
mvn test

# Run a single test class (from repo root)
mvn test -pl logging-filter -Dtest=LoggingFilterTest

# Run a single test method
mvn test -pl logging-filter -Dtest=LoggingFilterTest#testDoFilter

# Generate javadocs
mvn javadoc:javadoc

# Skip tests during build
mvn package -DskipTests
```

## Architecture

This is a multi-module Maven project. The root POM (`logging-filter-parent`) aggregates two modules:

- **`logging-filter`** — core library (Jakarta Servlet filter + JAX-RS filter + SLF4J/JDBC loggers)
- **`logging-filter-vertx`** — optional Quarkus/Vert.x integration (`VertxRemoteAddressResolver`)

The core library has no runtime framework dependency — only `jakarta.servlet-api`, `jakarta.ws.rs-api`, `jakarta.enterprise.cdi-api`, and `slf4j-api` are provided-scope.

### Entry points

Two public filter classes:

- **`LoggingFilter`** — Jakarta Servlet `Filter`. Registered in `web.xml`. Has access to `HttpServletRequest` for form params and session ID.
- **`ContainerLoggingFilter`** — JAX-RS `ContainerRequestFilter` + `ContainerResponseFilter`. CDI `@ApplicationScoped`, auto-discovered via `META-INF/beans.xml`. Configured via system properties only.

### Core flow (`LoggingFilter`)

1. Captures request headers and resolves client IP via `ClientIpResolverImpl`
2. Populates `RequestInfo` attributes via `AttributeCollector`
3. Calls `InfoLogger#logRequest` on each configured logger
4. Delegates to the filter chain
5. Captures response metadata into `ResponseInfo`
6. Calls `InfoLogger#logResponse` on each configured logger

### Core flow (`ContainerLoggingFilter`)

Same pattern, but:
- Client IP comes from `X-Forwarded-For` first, then from an optional `RemoteAddressResolver` CDI bean (fallback for direct socket access — see `logging-filter-vertx`).
- No form-body logging, no session ID.

### Stack-neutral SPI

All plugin and logger interfaces operate on `RequestInfo`/`ResponseInfo` only — no `HttpServletRequest`/`HttpServletResponse` parameters. Header and attribute data is captured once at the filter layer and stored on the info objects.

`RemoteAddressResolver` is the one runtime-specific escape hatch, exposed as an optional CDI SPI. `ContainerLoggingFilter` uses `@Inject Instance<RemoteAddressResolver>` so the injection is satisfied even when no implementation is registered.

### Plugin system

Every logger delegates formatting/storage decisions to a plugin:

- **`Slf4JLoggerImpl`** uses a `Slf4JLoggerPlugin` to produce a `List<Object>` of tab-separated values. Default: `Slf4JLoggerPluginDefaultImpl`. Extend this to add fields.
- **`JdbcLoggerImpl`** extends `AbstractBatchLogger` and uses a `JdbcLoggerPlugin` to define the DB schema and column values. Default: `JdbcLoggerPluginDefaultImpl`. Extend `JdbcLoggerPluginDefaultWithAdditionalColumns` to append columns without redefining the base schema.

Plugins are instantiated by class name via `PluginFactoryImpl` (reflection, no-arg constructor required).

### Batch logging

`AbstractBatchLogger` → `BatchQueueImpl`: requests are stored in a `ThreadLocal` and paired with their response on the same thread. The completed pair is added to a synchronized `LinkedList`. A `ScheduledExecutorService` drains the queue every `logging-filter.batch.write_period_in_millis` ms (default 5000) and calls `BatchLogger#logBatch`.

### Configuration

`ConfigurationDefaultImpl` resolves all properties by checking system properties first, then servlet filter `init-param` values. `ConfigurationContainerImpl` reads system properties only (for the JAX-RS path). The key property names are constants on each class (e.g. `LoggingFilter.PROPERTY_LOGGERS`, `JdbcLoggerImpl.PARAM_DATA_SOURCE_JNDI_NAME`).

### JAX-RS entry-point specifics

- **Client IP resolution:** `X-Forwarded-For` header is checked first. If absent, `ContainerLoggingFilter` calls `resolveRemoteAddress()`, which uses CDI `Instance<RemoteAddressResolver>` to find a registered implementation. The standard Quarkus impl is a 10-line `@RequestScoped` class that reads `@Context HttpServerRequest.remoteAddress()` — shipped in `logging-filter-vertx`.
- **`logging-filter-vertx`**: drop the dependency on the classpath; CDI auto-discovers `VertxRemoteAddressResolver` via its `META-INF/beans.xml`. No code changes needed in the consumer.

### JMX

Each `InfoLogger` exposes an `InfoLoggerMXBean` (enable/disable at runtime). The bean name is constructed by `JmxUtils.getBeanName(instanceName, class)` where `instanceName` is `contextPath/filterName` (servlet) or the value of `logging-filter.instance-name` (JAX-RS, defaults to `container-logging-filter`).

### Testing

Tests use JUnit 5 + Mockito. `JdbcLoggerImpl` tests use an in-memory Apache Derby database via the `test-derby` library. The `BatchQueueImpl` and scheduler are exercised with injected `ScheduledExecutorService` mocks. `VertxRemoteAddressResolverTest` uses reflection to inject the mock `HttpServerRequest` field (no CDI container in unit tests).
