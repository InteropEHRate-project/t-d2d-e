package eu.interopehrate.td2de;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

import eu.interopehrate.d2d.D2DOperation;
import eu.interopehrate.d2d.D2DParameter;
import eu.interopehrate.d2d.D2DParameterName;
import eu.interopehrate.d2d.D2DRequest;
import eu.interopehrate.protocols.common.ResourceCategory;
import eu.interopehrate.td2de.api.TD2D;
import eu.interopehrate.td2de.api.TD2DListener;
import eu.interopehrate.td2ds.TD2DSecurityInterpreter;

class DefaultTD2DImpl implements TD2D {

	private static final Logger logger = Logger.getLogger(DefaultTD2DImpl.class.getName());
	private TD2DCommunication comm;
	private int itemsPerPage;
		
	public DefaultTD2DImpl(TD2DListener td2dListener,
			TD2DSecurityInterpreter interpreter,
			BufferedReader reader, OutputStream writer,
			String structureDefinitionsPath) {

		// Vaildates mandatory attributes
		if (interpreter == null)
			throw new IllegalArgumentException("Instantiation failed: TD2DSecurityInterpreter cannot be null! ");
		if (td2dListener == null)
			throw new IllegalArgumentException("Instantiation failed: TD2DListener cannot be null! ");
		if (reader == null)
			throw new IllegalArgumentException("Instantiation failed: InputStream cannot be null! ");
		if (writer == null)
			throw new IllegalArgumentException("Instantiation failed: OutputStream cannot be null! ");

		comm = new TD2DCommunication(
				td2dListener,
				interpreter,
				reader, writer,
				structureDefinitionsPath);

		//starts the thread that waits for incoming responses
		logger.fine("Starting Thread for reception of messages from S-EHR...");
		comm.start();
		logger.fine("Thread started succesfully, TD2D ready for exchange messages with MD2D");
	}
	

	@Override
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}


	@Override
	public int getItemsPerPage() {
		return this.itemsPerPage;
	}


	@Override
	public Iterator<Resource> getResources(Date from, boolean isSummary) throws Exception {
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");
		
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(itemsPerPage);
		if(from!=null)
			req.addParameter(new D2DParameter(D2DParameterName.DATE, from));
		
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, isSummary));
		
		//TODO add logging system
		//TODO improve exception declaration
		try {
			comm.sendEncryptedD2DRequest(req, null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getResources()", e);
			throw e;
		}
		
		return null;
	}

	
	@Override
	public Iterator<Resource> getResourcesByCategories(Date from, boolean isSummary, ResourceCategory... categories) throws Exception {
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");
		
		if(categories == null || categories.length == 0) {
			throw new IllegalArgumentException("Argument category cannot be null");
		}
		
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(itemsPerPage);
		if(from!=null)
			req.addParameter(new D2DParameter(D2DParameterName.DATE, from));
		
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, isSummary));
		
		for(int i=0; i<categories.length; i++) {
			req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, categories[i]));
		}
		
		//TODO add logging system
		//TODO improve exception declaration
		try {
			comm.sendEncryptedD2DRequest(req, null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getResourcesByCategories()", e);
			throw e;
		}
		
		return null;
	}

	
	@Override
	public Iterator<Resource> getResourcesByCategory(ResourceCategory category, Date from, boolean isSummary) throws Exception {
		return getResourcesByCategory(category, null, null, from, isSummary);
	}

	
	@Override
	public Iterator<Resource> getResourcesByCategory(ResourceCategory category, String subCategory, String type,
			Date from, boolean isSummary) throws Exception {
		
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");

		if(category == null) {
			throw new IllegalArgumentException("Argument category cannot be null");
		}
		
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(itemsPerPage);
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, category));
		
		if(subCategory != null) 
			req.addParameter(new D2DParameter (D2DParameterName.SUB_CATEGORY, subCategory));
		
		if(type != null) 
			req.addParameter(new D2DParameter (D2DParameterName.TYPE, type));
		
		if(from!=null)
			req.addParameter(new D2DParameter(D2DParameterName.DATE, from));
		
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, isSummary));
		
		//TODO add logging system
		//TODO improve exception declaration
		try {
			comm.sendEncryptedD2DRequest(req, null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getResourcesByCategory()", e);
			throw e;
		}
		return null;
	}

	
	@Override
	public Iterator<Resource> getMostRecentResources(ResourceCategory category, int mostRecentSize, boolean isSummary) throws Exception {
		return getMostRecentResources(category, null, null, mostRecentSize, isSummary);
	}

	
	@Override
	public Iterator<Resource> getMostRecentResources(ResourceCategory category, String subCategory, String type,
			int mostRecentSize, boolean isSummary) throws Exception {
		
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");

		if(category == null) {
			throw new IllegalArgumentException("Argument category cannot be null");
		}
		
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(itemsPerPage);
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, category));
		
		if(subCategory != null) 
			req.addParameter(new D2DParameter (D2DParameterName.SUB_CATEGORY, subCategory));
		
		if(type != null) 
			req.addParameter(new D2DParameter (D2DParameterName.TYPE, type));
		
		req.addParameter(new D2DParameter(D2DParameterName.MOST_RECENT, mostRecentSize));
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, isSummary));
		
		//TODO add logging system
		//TODO improve exception declaration
		try {
			comm.sendEncryptedD2DRequest(req, null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getMostRecentResources()", e);
			throw e;
		}
		
		return null;
	}

	
	@Override
	public Iterator<Resource> getResourcesById(String... ids) throws Exception {
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");

		D2DRequest req = new D2DRequest(D2DOperation.READ);
		req.getHeader().setItemsPerPage(itemsPerPage);
		for(int i=0; i<ids.length; i++) {
			req.addParameter(new D2DParameter(D2DParameterName.ID, ids[i]));
		}
		
		try {
			comm.sendEncryptedD2DRequest(req, null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getResourcesById()", e);
			throw e;
		}
		
		return null;
	}


	@Override
	public void closeConnectionWithSEHR() throws Exception {
		// TODO Auto-generated method stub
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");

		try {
			comm.sendEncryptedD2DRequest(new D2DRequest(D2DOperation.CLOSE_CONNECTION), null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getResourcesById()", e);
			throw e;
		}
		
	}


	@Override
	public void sendHealthData(Bundle healthData) throws Exception {
		if (comm.getState() == Thread.State.TERMINATED) 
			throw new IllegalStateException("Connection to SHER has been closed. Create a new insatnce of TD2D.");
		
		D2DRequest req = new D2DRequest(D2DOperation.WRITE);
		
		try {
			comm.sendEncryptedD2DRequest(req, healthData);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in method getResourcesById()", e);
			throw e;
		}	
		
	}
	

/*
 * IMPLEMENTAION OF SECURITY INTERFACE
 */
/*
	@Override
	public void sendHCPCertificate() throws Exception {
		String alias = "mykey";
		char[] password = "password".toCharArray();
		String keystore = "keystore.jks";

		// Reload the keystore
		KeyStore keyStore = KeyStore.getInstance("jks");
		keyStore.load(new FileInputStream(keystore), password);

		java.security.cert.Certificate cert = keyStore.getCertificate("mykey");
		String certs = Base64.getEncoder().encodeToString(cert.getEncoded());

		D2DBluetoothConnector.printKeysinBase64(alias, password, keystore);
		System.out.println("MSSG sendHCPCertificate -> " + certs);
		comm.sendData("pubkey" + certs);
		
	}

	@Override
	public void sendSymKey() throws Exception {
		EncryptedCommunication encryptedCommunication = EncryptedCommunicationFactory.create();
		KeyPair aliceKpair = encryptedCommunication.aliceInitKeyPair();
		aliceKpairKA = encryptedCommunication.aliceKeyAgreement(aliceKpair);
		comm.setSymKey(aliceKpairKA);
		byte[] alicePubKeyEnc = encryptedCommunication.alicePubKeyEnc(aliceKpair);
		String alicePubKeyEncs = Base64.getEncoder().encodeToString(alicePubKeyEnc);
		System.out.println("MSSG Alice sendSymKey -> " + alicePubKeyEncs);
		comm.sendData("symkey"+alicePubKeyEncs);
	}

	// ALESSIO: NOT USED IN THE PROJECT
	@Override
	public boolean verifySignature(RSAPublicKey publicKey, byte[] payload, byte[] sign)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		byte[] signedPayloadContent = Base64.getDecoder().decode(sign);
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initVerify(publicKey);
		signature.update(payload);
		boolean result = signature.verify(signedPayloadContent);
		System.out.println("MSSG verifySignature -> " + String.valueOf(result));
		return result;
	}

	@Override
	public void getSignedConsent(Patient patient) throws Exception{
		Consent consent = comm.createConsent(Consent.ConsentState.ACTIVE, patient);
		String encoded = FhirContext.forR4().newJsonParser().encodeResourceToString(consent);
		String alias = "mykey";
		char[] password = "password".toCharArray();
		String keystore = "keystore.jks";
		// Reload the keystore and display key and certificate chain info
		PrivateKey privateKey = D2DBluetoothConnector.loadPrivateKey(alias, password, keystore);
		String signature = comm.signPayload(encoded, privateKey);
		System.out.println("MSGG ConsentDetailsDocument - > \nfrom: " + patient.getName()
				+ "\n" + consent.getPatient().getIdentifier().getValue() + "#" + signature);
		comm.sendData("ConsentDetailsDocument#" + encoded + "#" + signature);	
	}
	*/
	
}
