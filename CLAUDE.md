# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build and run all tests
mvn package

# Run tests only
mvn test

# Run a single test class
mvn test -Dtest=LoggingFilterTest

# Run a single test method
mvn test -Dtest=LoggingFilterTest#testDoFilter

# Generate javadocs
mvn javadoc:javadoc

# Skip tests during build
mvn package -DskipTests
```

## Architecture

This is a Jakarta Servlet filter library (`LoggingFilter`) that logs HTTP request/response pairs. It has no runtime framework dependency — only `jakarta.servlet-api` and `slf4j-api` are provided-scope.

### Core flow

`LoggingFilter` (the only public `Filter`) intercepts each request:
1. Captures request metadata into a `RequestInfo` via `ClientIpResolverImpl`
2. Calls `InfoLogger#logRequest` on each configured logger
3. Delegates to the filter chain
4. Captures response metadata into a `ResponseInfo`
5. Calls `InfoLogger#logResponse` on each configured logger

### Plugin system

Every logger delegates formatting/storage decisions to a plugin:

- **`Slf4JLoggerImpl`** uses a `Slf4JLoggerPlugin` to produce a `List<Object>` of tab-separated values. Default: `Slf4JLoggerPluginDefaultImpl`. Extend this to add fields.
- **`JdbcLoggerImpl`** extends `AbstractBatchLogger` and uses a `JdbcLoggerPlugin` to define the DB schema and column values. Default: `JdbcLoggerPluginDefaultImpl`. Extend `JdbcLoggerPluginDefaultWithAdditionalColumns` to append columns without redefining the base schema.

Plugins are instantiated by class name via `PluginFactoryImpl` (reflection, no-arg constructor required).

### Batch logging

`AbstractBatchLogger` → `BatchQueueImpl`: requests are stored in a `ThreadLocal` and paired with their response on the same thread. The completed pair is added to a synchronized `LinkedList`. A `ScheduledExecutorService` drains the queue every `logging-filter.batch.write_period_in_millis` ms (default 5000) and calls `BatchLogger#logBatch`.

### Configuration

`ConfigurationImpl` resolves all properties by checking system properties first, then servlet filter `init-param` values. The key property names are constants on each class (e.g. `LoggingFilter.PROPERTY_LOGGERS`, `JdbcLoggerImpl.PARAM_DATA_SOURCE_JNDI_NAME`).

### JMX

Each `InfoLogger` exposes an `InfoLoggerMXBean` (enable/disable at runtime). The bean name is constructed by `JmxUtils.getBeanName` using the servlet context path and filter name to ensure uniqueness per deployment.

### Testing

Tests use JUnit 5 + Mockito. `JdbcLoggerImpl` tests use an in-memory Apache Derby database via the `test-derby` library. The `BatchQueueImpl` and scheduler are exercised with injected `ScheduledExecutorService` mocks.
