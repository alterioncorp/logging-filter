package io.github.alterioncorp.loggingfilter.batch;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

/**
 * Default implementation of {@link BatchQueue} that accumulates request/response pairs in memory
 * and flushes them to the logger at a configurable interval via a scheduled executor.
 *
 * @param <T> the type of each logged element
 */
public class BatchQueueImpl<T> implements BatchQueue<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchQueueImpl.class);
	
	static final String PARAM_WRITE_PERIOD_IN_MILLIS = "logging-filter.batch.write_period_in_millis";
	static final long DEFAULT_WRITE_PERIOD_IN_MILLIS = 5000;

	private BatchPlugin<T> plugin;
	private BatchLogger<T> logger;
	private final ThreadLocal<RequestInfo> threadLocal = new ThreadLocal<>();
	private final ScheduledExecutorService executor;
	private List<T[]> queue = new LinkedList<>();
	
	/**
	 * Creates a new instance with a single-threaded scheduled executor.
	 */
	public BatchQueueImpl() {
		super();
		this.executor = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * Creates a new instance with the given plugin, logger, and executor.
	 *
	 * @param plugin the plugin used to extract values from each request/response pair
	 * @param logger the logger that receives flushed batches
	 * @param executor the executor used to schedule periodic flushes
	 */
	public BatchQueueImpl(BatchPlugin<T> plugin, BatchLogger<T> logger, ScheduledExecutorService executor) {
		super();
		this.plugin = plugin;
		this.logger = logger;
		this.executor = executor;
	}

	@Override
	public void init(Configuration config) {
		
		// determine how often to log to the DB
		String writePeriodInMillisProperty = config.getConfigProperty(PARAM_WRITE_PERIOD_IN_MILLIS);
		
		long writePeriodInMillis = writePeriodInMillisProperty != null ?
				Long.parseLong(writePeriodInMillisProperty) : DEFAULT_WRITE_PERIOD_IN_MILLIS;

		// schedule the queue to be logged to the DB every N millis
		executor.scheduleWithFixedDelay(()-> this.logQueuedData(), 0, writePeriodInMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void destroy() {
		executor.shutdown();
	}

	@Override
	public void queueRequest(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response) {
		threadLocal.set(requestInfo);
	}

	@Override
	public void queueResponse(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response) {
		
		// get the requestInfo that was previously pushed in this thread
		RequestInfo requestInfo = threadLocal.get();
		if (requestInfo == null) {
			throw new IllegalStateException("logRequest wasn't previously called from this thread.");
		}
		
		threadLocal.remove();
		
		T[] data = plugin.getValues(requestInfo, responseInfo, request, response);
		
		// queue the data to be logged
		synchronized (this) {
			queue.add(data);
		}
	}
	
	void logQueuedData() {

		try {
			List<T[]> dataToLog;
			synchronized (this) {
				dataToLog = queue;
				queue = new LinkedList<>();
			}
			
			if (! dataToLog.isEmpty()) {
				LOGGER.debug("writing queued data: count=" + dataToLog.size());
				logger.logBatch(dataToLog);
			}
			else {
				LOGGER.debug("no data to write");
			}
		}
		catch (Exception e) {
			LOGGER.error("Error writing batch.", e);
		}
	}

	@Override
	public void setPlugin(BatchPlugin<T> plugin) {
		this.plugin = plugin;
	}

	@Override
	public void setLogger(BatchLogger<T> logger) {
		this.logger = logger;
	}

	ThreadLocal<RequestInfo> getThreadLocal() {
		return threadLocal;
	}

	List<T[]> getQueue() {
		return queue;
	}
}
