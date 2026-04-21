package io.github.alterioncorp.loggingfilter.config;

/**
 * Abstracts the possible ways to configure the behavior of this filter.
 * <ol>
 * 	<li>system-properties</li>
 * 	<li>filter's init-params (servlet) or MP Config (container)</li>
 * </ol>
 *
 */
public interface Configuration {

	/**
	 * Looks for this property first in system-properties, and if not found, in the filter's init-params.
	 *
	 * @param propertyName the name of the system-property or filter init-param
	 * @return the value of the system-property or filter init-param
	 */
	String getConfigProperty(String propertyName);

	/**
	 * Returns a unique name identifying this filter instance, used for JMX bean naming.
	 *
	 * @return the instance name
	 */
	String getInstanceName();
}
