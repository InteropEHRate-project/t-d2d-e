package eu.interopehrate.td2ds;

import java.security.KeyPair;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;

import eu.interopehrate.encryptedCommunication.EncryptedCommunicationFactory;
import eu.interopehrate.encryptedCommunication.api.EncryptedCommunication;

public class TD2DSecurityInterpreter {

	private static final Logger logger = Logger.getLogger(TD2DSecurityInterpreter.class.getName());
    private static final String NEWLINE_REPLACEMENT = "##";
	private String symmetricKey;
	private KeyPair myKeypPair;
	private KeyAgreement myKeyAgreement;
	private EncryptedCommunication encryptedCommunication = EncryptedCommunicationFactory.create();
	
	/*
	public String generatePublicKey() throws Exception {
        // Alice = HCP App, Bob = S-EHR
		EncryptedCommunication encryptedCommunication = EncryptedCommunicationFactory.create();
		myKeypPair = encryptedCommunication.aliceInitKeyPair();
		myKeyAgreement = encryptedCommunication.aliceKeyAgreement(myKeypPair);
		byte[] hcpPubKeyEnc = encryptedCommunication.alicePubKeyEnc(myKeypPair);
		return Base64.getEncoder().encodeToString(hcpPubKeyEnc);
	}*/
	
	
	public TD2DSecurityInterpreter() throws Exception {
        // Alice = HCP App, Bob = S-EHR
		myKeypPair = encryptedCommunication.aliceInitKeyPair();
		myKeyAgreement = encryptedCommunication.aliceKeyAgreement(myKeypPair);
	}
	
	
	public String getSessionPublicKey() throws Exception {
		byte[] hcpPubKeyEnc = encryptedCommunication.alicePubKeyEnc(myKeypPair);
		return Base64.getEncoder().encodeToString(hcpPubKeyEnc);				
	}
	
	
	public void generateSessionSymmetricKey(String sehrPublicKey) throws Exception {
        // Alice = HCP App, Bob = S-EHR
		if (myKeyAgreement == null)
			throw new IllegalArgumentException("Before generating the symmetric key, the public key must have been generated!");
		
		byte[] sehrPublicKeyDecoded = Base64.getDecoder().decode(sehrPublicKey);
		KeyAgreement symkeyAgreement = encryptedCommunication.aliceKeyAgreementFin(sehrPublicKeyDecoded, myKeyAgreement);
		
		byte[] hcpSharedSecret = symkeyAgreement.generateSecret();
		SecretKeySpec symkeyspec = encryptedCommunication.generateSymmtericKey(hcpSharedSecret, 32);
		symmetricKey = Base64.getEncoder().encodeToString(symkeyspec.getEncoded());
		logger.fine("Symmetric key generated: " + symmetricKey);
	}
	
	
	public String encrypt(String stringToEncript) throws Exception {
		if (symmetricKey == null)
			throw new IllegalArgumentException("Encrypting is not possible, the symmetric key is null or empty!");

		String ecnryptedData = encryptedCommunication.encrypt(stringToEncript, symmetricKey);
        return ecnryptedData.replace("\n", NEWLINE_REPLACEMENT);
	}
	
	
	public String decrypt(String stringToDecrypt) throws Exception {
		if (symmetricKey == null)
			throw new IllegalArgumentException("Decrypting is not possible, the symmetric key is null or empty!");

        stringToDecrypt = stringToDecrypt.replace(NEWLINE_REPLACEMENT, "\n");
		return encryptedCommunication.decrypt(stringToDecrypt, symmetricKey);
	}
	
}
