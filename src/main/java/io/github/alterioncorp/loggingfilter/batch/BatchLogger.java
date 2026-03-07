package io.github.alterioncorp.loggingfilter.batch;

import java.util.List;

import io.github.alterioncorp.loggingfilter.loggers.InfoLogger;

/**
 * Interface for {@link BatchPlugin} to call back to the logger to log a batch of records.
 * 
 * @author alitovsky
 *
 * @param <T> superclass of all the properties
 */
public interface BatchLogger<T> extends InfoLogger {

	/**
	 * @param dataToLog the list of records to log
	 * @throws Exception if an error occurred while logging this batch
	 */
	void logBatch(List<T[]> dataToLog) throws Exception;
}
