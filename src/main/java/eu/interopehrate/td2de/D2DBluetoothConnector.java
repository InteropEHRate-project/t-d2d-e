package eu.interopehrate.td2de;

import java.io.IOException;
import java.util.logging.Logger;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import eu.interopehrate.td2de.api.D2DConnector;
import eu.interopehrate.td2de.api.TD2DSecureConnectionFactory;
import eu.interopehrate.td2ds.TD2DSecurityUtilities;
import eu.interopehrate.td2de.api.TD2DListener;

public class D2DBluetoothConnector implements D2DConnector {
	
	private static final Logger logger = Logger.getLogger(D2DBluetoothConnector.class.getName());
    private UUID uuid;
    private String connectionString = null;   
    private StreamConnection connection = null;
    private StreamConnectionNotifier streamConnNotifier; 
    
    
    @Override
	public TD2DSecureConnectionFactory openConnection(TD2DListener d2dListener, String structureDefinitionsPath) throws IOException {
        //Create a UUID for Server
        uuid = new UUID(0x1101);
        // Create the servicve url
        // Alessio: the name Sample SPP Server?
        connectionString = "btspp://localhost:" + uuid +";name=HCP App";      
        //open server url
        streamConnNotifier = (StreamConnectionNotifier)Connector.open(connectionString);
        //Wait for client connection
        logger.fine("Bluetooth connection is active. Waiting for S-EHR to connect...");
        connection = streamConnNotifier.acceptAndOpen();
        
        logger.fine("A S-EHR started connection procedure...");
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        logger.fine(String.format("Remote device address: %s, device name: %s ", 
        		dev.getBluetoothAddress(), dev.getFriendlyName(true)));
        
		return new TD2DSecureConnectionFactoryImpl(d2dListener,
				connection.openInputStream(),
				connection.openOutputStream(),
				structureDefinitionsPath);
	}


	public void closeConnection() throws IOException {
    	connection.close();
    	connection=null;
    	streamConnNotifier.close();
    	streamConnNotifier =null;
    }
	
	public boolean checkProvenance(String data) {
		if (data.contains("signature") && data.contains("author")) 
		{
			   return true;
		}
		else 
		{
			return false;
		}
	}

	
    //This will return the MAC address of the BT adapter in a specific format
    String getConnectionRequest(String BTadapterAddress){
        String s = BTadapterAddress;
        String s1 = s.substring(0, 2);
        String s2 = s.substring(2, 4);
        String s3 = s.substring(4, 6);
        String s4 = s.substring(6, 8);
        String s5 = s.substring(8, 10);
        String s6 = s.substring(10, 12);

        BTadapterAddress = s1 + ":" + s2 + ":" + s3 + ":" + s4 + ":" + s5 + ":" + s6;  
        
        return BTadapterAddress;
    }

    
    // Alessio: This method returns the BT addres, but also creates the jks
    // and fills it woth some default certificates
    public String getBtAdapterAddress() throws Exception {
        LocalDevice ld = LocalDevice.getLocalDevice() ;
        String btAdapterAddress = ld.getBluetoothAddress();
        btAdapterAddress= this.getConnectionRequest(btAdapterAddress);
        
        String signature = TD2DSecurityUtilities.signPayload(btAdapterAddress);
        return btAdapterAddress + "#" + signature;
    	
    	/*
        String alias = "mykey";
        char[] password = "password".toCharArray();
        String keystore = "keystore.jks";

        File f = new File(keystore);
        if (f.isFile() && f.canRead()) {
            System.out.println("EXIST -> LOAD");
            KeyStore keyStore=KeyStore.getInstance("jks");
            keyStore.load(new FileInputStream(keystore),password);
        } else {
            System.out.println("DON NOT EXIST -> FETCH");
            fetchCertificate();
        }

        LocalDevice ld =LocalDevice.getLocalDevice() ;
        String BTadapterAddress=ld.getBluetoothAddress();
        BTadapterAddress= this.getConnectionRequest(BTadapterAddress);

        //Reload the keystore and display key and certificate chain info
        PrivateKey privateKey = loadPrivateKey(alias, password, keystore);

        printKeysinBase64(alias,password,keystore);
        String signature = signPayload(BTadapterAddress, privateKey);
        System.out.println("MSGG SIGNATURE -> " + signature);

        return BTadapterAddress+"#"+signature;
        */
    }
    
    
    /*
    //This will initiate the BT connection & the messages exchange between the S-EHR app and the HCP app
    @Override
    @Deprecated
    public ConnectedThread listenConnection(D2DHRExchangeListeners listeners,D2DConnectionListeners listenersConnection, String structureDefinitionsPath) throws IOException{
        
        //Create a UUID for Server
        uuid = new UUID(0x1101);
        //Create the servicve url
        connectionString = "btspp://localhost:" + uuid +";name=Sample SPP Server";      
        //open server url
        streamConnNotifier = (StreamConnectionNotifier)Connector.open(connectionString);
        //Wait for client connection
        System.out.println("\nServer Started. Waiting for clients to connect...");
        connection = streamConnNotifier.acceptAndOpen();
        
        RemoteDevice dev = RemoteDevice.getRemoteDevice(connection);
        System.out.println("Remote device address: "+dev.getBluetoothAddress());
        System.out.println("Remote device name: "+dev.getFriendlyName(true));
        
        //Open new thread and start it so that it will listen for incoming streams
        try {
			thread = new ConnectedThread(connection,listeners,listenersConnection,streamConnNotifier, structureDefinitionsPath);
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        thread.start();  
        return thread;
    }  
    */

    //---SECURITY-UBIT---START
    

    /**
     * MUST BE MOVED IN SECURITY CLASSES.
     */
    /*
    @Override
    public void fetchCertificate() throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableEntryException {
        System.out.println("fetchCertificate-> CALLED");

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
	*/
    
    /*
    private static X509Certificate createSignedCertificate(X509Certificate cetrificate,X509Certificate issuerCertificate,PrivateKey issuerPrivateKey){
        try{
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
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private static void storeKeyAndCertificateChain(String alias, char[] password, String keystore, Key key, X509Certificate[] chain) throws Exception{
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(null,null);
        keyStore.setKeyEntry(alias, key, password, chain);
        keyStore.store(new FileOutputStream(keystore),password);
    }
    
    public static PrivateKey loadPrivateKey(String alias, char[] password, String keystore) throws Exception{
        //Reload the keystore
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore),password);

        Key key=keyStore.getKey(alias, password);
        java.security.cert.Certificate[] certs = new java.security.cert.Certificate[0];
        if(key instanceof PrivateKey){
            certs=keyStore.getCertificateChain(alias);
        }else{
            System.out.println("Key is not private key");
        }

        String priv = Base64.getEncoder().encodeToString(key.getEncoded());
        String cert = Base64.getEncoder().encodeToString(certs[2].getEncoded());

        return (PrivateKey) key;
    }

    public static PublicKey loadPublicKey(String alias, char[] password, String keystore) throws Exception{
        //Reload the keystore
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore),password);

        Key key=keyStore.getKey(alias, password);
        java.security.cert.Certificate[] certs = new java.security.cert.Certificate[0];
        if(key instanceof PrivateKey) {
            certs=keyStore.getCertificateChain(alias);
        }else{
            System.out.println("Key is not private key");
        }
        return (PublicKey) certs[0].getPublicKey();
    }

    private static void clearKeyStore(String alias,char[] password, String keystore) throws Exception{
        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore),password);
        keyStore.deleteEntry(alias);
        keyStore.store(new FileOutputStream(keystore),password);
    }
	*/

    /*
    @Override
    public String signPayload(String payload, PrivateKey privateKey)
            throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
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

    public static void printKeysinBase64(String alias, char[] password, String keystore) throws Exception {
        PrivateKey privkey = loadPrivateKey(alias, password, keystore);
        PublicKey publicKey = loadPublicKey(alias, password, keystore);

        String privkeys = Base64.getEncoder().encodeToString(privkey.getEncoded());
        String publicKeya = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        KeyStore keyStore=KeyStore.getInstance("jks");
        keyStore.load(new FileInputStream(keystore),password);
        java.security.cert.Certificate cert = keyStore.getCertificate(alias);
        String certa = Base64.getEncoder().encodeToString(cert.getEncoded());

        System.out.println("MSGG PRIVATE -> " + privkeys +
                "\nMSGG PUBLIC -> " + publicKeya +
                "\nMSGG CERTIFICATE -> " + certa);
    }
    */

    //---SECURITY-UBIT----END
}