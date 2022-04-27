package eu.interopehrate.td2de;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.hl7.fhir.r4.model.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import eu.interopehrate.d2d.D2DOperation;
import eu.interopehrate.d2d.D2DParameter;
import eu.interopehrate.d2d.D2DParameterConverter;
import eu.interopehrate.d2d.D2DRequest;
import eu.interopehrate.d2d.D2DResponse;
import eu.interopehrate.d2d.D2DStatusCodes;
import eu.interopehrate.td2de.api.TD2DListener;
import eu.interopehrate.td2ds.TD2DSecurityInterpreter;

class TD2DCommunication extends Thread {
	private static final Logger logger = Logger.getLogger(TD2DCommunication.class.getName());
	private TD2DListener listener;
	private Map<String, D2DRequest> requestCache = new HashMap<String, D2DRequest>();
	private final OutputStream outputChannel;
	private final BufferedReader inputChannel;
	private String lineRead;
	private String structureDefinitionsPath;
	private final TD2DSecurityInterpreter interpreter;
	private final Gson gson;
	private final IParser fhirParser;
	private final TD2DListenerInvoker listenerInvoker;
	
	// 
	private StreamConnection streamConnection;
	private StreamConnectionNotifier streamConnNotifier;

	
	//change!!!
	// Alessio: they will be stored in D2DSecurityInterpreter and are not needed here
	//private String symkey = "Bos0HSxY4HWrVwEZaoywbAnP8a0BWExEfl5pyHULEXQ=";
	//private KeyAgreement aliceKpairKA;
	//private KeyStore keystore;
	
	public TD2DCommunication(TD2DListener td2dListener,
			TD2DSecurityInterpreter interpreter,
			BufferedReader reader, OutputStream writer,
			String structureDefinitionsPath) {
		
		
		setName("TD2D");
		// Vaildates mandatory attributes
		if (interpreter == null)
			throw new IllegalArgumentException("Instantiation failed: TD2DSecurityInterpreter cannot be null! ");
		if (td2dListener == null)
			throw new IllegalArgumentException("Instantiation failed: TD2DListener cannot be null! ");
		if (reader == null)
			throw new IllegalArgumentException("Instantiation failed: InputStream cannot be null! ");
		if (writer == null)
			throw new IllegalArgumentException("Instantiation failed: OutputStream cannot be null! ");
		
		this.listener = td2dListener;
		this.structureDefinitionsPath = structureDefinitionsPath;
		this.outputChannel = writer;
		this.inputChannel = reader;
		this.interpreter = interpreter;
		
		// Create Gson serializer / deserializer
		gson = new GsonBuilder().
				registerTypeAdapter(D2DParameter.class, new D2DParameterConverter())
				.create();
		// create FHIR parse
		fhirParser = FhirContext.forR4().newJsonParser();
		// creates and starts listener invoker
		listenerInvoker = new TD2DListenerInvoker();
		listenerInvoker.start();
	}
	
	
	public void run() {
		D2DResponse response;
		D2DRequest request;
		Bundle responseBundle;
		
		while (true) {
			try {
				// step 1: receiving message from S_EHR
				try {
					lineRead = inputChannel.readLine();
	                if(lineRead == null) {
	                	logger.fine("BT Connection closed by SEHR. Closing communication channels and stopping Thread.");
						closeConnection();
						break;
	                }
					logger.fine("Received a message of " + lineRead.length() + " bytes from SEHR:" + lineRead);
	                if (lineRead.trim().isEmpty()) {
	                	logger.fine("Warning: received empty line from SEHR, line has been ignored.");
	                    continue;
	                }
				} catch (IOException ioe) {
					logger.fine("BT Connection closed by SEHR. Closing communication channels and stopping Thread.");
					closeConnection();
					break;
				}
				
				// step 2: deserialization of the received string to a D2DRequest
				response = gson.fromJson(lineRead, D2DResponse.class);
				logger.fine("Received response from SEHR with the following status: " + response.getStatus());
				// looks for corresponding D2DRequest
				request = requestCache.get(response.getHeader().getRequestId());
				
				if (request != null) {
					if (response.getStatus() == D2DStatusCodes.SUCCESSFULL) {
						// Handle successful response
						// extract header to analyze how many packets compose the shipping
						logger.fine("Decrypting and deserialized response body...");
				        long start = System.currentTimeMillis();
				        String decryptedBody;
				        try {
					        decryptedBody = interpreter.decrypt(response.getBody());
					        logger.fine("Decryption duration: "  + ((System.currentTimeMillis() - start) / 1000F));
				        }catch(Exception e) {
				        	logger.severe("Data can not be decrtypted. Connection with SEHR is clossed");
				        	closeConnection();
				        	break;
				        }
				        
				        try {
							responseBundle = fhirParser.parseResource(Bundle.class, decryptedBody);
							logger.fine("Deserialization duration: "  + ((System.currentTimeMillis() - start) / 1000F));
							listenerInvoker.addNotification(request, response, responseBundle, this.listener);
				        } catch(Exception e){
				        	logger.severe("Bundle was not compliant to FHIR. Connection with SEHR is clossed");
				        	closeConnection();
				        	break;
				        }
				        
					} else {
						listenerInvoker.addNotification(request, response, null, this.listener);
					}
					responseBundle = null;
					
					// removes request from cache because it has been completely handled
					if (response.getHeader().getPage() == response.getHeader().getTotalPages()) {
						if (requestCache.containsKey(request.getId()))
							requestCache.remove(request.getId());
					}					
				} else {
					logger.severe(String.format(
							"Error: received response contains an invalid request id: %s ! Response discarded.", 
							response.getHeader().getRequestId()));					
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Unexpected exception in handling response from S-EHR", e);
			}
		}
	}
	
		

	
	public void sendEncryptedD2DRequest(D2DRequest request, Bundle healthData) throws Exception {
		/**
		String serializedRequest = gson.toJson(request);
		String encryptedRequest = "enc" + interpreter.encrypt(serializedRequest) + "\n";
		//logger.fine("Sending encrypted request: " + encryptedRequest);
		outputChannel.writeUTF(encryptedRequest);
		outputChannel.flush();
		*/
        // encrypt the healthData (data are encrypted and then Base64 encoded)
		if (healthData != null) {
			logger.fine("Encrypting request body...");
	        String serializedBundle = fhirParser.encodeResourceToString(healthData);
	        request.setBody(interpreter.encrypt(serializedBundle));
	        // to release memory
	        serializedBundle = null;
		}

        // Serialize D2DRequest to JSON
		String serializedRequest = gson.toJson(request);
		
		logger.fine("Sending request...");
		outputChannel.write(serializedRequest.getBytes(StandardCharsets.UTF_8.name()));
		outputChannel.write("\n".getBytes(StandardCharsets.UTF_8.name()));
		outputChannel.flush();
		requestCache.put(request.getId(), request);
	}

	
	public void closeConnection() throws Exception {
		this.closeStreams();
		this.listener.onConnectionClosure();
		this.listenerInvoker.interrupt();
		if(streamConnection != null) {
			this.streamConnection.close();
			streamConnection = null;
		}
		if(streamConnNotifier != null) {
			streamConnNotifier.close();
			streamConnNotifier = null;
		}
	}

	public void closeStreams() throws Exception {
		// closing streams
		try {
			inputChannel.close();
			System.out.println("Streams closed.");
		} catch (IOException e) {}
		
		try {
			outputChannel.close();
		} catch (IOException e) {
			System.out.println("Streams not closed");
		}		
	}

	/*
	public Consent createConsent(Consent.ConsentState theState, Patient thePatient) {
		Consent consent = new Consent();
		consent.setStatus(theState);

		consent.addCategory().addCoding(new Coding("http://loinc.org", "59284-0", "Patient Consent"));
		consent.setPatient(new Reference(thePatient.getIdElement()));
		consent.setDateTime(new Date());
		Narrative narrative = new Narrative();
		narrative.setStatusAsString("generated");
		narrative.setDivAsString("I have read and understood InteropEHRate's <a href=\"\">Privacy Policy</a>.\\n\\n"
				+ "I hereby give permission to the recipient health care provider to process (view, store, edit etc.) "
				+ "the personal data stored in my Personal Health Record on this application for the purpose of medical diagnosis and/or treatment. "
				+ "I understand that my consent will remain valid for these purposes unless I affirmatively withdraw it. "
				+ "I have the right to withdraw this consent at any time.");
		consent.setText(narrative);
		// TODO: update values
		return consent;
	}
	
	public String signPayload(String payload, PrivateKey privateKey) throws IOException, SignatureException,
			InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		Signature privateSignature = null;
		try {
			privateSignature = Signature.getInstance("SHA256withRSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		privateSignature.initSign(privateKey);
		privateSignature.update(payload.getBytes(UTF_8));
		byte[] signature = privateSignature.sign();
		return Base64.getEncoder().encodeToString(signature);
	}
	
	public void setSymKey(KeyAgreement aliceKpairKA) {
		this.aliceKpairKA = aliceKpairKA;
	}
	
	private void onCertificateReceivedStore(byte[] tempCert)
			throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		java.security.cert.Certificate trustedCertificate = cf.generateCertificate(new ByteArrayInputStream(tempCert));
		System.out.print("Received Public:" + trustedCertificate.getPublicKey().toString());

		// Store the certificate chain
		KeyStore keyStore = KeyStore.getInstance("jks");
		keyStore.load(null, null);
		keyStore.setCertificateEntry("InteropEHRate", trustedCertificate);
		keystore = keyStore;

		File keystoreFile = new File("keystore.jks");
		// Load the keystore contents
		FileInputStream in = new FileInputStream(keystoreFile);
		keystore.load(in, "password".toCharArray());
		in.close();

		// Add the certificate
		keystore.setCertificateEntry("InteropEHRate", trustedCertificate);

		// Save the new keystore contents
		FileOutputStream out = new FileOutputStream(keystoreFile);
		keystore.store(out, "password".toCharArray());
		out.close();
	}
	*/

}
