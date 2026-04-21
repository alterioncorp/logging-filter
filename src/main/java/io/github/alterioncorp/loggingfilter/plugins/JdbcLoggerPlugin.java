package io.github.alterioncorp.loggingfilter.plugins;

import java.sql.Types;

import io.github.alterioncorp.loggingfilter.batch.BatchPlugin;
import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.loggers.JdbcLoggerImpl;

/**
 * Plugin to customize the behavior of {@link JdbcLoggerImpl}.
 *
 * To add custom data, you should extend {@link JdbcLoggerPluginDefaultWithAdditionalColumns}.
 *
 * @see JdbcLoggerPluginDefaultImpl
 * @see JdbcLoggerPluginDefaultWithAdditionalColumns
 * @see JdbcLoggerImpl
 */
public interface JdbcLoggerPlugin extends BatchPlugin<Object>, Configurable, PluginLifecycle {

	/**
	 * Return the number of columns in the table, including the auto-generated ID column.
	 *
	 * @return the column count
	 */
	int getColumnCount();

	/**
	 * Return the name of the column for the 0-based index.
	 *
	 * @param columnIndex the columnIndex
	 * @return the column name
	 */
	String getColumnName(int columnIndex);

	/**
	 * Return the SQLType of the column for the 0-based index.
	 *
	 * @param columnIndex the columnIndex
	 * @return the SQLType of the column
	 * @see Types
	 */
	int getColumnSqlType(int columnIndex);

	/**
	 * Return the String for the column-create SQL for the 0-based index.
	 * E.g. NVARCHAR(256) NOT NULL
	 *
	 * @param columnIndex the columnIndex
	 * @return the SQL for creating this column
	 */
	String getColumnTypeForTableCreate(int columnIndex);

	/**
	 * Return the value to insert for the 0-based index.
	 *
	 * @param columnIndex the columnIndex
	 * @param requestInfo the requestInfo
	 * @param responseInfo the responseInfo
	 * @return the value to insert at this column index
	 */
	Object getValueToInsert(int columnIndex, RequestInfo requestInfo, ResponseInfo responseInfo);

	/**
	 * Returns the version number for the data-model.
	 * This will be appended to the table name.
	 * If changing the schema, you need to increment this version.
	 *
	 * @return the version of the data-model
	 */
	String getDataVersion();

	/**
	 * Return the "IF NOT EXISTS... " statement prefix
	 *
	 * @param tableName the table name
	 * @return prefix for the "IF EXISTS" SQL
	 */
	String getTableExistsSqlPrefix(String tableName);

	/**
	 * Return the "IF NOT EXISTS... " statement suffix
	 *
	 * @param tableName the table name
	 * @return suffix for the "IF EXISTS" SQL
	 */
	String getTableExistsSqlSuffix(String tableName);

	default int getElementCount() {
		return this.getColumnCount();
	}

	default Object[] getValues(RequestInfo requestInfo, ResponseInfo responseInfo) {

		Object[] values = new Object[this.getElementCount()];

		for (int i = 0; i < values.length; i++) {
			values[i] = this.getValueToInsert(i, requestInfo, responseInfo);
		}

		return values;
	}
}
