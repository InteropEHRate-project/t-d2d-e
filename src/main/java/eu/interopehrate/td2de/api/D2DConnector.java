package eu.interopehrate.td2de.api;

import java.io.IOException;

/*
 *		Author: University of Piraeus Research Center
 *		Project: InteropEHRate - www.interopehrate.eu
 *
 *	Description: Interface of device-to-device (D2D) connection compliant to D2D specifications.
 *		     It allows a HCP application to start listening for a connection from a mobile device (S-EHR application).
 *		     Also it allows the closure of the Bluetooth connection between the two applications.
 */

public interface D2DConnector {

	
	/**
	 * open a Bluetooth conenction and waits for device to connect
	 * 
	 * @param d2dListener
	 * @param structureDefinitionsPath
	 * @return
	 */
	public TD2DSecureConnectionFactory openConnection(TD2DListener d2dListener, 
			String structureDefinitionsPath) throws IOException;
 
	
	/**
     *
     * Responsible for closing the Bluetooth Connection.
     *
     */
    public void closeConnection() throws IOException;

    
    /**
     *
     * Responsible for getting the current device's Bluetooth Adapter MAC Address.
     *
     * @return this device's Bluetooth MAC Address.
     */
    public String getBtAdapterAddress() throws Exception;

}