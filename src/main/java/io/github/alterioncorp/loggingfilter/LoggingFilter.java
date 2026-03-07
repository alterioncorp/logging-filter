package io.github.alterioncorp.loggingfilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.alterioncorp.loggingfilter.batch.AbstractBatchLogger;
import io.github.alterioncorp.loggingfilter.config.ConfigurationImpl;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;
import io.github.alterioncorp.loggingfilter.loggers.Slf4JLoggerImpl;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactoryImpl;

/**
 * Filter that logs information about request/response round-trips.
 * The actual logging implementation is delegated to a collection of {@link InfoLogger} instances.
 * This class will use a {@link Slf4JLoggerImpl} by default.
 * 
 * The following system-properties or filter init-params customize the behavior of this filter:
 * <ul>
 * 	<li>logging-filter.loggers: A comma-separated list of {@link InfoLogger} classes to use.  If not specified, {@link Slf4JLoggerImpl} will be used.</li>
 * 	<li>logging-filter.param-names-to-hide: a comma-separated list of parameter names whose values should be masked (e.g. password).</li>
 * </ul>
 * 
 * @author alitovsky
 * @see InfoLogger
 * @see Slf4JLoggerImpl
 * @see AbstractBatchLogger
 */
public final class LoggingFilter implements Filter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
	
	static final String PROPERTY_SERVER_NAME = "jboss.node.name";
	static final String PROPERTY_LOGGERS = "logging-filter.loggers";
	static final String PROPERTY_PARAM_NAMES_TO_HIDE = "logging-filter.param-names-to-hide";
	static final String HIDDEN_PARAM_VALUE = "*****";
	
	private final ClientIpResolver clientIpResolver;
	private final PluginFactory pluginFactory;
	private final Properties systemProperties;
	private final MBeanServer mBeanServer;
	
	private List<InfoLogger> loggers;
	private Set<String> paramNamesToHide = new HashSet<>();
	private String serverName;

	public LoggingFilter() {
		super();
		clientIpResolver = new ClientIpResolverImpl();
		pluginFactory = new PluginFactoryImpl();
		systemProperties = System.getProperties();
		mBeanServer = ManagementFactory.getPlatformMBeanServer();
	}

	LoggingFilter(ClientIpResolver clientIpResolver, PluginFactory pluginFactory, Properties properties, List<InfoLogger> loggers, MBeanServer mBeanServer) {
		super();
		this.clientIpResolver = clientIpResolver;
		this.pluginFactory = pluginFactory;
		this.systemProperties = properties;
		this.loggers = loggers;
		this.mBeanServer = mBeanServer;
	}

	@Override
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
   	
    	String requestIP = null;
    	String method = null;
    	String path = null;
    	String params = null;
    	String sessionId = null;
    	int responseStatus;

		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
    	HttpServletResponse httpServletResponse = (HttpServletResponse)response;

    	requestIP = clientIpResolver.getClientIP(httpServletRequest);
    	method = httpServletRequest.getMethod();
        path = httpServletRequest.getRequestURI();
    	params = requestParamsToString(httpServletRequest);
    	sessionId = httpServletRequest.getSession(false) == null ? null : httpServletRequest.getSession().getId();
    	
    	RequestInfo requestInfo = new RequestInfo();
    	requestInfo.setStartTimestamp(new Date());
    	requestInfo.setClientNameOrAddress(requestIP);
    	requestInfo.setServerNameOrAddress(serverName);
    	requestInfo.setMethod(method);
    	requestInfo.setPath(path);
    	requestInfo.setParamsAsString(params);
    	requestInfo.setSessionId(sessionId);
    	
    	for (InfoLogger logger : loggers) {
    		if (logger.isEnabled()) {
    			logger.logRequest(requestInfo, httpServletRequest, httpServletResponse);
    		}
    	}
    	
    	try {
    		chain.doFilter(request, response);
    	}
    	finally {
    		
        	responseStatus = httpServletResponse.getStatus();
        	sessionId = httpServletRequest.getSession(false) == null ? null : httpServletRequest.getSession().getId();

	    	ResponseInfo responseInfo = new ResponseInfo();
	    	responseInfo.setEndTimestamp(new Date());
	    	responseInfo.setClientNameOrAddress(requestIP);
	    	responseInfo.setServerNameOrAddress(serverName);
	    	responseInfo.setMethod(method);
	    	responseInfo.setPath(path);
	    	responseInfo.setParamsAsString(params);
	    	responseInfo.setSessionId(sessionId);
	    	responseInfo.setResponseCode(responseStatus);
	    	
	    	for (InfoLogger logger : loggers) {
	    		if (logger.isEnabled()) {
	    			logger.logResponse(responseInfo, httpServletRequest, httpServletResponse);
	    		}
	    	}
    	}
	}

	@Override
	public final void init(FilterConfig filterConfig) throws ServletException {
		
		ConfigurationImpl config = new ConfigurationImpl(filterConfig, systemProperties);
		
		String paramNamesToHideValue = config.getConfigProperty(PROPERTY_PARAM_NAMES_TO_HIDE);		

		if (paramNamesToHideValue != null) {
			paramNamesToHide = Arrays.stream(paramNamesToHideValue.split(","))
					.map(String::trim)
					.collect(Collectors.toSet());
		}
				
		if (loggers == null) {

			String propertyLoggers = config.getConfigProperty(PROPERTY_LOGGERS);

			String[] loggerClasses;
			if (propertyLoggers != null) {
				loggerClasses = propertyLoggers.split(",");
			}
			else {
				loggerClasses = new String[] {Slf4JLoggerImpl.class.getName()};
			}
			
			loggers = new ArrayList<>(loggerClasses.length);
			for (String loggerClass : loggerClasses) {
				loggers.add(pluginFactory.getPlugin(InfoLogger.class, loggerClass.trim()));
			}
		}
		
		serverName = config.getConfigProperty(PROPERTY_SERVER_NAME);
		
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

	@Override
	public final void destroy() {

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
	
	private String requestParamsToString(HttpServletRequest request) {

        StringBuilder sb = new StringBuilder();

        boolean firstParam = true;
        
        for (String name : request.getParameterMap().keySet()) {
        	for (String value : request.getParameterMap().get(name)) {
                try {
                	if (! firstParam) {
                		sb.append("&");
                	}
	                sb.append(URLEncoder.encode(name, "UTF-8"));
	                sb.append("=");
                	if (paramNamesToHide.contains(name)) {
                		sb.append(HIDDEN_PARAM_VALUE);
                	} else {
                		sb.append(URLEncoder.encode(value, "UTF-8"));
                	}
	                firstParam = false;
                } catch (UnsupportedEncodingException ex) {
                	// OK
                }
        	}
        }
        
        return sb.toString();
    }
		
	Set<String> getParamNamesToHide() {
		return paramNamesToHide;
	}

	String getServerName() {
		return serverName;
	}

	void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
