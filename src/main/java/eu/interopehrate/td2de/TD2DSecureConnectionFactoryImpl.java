package eu.interopehrate.td2de;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import eu.interopehrate.d2d.D2DSecurityMessage;
import eu.interopehrate.d2d.D2DSecurityOperation;
import eu.interopehrate.td2de.api.TD2DSecureConnectionFactory;
import eu.interopehrate.td2de.api.TD2D;
import eu.interopehrate.td2de.api.TD2DListener;
import eu.interopehrate.td2ds.TD2DSecurityInterpreter;
import eu.interopehrate.td2ds.TD2DSecurityUtilities;

class TD2DSecureConnectionFactoryImpl implements TD2DSecureConnectionFactory {

	private static final Logger logger = Logger.getLogger(TD2DSecureConnectionFactoryImpl.class.getName());
	private TD2DListener td2dListener;
	private OutputStream outputChannel;
	private BufferedReader inputChannel;
	private TD2DSecurityInterpreter securityInterpreter;
	private Gson gson = new Gson();	
	
	TD2DSecureConnectionFactoryImpl(TD2DListener td2dListener, InputStream in, 
			OutputStream out, String structureDefinitionPath) throws UnsupportedEncodingException {

		// opens input channel
		inputChannel = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		// opens output channel
		outputChannel = new BufferedOutputStream(out);
		this.td2dListener = td2dListener;
	}
	
	@Override
	public TD2D createSecureConnection(Practitioner practitioner) throws Exception {
		logger.fine("Starting secure connection protocol...");
		
		// executes some security checks
		TD2DSecurityUtilities.initialize();
		// Creates the parser for FHIR resources
		IParser fhirParser = FhirContext.forR4().newJsonParser();
		
		/*
		 *  1 Exchange of demographic info
		 */
		logger.fine("Sending HELLO_SEHR message...");
		D2DSecurityMessage outgoingMsg = new D2DSecurityMessage();
		D2DSecurityMessage replyMsg = null;
		outgoingMsg.setOperation(D2DSecurityOperation.HELLO_SEHR);
		outgoingMsg.setBody(fhirParser.encodeResourceToString(practitioner));
		replyMsg = sendsMsgAndWaitForReply(outgoingMsg);
		if (replyMsg.getOperation() != D2DSecurityOperation.HELLO_HCP)
            throw new IllegalStateException("Received " + replyMsg.getOperation()
            + " message while expecting HELLO_HCP message.");
		
		if (replyMsg.getBody() == null || replyMsg.getBody().isEmpty())
            throw new IllegalStateException("Received empty message, secure connection protocol aborted.");
		
		// check for ClassCastException or body 
		Patient sehrUser = (Patient)fhirParser.parseResource(replyMsg.getBody());
		if (!td2dListener.onCitizenPersonalDataReceived(sehrUser)) {
			logger.info("D2DSecureConnection not established, citizen identity not matched by HCP");
			throw new IllegalStateException();
		}
		logger.fine("Exchange of demographic information was successfull.");	
		// logger.log(Level.SEVERE, e.getMessage(), e);
		
		// 2 sends consent
		
		/*
		 * 3 Exchange of HCP certificates and expects to receive SEHR certificates
		 */
//		logger.fine("Sending HCP_CERTIFICATE message...");
//		outgoingMsg = new D2DSecurityMessage();
//		outgoingMsg.setOperation(D2DSecurityOperation.HCP_CERTIFICATE);
//		outgoingMsg.setBody(TD2DSecurityUtilities.getHCPCertificate());
//		replyMsg = sendsMsgAndWaitForReply(outgoingMsg);
//		if (replyMsg.getOperation() != D2DSecurityOperation.SEHR_CERTIFICATE)
//            throw new IllegalStateException("Received " + replyMsg.getOperation()
//            + " message while expecting SEHR_CERTIFICATE message.");
//
//		if (replyMsg.getBody() == null || replyMsg.getBody().isEmpty())
//            throw new IllegalStateException("Received empty message, secure connection protocol aborted.");

		// store SEHR certificate
		
		// 4 sends public key and expects to receive SEHR public key
		securityInterpreter = new TD2DSecurityInterpreter();
		logger.fine("Sending HCP_PUBLIC_KEY message...");
		outgoingMsg = new D2DSecurityMessage();
		outgoingMsg.setOperation(D2DSecurityOperation.HCP_PUBLIC_KEY);
		outgoingMsg.setBody(securityInterpreter.getSessionPublicKey());
		replyMsg = sendsMsgAndWaitForReply(outgoingMsg);
		if (replyMsg.getOperation() != D2DSecurityOperation.SEHR_PUBLIC_KEY)
            throw new IllegalStateException("Received " + replyMsg.getOperation()
            + " message while expecting SEHR_PUBLIC_KEY message.");

		if (replyMsg.getBody() == null || replyMsg.getBody().isEmpty())
            throw new IllegalStateException("Received empty message, secure connection protocol aborted.");
		// security uses S-EHR public key to generate the symmetric key 
		// for the communication
		
		logger.fine("Generating Symmetric Key...");
		securityInterpreter.generateSessionSymmetricKey(replyMsg.getBody());
		
		logger.fine("Secure connection successfully established");
		
		// return the instance of TD2DImpl that will handle the exchange of health data
		return new DefaultTD2DImpl(td2dListener, securityInterpreter, inputChannel, outputChannel, null);
	}
	
	private D2DSecurityMessage sendsMsgAndWaitForReply(D2DSecurityMessage outMsg) throws Exception {
		outputChannel.write(gson.toJson(outMsg).getBytes(StandardCharsets.UTF_8));
		outputChannel.write("\n".getBytes(StandardCharsets.UTF_8));
		outputChannel.flush();
		
		String replyMsg = inputChannel.readLine();
		logger.fine("replyMsg:"+ replyMsg);
		// replyMsg = replyMsg.substring(2);
		return gson.fromJson(replyMsg, D2DSecurityMessage.class);
	}

}
