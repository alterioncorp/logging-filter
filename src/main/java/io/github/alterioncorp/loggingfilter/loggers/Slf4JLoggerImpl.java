package io.github.alterioncorp.loggingfilter.loggers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.alterioncorp.loggingfilter.LoggingFilter;
import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.jmx.JmxUtils;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactoryImpl;
import io.github.alterioncorp.loggingfilter.plugins.Slf4JLoggerPlugin;
import io.github.alterioncorp.loggingfilter.plugins.Slf4JLoggerPluginDefaultImpl;

/**
 * Logger that writes 2 log entries to SLF4J: one for a request and another for a response.
 * 
 * The following system-properties or filter init-params customize the behavior of this class:
 * <ul>
 * 	<li>logging-filter.slf4j.plugin: the class of the {@link Slf4JLoggerPlugin} to use.  If not set, {@link Slf4JLoggerPluginDefaultImpl} will be used.</li>
 * </ul>
 * 
 * @see Slf4JLoggerPlugin
 * @see Slf4JLoggerPluginDefaultImpl
 *
 */
public final class Slf4JLoggerImpl implements InfoLogger, InfoLoggerMXBean {
	
	static final String PARAM_PLUGIN = "logging-filter.slf4j.plugin";

	private final Logger loggerRequest;
	private final Logger loggerResponse;
	private final PluginFactory pluginFactory;
	private Slf4JLoggerPlugin plugin;
	private volatile boolean enabled = true;
	private Configuration configuration;
	
	/**
	 * Creates a new instance using the default SLF4J loggers and plugin factory.
	 */
	public Slf4JLoggerImpl() {
		loggerRequest = LoggerFactory.getLogger(LoggingFilter.class.getName() + ".rqst");
		loggerResponse = LoggerFactory.getLogger(LoggingFilter.class.getName() + ".resp");
		pluginFactory = new PluginFactoryImpl();
	}

	Slf4JLoggerImpl(Logger loggerRequest, Logger loggerResponse, PluginFactory pluginFactory, Slf4JLoggerPlugin plugin) {
		this.loggerRequest = loggerRequest;
		this.loggerResponse = loggerResponse;
		this.pluginFactory = pluginFactory;
		this.plugin = plugin;
	}

	@Override
	public void init(Configuration config) {
		
		this.configuration = config;
		
		if (plugin == null) {
			String pluginName = config.getConfigProperty(PARAM_PLUGIN);
			if (pluginName == null) {
				pluginName = Slf4JLoggerPluginDefaultImpl.class.getName();
			}
			plugin = pluginFactory.getPlugin(Slf4JLoggerPlugin.class, pluginName);
		}
		
		plugin.init(config);
	}

	@Override
	public void destroy() {
		plugin.destroy();
	}

	@Override
	public void logRequest(RequestInfo info, HttpServletRequest request, HttpServletResponse response) {
		
    	List<Object> valuesToLog = plugin.getRequestValuesToLog(info, request, response);

    	loggerRequest.info(logDataToString(valuesToLog));
    	
    	this.logRequestHeaders(request);
	}

	@Override
	public void logResponse(ResponseInfo info, HttpServletRequest request, HttpServletResponse response) {
		
    	List<Object> valuesToLog = plugin.getResponseValuesToLog(info, request, response);

    	loggerResponse.info(logDataToString(valuesToLog));	
    	
    	this.logResponseHeaders(response);
	}
	
	private void logRequestHeaders(HttpServletRequest httpServletRequest) {
    	if (loggerRequest.isDebugEnabled()) {
	    	StringBuilder reqHeaders = new StringBuilder("request headers:\n");
	    	for (String headerName : Collections.list(httpServletRequest.getHeaderNames())) {
	    		reqHeaders.append(headerName);
	    		reqHeaders.append("=");
	    		reqHeaders.append(String.join(",", Collections.list(httpServletRequest.getHeaders(headerName))));
	    		reqHeaders.append("\n");
	    	}
	    	loggerRequest.debug(reqHeaders.toString());
    	}
	}
	
	private void logResponseHeaders(HttpServletResponse httpServletResponse) {
    	if (loggerResponse.isDebugEnabled()) {
	    	StringBuilder respHeaders = new StringBuilder("response headers:\n");
	    	for (String headerName : httpServletResponse.getHeaderNames()) {
	    		respHeaders.append(headerName);
	    		respHeaders.append("=");
	    		respHeaders.append(String.join(",", httpServletResponse.getHeaders(headerName)));
	    		respHeaders.append("\n");
	    	}
	    	loggerResponse.debug(respHeaders.toString());
    	}
	}
	
	private static String logDataToString(List<Object> valuesToLog) {
		return valuesToLog.stream()
				.map(String::valueOf)
				.collect(Collectors.joining("\t"));
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getBeanName() {
		return JmxUtils.getBeanName(configuration.getFilterConfig(), this.getClass());
	}

	@Override
	public InfoLoggerMXBean getMBean() {
		return this;
	}

	Configuration getConfiguration() {
		return configuration;
	}

	void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
}
