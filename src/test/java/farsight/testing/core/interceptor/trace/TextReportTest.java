package farsight.testing.core.interceptor.trace;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import farsight.testing.core.interceptor.trace.TraceInterceptor.Trace;

public class TextReportTest {

	private static InputStream getStream(String path) {
		return TextReportTest.class.getClassLoader().getResourceAsStream(path);
	}

	public static void main(String[] args) throws Exception {
		Trace trace = new Trace(new InputStreamReader(getStream("trace/example.trace"))).parse();
		TraceReport report = TraceReport.create(TextReport.class, trace);
		try (OutputStreamWriter osw = new OutputStreamWriter(System.out)) {
			report.report(osw);
			osw.flush();
		}
	}

}
