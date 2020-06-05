package farsight.testing.core.interceptor.pipline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.wm.data.IData;
import com.wm.data.IDataUtil;

import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.interceptor.InterceptionException;

public class InMemoryPipelineCaptureInterceptor extends BaseInterceptor {

	public static final String MAP_CAPTURES = "captures";
	public static final String MAP_CAPTURE_COUNT = "captureCount";
	
	private static final Logger logger = LogManager.getLogger(InMemoryPipelineCaptureInterceptor.class);
	
	private IData[] storage;
	private int captureCount = 0;

	public InMemoryPipelineCaptureInterceptor(int capacity) {
		super("InMemoryPipelineCapture");
		this.storage = new IData[capacity];
	}

	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData idata) {
		invokeCount++;
		
		if(captureCount < storage.length) {
			try {
				storage[captureCount++] = IDataUtil.deepClone(idata);
			} catch (IOException e) {
				throw new InterceptionException("Could not clone input pipeline", e);
			}
			logger.info("Captured pipline " + captureCount + " of " + storage.length);
		} else {
			logger.info("Dismissed pipeline because the capture limit was reached");
		}
		
		return InterceptResult.TRUE;
	}

	protected OutputStream getFileOutputStream(String fileName) throws FileNotFoundException {
		return new FileOutputStream(fileName);
	}

	@Override
	protected void addMap(Map<String, Object> am) {
		IData[] captures = new IData[captureCount];
		if(captures.length > 0) 
			System.arraycopy(this.storage, 0, captures, 0, captures.length);
		
		am.put(MAP_TYPE, "InMemoryPipelineCaptureInterceptor");
		am.put(MAP_CAPTURE_COUNT, captureCount);
		am.put(MAP_CAPTURES, captures);
		
	}
}
