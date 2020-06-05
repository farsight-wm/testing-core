package farsight.testing.core.interceptor.trace;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.softwareag.util.IDataMap;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Service;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;

import farsight.testing.core.advice.Advice;
import farsight.testing.core.advice.AdviceManager;
import farsight.testing.core.advice.AdviceState;
import farsight.testing.core.advice.remit.GlobalRemit;
import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptPoint;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.matcher.FlowPositionMatcher;
import farsight.testing.core.pointcut.PointCut;


public class TraceInterceptor extends BaseInterceptor {
	
	private static final Logger logger = LogManager.getLogger(TraceInterceptor.class);
	
	private static class AnyPointCut implements PointCut {
		
		public final InterceptPoint interceptPoint;
		
		public AnyPointCut(InterceptPoint ip) {
			interceptPoint = ip;
		}

		@Override
		public boolean isApplicable(FlowPosition pipelinePosition, IData idata) {
			if(logger.isDebugEnabled())
				logger.debug("isApplicable: " + pipelinePosition);
			return true;
		}

		@Override
		public InterceptPoint getInterceptPoint() {
			return interceptPoint;
		}

		@Override
		public FlowPositionMatcher getFlowPositionMatcher() {
			return null;
		}

		@Override
		public Object toMap() {
			return "ANY_POINT_CUT";
		}
	}
	
	public static class TraceItem {
		public final String serviceName;
		
		/** in servicesNode: number of invokes */ 
		int invokeCount = 0;
		
		/** start time relative to trace start */
		long traceTime;
		
		/** total runtime */
		long runTime;
		
		//children
		private LinkedList<TraceItem> children;
		
		public TraceItem(String serviceName, long traceTime) {
			this.serviceName = serviceName;
			this.traceTime = traceTime;
		}
		
		public static TraceItem create(String serviceName, long time) {
			return new TraceItem(serviceName, time);
		}
	
		public static TraceItem createNull() {
			return new TraceItem(null, -1);
		}
		
		// access
	
		public boolean hasChildren() {
			return children != null;
		}
		
		public boolean isxRoot() {
			return serviceName == null;
		}
		
		// evaluation
		
		protected void addChild(TraceItem tree) {
			if(children == null)
				children = new LinkedList<>();
			children.add(tree);
		}
	
		protected void countInvoke() {
			invokeCount++;
		}
		
		protected void addRuntime(long delta) {
			runTime += delta;
		}
		
		protected void treeEnd(long traceTime) {
			runTime = traceTime - this.traceTime;
		}
		
		// results
			
		public IData asIData() {
			IDataMap entry = new IDataMap();
			entry.put("service", serviceName);
			if(traceTime >= 0)
				entry.put("traceTime", traceTime);
			if(invokeCount > 0)
				entry.put("invokeCount", invokeCount);
			entry.put("runTime", runTime);
			
			if(hasChildren()) {
				IData[] ch = new IData[children.size()];
				int i = 0;
				for(TraceItem child: children) {
					ch[i++] = child.asIData();
				}
				entry.put("calls", ch);
			}
			return entry.getIData();
		}

		public TraceItem getChild(int index) {
			return hasChildren() && children.size() > index ? children.get(index) : null;
		}

		public List<TraceItem> children() {
			return hasChildren() ? children : Collections.emptyList();
		}
	}
	
	private static enum TraceLineType {
		SOT, EOT, BEFORE, AFTER
	}
	
	public static class Trace {
	
		private BufferedReader reader = null;
		private LinkedHashMap<String, TraceItem> services = new LinkedHashMap<>();
		private Stack<TraceItem> stack = new Stack<>();
		
		private int lineNo = 0;
		private TraceLineType type = TraceLineType.SOT;
		private String serviceName;
		private long time = 0;
		private long duration = 0;
		private String line;
		
		private long traceOffset = 0;
		private TraceItem root, currentService;
		
		private IData output;
		private Exception serviceException;
	
		public Trace(Reader trace) {
			reader = new BufferedReader(trace);
			root = TraceItem.createNull();
			stack.push(root);
		}
		
		private boolean next() throws Exception {
			line = reader.readLine();
			if(line == null)
				return false;
			
			lineNo++;
			String[] parts = line.split(";");
			long cur = time;
			time = parseTime(parts[0]) - traceOffset;
			duration = time - cur;
			type = parseType(parts[1]);
			serviceName = parts.length > 2 ? parts[2] : null;
			
			return true;
		}
		
		private long parseTime(String str) {
			return Long.parseLong(str, 10);
		}
		
		private TraceLineType parseType(String str) throws Exception {
			if("BEGIN_TRACE".equals(str))
				return TraceLineType.SOT;
			if("BEFORE".equals(str))
				return TraceLineType.BEFORE;
			if("AFTER".equals(str))
				return TraceLineType.AFTER;
			if("END_TRACE".equals(str))
				return TraceLineType.EOT;
			throw new Exception(error("Unkown line type 'str'"));
		}
		
		private String error(String message) {
			return "ParserError: " + message + "at line " + lineNo + "\n>> " + line;
		}
		
		private void push(TraceItem top) {
			stack.peek().addChild(top);
			stack.push(top);
		}
		
		private TraceItem pop() throws Exception {
			if(stack.isEmpty())
				throw new Exception(error("call stack is empty"));
			return stack.pop();
		}
	
		public Trace parse() throws Exception {
			try {
				next();
				parseSOT();
				while(next() && type != TraceLineType.EOT)
					parseLine();
				
				//for incomplete traces -> end all open tree items
				while(stack.size() > 1)
					parseAfter();
				
			} finally {
				// in any case, close reader!
				reader.close();
			}
			
			return this;
		}
		
		public TraceItem getTree() {
			return root.getChild(0);
		}
		
		public TraceItem[] getServices() {
			return services.values().toArray(new TraceItem[services.size()]);
		}
		
		protected void setOutput(IData output, Exception serviceException) {
			this.output = output;
			this.serviceException = serviceException;
		}
		
		public IData getOutput() {
			return output;
		}
		
		public Exception getServiceException() {
			return serviceException;
		}
	
		private void parseSOT() throws Exception {
			if(type != TraceLineType.SOT)
				throw new Exception(error("expected 'BEGIN_TRACE'"));
			traceOffset = time;
		}
	
		private void parseLine() throws Exception {
			switch(type) {
			case BEFORE:
				parseBefore();
				return;
			case AFTER:
				parseAfter();
				return;
			default:
				throw new Exception(error("expected 'BEFORE' or 'AFTER'"));
			}	
		}
	
		private void parseBefore() throws Exception {
			//services
			TraceItem serviceItem = services.get(serviceName);
			if(serviceItem == null) {
				//service first seen
				serviceItem = TraceItem.create(serviceName, time);
				services.put(serviceName, serviceItem);
			}
			serviceItem.countInvoke();
			
			if(currentService != null) currentService.addRuntime(duration);
			currentService = serviceItem;
			
			//tree
			push(TraceItem.create(serviceName, time));
			
		}
	
		private void parseAfter() throws Exception {
			pop().treeEnd(time);
			currentService.addRuntime(duration);
			//get services item for current stack top
			if(!stack.isEmpty() && stack.peek().serviceName != null) {
				currentService = services.get(stack.peek().serviceName);
			}
		}	
	}
	
	private static final String TRACE_KEY = "$TraceInterceptor$";
	private static final AnyPointCut BEFORE = new AnyPointCut(InterceptPoint.BEFORE);
	private static final AnyPointCut AFTER = new AnyPointCut(InterceptPoint.AFTER);

	protected TraceInterceptor() {
		super("TraceInterceptor");
	}
	
	private static AdviceManager getAdviceManager() {
		return FarsightChainProcessor.getInstance().getAdviceManager();
	}

	public static void install() {
		TraceInterceptor interceptor = new TraceInterceptor();
		GlobalRemit remit = new GlobalRemit();
		getAdviceManager().registerAdvice(new Advice(TRACE_KEY + "BEFORE", remit, BEFORE, interceptor));
		getAdviceManager().registerAdvice(new Advice(TRACE_KEY + "AFTER", remit, AFTER, interceptor));
	}

	public static boolean isInstalled() {
		Advice advice = getAdviceManager().getAdvice(TRACE_KEY + "BEFIRE");
		return advice != null && advice.getAdviceState() == AdviceState.ENABLED;
	}
	
	public static void uninstall() {
		getAdviceManager().unregisterAdvice(TRACE_KEY + "BEFORE");
		getAdviceManager().unregisterAdvice(TRACE_KEY + "AFTER");
	}

	public static void startTrace() {
		InvokeState state = InvokeState.getCurrentState();
		StringBuilder trace = new StringBuilder();
		trace.append(System.currentTimeMillis() + ";BEGIN_TRACE\n");
		state.setPrivateData(TRACE_KEY, trace);
	}
	
	public static Trace getTrace() throws Exception {
		StringBuilder traceBuilder = (StringBuilder) InvokeState.getCurrentState().getPrivateData(TRACE_KEY);
		if(traceBuilder == null)
			return null;
		traceBuilder.append(System.currentTimeMillis() + ";END_TRACE\n");
		InvokeState.getCurrentState().removePrivateData(TRACE_KEY);
		return new Trace(new StringReader(traceBuilder.toString())).parse();
	}
	
	public static Trace traceInvoke(NSName service, IData input, boolean autoEnable) {
		boolean frameworkEnabled = FarsightChainProcessor.getInstance().isEnabled();
		boolean traceEnabled = isInstalled();
		if(!(frameworkEnabled && traceEnabled)) {
			if(!autoEnable)
				return null;
			
			if(!frameworkEnabled)
				FarsightChainProcessor.getInstance().setEnabled(true);
			if(!traceEnabled)
				install();
		}
		
		startTrace();
		Exception serviceException = null;
		IData output = null;
		Trace trace = null;
		try {
			output = Service.doInvoke(service, input);
		} catch(Exception e) {
			serviceException = e;
		} 
		try {
			trace = getTrace();
			trace.setOutput(output, serviceException);
		} catch (Exception e) {
			//this should not happen
			logger.error("Could not parse trace", e);
		} finally {
			//restore state as before
			if(!frameworkEnabled)
				FarsightChainProcessor.getInstance().setEnabled(false);
			if(!traceEnabled)
				uninstall();	
		}
		return trace;	
	}

	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData idata) {
		trace(flowPosition);
		return InterceptResult.FALSE;
	}

	private void trace(FlowPosition flowPosition) {
		StringBuilder trace = (StringBuilder) InvokeState.getCurrentState().getPrivateData(TRACE_KEY);
		if(trace != null) {
			trace.append(System.currentTimeMillis() + ";" + flowPosition.getInterceptPoint() + ";" + flowPosition.getFqname() + "\n");
		}
	}


	@Override
	protected void addMap(Map<String, Object> am) {
				
	}

}
