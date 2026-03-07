package io.github.alterioncorp.loggingfilter.batch;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.github.alterioncorp.loggingfilter.config.Configurable;
import io.github.alterioncorp.loggingfilter.data.RequestInfo;
import io.github.alterioncorp.loggingfilter.data.ResponseInfo;

public interface BatchQueue<T> extends Configurable {

	void queueRequest(RequestInfo requestInfo, HttpServletRequest request, HttpServletResponse response);
	
	void queueResponse(ResponseInfo responseInfo, HttpServletRequest request, HttpServletResponse response);
	
	void setPlugin(BatchPlugin<T> plugin);
	
	void setLogger(BatchLogger<T> logger);
}
