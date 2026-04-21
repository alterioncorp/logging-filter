package io.github.alterioncorp.loggingfilter.loggers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.alterioncorp.loggingfilter.batch.AbstractBatchLogger;
import io.github.alterioncorp.loggingfilter.batch.BatchPlugin;
import io.github.alterioncorp.loggingfilter.batch.BatchQueue;
import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.plugins.JdbcLoggerPlugin;
import io.github.alterioncorp.loggingfilter.plugins.JdbcLoggerPluginDefaultImpl;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;

/**
 * Logger that inserts a record into a database table for each request/response pair.
 * The records will be queued-up and inserted asynchronously in batches to avoid the overhead of a DB insert per request.
 *
 * The data-source is looked up from JNDI as 'java:jboss/datasources/RequestLog' by default, or can be overridden by logging-filter.jdbc.data_source_jndi.
 * The table to log the data will be automatically created, if it does not exist.
 * The name of the table is REQUEST_LOG_X, where X is the data-version determined by the plugin.
 *
 * The following system-properties or filter init-params customize the behavior of this class:
 * <ul>
 * 	<li>logging-filter.jdbc.data_source_jndi the JNDI name of the data-source to use (default=java:jboss/datasources/RequestLog).</li>
 * 	<li>logging-filter.jdbc.plugin: the class of the {@link JdbcLoggerPlugin} to use.  If not set, {@link JdbcLoggerPluginDefaultImpl} will be used.</li>
 * 	<li>logging-filter.batch.write_period_in_millis: how often (in milliseconds) the queued-up data will be written to the DB (default=5000ms).</li>
 * </ul>
 *
 * @see JdbcLoggerPlugin
 * @see JdbcLoggerPluginDefaultImpl
 */
public final class JdbcLoggerImpl extends AbstractBatchLogger<Object> implements InfoLoggerMXBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcLoggerImpl.class);
	static final String PARAM_DATA_SOURCE_JNDI_NAME = "logging-filter.jdbc.data_source_jndi";
	static final String DEFAULT_DATA_SOURCE_JNDI_NAME = "java:jboss/datasources/RequestLog";
	static final String PARAM_PLUGIN = "logging-filter.jdbc.plugin";

	private final Context context;

	private DataSource dataSource;
	private JdbcLoggerPlugin plugin;

	/**
	 * Creates a new instance using the default JNDI {@link InitialContext}.
	 */
	public JdbcLoggerImpl() {

		super();

		try {
			this.context = new InitialContext();
		}
		catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	JdbcLoggerImpl(Context context, DataSource dataSource, PluginFactory pluginFactory, JdbcLoggerPlugin plugin, BatchQueue<Object> queue) {
		super(pluginFactory, queue);
		this.context = context;
		this.dataSource = dataSource;
		this.plugin = plugin;
	}

	@Override
	protected void onInit(Configuration configuration) {

		String dataSourceJndiName = configuration.getConfigProperty(PARAM_DATA_SOURCE_JNDI_NAME);
		if (dataSourceJndiName == null) {
			dataSourceJndiName = DEFAULT_DATA_SOURCE_JNDI_NAME;
		}

		try {
			dataSource = (DataSource)context.lookup(dataSourceJndiName);
		}
		catch (NamingException e) {
			throw new RuntimeException(e);
		}

		this.createTableIfNeeded();
	}

	@Override
	protected void onDestroy() {
	}

	@Override
	protected BatchPlugin<Object> createPlugin(PluginFactory pluginFactory, Configuration configuration) {

		String pluginName = configuration.getConfigProperty(PARAM_PLUGIN);
		if (pluginName == null) {
			pluginName = JdbcLoggerPluginDefaultImpl.class.getName();
		}
		plugin = pluginFactory.getPlugin(JdbcLoggerPlugin.class, pluginName);
		return plugin;
	}

	@Override
	public void logBatch(List<Object[]> dataToLog) throws Exception {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("inserting queued data: count=" + dataToLog.size());
		}

		StringBuilder sql = this.getInsertSql();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(sql.toString());
		}

		try (Connection connection = dataSource.getConnection()) {

			try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {

				for (Object[] data : dataToLog) {

					// assume that 0 is an identity column
					for (int i = 1; i < plugin.getColumnCount(); i++) {

						Object value = data[i];
						int sqlType = plugin.getColumnSqlType(i);

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug(i + ": " + value);
						}

						if (value == null) {
							statement.setNull(i, sqlType);
						}
						else {
							statement.setObject(i, value, sqlType);
						}
					}

					statement.addBatch();
				}

				statement.executeBatch();
			}
		}
	}

	private StringBuilder getInsertSql() {

		StringBuilder sql = new StringBuilder();

		sql.append("insert into ").append(this.getTableName()).append("(\n");
		// assume that 0 is an identity column
		for (int i = 1; i < plugin.getColumnCount(); i++) {
			if (i > 1) {
				sql.append(", ");
			}
			sql.append(plugin.getColumnName(i));
		}
		sql.append("\n) values (\n");
		// assume that 0 is an identity column
		for (int i = 1; i < plugin.getColumnCount(); i++) {
			if (i > 1) {
				sql.append(", ");
			}
			sql.append("?");
		}
		sql.append("\n)");

		return sql;
	}

	void createTableIfNeeded() {

		final String tableName = this.getTableName();

		StringBuilder sql = new StringBuilder();
		sql.append(plugin.getTableExistsSqlPrefix(tableName));
		sql.append("create table ").append(tableName).append(" (");
		for (int i = 0; i < plugin.getColumnCount(); i++) {
			if (i > 0) {
				sql.append(",");
			}
			sql.append("\n\t").append(plugin.getColumnName(i)).append(" ").append(plugin.getColumnTypeForTableCreate(i));
		}
		sql.append("\n)");
		sql.append(plugin.getTableExistsSqlSuffix(tableName));

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(sql.toString());
		}

		try {
			try (Connection connection = dataSource.getConnection()) {
				try (Statement statement = connection.createStatement()) {
					statement.executeUpdate(sql.toString());
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	String getTableName() {
		return "REQUEST_LOG_" + plugin.getDataVersion();
	}
}
