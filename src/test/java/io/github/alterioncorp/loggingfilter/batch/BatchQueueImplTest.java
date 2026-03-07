package io.github.alterioncorp.loggingfilter.batch;

import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.alterioncorp.loggingfilter.config.Configuration;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

public class BatchQueueImplTest {

private ScheduledExecutorService executor;
	private BatchLogger<Object> logger;
	private BatchPlugin<Object> plugin;
	private BatchQueueImpl<Object> queue;
	private Configuration config;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		executor = Mockito.mock(ScheduledExecutorService.class);
		logger = Mockito.mock(BatchLogger.class);
		plugin = Mockito.mock(BatchPlugin.class);
		queue = new BatchQueueImpl<>(plugin, logger, executor);
		config = Mockito.mock(Configuration.class);
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
	}
	
	@Test
	public void testInit_Schedule_PeriodNotSet() {
		
		queue.init(config);
		
		Mockito.verify(executor).scheduleWithFixedDelay(
				Mockito.any(Runnable.class),
				Mockito.eq(0L),
				Mockito.eq(BatchQueueImpl.DEFAULT_WRITE_PERIOD_IN_MILLIS),
				Mockito.eq(TimeUnit.MILLISECONDS));
	}
	
	@Test
	public void testInit_Schedule_PeriodSet() {

		final long period = 69L;
		
		Mockito
			.when(config.getConfigProperty(BatchQueueImpl.PARAM_WRITE_PERIOD_IN_MILLIS))
			.thenReturn(String.valueOf(period));
		
		queue.init(config);
		
		Mockito.verify(executor).scheduleWithFixedDelay(
				Mockito.any(Runnable.class),
				Mockito.eq(0L),
				Mockito.eq(period),
				Mockito.eq(TimeUnit.MILLISECONDS));
	}
	
	@Test
	public void testDestroy_ShutdownExecutor() {
		
		queue.destroy();
		
		Mockito.verify(executor).shutdown();
	}
	
	@Test
	public void testQueueRequest() throws Exception {
		
		RequestInfo requestInfo = new RequestInfo();
		queue.queueRequest(requestInfo, request, response);
		
		assertNotNull(queue.getThreadLocal());
		assertEquals(requestInfo, queue.getThreadLocal().get());
		assertEquals(0, queue.getQueue().size());
	}
	
	@Test
	public void testQueueResponse() throws Exception {
		
		RequestInfo requestInfo = new RequestInfo();
		ResponseInfo responseInfo = new ResponseInfo();

		Mockito
			.when(plugin.getElementCount())
			.thenReturn(1);
		
		Mockito
			.when(plugin.getValues(requestInfo, responseInfo, request, response))
			.thenReturn(new Object[] {"abc"});

		queue.queueRequest(requestInfo, request, response);
		queue.queueResponse(responseInfo, request, response);
		
		assertNotNull(queue.getThreadLocal());
		assertNull(queue.getThreadLocal().get());
		
		assertEquals(1, queue.getQueue().size());
		assertEquals(1, queue.getQueue().get(0).length);
		assertEquals("abc", queue.getQueue().get(0)[0]);
	}
	
	@Test
	public void testQueueResponse_LogRequestNotCalled() {
		assertThrows(IllegalStateException.class, () -> queue.queueResponse(new ResponseInfo(), request, response));
	}
	
	@Test
	public void testLogQueuedData() throws Exception {
		
		Object[] data0 = new Object[] {null, "A1", "B1"};
		Object[] data1 = new Object[] {null, "A2", null};
		
		queue.getQueue().add(data0);
		queue.getQueue().add(data1);
		
		queue.logQueuedData();
		
		assertNotNull(queue.getQueue());
		assertTrue(queue.getQueue().isEmpty());
		
		Mockito.verify(logger).logBatch(Arrays.asList(data0, data1));
	}
	
	@Test
	public void testLogQueuedData_NoData() throws Exception {
		
		queue.logQueuedData();
		
		assertNotNull(queue.getQueue());
		assertTrue(queue.getQueue().isEmpty());

		Mockito.verify(logger, Mockito.never()).logBatch(Mockito.anyList());
	}
	
	@Test
	public void testLogQueuedData_RuntimeException() throws Exception {
		
		queue.getQueue().add(new Object[] {"a"});
		
		Mockito.doThrow(new RuntimeException("test")).when(logger).logBatch(Mockito.anyList());
		
		try {
			queue.logQueuedData();		
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testLogQueuedData_Exception() throws Exception {
		
		queue.getQueue().add(new Object[] {"a"});
		
		Mockito.doThrow(new Exception("test")).when(logger).logBatch(Mockito.anyList());
		
		try {
			queue.logQueuedData();		
		}
		catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
