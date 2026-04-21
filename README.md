# logging-filter

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![codecov](https://codecov.io/gh/alterioncorp/logging-filter/branch/main/graph/badge.svg)](https://codecov.io/gh/alterioncorp/logging-filter)

A Jakarta Servlet and JAX-RS filter that logs HTTP request/response pairs. Supports SLF4J and JDBC backends out of the box, with a plugin API for customization.

## Installation

```xml
<dependency>
    <groupId>io.github.alterioncorp</groupId>
    <artifactId>logging-filter</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Setup

### Servlet (`LoggingFilter`)

Register the filter in `web.xml`:

```xml
<filter>
    <filter-name>LoggingFilter</filter-name>
    <filter-class>io.github.alterioncorp.loggingfilter.LoggingFilter</filter-class>
</filter>

<filter-mapping>
    <filter-name>LoggingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

By default, the filter logs to SLF4J. To enable JDBC logging (or use multiple loggers):

```xml
<filter>
    <filter-name>LoggingFilter</filter-name>
    <filter-class>io.github.alterioncorp.loggingfilter.LoggingFilter</filter-class>
    <init-param>
        <param-name>logging-filter.loggers</param-name>
        <param-value>io.github.alterioncorp.loggingfilter.loggers.JdbcLoggerImpl</param-value>
    </init-param>
</filter>
```

All configuration properties can be set as servlet `init-param` values or as JVM system properties (system properties take precedence).

### JAX-RS (`ContainerLoggingFilter`)

`ContainerLoggingFilter` is a CDI-managed `@ApplicationScoped` bean annotated with `@Provider`. It is auto-discovered when `META-INF/beans.xml` is present with `bean-discovery-mode="annotated"` — no explicit registration needed.

Configuration is read exclusively from JVM system properties (no `web.xml` equivalent for JAX-RS). The same property names apply.

**Limitations compared to the servlet filter:**
- Client IP comes from `X-Forwarded-For` only (no `RemoteAddr` fallback).
- No form-body logging.
- No session ID — JAX-RS has no session concept.

## Configuration

### General

| Property | Default | Description |
|---|---|---|
| `logging-filter.loggers` | `Slf4JLoggerImpl` | Comma-separated list of `InfoLogger` implementation class names |
| `logging-filter.param-names-to-hide` | _(none)_ | Comma-separated list of query parameter names whose values will be masked (e.g. `password`) |
| `jboss.node.name` | _(none)_ | Server name to include in each log entry |

### Attributes

Attributes are key-value strings collected from MDC, request headers, or system properties and attached to each `RequestInfo`/`ResponseInfo`. They can be referenced by logger plugins (e.g. prepended to SLF4J output).

| Property | Default | Description |
|---|---|---|
| `logging-filter.attributes.from-mdc` | _(none)_ | Comma-separated MDC key names to capture |
| `logging-filter.attributes.from-header` | _(none)_ | Comma-separated `attrName:HeaderName` pairs (use bare `name` when attr name equals header name) |
| `logging-filter.attributes.from-sysprop` | _(none)_ | Comma-separated `attrName:syspropName` pairs |

Header lookup is case-insensitive.

### SLF4J logger

Entries are logged at `INFO` level. Request headers are logged at `DEBUG` level.

| Property | Default | Description |
|---|---|---|
| `logging-filter.slf4j.plugin` | `Slf4JLoggerPluginDefaultImpl` | Plugin class to use |
| `logging-filter.slf4j.show-session-id` | `false` | Include the HTTP session ID in each log entry |
| `logging-filter.slf4j.prepend-attributes` | _(none)_ | Comma-separated attribute names to prepend to each log entry (missing values appear as `---`) |

Each request/response pair produces two log entries (tab-separated) under the loggers `LoggingFilter.rqst` and `LoggingFilter.resp`:

```
2024-03-07 10:23:44.801 INFO  LoggingFilter.rqst - 192.168.1.42	---	POST	/api/users?role=admin
2024-03-07 10:23:44.923 INFO  LoggingFilter.resp - 192.168.1.42	201	POST	/api/users?role=admin
```

With `logging-filter.param-names-to-hide=password` and `logging-filter.slf4j.show-session-id=true`:

```
2024-03-07 10:23:44.801 INFO  LoggingFilter.rqst - 192.168.1.42	---	POST	/login?username=alice&password=*****	A1B2C3D4E5F6
2024-03-07 10:23:44.923 INFO  LoggingFilter.resp - 192.168.1.42	200	POST	/login?username=alice&password=*****	A1B2C3D4E5F6
```

With `logging-filter.attributes.from-mdc=tenantId` and `logging-filter.slf4j.prepend-attributes=tenantId`:

```
2024-03-07 10:23:44.801 INFO  LoggingFilter.rqst - acme	192.168.1.42	---	POST	/api/users
2024-03-07 10:23:44.923 INFO  LoggingFilter.resp - acme	192.168.1.42	201	POST	/api/users
```

Request headers are logged at `DEBUG` level when debug logging is enabled for those loggers. The request entry uses `---` as the response code placeholder.

### JDBC logger

The JDBC logger queues records in memory and writes them to the database in batches asynchronously. The log table is created automatically if it does not exist.

| Property | Default | Description |
|---|---|---|
| `logging-filter.jdbc.data_source_jndi` | `java:jboss/datasources/RequestLog` | JNDI name of the `DataSource` to use |
| `logging-filter.jdbc.plugin` | `JdbcLoggerPluginDefaultImpl` | Plugin class to use |
| `logging-filter.batch.write_period_in_millis` | `5000` | How often (ms) the in-memory queue is flushed to the database |

A sample row in the default table (`REQUEST_LOG_1`):

| ID | START_TIMESTAMP | CLIENT_INFO | SERVER_INFO | METHOD | PATH | PARAMS | DURATION | RESPONSE_STATUS | SESSION_ID |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 2024-03-07 10:23:44.801 | 192.168.1.42 | node-1 | POST | /api/users | role=admin | 122 | 201 | A1B2C3D4E5F6 |

**Schema** (`REQUEST_LOG_1`):

| Column | Type | Description |
|---|---|---|
| `ID` | `BIGINT IDENTITY` | Auto-generated primary key |
| `START_TIMESTAMP` | `DATETIME` | When the request was received |
| `CLIENT_INFO` | `NVARCHAR(256)` | Client IP (from `X-Forwarded-For` or `RemoteAddr`) |
| `SERVER_INFO` | `NVARCHAR(256)` | Server name (`jboss.node.name`) |
| `METHOD` | `NVARCHAR(8)` | HTTP method |
| `PATH` | `NVARCHAR(256)` | Request URI |
| `PARAMS` | `NVARCHAR(MAX)` | URL-encoded query string |
| `DURATION` | `INT` | Request duration in milliseconds |
| `RESPONSE_STATUS` | `SMALLINT` | HTTP response status code |
| `SESSION_ID` | `NVARCHAR(256)` | HTTP session ID at response time |

## Customization

### Adding SLF4J fields

Extend `Slf4JLoggerPluginDefaultImpl` and override `getRequestValuesToLog` / `getResponseValuesToLog`:

```java
public class MySlf4JPlugin extends Slf4JLoggerPluginDefaultImpl {

    @Override
    public List<Object> getRequestValuesToLog(RequestInfo info) {
        List<Object> values = super.getRequestValuesToLog(info);
        values.add(info.getAttribute("correlationId"));
        return values;
    }
}
```

Then configure:
```xml
<init-param>
    <param-name>logging-filter.slf4j.plugin</param-name>
    <param-value>com.example.MySlf4JPlugin</param-value>
</init-param>
```

To make custom fields available via `info.getAttribute(...)`, configure the attribute collector:
```xml
<init-param>
    <param-name>logging-filter.attributes.from-header</param-name>
    <param-value>correlationId:X-Correlation-Id</param-value>
</init-param>
```

### Adding JDBC columns

Extend `JdbcLoggerPluginDefaultWithAdditionalColumns`:

```java
public class MyJdbcPlugin extends JdbcLoggerPluginDefaultWithAdditionalColumns {

    @Override
    protected int getAdditionalColumnCount() { return 1; }

    @Override
    protected String getAdditionalColumnName(int i) { return "CORRELATION_ID"; }

    @Override
    protected int getAdditionalColumnSqlType(int i) { return Types.NVARCHAR; }

    @Override
    protected String getAdditionalColumnTypeForTableCreate(int i) { return "NVARCHAR(256)"; }

    @Override
    protected Object getAdditionalValueToInsert(int i, RequestInfo requestInfo, ResponseInfo responseInfo) {
        return requestInfo.getAttribute("correlationId");
    }
}
```

Changing the schema requires overriding `getDataVersion()` to return a new value — this causes a new table to be created.

## JMX

Each logger registers an MBean that lets you enable or disable it at runtime without restarting the application. The object name pattern is:

```
io.github.alterioncorp.loggingfilter:type=<LoggerClass>,instance=<contextPath>/<filterName>
```

## License

Apache License 2.0
