package io.github.alterioncorp.loggingfilter;

import java.lang.management.ManagementFactory;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.alterioncorp.loggingfilter.config.ConfigurationContainerImpl;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;
import io.github.alterioncorp.loggingfilter.loggers.Slf4JLoggerImpl;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactoryImpl;

/**
 * JAX-RS filter that logs information about request/response round-trips.
 * Auto-discovered via CDI when {@code META-INF/beans.xml} is present with
 * {@code bean-discovery-mode="annotated"}.
 *
 * <p>Limitations compared to the servlet {@link LoggingFilter}:
 * <ul>
 *   <li>Client IP comes from {@code X-Forwarded-For} first, then from an optional
 *       {@link RemoteAddressResolver} CDI bean (e.g. the {@code logging-filter-vertx} module).
 *       If neither is available the IP is {@code null}.</li>
 *   <li>No form-body logging — reading the entity would consume it before the resource sees it.</li>
 *   <li>No session ID — JAX-RS has no session concept.</li>
 * </ul>
 *
 * <p>Runs at {@code @Priority(100)}, i.e. before {@code Priorities.AUTHENTICATION} (1000). Business
 * filters that need to populate MDC/attributes before the request log line is written must run at a
 * lower priority number than 100; because JAX-RS response filters run in reverse request order, those
 * same filters must clear that state late enough to still be visible when this filter's response-side
 * line is written. See CLAUDE.md, "Things that will bite you".
 *
 * <p>A no-op {@link #onStartup} CDI observer forces this bean to be instantiated (and {@link #init()}
 * to run) at application startup rather than on first request, since {@code @PostConstruct} alone is
 * fired lazily by CDI.
 *
 * @see LoggingFilter
 */
@Provider
@ApplicationScoped
@Priority(100)
public class ContainerLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContainerLoggingFilter.class);

	private static final String PROP_REQUEST_INFO = ContainerLoggingFilter.class.getName() + ".requestInfo";

	static final String PROPERTY_LOGGERS = "logging-filter.loggers";
	static final String PROPERTY_PARAM_NAMES_TO_HIDE = "logging-filter.param-names-to-hide";
	static final String HIDDEN_PARAM_VALUE = "*****";

	@Inject
	private Instance<RemoteAddressResolver> remoteAddressResolvers;

	private final PluginFactory pluginFactory;
	private final MBeanServer mBeanServer;
	private final Properties systemProperties;
	private final ClientIpResolver clientIpResolver;

	private List<InfoLogger> loggers;
	private Set<String> paramNamesToHide = new HashSet<>();
	private AttributeCollector attributeCollector;

	/**
	 * Creates a new instance with default dependencies.
	 */
	public ContainerLoggingFilter() {
		this.pluginFactory = new PluginFactoryImpl();
		this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
		this.systemProperties = System.getProperties();
		this.clientIpResolver = new ClientIpResolverImpl();
	}

	ContainerLoggingFilter(PluginFactory pluginFactory, MBeanServer mBeanServer, Properties systemProperties,
			ClientIpResolver clientIpResolver, List<InfoLogger> loggers) {
		this.pluginFactory = pluginFactory;
		this.mBeanServer = mBeanServer;
		this.systemProperties = systemProperties;
		this.clientIpResolver = clientIpResolver;
		this.loggers = loggers;
	}

	/**
	 * Initializes loggers and registers JMX MBeans. Called by the CDI container after construction.
	 */
	@PostConstruct
	public void init() {

		ConfigurationContainerImpl config = new ConfigurationContainerImpl(systemProperties);

		String paramNamesToHideValue = config.getConfigProperty(PROPERTY_PARAM_NAMES_TO_HIDE);
		if (paramNamesToHideValue != null) {
			paramNamesToHide = Arrays.stream(paramNamesToHideValue.split(","))
					.map(String::trim)
					.collect(Collectors.toSet());
		}

		if (loggers == null) {
			String propertyLoggers = config.getConfigProperty(PROPERTY_LOGGERS);
			String[] loggerClasses = propertyLoggers != null
					? propertyLoggers.split(",")
					: new String[]{Slf4JLoggerImpl.class.getName()};

			loggers = new ArrayList<>(loggerClasses.length);
			for (String loggerClass : loggerClasses) {
				loggers.add(pluginFactory.getPlugin(InfoLogger.class, loggerClass.trim()));
			}
		}

		attributeCollector = new AttributeCollector(config);

		for (InfoLogger logger : loggers) {
			logger.init(config);
		}

		for (InfoLogger logger : loggers) {
			try {
				InfoLoggerMXBean mBean = logger.getMBean();
				mBeanServer.registerMBean(mBean, new ObjectName(mBean.getBeanName()));
			}
			catch (JMException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * No-op observer of the CDI application-startup event. Its only purpose is to force the container
	 * to obtain a contextual instance of this bean (and thus run {@link #init()}) eagerly at deployment,
	 * rather than deferring it to the first injection/request as {@code @PostConstruct} alone would.
	 *
	 * @param event the application-scoped initialized event (unused)
	 */
	void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) {
	}

	/**
	 * Unregisters JMX MBeans and destroys loggers. Called by the CDI container before destruction.
	 */
	@PreDestroy
	public void destroy() {

		if (loggers == null) {
			return;
		}

		for (InfoLogger logger : loggers) {
			try {
				InfoLoggerMXBean mBean = logger.getMBean();
				mBeanServer.unregisterMBean(new ObjectName(mBean.getBeanName()));
			}
			catch (JMException e) {
				LOGGER.warn("error unregistering MBean", e);
			}
			logger.destroy();
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext) {

		Map<String, List<String>> headers = requestContext.getHeaders();
		String clientIp = clientIpResolver.getClientIp(headers, resolveRemoteAddress());
		String method = requestContext.getMethod();
		String path = requestContext.getUriInfo().getPath();
		String params = queryParamsToString(requestContext);

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setStartTimestamp(new Date());
		requestInfo.setClientNameOrAddress(clientIp);
		requestInfo.setMethod(method);
		requestInfo.setPath(path);
		requestInfo.setParamsAsString(params);
		requestInfo.setHeaders(headers);

		if (attributeCollector != null) {
			attributeCollector.apply(requestInfo, headers);
		}

		requestContext.setProperty(PROP_REQUEST_INFO, requestInfo);

		for (InfoLogger logger : loggers) {
			if (logger.isEnabled()) {
				logger.logRequest(requestInfo);
			}
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

		RequestInfo requestInfo = (RequestInfo) requestContext.getProperty(PROP_REQUEST_INFO);
		if (requestInfo == null) {
			// aborted request — no request phase ran
			return;
		}

		Map<String, List<String>> requestHeaders = requestInfo.getHeaders();

		ResponseInfo responseInfo = new ResponseInfo();
		responseInfo.setEndTimestamp(new Date());
		responseInfo.setClientNameOrAddress(requestInfo.getClientNameOrAddress());
		responseInfo.setMethod(requestInfo.getMethod());
		responseInfo.setPath(requestInfo.getPath());
		responseInfo.setParamsAsString(requestInfo.getParamsAsString());
		responseInfo.setResponseCode(responseContext.getStatus());

		if (attributeCollector != null) {
			attributeCollector.apply(responseInfo, requestHeaders);
		}

		for (InfoLogger logger : loggers) {
			if (logger.isEnabled()) {
				logger.logResponse(responseInfo);
			}
		}
	}

	private String queryParamsToString(ContainerRequestContext requestContext) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, List<String>> entry : requestContext.getUriInfo().getQueryParameters().entrySet()) {
			String name = entry.getKey();
			for (String value : entry.getValue()) {
				if (!first) sb.append("&");
				sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
				sb.append("=");
				if (paramNamesToHide.contains(name)) {
					sb.append(HIDDEN_PARAM_VALUE);
				} else {
					sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
				}
				first = false;
			}
		}
		return sb.toString();
	}

	Set<String> getParamNamesToHide() {
		return paramNamesToHide;
	}

	private String resolveRemoteAddress() {
		if (remoteAddressResolvers == null || remoteAddressResolvers.isUnsatisfied()) {
			return null;
		}
		if (remoteAddressResolvers.isAmbiguous()) {
			LOGGER.warn("Multiple RemoteAddressResolver beans registered; falling back to X-Forwarded-For only");
			return null;
		}
		try {
			return remoteAddressResolvers.get().getRemoteAddress();
		}
		catch (RuntimeException e) {
			LOGGER.warn("RemoteAddressResolver failed", e);
			return null;
		}
	}

	/**
	 * Test-only hook for injecting a mock {@link Instance} in place of CDI's {@code @Inject}.
	 *
	 * @param remoteAddressResolvers the resolvers instance to use
	 */
	void setRemoteAddressResolvers(Instance<RemoteAddressResolver> remoteAddressResolvers) {
		this.remoteAddressResolvers = remoteAddressResolvers;
	}
}
