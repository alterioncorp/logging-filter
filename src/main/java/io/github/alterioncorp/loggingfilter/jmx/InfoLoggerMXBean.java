package io.github.alterioncorp.loggingfilter.jmx;

/**
 * JMX management interface for {@link io.github.alterioncorp.loggingfilter.loggers.InfoLogger} instances.
 */
public interface InfoLoggerMXBean {

	/**
	 * Returns whether this logger is currently enabled.
	 *
	 * @return {@code true} if this logger is currently enabled
	 */
	boolean isEnabled();

	/**
	 * Enables or disables this logger.
	 *
	 * @param enabled {@code true} to enable this logger, {@code false} to disable it
	 */
	void setEnabled(boolean enabled);

	/**
	 * Returns the fully-qualified JMX object name for this bean.
	 *
	 * @return the fully-qualified JMX object name for this bean
	 */
	String getBeanName();
}
