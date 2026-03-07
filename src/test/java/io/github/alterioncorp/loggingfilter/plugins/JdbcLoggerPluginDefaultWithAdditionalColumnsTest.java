package io.github.alterioncorp.loggingfilter.plugins;

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

public class JdbcLoggerPluginDefaultWithAdditionalColumnsTest {

	private static final String ADDITIONAL_COLUMN_NAME = "col1";
	private static final int ADDITIONAL_COLUMN_SQL_TYPE = 69;
	private static final String ADDITIONAL_COLUMN_CREATE_TYPE = "MYTYPE NULL";
	private static final String ADDITIONAL_COLUMN_VALUE = "val123";

	private static class TestPlugin extends JdbcLoggerPluginDefaultWithAdditionalColumns {

		@Override
		protected int getAdditionalColumnCount() {
			return 1;
		}

		@Override
		protected String getAdditionalColumnName(int columnIndex) {

			if (columnIndex == 0) {
				return ADDITIONAL_COLUMN_NAME;
			}
			
			throw new IllegalArgumentException("invalid index: " + columnIndex);
		}

		@Override
		protected int getAdditionalColumnSqlType(int columnIndex) {

			if (columnIndex == 0) {
				return ADDITIONAL_COLUMN_SQL_TYPE;
			}
			
			throw new IllegalArgumentException("invalid index: " + columnIndex);
		}

		@Override
		protected String getAdditionalColumnTypeForTableCreate(int columnIndex) {

			if (columnIndex == 0) {
				return ADDITIONAL_COLUMN_CREATE_TYPE;
			}
			
			throw new IllegalArgumentException("invalid index: " + columnIndex);
		}

		@Override
		protected Object getAdditionalValueToInsert(int columnIndex, RequestInfo requestInfo, ResponseInfo responseInfo,
				HttpServletRequest request, HttpServletResponse response) {
			
			if (columnIndex == 0) {
				return ADDITIONAL_COLUMN_VALUE;
			}
			
			throw new IllegalArgumentException("invalid index: " + columnIndex);
		}
	}
	
	private JdbcLoggerPluginDefaultWithAdditionalColumns plugin;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	@BeforeEach
	public void before() {
		plugin = new TestPlugin();
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
	}
	
	@Test
	public void testGetColumnCount() {
		assertEquals(
				JdbcLoggerPluginDefaultImpl.COLUMN_NAMES.length + 1,
				plugin.getColumnCount());
	}
	
	@Test
	public void testGetColumnName_Default() {
		assertEquals(
				JdbcLoggerPluginDefaultImpl.COLUMN_NAMES[0],
				plugin.getColumnName(0));
	}
	
	@Test
	public void testGetColumnName_Additional() {
		assertEquals(
				ADDITIONAL_COLUMN_NAME,
				plugin.getColumnName(JdbcLoggerPluginDefaultImpl.COLUMN_NAMES.length));
	}
	
	@Test
	public void testGetColumnSqlType_Default() {
		assertEquals(
				JdbcLoggerPluginDefaultImpl.COLUMN_SQL_TYPES[0],
				plugin.getColumnSqlType(0));
	}
	
	@Test
	public void testGetColumnSqlType_Additional() {
		assertEquals(
				ADDITIONAL_COLUMN_SQL_TYPE,
				plugin.getColumnSqlType(JdbcLoggerPluginDefaultImpl.COLUMN_NAMES.length));
	}
	
	@Test
	public void testGetColumnTypeForTableCreate_Default() {
		assertEquals(
				JdbcLoggerPluginDefaultImpl.COLUMN_TYPES_FOR_CREATE[0],
				plugin.getColumnTypeForTableCreate(0));
	}
	
	@Test
	public void testGetColumnTypeForTableCreate_Additional() {
		assertEquals(
				ADDITIONAL_COLUMN_CREATE_TYPE,
				plugin.getColumnTypeForTableCreate(JdbcLoggerPluginDefaultImpl.COLUMN_NAMES.length));
	}
	
	@Test
	public void testGetValueToInsert_Default() {
		
		final Date startTimestamp = new Date();
		
		RequestInfo requestInfo = new RequestInfo();
		ResponseInfo responseInfo = new ResponseInfo();

		requestInfo.setStartTimestamp(startTimestamp);
		
		assertEquals(startTimestamp, plugin.getValueToInsert(1, requestInfo, responseInfo, request, response));
	}
	
	@Test
	public void testGetValueToInsert_Additional() {
		
		RequestInfo requestInfo = new RequestInfo();
		ResponseInfo responseInfo = new ResponseInfo();

		int startIndex = JdbcLoggerPluginDefaultImpl.COLUMN_NAMES.length;
		assertEquals(ADDITIONAL_COLUMN_VALUE, plugin.getValueToInsert(startIndex, requestInfo, responseInfo, request, response));
	}
}
