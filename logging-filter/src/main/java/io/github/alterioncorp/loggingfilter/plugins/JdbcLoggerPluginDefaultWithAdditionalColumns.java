package io.github.alterioncorp.loggingfilter.plugins;

import java.sql.Types;

import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

/**
 * Convenience superclass for applications that wish to append columns to the default implementation.
 *
 *
 */
public abstract class JdbcLoggerPluginDefaultWithAdditionalColumns extends JdbcLoggerPluginDefaultImpl {

	/**
	 * Creates a new instance.
	 */
	protected JdbcLoggerPluginDefaultWithAdditionalColumns() {
	}

	@Override
	public final int getColumnCount() {
		return super.getColumnCount() + this.getAdditionalColumnCount();
	}

	@Override
	public final String getColumnName(int columnIndex) {

		String columnName;

		if (columnIndex < super.getColumnCount()) {
			columnName = super.getColumnName(columnIndex);
		}
		else {
			columnName = this.getAdditionalColumnName(columnIndex - super.getColumnCount());
		}

		return columnName;
	}

	@Override
	public final int getColumnSqlType(int columnIndex) {

		int type;

		if (columnIndex < super.getColumnCount()) {
			type = super.getColumnSqlType(columnIndex);
		}
		else {
			type = this.getAdditionalColumnSqlType(columnIndex - super.getColumnCount());
		}

		return type;
	}

	@Override
	public final String getColumnTypeForTableCreate(int columnIndex) {

		String type;

		if (columnIndex < super.getColumnCount()) {
			type = super.getColumnTypeForTableCreate(columnIndex);
		}
		else {
			type = this.getAdditionalColumnTypeForTableCreate(columnIndex - super.getColumnCount());
		}

		return type;
	}

	@Override
	public final Object getValueToInsert(int columnIndex, RequestInfo requestInfo, ResponseInfo responseInfo) {

		Object value;

		if (columnIndex < super.getColumnCount()) {
			value = super.getValueToInsert(columnIndex, requestInfo, responseInfo);
		}
		else {
			value = this.getAdditionalValueToInsert(columnIndex - super.getColumnCount(), requestInfo, responseInfo);
		}

		return value;
	}

	/**
	 * Return the number of additional columns in the table, not including the default columns.
	 *
	 * @return the column count
	 */
	protected abstract int getAdditionalColumnCount();

	/**
	 * Return the name of the additional column for the 0-based index.
	 * This index is for the additional columns and does not include the default columns.
	 *
	 * @param columnIndex the columnIndex
	 * @return the column name
	 */
	protected abstract String getAdditionalColumnName(int columnIndex);

	/**
	 * Return the SQLType of the additional column for the 0-based index.
	 * This index is for the additional columns and does not include the default columns.
	 *
	 * @param columnIndex the columnIndex
	 * @return the SQLType of the column
	 * @see Types
	 */
	protected abstract int getAdditionalColumnSqlType(int columnIndex);

	/**
	 * Return the String for the column-create SQL for the 0-based index of the additional column.
	 * E.g. NVARCHAR(256) NOT NULL
	 * This index is for the additional columns and does not include the default columns.
	 *
	 * @param columnIndex the columnIndex
	 * @return the SQL for creating this column
	 */
	protected abstract String getAdditionalColumnTypeForTableCreate(int columnIndex);

	/**
	 * Return the value to insert for the 0-based index of the additional column.
 	 * This index is for the additional columns and does not include the default columns.
 	 *
	 * @param columnIndex the columnIndex
	 * @param requestInfo the requestInfo
	 * @param responseInfo the responseInfo
	 * @return the value to insert at this column index
	 */
	protected abstract Object getAdditionalValueToInsert(int columnIndex, RequestInfo requestInfo, ResponseInfo responseInfo);
}
