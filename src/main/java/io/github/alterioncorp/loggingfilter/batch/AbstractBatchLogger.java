package io.github.alterioncorp.loggingfilter.batch;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;
import io.github.alterioncorp.loggingfilter.jmx.InfoLoggerMXBean;
import io.github.alterioncorp.loggingfilter.jmx.JmxUtils;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactory;
import io.github.alterioncorp.loggingfilter.plugins.PluginFactoryImpl;

/**
 * Superclass for logger implementations that need to queue up requests,
 * and to periodically log them in a batch (e.g. JDBC, Azure Tables).
 *
 *
 * @param <T> the superclass of each logged property
 */
public abstract class AbstractBatchLogger<T> implements InfoLoggerMXBean, BatchLogger<T> {

	private final PluginFactory pluginFactory;
	private final BatchQueue<T> queue;

	private Configuration config;
	private BatchPlugin<T> plugin;
	private volatile boolean enabled = true;

	/**
	 * Creates a new instance using the default plugin factory and queue implementations.
	 */
	public AbstractBatchLogger() {

		super();

		this.pluginFactory = new PluginFactoryImpl();
		this.queue = new BatchQueueImpl<T>();
	}

	/**
	 * Creates a new instance with the given plugin factory and queue, for use by subclasses.
	 *
	 * @param pluginFactory the factory used to instantiate the plugin
	 * @param queue the queue used to accumulate records
	 */
	protected AbstractBatchLogger(PluginFactory pluginFactory, BatchQueue<T> queue) {

		super();

		this.pluginFactory = pluginFactory;
		this.queue = queue;
	}

	AbstractBatchLogger(PluginFactory pluginFactory, BatchQueue<T> queue, Configuration config, BatchPlugin<T> plugin) {

		super();

		this.pluginFactory = pluginFactory;
		this.queue = queue;
		this.config = config;
		this.plugin = plugin;
	}


	@Override
	public final void init(Configuration config) {

		this.config = config;

		plugin = this.createPlugin(pluginFactory, config);
		plugin.init(config);

		this.onInit(config);

		queue.setLogger(this);
		queue.setPlugin(plugin);
		queue.init(config);
	}

	@Override
	public final void destroy() {

		queue.destroy();
		plugin.destroy();

		this.onDestroy();
	}

	/**
	 * Called after the plugin and queue have been initialized. Subclasses can override to perform additional setup.
	 *
	 * @param configuration a configuration instance
	 */
	protected abstract void onInit(Configuration configuration);

	/**
	 * Called after the queue and plugin have been destroyed. Subclasses can override to release additional resources.
	 */
	protected abstract void onDestroy();

	/**
	 * Method for subclasses to return the plugin needed for this logger.
	 *
	 * @param pluginFactory factory for plugin creation
	 * @param configuration a configuration instance
	 * @return the plugin
	 */
	protected abstract BatchPlugin<T> createPlugin(PluginFactory pluginFactory, Configuration configuration);

	@Override
	public final void logRequest(RequestInfo requestInfo) {

		plugin.onLogRequestStart(requestInfo);

		queue.queueRequest(requestInfo);

		plugin.onLogRequestEnd(requestInfo);
	}

	@Override
	public final void logResponse(ResponseInfo responseInfo) {

		plugin.onLogResponseStart(responseInfo);

		queue.queueResponse(responseInfo);

		plugin.onLogResponseEnd(responseInfo);
	}

	@Override
	public final boolean isEnabled() {
		return enabled;
	}

	@Override
	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public final String getBeanName() {
		return JmxUtils.getBeanName(config.getInstanceName(), this.getClass());
	}

	@Override
	public final InfoLoggerMXBean getMBean() {
		return this;
	}
}
