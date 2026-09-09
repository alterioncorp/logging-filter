package io.github.alterioncorp.loggingfilter.jmx;

/**
 * Utility methods for JMX bean naming.
 */
public final class JmxUtils {

	/** The JMX domain prefix used for all beans registered by this library. */
	public static final String BEAN_PREFIX = "io.github.alterioncorp.loggingfilter";

	/**
	 * Returns a unique MBean name for this logger instance.
	 * Values containing JMX special characters ({@code : , = " * ?}) are quoted.
	 *
	 * @param instanceName a unique name identifying the filter instance (e.g. contextPath + "/" + filterName)
	 * @param type the class of this MBean
	 * @return the unique bean name
	 */
	public static String getBeanName(String instanceName, Class<? extends InfoLoggerMXBean> type) {
		String sanitized = needsQuoting(instanceName)
				? "\"" + instanceName.replace("\"", "\\\"") + "\""
				: instanceName;
		return BEAN_PREFIX + ":type=" + type.getSimpleName() + ",instance=" + sanitized;
	}

	private static boolean needsQuoting(String value) {
		if (value == null) return false;
		for (char c : new char[] {':', ',', '=', '"', '*', '?'}) {
			if (value.indexOf(c) >= 0) return true;
		}
		return false;
	}

	private JmxUtils() {
	}
}
