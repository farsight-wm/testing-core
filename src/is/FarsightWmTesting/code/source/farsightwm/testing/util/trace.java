package farsightwm.testing.util;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.lang.ns.NSName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Stack;
import com.softwareag.util.IDataMap;
import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.interceptor.trace.TextReport;
import farsight.testing.core.interceptor.trace.TraceInterceptor;
import farsight.testing.core.interceptor.trace.TraceReport;
import farsight.testing.core.interceptor.trace.TraceInterceptor.Trace;
import farsight.testing.core.interceptor.trace.TraceInterceptor.TraceItem;
// --- <<IS-END-IMPORTS>> ---

public final class trace

{
	// ---( internal utility methods )---

	final static trace _instance = new trace();

	static trace _newInstance() { return new trace(); }

	static trace _cast(Object o) { return (trace)o; }

	// ---( server methods )---




	public static final void traceInvoke (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(traceInvoke)>> ---
		// @sigtype java 3.5
		// [i] field:0:required serviceName
		// [i] record:0:optional pipeline
		// [o] field:0:required trace
		// [o] record:0:required output
		IDataMap p = new IDataMap(pipeline);
		String serviceName = p.getAsString("serviceName");
		IData input = p.getAsIData("pipeline", IDataFactory.create());
		NSName nsname = NSName.create(serviceName);
			
		Trace trace = TraceInterceptor.traceInvoke(nsname, input, true);
		
		p.put("output", trace.getOutput());
		p.put("exception", trace.getServiceException());
		
		TraceItem[] services = trace.getServices();
		IData[] servicesIData = new IData[services.length];
		for(int i = 0; i < services.length; i++) {
			servicesIData[i] = services[i].asIData();
		}
		
		p.put("services", servicesIData);
		p.put("tree", trace.getTree().asIData());
		
		StringWriter report = new StringWriter();
		try {
			TraceReport.create(TextReport.class, trace).report(report);
			p.put("report", report.toString());
		} catch (Exception e) {
			p.put("report", "Error generating report: " + e.toString());
		}
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---

	
	// --- <<IS-END-SHARED>> ---
}

