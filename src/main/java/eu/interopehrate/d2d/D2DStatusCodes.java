package eu.interopehrate.d2d;

public final class D2DStatusCodes {

	// Request was successfully executed
	public static final int SUCCESSFULL = 200;
	
	// Request was executed but ended with an exception
	public static final int SEHR_APP_ERROR = 300;

	// The received request does not have a valid format
	public static final int BAD_REQUEST = 400;
	
	// The received response does not have a valid format
	public static final int BAD_RESPONSE = 410;

	// Exception raised in MD2D / TD2D classes
	public static final int D2D_ERROR = 500;

}
