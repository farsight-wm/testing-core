package farsight.testing.core.flow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.wm.data.IData;
import com.wm.util.coder.IDataXMLCoder;
import com.wm.util.coder.InvalidDatatypeException;

public class IDataXmlTool {
	
	private static final String DEFAULT_CHARSET_NAME = "UTF-8";
	private static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);
	private static final Charset WINDOWS_FALLBACK = Charset.forName("windows-1252");
	
	public static IData decode(String idataXML) throws IOException {
		try {
			return decode(idataXML, DEFAULT_CHARSET);
		} catch(InvalidDatatypeException e) {
			//retry once with windows charset
			return decode(idataXML, WINDOWS_FALLBACK);
		}
	}
	
	private static IData decode(String idataXML, Charset charset) throws IOException {
		return new IDataXMLCoder().decodeFromBytes(idataXML.getBytes(charset));
	}
	
	public static IData decode(InputStream idataStream) throws IOException {
		return new IDataXMLCoder().decode(idataStream);
	}
	
	public static String encode(IData idata) throws IOException {
		return new String(new IDataXMLCoder(DEFAULT_CHARSET_NAME).encodeToBytes(idata), DEFAULT_CHARSET);
	}
	
	public static void encode(OutputStream os, IData idata) throws IOException {
		new IDataXMLCoder(DEFAULT_CHARSET_NAME).encode(os, idata);
	}

}
