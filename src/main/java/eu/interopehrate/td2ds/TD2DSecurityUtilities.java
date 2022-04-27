package eu.interopehrate.td2ds;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Logger;

import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public final class TD2DSecurityUtilities {

	private static final Logger logger = Logger.getLogger(TD2DSecurityUtilities.class.getName());
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

	private static final String KEYSTORE_NAME = "keystore.jks";
	private static final String KEYSTORE_TYPE = "jks";
	private static final String KEYSTORE_PASSWORD = "password";
	private static final String HCP_CERTIFICATE_ALIAS = "mykey";
	
	//private static final String CERTIFICATE_ENTRY = "InteropEHRate";

	/**
	 * @throws Exception 
	 * 
	 */
	public static void initialize() throws Exception {
		logger.fine("Initializing SecurityUtilities...");
        File f = new File(KEYSTORE_NAME);
        if (!f.isFile() || !f.canRead()) {
    		logger.fine("KeyStore does not exist. Creation of keystore in progress...");
    		generateKeyStore();
    		logger.fine("KeyStore created and stored successfully");
        }
	}
	
	
    private static void generateKeyStore() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableEntryException {
        PrivateKey topPrivateKey = null;

        try{
            //Generate ROOT certificate
            CertAndKeyGen keyGen=new CertAndKeyGen("RSA","SHA1WithRSA",null);
            keyGen.generate(1024);
            PrivateKey rootPrivateKey=keyGen.getPrivateKey();

            X509Certificate rootCertificate = keyGen.getSelfCertificate(new X500Name("CN=ROOT"), (long) 365 * 24 * 60 * 60);

            //Generate intermediate certificate
            CertAndKeyGen keyGen1=new CertAndKeyGen("RSA","SHA1WithRSA",null);
            keyGen1.generate(1024);
            PrivateKey middlePrivateKey=keyGen1.getPrivateKey();

            X509Certificate middleCertificate = keyGen1.getSelfCertificate(new X500Name("CN=MIDDLE"), (long) 365 * 24 * 60 * 60);

            //Generate leaf certificate
            CertAndKeyGen keyGen2=new CertAndKeyGen("RSA","SHA1WithRSA",null);
            keyGen2.generate(1024);
            topPrivateKey=keyGen2.getPrivateKey();

            X509Certificate topCertificate = keyGen2.getSelfCertificate(new X500Name("CN=TOP"), (long) 365 * 24 * 60 * 60);

            rootCertificate   = createSignedCertificate(rootCertificate,rootCertificate,rootPrivateKey);
            middleCertificate = createSignedCertificate(middleCertificate,rootCertificate,rootPrivateKey);
            topCertificate    = createSignedCertificate(topCertificate,middleCertificate,middlePrivateKey);

            X509Certificate[] chain = new X509Certificate[3];
            chain[0]=topCertificate;
            chain[1]=middleCertificate;
            chain[2]=rootCertificate;

            String alias = "mykey";
            char[] password = "password".toCharArray();
            String keystore = "keystore.jks";

            //Store the certificate chain
            storeKeyAndCertificateChain(alias, password, keystore, topPrivateKey, chain);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    
    private static X509Certificate createSignedCertificate(X509Certificate cetrificate,
    		X509Certificate issuerCertificate, PrivateKey issuerPrivateKey) throws Exception {
        Principal issuer = issuerCertificate.getSubjectDN();
        String issuerSigAlg = issuerCertificate.getSigAlgName();

        byte[] inCertBytes = cetrificate.getTBSCertificate();
        X509CertInfo info = new X509CertInfo(inCertBytes);
        info.set(X509CertInfo.ISSUER, issuer);

        //No need to add the BasicContraint for leaf cert
        if(!cetrificate.getSubjectDN().getName().equals("CN=TOP")){
            CertificateExtensions exts=new CertificateExtensions();
            BasicConstraintsExtension bce = new BasicConstraintsExtension(true, -1);
            exts.set(BasicConstraintsExtension.NAME,new BasicConstraintsExtension(false, bce.getExtensionValue()));
            info.set(X509CertInfo.EXTENSIONS, exts);
        }

        X509CertImpl outCert = new X509CertImpl(info);
        outCert.sign(issuerPrivateKey, issuerSigAlg);

        return outCert;
    }

    
    private static void storeKeyAndCertificateChain(String alias, char[] password, 
    		String keystore, Key key, X509Certificate[] chain) throws Exception{
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(null,null);
        keyStore.setKeyEntry(alias, key, password, chain);
        keyStore.store(new FileOutputStream(keystore),password);
    }
    
    
    private static PrivateKey getPrivateKey() throws Exception {
		KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
		keyStore.load(new FileInputStream(KEYSTORE_NAME), KEYSTORE_PASSWORD.toCharArray());
        Key key = keyStore.getKey(HCP_CERTIFICATE_ALIAS, KEYSTORE_PASSWORD.toCharArray());

        java.security.cert.Certificate[] certs = new java.security.cert.Certificate[0];
        if (key instanceof PrivateKey)
            certs = keyStore.getCertificateChain(HCP_CERTIFICATE_ALIAS);
        else
            throw new IllegalStateException("Key is not private key");

        return (PrivateKey) key;
    }
    
	/**
	 * 
	 * @param publicKey
	 * @param payload
	 * @param sign
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public static boolean verifySignature(RSAPublicKey publicKey, byte[] payload, byte[] sign)
			throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		byte[] signedPayloadContent = Base64.getDecoder().decode(sign);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicKey);
		signature.update(payload);
		boolean result = signature.verify(signedPayloadContent);
		logger.fine("verifySignature: " + result);
		return result;
	}

	/**
	 * 
	 * @param payload
	 * @param privateKey
	 * @return
	 * @throws IOException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static String signPayload(String payload) throws Exception {
		Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
		PrivateKey privateKey = getPrivateKey();
		privateSignature.initSign(privateKey);
		privateSignature.update(payload.getBytes(UTF_8));
		byte[] signature = privateSignature.sign();
		return Base64.getEncoder().encodeToString(signature);
	}
	
	
	/**
	 * Retrieves the HCP Certificate from the keystore
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws Exception
	 */
	public static String getHCPCertificate() throws Exception,
			FileNotFoundException, IOException {
		KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
		keyStore.load(new FileInputStream(KEYSTORE_NAME), KEYSTORE_PASSWORD.toCharArray());

		Certificate cert = keyStore.getCertificate(HCP_CERTIFICATE_ALIAS);
		return Base64.getEncoder().encodeToString(cert.getEncoded());
	}
	
		
	/*
	public static String storeCertificate(byte[] tempCert) {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate trustedCertificate = cf.generateCertificate(new ByteArrayInputStream(tempCert));
		System.out.print("Received Public:" + trustedCertificate.getPublicKey().toString());

		// Creates an empty KeyStore and add the certificate to it
		KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
		keyStore.load(null, null);
		keyStore.setCertificateEntry(CERTIFICATE_ENTRY, trustedCertificate);
		// keystore = keyStore;

		// Stores the new KeyStore in a file called KEYSTORE_NAME
		File keystoreFile = new File(KEYSTORE_NAME);
		FileInputStream in = new FileInputStream(keystoreFile);
		keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
		in.close();

		// Add the certificate
		keystore.setCertificateEntry("InteropEHRate", trustedCertificate);

		// Save the new keystore contents
		FileOutputStream out = new FileOutputStream(keystoreFile);
		keystore.store(out, "password".toCharArray());
		out.close();
	}
	*/
	
	/*
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
