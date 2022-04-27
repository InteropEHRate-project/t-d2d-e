package eu.interopehrate.td2de.api;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public interface TD2DListener {

	public boolean onCitizenPersonalDataReceived(Patient patient);
	
	
	public void onRead(Bundle healthDataBundle, int currentPage, int totalPages);
	
	
	public void onSearch(Bundle healthDataBundle, int currentPage, int totalPages);
	
	
	public void onError(int errorCode, String errorMessage);

	
	public void onConnectionClosure();
		
}
