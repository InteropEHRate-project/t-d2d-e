package eu.interopehrate.td2de.api;

import org.hl7.fhir.r4.model.Practitioner;

public interface TD2DSecureConnectionFactory {
	
	
	TD2D createSecureConnection(Practitioner practitioner) throws Exception;

}
