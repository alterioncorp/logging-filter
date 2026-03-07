package io.github.alterioncorp.loggingfilter.config;

import java.util.Properties;

import jakarta.servlet.FilterConfig;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ConfigurationImplTest {

	private FilterConfig filterConfig;
	private Properties properties;
	private ConfigurationImpl configuration;
	
	@BeforeEach
	public void before() {
		filterConfig = Mockito.mock(FilterConfig.class);
		properties = new Properties();
		configuration = new ConfigurationImpl(filterConfig, properties);
	}
	
	@Test
	public void testGetConfigProperty_FromFilterParam() {
		
		String name = "prop1";
		String value = "val1";
		
		Mockito
			.when(filterConfig.getInitParameter(name))
			.thenReturn(value);
		
		assertEquals(value, configuration.getConfigProperty(name));
	}
	
	@Test
	public void testGetConfigProperty_FromSystemProperty() {
		
		String name = "prop1";
		String value = "val1";
		
		properties.put(name, value);
		
		assertEquals(value, configuration.getConfigProperty(name));
	}
	
	@Test
	public void testGetConfigProperty_SystemPropertyOverrides() {
		
		String name = "prop1";
		String valueFromFilter = "val1";
		String valueFromProps = "val2";
		
		Mockito
			.when(filterConfig.getInitParameter(name))
			.thenReturn(valueFromFilter);
		
		properties.put(name, valueFromProps);
		
		assertEquals(valueFromProps, configuration.getConfigProperty(name));
	}
	
	@Test
	public void testGetFilterConfig() {
		assertEquals(filterConfig, configuration.getFilterConfig());
	}
}
