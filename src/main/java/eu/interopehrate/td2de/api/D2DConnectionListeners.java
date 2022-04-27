package eu.interopehrate.td2de.api;

/*
 *		Author: University of Piraeus Research Center
 *		Project: InteropEHRate - www.interopehrate.eu
 *
 *	Description: Interface of device-to-device (D2D) connection listeners compliant to D2D specifications. 
 *			 It allows a Health Practitioner to have a connection closure message when the connection is closed.
 */
@Deprecated
public interface 	D2DConnectionListeners {

	/**
	*
	* Responsible for informing the HCP app that the connection has been closed.
	* 
	*/
	public void onConnectionClosure();
}
