package eu.interopehrate.td2de.dummy;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Practitioner;

import eu.interopehrate.d2d.D2DOperation;
import eu.interopehrate.d2d.D2DParameter;
import eu.interopehrate.d2d.D2DParameterName;
import eu.interopehrate.d2d.D2DRequest;
import eu.interopehrate.protocols.common.DocumentCategory;
import eu.interopehrate.protocols.common.FHIRResourceCategory;
import eu.interopehrate.td2de.D2DBluetoothConnector;
import eu.interopehrate.td2de.api.TD2DSecureConnectionFactory;
import eu.interopehrate.td2de.api.TD2D;
import eu.interopehrate.td2de.api.TD2DListener;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;


public class Main {

	private static final String STRUCTURE_DEFINITION_PATH = "C:\\development\\InteropEHRate_d2d_refactoring\\terminal-d2d-hr-exchange\\src\\main\\resources\\ipsValidatorPack";
	private static Logger logger; // = Logger.getLogger("TD2D");
	
	public static void main(String[] args) {
		
		// Configuration of logging
		String path = Main.class.getClassLoader().getResource("logging.properties").getFile();
		System.setProperty("java.util.logging.config.file", path);
		logger = Logger.getLogger(Main.class.getName());
		TD2D td2d = null;
		D2DBluetoothConnector btc = new D2DBluetoothConnector();
		TD2DListener listener = new DummyTD2DListener();
		
		//test checkProvenance
		if(btc.checkProvenance("{'author':'Kostis', 'signature':'aslhkhslhabckhjbckha', 'health_data':null}"))
		{
			System.out.println("Provenance data are present.");
		}
		else
		{
			System.out.println("Provenance data are not present.");
		}
		
		
		try {
			
			D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
			req.getHeader().setItemsPerPage(0);
			req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT));
			req.addParameter(new D2DParameter(D2DParameterName.MOST_RECENT, 2));
			req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, false));
			req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.PATIENT_SUMMARY));
			D2DParameter parameterTest =  req.getFirstOccurenceByName(D2DParameterName.CATEGORY);
			System.out.println(parameterTest);
			
			D2DParameter parameterTrue = new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT);
			System.out.println(parameterTrue);
			
			
			// starts waiting for connection from S-EHR
			TD2DSecureConnectionFactory d2dSecureConnection = btc.openConnection(listener, STRUCTURE_DEFINITION_PATH);
			// A S-EHR started the connection, from here on it starts the 
			// secure connection handshake
			FHIRObjectsFactory fhir = new FHIRObjectsFactory();
			Practitioner practitioner = fhir.buildPractitioner();
			td2d = d2dSecureConnection.createSecureConnection(practitioner);
			//td2d.setItemsPerPage(150);
			
			// from here on, it starts the exchange of health data
			Scanner sc = new Scanner(System.in);
			// TODO: add the close connection request
			// TODO: add the sending of a not compliant request
			while (true) {
				System.out.println("Type ps to request the patient summary" +
						"\nor id to request some specific resources by id" +
						"\nor all to request all resources " +
						"\nor categories to request only two categories " +
						"\nor category to request vital signs " +
						"\nor summary to request all resource in short form" +
						"\nor close to close the connection: ");
				String var = sc.nextLine();
				if (var.equals("close")) {
					td2d.closeConnectionWithSEHR();
					btc.closeConnection();
					break;
				} 
				else if(var.equals("ps")) {
					td2d.getResourcesByCategory(DocumentCategory.PATIENT_SUMMARY, 
							null, false);
				}
				else if(var.equals("id")) {
					String[] ids = {"4134134-341234-5674-76534","97979-2342342-776567-12345"}; 
					td2d.getResourcesById(ids);
				}
				else if(var.equals("all")) {
					td2d.getResources(null, false);
				}
				else if(var.equals("categories")) {
					GregorianCalendar gc = new GregorianCalendar(2019, Calendar.FEBRUARY, 14);
					td2d.getResourcesByCategories(gc.getTime(),
							false,
							DocumentCategory.LABORATORY_REPORT, 
							FHIRResourceCategory.OBSERVATION);
				}
				else if(var.equals("category")) {
					GregorianCalendar gc = new GregorianCalendar(2019, Calendar.FEBRUARY, 14);
					td2d.getResourcesByCategory(FHIRResourceCategory.OBSERVATION, 
							"vital-signs", 
							null, 
							gc.getTime(),
							false);
				}
				else if(var.equals("summary")) {
					td2d.getResources(null, true);
				}
				else if(var.equals("mr_image")) {
					td2d.getMostRecentResources(DocumentCategory.IMAGE_REPORT, 2, true);
				}
				else if(var.equals("mr_lab")) {
					td2d.getMostRecentResources(DocumentCategory.LABORATORY_REPORT, "vital-signs", null, 3, true);
				}
				else if(var.equals("write")) {
					int numObs = 100;
					Bundle b = new Bundle();
			        b.setType(Bundle.BundleType.SEARCHSET);

			        Observation obs;
			        Coding code;
			        Identifier id;
			        for (int i = 0; i < numObs; i++) {
			            obs = new Observation();
			            id = new Identifier();
			            id.setValue(String.valueOf(i));
			            obs.addIdentifier(id);
			            obs.addCategory(new CodeableConcept(new Coding(
			                    "http://terminology.hl7.org/CodeSystem/observation-category",
			                    "vital-signs", "")));
			            obs.setStatus(Observation.ObservationStatus.FINAL);
			            obs.setEffective(new DateTimeType());
			            obs.addNote(new Annotation().setText("This is the a dummy annotation for observation " +
			                    "number " + (i + 1) + " created by Charis and Alessio."));

			            code = new Coding("http://loinc.org", "29463-7", "Body Weight");
			            obs.setCode(new CodeableConcept(code));

			            Quantity q = new Quantity();
			            q.setValue(100);
			            q.setUnit("kg");
			            q.setSystem("http://unitsofmeasure.org");
			            q.setCode("kg");
			            obs.setValue(q);

			            b.addEntry().setResource(obs);
			            
				}
			        td2d.sendHealthData(b);
			}
				
		}
	} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
/*	
	public static void oldmain(String[] args) throws Exception {

		ConnectedThread thread;
		TD2D td2d;

		// the way that the methods should be called to initiate the connection and
		// exchange messages between S-EHR app and HCP app
		D2DBluetoothConnector btc = new D2DBluetoothConnector();
		TD2DListener listener = new DummyTD2DListener();
		DummyD2DConnectionListeners listenersConnection = new DummyD2DConnectionListeners();

		// get BT addapter + # + BT addapter signature
		
		// ALESSIO: what is it for? is it important this method?
		System.out.println("MSGG BT MAC ADDRESS -> " + btc.getBtAdapterAddress());

		// charis
		// kostis
		// String structureDefinitionsPath="C:\\Users\\kosvid\\Dropbox\\interop
		// code\\terminal-d2d-hr-exchangetesting\\src\\main\\resources\\ipsValidatorPack";
		// sofianna
		// String structureDefinitionsPath =
		// "D:\\\\IntelijProjects\\terminal-d2d-hr-exchange\\src\\main\\resources\\ipsValidatorPack";
		
		td2d = btc.newListenConnection(listener, STRUCTURE_DEFINITION_PATH); //Server Started

		// auto send when the connection is made.
		FHIRObjectsFactory fhir = new FHIRObjectsFactory();
		Practitioner practitioner = fhir.buildPractitioner();
		
		// Alessio: MANDATORY step
		td2d.sendHCPIdentity(practitioner);

		// We received the patient
		List myList = new ArrayList();
		myList.add("Onomaaa");
		Patient patient = fhir.buildPatient();
		patient.setName(myList);
		// Alessio: MANDATORY step
		td2d.sendHCPCertificate();
		
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println("Type getconsent to send consent details" +
					"\nor pubkey to send HCP cert" +
					"\nor symkey to establish session key" +
					"\nor prescr to send prescription " +
					"\nor vital to send vital signs " +
					"\nor med_doc to send medical document request" +
					"\nor close to close the connection: ");
			String var = sc.nextLine();

			if (var.equals("getconsent")) {
				td2d.getSignedConsent(patient);
			} else if (var.equals("pubkey")) {
				td2d.sendHCPCertificate();
			} else if (var.equals("close")) {
				btc.closeConnection();
				break;
			} else if (var.equals("prescr")) {
				Bundle prescription = new Bundle();
				//thread.sendPrescription(prescription);
			} else if (var.equals("vital")) {
				URL is = Main.class.getClassLoader().getResource("VITAL_SIGN_EXAMPLE.json");//("PathologyCompositionExampleIPS_202010301409.json"); Sofianna testare to kai me to pathology pou doulevei
				File f = new File(is.toURI());
				Scanner scanner = new Scanner(f);
				String content = scanner.useDelimiter("\\Z").next();
				IParser parser = FhirContext.forR4().newJsonParser();
				Bundle vitalSigns = parser.parseResource(Bundle.class, content);
				//thread.sendVitalSigns(vitalSigns);
				scanner.close();
			} else if (var.equals("symkey")) {
				td2d.sendSymKey();
			}
//			else if(var.equals("med_doc")) {
//				LocalDate startingDate = LocalDate.of(2020,02,20);
//				LocalDate endingDate = LocalDate.of(2020,03,25);
//				String type = "cardiology";
//				//thread.sendMedicalDocumentRequest(startingDate,endingDate,type);
//			}
			else if(var.equals("ps")) {
				td2d.getResourcesByCategory(DocumentCategory.PATIENT_SUMMARY, 
						null, false);
			}
			else if(var.equals("id")) {
				String[] ids = {"4134134-341234-5674-76534","97979-2342342-776567-12345"}; 
				td2d.getResourcesById(ids);
				//td2d.getResourcesById("4134134-341234-5674-76534","4134134-341234-5674-76534");
			}
			else if(var.equals("no_cat")) {
				Date date = new Date();
				System.out.println(date);
				td2d.getResources(date,
						false);
			}
			else if(var.equals("categories")) {
				Date date = new Date();  
				td2d.getResourcesByCategories(date,
						false,
						DocumentCategory.LABORATORY_REPORT, 
						FHIRResourceCategory.OBSERVATION);
			}
		}
	 

		// 2nd connection - when testing if you close 1st one from SEHR press any button
		// to get out of the loop
//        ConnectedThread thread2;  
//        //the way that the methods should be called to initiate the connection and exchange messages between S-EHR app and HCP app
//        BluetoothConnection btc2 = new BluetoothConnection();                 
//        thread2 = btc.listenConnection(listeners,listenersConnection);
//        FHIRObjectsFactory fhir2 = new FHIRObjectsFactory();
//      	Practitioner practitioner2 =fhir2.buildPractitioner();
//    
		sc.close();
	}
*/
	
}