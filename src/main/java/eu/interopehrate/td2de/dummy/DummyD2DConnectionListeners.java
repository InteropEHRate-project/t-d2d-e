package eu.interopehrate.td2de.dummy;

import java.util.logging.Logger;

import eu.interopehrate.td2de.api.D2DConnectionListeners;

public class DummyD2DConnectionListeners implements D2DConnectionListeners {
	
	private static final Logger logger = Logger.getLogger(DummyD2DConnectionListeners.class.getName());

	public void onConnectionClosure() {
		logger.fine("onConnectionClosure()");
	}
	
}
