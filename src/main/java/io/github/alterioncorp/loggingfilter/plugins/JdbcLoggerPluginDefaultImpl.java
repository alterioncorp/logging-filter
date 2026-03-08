package io.github.alterioncorp.loggingfilter.plugins;

import java.sql.Types;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.loggers.JdbcLoggerImpl;

/**
 * Default plugin for {@link JdbcLoggerImpl}.
 * Applications that wish to add columns should subclass {@link JdbcLoggerPluginDefaultWithAdditionalColumns} instead.
 * 
 * Each record will contain the following columns:
 * <ol>
 * 	<li>ID: auto-generated identity</li>
 * 	<li>START_TIMESTAMP: timestamp when the request was received</li>
 * 	<li>CLIENT_INFO: IP or host-name of the client that made the request</li>
 * 	<li>SERVER_INFO: IP or host-name of the server that handled this request</li>
 * 	<li>METHOD: HTTP method (e.g. GET, POST)</li>
 * 	<li>PATH: relative URL of the request, starting with the context</li>
 * 	<li>PARAMS: query string</li>
 * 	<li>DURATION: the time it took the server to process this request, in millis</li>
 * 	<li>RESPONSE_STATUS: HTTP status of the response (e.g. 200, 401)</li>
 * 	<li>SESSION_ID: JSESSIONID at the time of the response</li>
 * </ol>
 *
 *
 */
public class JdbcLoggerPluginDefaultImpl implements JdbcLoggerPlugin {

	/** Data-model version, appended to the table name. Increment when the schema changes. */
	public static final String VERSION = "1";

	/** Ordered column names for the log table. */
	public static final String[] COLUMN_NAMES = new String[] {
			"ID",
			"START_TIMESTAMP",
			"CLIENT_INFO",
			"SERVER_INFO",
			"METHOD",
			"PATH",
			"PARAMS",
			"DURATION",
			"RESPONSE_STATUS",
			"SESSION_ID"
	};
	
	/** {@link java.sql.Types} constants for each column, in the same order as {@link #COLUMN_NAMES}. */
	public static final int[] COLUMN_SQL_TYPES = new int[] {
			Types.BIGINT,
			Types.TIMESTAMP,
			Types.NVARCHAR,
			Types.NVARCHAR,
			Types.NVARCHAR,
			Types.NVARCHAR,
			Types.NVARCHAR,
			Types.INTEGER,
			Types.SMALLINT,
			Types.NVARCHAR
	};
	
	/** DDL column type expressions for table creation, in the same order as {@link #COLUMN_NAMES}. */
	public static final String[] COLUMN_TYPES_FOR_CREATE = new String[] {
			"BIGINT NOT NULL IDENTITY(1,1) PRIMARY KEY",
			"DATETIME",
			"NVARCHAR(256)",
			"NVARCHAR(256)",
			"NVARCHAR(8)",
			"NVARCHAR(256)",
			"NVARCHAR(MAX)",
			"INT",
			"SMALLINT",
			"NVARCHAR(256)"
	};

	/**
	 * Creates a new instance.
	 */
	public JdbcLoggerPluginDefaultImpl() {
	}

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
		return COLUMN_TYPES_FOR_CREATE[columnIndex];
	}

	@Override
	public Object getValueToInsert(int columnIndex, RequestInfo requestInfo, ResponseInfo responseInfo,
			HttpServletRequest request, HttpServletResponse response) {
		
		Object value;
		
		switch (columnIndex) {
			case 0:
				// identity column
				value = null;
				break;
			case 1:
				value = requestInfo.getStartTimestamp();
				break;
			case 2:
				value = requestInfo.getClientNameOrAddress();
				break;
			case 3:
				value = requestInfo.getServerNameOrAddress();
				break;
			case 4:
				value = requestInfo.getMethod();
				break;
			case 5:
				value = requestInfo.getPath();
				break;
			case 6:
				value = requestInfo.getParamsAsString();
				break;
			case 7:
				value = responseInfo.getEndTimestamp().getTime() - requestInfo.getStartTimestamp().getTime();
				break;
			case 8:
				value = responseInfo.getResponseCode();
				break;
			case 9:
				value = responseInfo.getSessionId();
				break;
			default:
				throw new IllegalArgumentException("invalid columnIndex: " + columnIndex);
		}
		
		return value;
	}

	@Override
	public String getDataVersion() {
		return VERSION;
	}

	@Override
	public final String getTableExistsSqlPrefix(String tableName) {
		return "if not exists (select * from sysobjects where name='" + tableName +"' and xtype='U')\n";
	}

	@Override
	public final String getTableExistsSqlSuffix(String tableName) {
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
