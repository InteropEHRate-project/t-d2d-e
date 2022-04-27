package eu.interopehrate.td2de.api;

import eu.interopehrate.protocols.client.ResourceReader;
import eu.interopehrate.protocols.client.ResourceWriter;

public interface TD2D extends ResourceReader, ResourceWriter {
	
	/**
	 * 
	 * @param itemsPerPage
	 */
	public void setItemsPerPage(int itemsPerPage);
	
	/**
	 * 
	 * @return
	 */
	public int getItemsPerPage();
	
	/**
	 * 
	 */
	public void closeConnectionWithSEHR() throws Exception;
}
