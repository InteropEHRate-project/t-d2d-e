package eu.interopehrate.td2de.dummy;

import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import eu.interopehrate.td2de.api.TD2DListener;

public class DummyTD2DListener implements TD2DListener{
	
	private static final Logger logger = Logger.getLogger(DummyTD2DListener.class.getName());

	@Override
	public void onRead(Bundle healthDataBundle, int currentPage, int totalPages) {
		logger.fine("onRead page: " + currentPage + ", pages: " + totalPages);
	}

	@Override
	public void onSearch(Bundle healthDataBundle, int currentPage, int totalPages) {
		logger.fine("onSearch page: " + currentPage + ", pages: " + totalPages);
	}

	@Override
	public boolean onCitizenPersonalDataReceived(Patient patient) {
		logger.fine("onCitizenPersonalDataReceived");		
		return true;
	}

	@Override
	public void onConnectionClosure() {
		logger.fine("onConnectionClosure");		
	}

	@Override
	public void onError(int errorCode, String errorMessage) {
		logger.fine("onError: " + errorCode + " - " + errorMessage);		
	}

	
	
}
