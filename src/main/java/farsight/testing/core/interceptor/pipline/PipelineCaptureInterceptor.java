package farsight.testing.core.interceptor.pipline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.wm.data.IData;

import farsight.testing.core.flow.IDataXmlTool;
import farsight.testing.core.interceptor.BaseInterceptor;
import farsight.testing.core.interceptor.FlowPosition;
import farsight.testing.core.interceptor.InterceptResult;
import farsight.testing.core.interceptor.InterceptionException;

public class PipelineCaptureInterceptor extends BaseInterceptor {

	public static final String MAP_CURRENT_FILE = "currentFile";
	
	private final String prefix;
	private final String suffix;
	private int fileCount;

	public PipelineCaptureInterceptor(String fileName) {
		super("PipelineCapture-"+fileName);
		int dotPos = fileName.lastIndexOf('.');
		if (dotPos == -1) {
			prefix = fileName;
			suffix = ".xml";
		} else {
			prefix = fileName.substring(0, dotPos);
			suffix = fileName.substring(dotPos);
		}
	}

	@Override
	public InterceptResult intercept(FlowPosition flowPosition, IData idata) {
		invokeCount++;
		++fileCount;
		try (OutputStream fos = getFileOutputStream(getFileName())) {
			IDataXmlTool.encode(fos, idata);
		} catch (IOException e) {
			throw new InterceptionException("Error when writing pipeline to file " + getFileName(),e);
		}
		return InterceptResult.TRUE;
	}

	private String getFileName() {
		return prefix + '-' + fileCount + suffix;
	}

	protected OutputStream getFileOutputStream(String fileName) throws FileNotFoundException {
		return new FileOutputStream(fileName);
	}

	@Override
	protected void addMap(Map<String, Object> am) {
		am.put(MAP_TYPE, "PipelineCaptureInterceptor");
		am.put(MAP_CURRENT_FILE, fileCount == 0 ? "No file captured" : getFileName());
	}
}
