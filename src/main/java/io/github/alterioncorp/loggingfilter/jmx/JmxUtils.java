package io.github.alterioncorp.loggingfilter.jmx;

import jakarta.servlet.FilterConfig;

/**
 * Utility methods for JMX bean naming.
 */
public final class JmxUtils {

	/** The JMX domain prefix used for all beans registered by this library. */
	public static final String BEAN_PREFIX = "io.github.alterioncorp.loggingfilter";
	
	/**
	 * Returns a unique MBean name for this logger instance.
	 * The name must include the app's context and the filter name to be unique.
	 * 
	 * @param filterConfig the config for the filter
	 * @param type the class of this MBean
	 * @return the unique bean name
	 */
	public static String getBeanName(FilterConfig filterConfig, Class<? extends InfoLoggerMXBean> type) {
		return BEAN_PREFIX +
				":type=" + type.getSimpleName() +
				",context=" + filterConfig.getServletContext().getContextPath() +
				",filter=" + filterConfig.getFilterName();
	}
	
	private JmxUtils() {
	}

}
