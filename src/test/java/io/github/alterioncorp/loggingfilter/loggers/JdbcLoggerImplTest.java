package io.github.alterioncorp.loggingfilter.loggers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.test.derby.DerbyEmbeddedUtils;

import io.github.alterioncorp.loggingfilter.batch.BatchQueue;
import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.plugins.JdbcLoggerPlugin;
import io.github.alterioncorp.loggingfilter.plugins.JdbcLoggerPluginDefaultImpl;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;

public class JdbcLoggerImplTest {
	
	public static class PluginTestImpl implements JdbcLoggerPlugin {
		
		private static final String[] COLUMN_NAMES = new String[] {
				"COLUMN_ID",
				"COLUMN_DATA1",
				"COLUMN_DATA2",
		};
		
		private static final int[] COLUMN_SQL_TYPES = new int[] {
				Types.BIGINT,
				Types.VARCHAR,
				Types.VARCHAR
		};

		private static final String[] COLUMN_CREATE_TYPES = new String[] {
				"INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY",
				"VARCHAR(64)",
				"VARCHAR(64)",
		};

		@Override
		public void init(Configuration config) {
		}

		@Override
		public void destroy() {
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}

		@Override
		public int getColumnSqlType(int columnIndex) {
			return COLUMN_SQL_TYPES[columnIndex];
		}

		@Override
		public String getColumnTypeForTableCreate(int columnIndex) {
			return COLUMN_CREATE_TYPES[columnIndex];
		}

		@Override
		public Object getValueToInsert(int columnIndex, RequestInfo requestInfo, ResponseInfo responseInfo,
				HttpServletRequest request, HttpServletResponse response) {
			
			Object value;
			
			if (columnIndex == 1) {
				value = DATA_VALUE;
			}
			else if (columnIndex == 2) {
				value = null;
			}
			else {
				throw new IllegalArgumentException("invalid columnIndex: " + columnIndex);
			}
			
			return value;
		}

		@Override
		public String getDataVersion() {
			return DATA_VERSION;
		}

		@Override
		public String getTableExistsSqlPrefix(String tableName) {
			return "";
		}

		@Override
		public String getTableExistsSqlSuffix(String tableName) {
			return "";
		}

		@Override
		public void onLogRequestStart(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response) {
		}

		@Override
		public void onLogRequestEnd(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response) {
		}

		@Override
		public void onLogResponseStart(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response) {
		}

		@Override
		public void onLogResponseEnd(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response) {
		}
	}
	
	private static final String DB_NAME = JdbcLoggerImplTest.class.getSimpleName();
	private static final String DATA_VERSION = "1a";
	private static final String DATA_VALUE = "Test123";

	@BeforeAll
	public static void beforeClass() throws Exception {
		DerbyEmbeddedUtils.createDatabase(DB_NAME);
	}

	@AfterAll
	public static void afterClass() throws Exception {
		DerbyEmbeddedUtils.dropDatabase(DB_NAME);
	}
	
	private Connection connection;
	private Context context;
	private DataSource dataSource;
	private PluginFactory pluginFactory;
	private JdbcLoggerPlugin plugin;
	private BatchQueue<Object> queue;
	private JdbcLoggerImpl logger;
	private Configuration config;
	
	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() throws Exception {

		connection = DerbyEmbeddedUtils.openConnection(DB_NAME);
		dataSource = DerbyEmbeddedUtils.createDataSource(DB_NAME);
		context = Mockito.mock(Context.class);
		pluginFactory = Mockito.mock(PluginFactory.class);
		plugin = new PluginTestImpl();
		queue = Mockito.mock(BatchQueue.class);
		logger = new JdbcLoggerImpl(context, dataSource, pluginFactory, plugin, queue);
		config = Mockito.mock(Configuration.class);

		Mockito
			.when(context.lookup(Mockito.anyString()))
			.thenReturn(dataSource);

		Mockito
			.when(pluginFactory.getPlugin(Mockito.eq(JdbcLoggerPlugin.class), Mockito.anyString()))
			.thenReturn(plugin);
	}
	
	@AfterEach
	public void after() throws Exception {
		if (connection != null) {
			this.dropTableIfExists();
			connection.close();
		}
	}
	
	@Test
	public void testOnInit_CreateTable() throws Exception {
		logger.onInit(config);
		assertTrue(this.isTableExists());
	}
	
	@Test
	public void testOnInit_TableAlreadyExists() throws Exception {
		
		logger.createTableIfNeeded();

		try {
			logger.onInit(config);
			fail();
		}
		catch (RuntimeException e) {
			assertNotNull(e.getCause());
			assertTrue(e.getCause() instanceof SQLException);
			SQLException cause = (SQLException)e.getCause();
			assertEquals("X0Y32", cause.getSQLState());
		}
	}
	
	@Test
	public void testCreatePlugin_DefaultPlugin() throws Exception {
		
		logger = new JdbcLoggerImpl(context, dataSource, pluginFactory, null, queue);
		
		logger.createPlugin(pluginFactory, config);
		
		Mockito.verify(pluginFactory).getPlugin(JdbcLoggerPlugin.class, JdbcLoggerPluginDefaultImpl.class.getName());
	}
	
	@Test
	public void testCreatePlugin_CustomPlugin() throws Exception {

		final String pluginClassName = "test123";
		
		Mockito
			.when(config.getConfigProperty(JdbcLoggerImpl.PARAM_PLUGIN))
			.thenReturn(pluginClassName);
		
		logger = new JdbcLoggerImpl(context, dataSource, pluginFactory, null, queue);
		
		logger.createPlugin(pluginFactory, config);
		
		Mockito.verify(pluginFactory).getPlugin(JdbcLoggerPlugin.class, pluginClassName);
	}
	
	@Test
	public void testOnInit_DataSourceDefaultJndi() throws Exception {
		
		logger.onInit(config);
		
		Mockito.verify(context).lookup(JdbcLoggerImpl.DEFAULT_DATA_SOURCE_JNDI_NAME);
	}
	
	@Test
	public void testOnInit_DataSourceCustomJndi() throws Exception {
		
		final String jndiName = "test123";
		
		Mockito
			.when(config.getConfigProperty(JdbcLoggerImpl.PARAM_DATA_SOURCE_JNDI_NAME))
			.thenReturn(jndiName);
		
		logger.onInit(config);
		
		Mockito.verify(context).lookup(jndiName);
	}
			
	@Test
	public void testLogBatch() throws Exception {
		
		logger.createTableIfNeeded();
		
		Object[] data0 = new Object[] {null, "A1", "B1"};
		Object[] data1 = new Object[] {null, "A2", null};
		
		logger.logBatch(Arrays.asList(data0, data1));
		
		List<Object[]> loggedData = this.getRecords();
		assertEquals(2, loggedData.size());

		assertNotNull(loggedData.get(0)[0]);
		assertEquals(data0[1], loggedData.get(0)[1]);
		assertEquals(data0[2], loggedData.get(0)[2]);

		assertNotNull(loggedData.get(1)[0]);
		assertEquals(data1[1], loggedData.get(1)[1]);
		assertEquals(data1[2], loggedData.get(1)[2]);
	}
		
	private void dropTableIfExists() throws SQLException {
		
		if (this.isTableExists()) {

			String sql = "DROP TABLE " + logger.getTableName();
			
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate(sql);
			}
		}
	}
	
	private boolean isTableExists() throws SQLException {
		
		boolean exists = false;
		
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		try (ResultSet resultSet = databaseMetaData.getTables(null, null, null, new String[] {"TABLE"})) {
			while (resultSet.next()) {
				if (resultSet.getString("TABLE_NAME").equalsIgnoreCase(logger.getTableName())) {
					exists = true;
					break;
				}
			}
		}
		
		return exists;
	}
	
	private List<Object[]> getRecords() throws SQLException {

		LinkedList<Object[]> results = new LinkedList<>();
		
		try (Statement statement = connection.createStatement()) {
			try (ResultSet resultSet = statement.executeQuery("select * from " + logger.getTableName())) {
				while (resultSet.next()) {
					results.add(new Object[] {
							resultSet.getInt(1),
							resultSet.getString(2),
							resultSet.getString(3)
					});
				}
			}
		}
		
		return results;
	}
}