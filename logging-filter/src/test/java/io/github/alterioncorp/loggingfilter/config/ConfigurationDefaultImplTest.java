package io.github.alterioncorp.loggingfilter.config;

import java.util.Properties;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ConfigurationDefaultImplTest {

	private FilterConfig filterConfig;
	private ServletContext servletContext;
	private Properties properties;
	private ConfigurationDefaultImpl configuration;

	@BeforeEach
	public void before() {
		filterConfig = Mockito.mock(FilterConfig.class);
		servletContext = Mockito.mock(ServletContext.class);
		properties = new Properties();
		configuration = new ConfigurationDefaultImpl(filterConfig, properties);

		Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
	}

	@Test
	public void testGetInstanceName() {

		Mockito.when(servletContext.getContextPath()).thenReturn("/myapp");
		Mockito.when(filterConfig.getFilterName()).thenReturn("loggingFilter");

		assertEquals("/myapp/loggingFilter", configuration.getInstanceName());
	}

	@Test
	public void testGetInstanceName_NullContextPath() {

		Mockito.when(servletContext.getContextPath()).thenReturn(null);
		Mockito.when(filterConfig.getFilterName()).thenReturn("loggingFilter");

		assertEquals("/loggingFilter", configuration.getInstanceName());
	}

	@Test
	public void testGetInstanceName_NullFilterName() {

		Mockito.when(servletContext.getContextPath()).thenReturn("/myapp");
		Mockito.when(filterConfig.getFilterName()).thenReturn(null);

		assertEquals("/myapp/", configuration.getInstanceName());
	}

	@Test
	public void testGetInstanceName_BothNull() {

		Mockito.when(servletContext.getContextPath()).thenReturn(null);
		Mockito.when(filterConfig.getFilterName()).thenReturn(null);

		assertEquals("/", configuration.getInstanceName());
	}
}
