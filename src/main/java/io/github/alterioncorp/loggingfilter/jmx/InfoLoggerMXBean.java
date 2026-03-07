package io.github.alterioncorp.loggingfilter.jmx;

/**
 * JMX management interface for {@link io.github.alterioncorp.loggingfilter.loggers.InfoLogger} instances.
 */
public interface InfoLoggerMXBean {

	/** @return {@code true} if this logger is currently enabled */
	boolean isEnabled();

	/** @param enabled {@code true} to enable this logger, {@code false} to disable it */
	void setEnabled(boolean enabled);

	/** @return the fully-qualified JMX object name for this bean */
	String getBeanName();
}
