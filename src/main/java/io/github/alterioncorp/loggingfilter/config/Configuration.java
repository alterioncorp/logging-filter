package io.github.alterioncorp.loggingfilter.config;

import jakarta.servlet.FilterConfig;

/**
 * Abstracts the possible ways to configure the behavior of this filter.
 * <ol>
 * 	<li>system-properties</li>
 * 	<li>filter's init-params</li>
 * </ol>
 * 
 * @author alitovsky
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
	 * Returns the configuration for this instance of the logging filter.
	 * 
	 * @return the config
	 */
	FilterConfig getFilterConfig();

}
