package farsight.testing.core.interceptor.bdd;

import javax.xml.bind.JAXBException;

public class BddParseException extends Exception {

	private static final long serialVersionUID = -1464226568271367494L;

	public BddParseException(String message, JAXBException e) {
		super(message, e);
	}

}
