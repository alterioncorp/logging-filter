package io.github.alterioncorp.loggingfilter.plugins;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

public class JdbcLoggerPluginDefaultImplTest {

	private JdbcLoggerPluginDefaultImpl plugin;

	@BeforeEach
	public void before() {
		plugin = new JdbcLoggerPluginDefaultImpl();
	}

	@Test
	public void testGetColumnCount() {
		assertEquals(JdbcLoggerPluginDefaultImpl.COLUMN_NAMES.length, plugin.getColumnCount());
		assertEquals(JdbcLoggerPluginDefaultImpl.COLUMN_SQL_TYPES.length, plugin.getColumnCount());
		assertEquals(JdbcLoggerPluginDefaultImpl.COLUMN_TYPES_FOR_CREATE.length, plugin.getColumnCount());
	}

	@Test
	public void testGetColumnName() {
		for (int i = 0; i < plugin.getColumnCount(); i++) {
			assertEquals(JdbcLoggerPluginDefaultImpl.COLUMN_NAMES[i], plugin.getColumnName(i));
		}
	}

	@Test
	public void testGetColumnSqlType() {
		for (int i = 0; i < plugin.getColumnCount(); i++) {
			assertEquals(JdbcLoggerPluginDefaultImpl.COLUMN_SQL_TYPES[i], plugin.getColumnSqlType(i));
		}
	}

	@Test
	public void testGetColumnTypeForTableCreate() {
		for (int i = 0; i < plugin.getColumnCount(); i++) {
			assertEquals(JdbcLoggerPluginDefaultImpl.COLUMN_TYPES_FOR_CREATE[i], plugin.getColumnTypeForTableCreate(i));
		}
	}

	@Test
	public void testGetDataVersion() {
		assertEquals(JdbcLoggerPluginDefaultImpl.VERSION, plugin.getDataVersion());
	}

	@Test
	public void testGetTableExistsSqlPrefix() {

		final String tableName = "test123";

		String sql = plugin.getTableExistsSqlPrefix(tableName);
		assertTrue(sql.toLowerCase().contains("if not exists"));
		assertTrue(sql.toLowerCase().contains(tableName));
	}

	@Test
	public void testGetTableExistsSqlSuffix() {

		final String tableName = "test123";

		String sql = plugin.getTableExistsSqlSuffix(tableName);
		assertNotNull(sql);
	}

	public void testGetValueToInsert_0() {
		assertNull(plugin.getValueToInsert(0, new RequestInfo(), new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_1() {

		final Date startTimestamp = new Date();

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setStartTimestamp(startTimestamp);

		assertEquals(startTimestamp, plugin.getValueToInsert(1, requestInfo, new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_2() {

		final String ip = "1.2.3.4";

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setClientNameOrAddress(ip);

		assertEquals(ip, plugin.getValueToInsert(2, requestInfo, new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_3() {

		final String ip = "1.2.3.4";

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setServerNameOrAddress(ip);

		assertEquals(ip, plugin.getValueToInsert(3, requestInfo, new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_4() {

		final String method = "GET";

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setMethod(method);

		assertEquals(method, plugin.getValueToInsert(4, requestInfo, new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_5() {

		final String path = "/hello/world";

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setPath(path);

		assertEquals(path, plugin.getValueToInsert(5, requestInfo, new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_6() {

		final String params = "a=b&c=d";

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setParamsAsString(params);

		assertEquals(params, plugin.getValueToInsert(6, requestInfo, new ResponseInfo()));
	}

	@Test
	public void testGetValueToInsert_7() {

		final long duration = 1000L;
		final long millis = System.currentTimeMillis();
		final Date startTime = new Date(millis - duration);
		final Date endTime = new Date(millis);

		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setStartTimestamp(startTime);

		ResponseInfo responseInfo = new ResponseInfo();
		responseInfo.setEndTimestamp(endTime);

		assertEquals(Long.valueOf(duration), plugin.getValueToInsert(7, requestInfo, responseInfo));
	}

	@Test
	public void testGetValueToInsert_8() {

		final int responseCode = 200;

		ResponseInfo responseInfo = new ResponseInfo();
		responseInfo.setResponseCode(responseCode);

		assertEquals(Integer.valueOf(responseCode), plugin.getValueToInsert(8, new RequestInfo(), responseInfo));
	}

	@Test
	public void testGetValueToInsert_9() {

		final String sessionId = "abc123";

		ResponseInfo responseInfo = new ResponseInfo();
		responseInfo.setSessionId(sessionId);

		assertEquals(sessionId, plugin.getValueToInsert(9, new RequestInfo(), responseInfo));
	}

	@Test
	public void testGetValueToInsert_10() {
		assertThrows(IllegalArgumentException.class, () -> plugin.getValueToInsert(10, new RequestInfo(), new ResponseInfo()));
	}
}
