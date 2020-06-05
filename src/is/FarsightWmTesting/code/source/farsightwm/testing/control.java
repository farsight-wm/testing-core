package farsightwm.testing;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import com.softwareag.util.IDataMap;
import farsight.testing.core.chainprocessor.FarsightChainProcessor;
import farsight.testing.core.flow.MockManager;
import farsight.utils.ManifestUtils;
// --- <<IS-END-IMPORTS>> ---

public final class control

{
	// ---( internal utility methods )---

	final static control _instance = new control();

	static control _newInstance() { return new control(); }

	static control _cast(Object o) { return (control)o; }

	// ---( server methods )---




	public static final void enableInterception (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(enableInterception)>> ---
		// @sigtype java 3.5
		// [i] field:0:required enabled
		// [o] field:0:required enabled
		new MockManager().enableInterception(pipeline);
		// --- <<IS-END>> ---

                
	}



	public static final void initFramework (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(initFramework)>> ---
		// @sigtype java 3.5
		// [i] field:0:required action {"check","install","uninstall"}
		// [o] field:0:required chainProcessorState
		IDataMap p = new IDataMap(pipeline);
		String action = p.getAsString("action", "check");
		if("check".equals(action)) {
			p.put("report", checkProcessor());
		} else if("install".equals(action)) {
			installProcessor();
		} else if("uninstall".equals(action)) {
			uninstallProcessor();
		}
			
		// --- <<IS-END>> ---

                
	}



	public static final void initLogging (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(initLogging)>> ---
		// @sigtype java 3.5
		// [i] field:0:required level
		/* logging is changed significantly for wm 10.x (API) needs adapting to Log4J 2!! 
		 
		IDataMap p = new IDataMap(pipeline);
		Level level = Level.toLevel(p.getAsString("level", "INFO"));
		Logger logger = Logger.getLogger("fintyre.esb.aop");
		logger.setLevel(level);
		
		Appender appender = logger.getAppender("AOP.log");
		if(appender == null) {
			PatternLayout patternLayout = new PatternLayout();
			patternLayout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
			DailyRollingFileAppender dailyRollingFileAppender = new DailyRollingFileAppender();
			dailyRollingFileAppender.setName("AOP.log");
			dailyRollingFileAppender.setLayout(patternLayout);
			dailyRollingFileAppender.setDatePattern("'.'yyyy-MM-dd");
			dailyRollingFileAppender.setFile("logs/AOP.log");
			dailyRollingFileAppender.activateOptions();
			logger.addAppender(dailyRollingFileAppender);
			
			logger.info("Installed appender: " + dailyRollingFileAppender.toString());
		}
		
		logger.info("Set log level for org.wmaop to: " + level);*/
			
		// --- <<IS-END>> ---

                
	}



	public static final void resetAdviceAndDisable (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(resetAdviceAndDisable)>> ---
		// @sigtype java 3.5
		new MockManager().reset(pipeline);
		// --- <<IS-END>> ---

                
	}



	public static final void versionInfo (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(versionInfo)>> ---
		// @sigtype java 3.5
		IDataMap p = new IDataMap(pipeline), info = new IDataMap();
		try {
			Manifest manifest = ManifestUtils.getManifest(FarsightChainProcessor.class);
			if(manifest == null) {
				p.put("message", "Could not get jar Manifest");
				p.put("error", "AOPChainProcessor seems to not be loaded from jar!");
			} else {
				info.put("Project", ManifestUtils.getAttribute(manifest, "project-id"));
				info.put("Name", ManifestUtils.getAttribute(manifest, "project-name"));
				info.put("Version", ManifestUtils.getAttribute(manifest, "project-version"));
				info.put("BuildDate", ManifestUtils.getAttribute(manifest, "project-build-date"));
				p.put("info", info.getIData());
			}
		} catch (IOException e) {
			p.put("message", "Could not get jar Manifest");
			p.put("error", e.getMessage());
		}
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static ArrayList<InvokeChainProcessor> getProcessors() throws ServiceException {
		ArrayList<InvokeChainProcessor> processors = null;
		InvokeManager invokeManager = InvokeManager.getDefault();
		try {
			Field _processors = InvokeManager.class.getDeclaredField("_processors");
			_processors.setAccessible(true);
			processors = (ArrayList<InvokeChainProcessor>) _processors.get(invokeManager);
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		return processors;
	}
	
	private static void installProcessor() throws ServiceException {
		FarsightChainProcessor instance = FarsightChainProcessor.getInstance();
		if(instance == null) {
			//create with default consturctor
			instance = new FarsightChainProcessor();
		}
		
		FarsightChainProcessor.install();
	}
	
	private static void uninstallProcessor() throws ServiceException {
		FarsightChainProcessor.uninstall();
	}
	
	private static IData checkProcessor() throws ServiceException {
		IDataMap p = new IDataMap();
		//find installed processor
		InvokeChainProcessor proc = null;
		int instances = 0;
		String procName = FarsightChainProcessor.class.getCanonicalName();
		for(InvokeChainProcessor processor: getProcessors()) {
			if(processor.getClass().getCanonicalName().equals(procName)) {
				proc = processor;
				instances++;
			}
		}
		
		
		FarsightChainProcessor defaultInnstance = FarsightChainProcessor.getInstance();
		
		//analyse
		
		p.put("instance", defaultInnstance == null ? "not-found" : "found");
		
		if(defaultInnstance != null && instances == 1) {
			p.put("instance-match-chain", defaultInnstance == proc);
		}
		
		
		if(instances == 0) {
			p.put("installed", "false");
		} else if(instances == 1) {
			p.put("installed", "true");
			
			
		} else {
			p.put("installed", "true");
			p.put("instances", instances);
			p.put("message", "Warning: multiple instances in chain found!");
		}
		
		return p.getIData();
	}
	
	
	
	
	
		
	// --- <<IS-END-SHARED>> ---
}

