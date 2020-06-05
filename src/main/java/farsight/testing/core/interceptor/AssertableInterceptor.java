package farsight.testing.core.interceptor;

public interface AssertableInterceptor extends Interceptor {
	int getInvokeCount();
	String getName();
}
