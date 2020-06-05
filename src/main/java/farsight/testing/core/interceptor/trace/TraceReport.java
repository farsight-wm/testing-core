package farsight.testing.core.interceptor.trace;

import java.io.Writer;

import farsight.testing.core.interceptor.trace.TraceInterceptor.Trace;

public abstract class TraceReport {

	protected final Trace trace;
	
	public TraceReport(Trace trace) {
		this.trace = trace;
	}
	
	public static TraceReport create(Class<? extends TraceReport> clazz, Trace trace) throws Exception {
		return clazz.getConstructor(Trace.class).newInstance(trace);
	}
	
	public abstract void report(Writer writer);
}
