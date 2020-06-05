package farsight.testing.core.interceptor.trace;

import java.io.PrintWriter;
import java.io.Writer;

import farsight.testing.core.interceptor.trace.TraceInterceptor.Trace;
import farsight.testing.core.interceptor.trace.TraceInterceptor.TraceItem;

public class TextReport extends TraceReport {

	private static final String TREEICON_ITEM = "\u251c\u2500";
	private static final String TREEICON_ITEM_LAST_SIBBLING = "\u2514\u2500";
	private static final String TREEICON_SUBPATH = "\u2502 ";
	private static final String TREEICON_SUBPATH_LAST_SIBBLING = "  ";
	private static final String TREEICON_ARROW_PARENT = "\u252c>";
	private static final String TREEICON_ARROW_CHILDLESS = "\u2500>";
	
	private TraceItem[] services;
	private TraceItem tree;

	public TextReport(Trace trace) {
		super(trace);
		this.tree = trace.getTree();
		this.services = trace.getServices();
	}


	public void report(PrintWriter out) {
		
		long total = tree.runTime;
		
		out.println("=== Trace Report ===============================================================");
		out.println("      service: " + tree.serviceName);
		out.println("total runtime: " + tree.runTime + " ms" );
		out.println("service calls: " + countServiceCalls());
		out.println();
		out.println("--- Called Services ------------------------------------------------------------");
		out.println("  # | calls |   run time |  (%) | service");
		for(int i = 0; i < services.length; i++)
			out.println(String.format("% 3d | % 5d | % 10d | %3s%% | %s", i + 1, services[i].invokeCount,
					services[i].runTime, relative(services[i].runTime, total), services[i].serviceName));			
		out.println();
		out.println("--- Call Tree ------------------------------------------------------------------");
		out.println("  # | trace time |   run time |  (%) | service");
		printTree(out, tree);
	}

	
	private void printTree(PrintWriter out, TraceItem tree) {
		printTree(out, tree, 0, tree.runTime, "", true);
	}
	
	private int printTree(PrintWriter out, TraceItem tree, int line, long treeTime, String treeStr, boolean lastSibbling) {
		line++;
		String treeIcon = treeStr;
		if(lastSibbling) {
			treeIcon += TREEICON_ITEM_LAST_SIBBLING;
			treeStr += TREEICON_SUBPATH_LAST_SIBBLING;
		} else {
			treeIcon += TREEICON_ITEM;
			treeStr += TREEICON_SUBPATH;
		}
		treeIcon += tree.hasChildren() ? TREEICON_ARROW_PARENT : TREEICON_ARROW_CHILDLESS;

		out.println(String.format("% 3d | % 10d | % 10d | %3s%% | %s %s", line, tree.traceTime, tree.runTime, relative(tree.runTime, treeTime), treeIcon, tree.serviceName));
		
		int lastChild = tree.hasChildren() ? tree.children().size() : 0, curChild = 1;
		for(TraceItem child: tree.children()) {
			line = printTree(out, child, line, treeTime, treeStr, lastChild == curChild++);
		}
		
		return line;
	}

	private String relative(long fraction, long total) {
		if(total == 0)
			return "";
		return Math.round((double)100 * fraction / total) + "";
			
	}

	private int countServiceCalls() {
		int calls = 0;
		for(TraceItem service: services)
			calls += service.invokeCount;
		return calls;
	}


	@Override
	public void report(Writer writer) {
		report(new PrintWriter(writer));
	}

}
