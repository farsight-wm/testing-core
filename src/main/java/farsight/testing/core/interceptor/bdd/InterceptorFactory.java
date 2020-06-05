package farsight.testing.core.interceptor.bdd;

import java.io.IOException;
import java.util.List;

import org.wmaop.interceptor.bdd.xsd.Assert;
import org.wmaop.interceptor.bdd.xsd.Then;

import farsight.testing.core.interceptor.InterceptionException;
import farsight.testing.core.interceptor.Interceptor;
import farsight.testing.core.interceptor.assertion.AssertionInterceptor;
import farsight.testing.core.interceptor.mock.canned.CannedResponseInterceptor;
import farsight.testing.core.interceptor.mock.canned.CannedResponseInterceptor.ResponseSequence;
import farsight.testing.core.interceptor.mock.exception.ExceptionInterceptor;
import farsight.testing.core.interceptor.pipline.InMemoryPipelineCaptureInterceptor;
import farsight.testing.core.interceptor.pipline.PipelineCaptureInterceptor;

public class InterceptorFactory {

	public Interceptor getInterceptor(Then then) {
		if (then.getAssert() != null) {
			return getAssertInterceptor(then.getAssert());
		} else if (then.getReturn() != null && !then.getReturn().isEmpty()) {
			return getReturnInterceptor(then.getReturn());
		} else if (then.getPipelineCapture() != null) {
			return getPipelineCaptureInterceptor(then.getPipelineCapture());
		} else if (then.getThrow() != null) {
			return getExceptionInterceptor(then.getThrow());
		} else {
			throw new InterceptionException("No then actions within the scenario");
		}
	}

	public Interceptor getReturnInterceptor(List<String> list) {
		try {
			return new CannedResponseInterceptor(ResponseSequence.SEQUENTIAL, list);
		} catch (IOException e) {
			throw new InterceptionException("Error while decoding IData for CannedResponse - Format issue with IDATA?", e);
		}
	}

	public Interceptor getPipelineCaptureInterceptor(String fileName) {
		return new PipelineCaptureInterceptor(fileName);
	}
	
	public Interceptor getInMemoryPipelineCaptureInterceptor(int capacity) {
		return new InMemoryPipelineCaptureInterceptor(capacity);
	}

	public Interceptor getExceptionInterceptor(String exceptionClassName) {
		try {
			return new ExceptionInterceptor(exceptionClassName);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new InterceptionException("Problem resolving exception class " + exceptionClassName, e);
		}
	}

	public Interceptor getAssertInterceptor(Assert ass) {
		return new AssertionInterceptor(ass.getId());
	}

}
