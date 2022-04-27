package eu.interopehrate.td2de;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import eu.interopehrate.d2d.D2DOperation;
import eu.interopehrate.d2d.D2DParameter;
import eu.interopehrate.d2d.D2DParameterName;
import eu.interopehrate.d2d.D2DRequest;
import eu.interopehrate.protocols.common.DocumentCategory;
import eu.interopehrate.td2de.dummy.DummyD2DConnectionListeners;
import eu.interopehrate.td2de.dummy.FHIRObjectsFactory;

public class TestTD2D {
	
	@Before
	public void init() {
        DummyD2DConnectionListeners listenersConnection = new DummyD2DConnectionListeners();
	}

	//have bluetooth open on computer to do these tests.

	@Test 
	public void testBTadapterFormatChange() {
		
		D2DBluetoothConnector btc = new D2DBluetoothConnector();
		String beforeFormat = "001122334455";
		String afterFormat = "00:11:22:33:44:55";
		assertEquals(afterFormat,btc.getConnectionRequest(beforeFormat));
	}
	
	@Test
	public void testCreateFhirObjectsFactory() {
		
		FHIRObjectsFactory fhir = new FHIRObjectsFactory();
		assertNotNull(fhir);
		assertTrue(fhir instanceof FHIRObjectsFactory);
	}
	
	@Test
	public void testCreatePractitioner() {
		
		FHIRObjectsFactory fhir = new FHIRObjectsFactory();
		Practitioner practitioner =fhir.buildPractitioner();
		assertNotNull(practitioner);
		assertTrue(practitioner instanceof Practitioner);
	}
	
	@Test
	public void testD2DRequest() {
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		assertTrue(req instanceof D2DRequest);
	}
	
	@Test
	public void testGetFirstOccurenceByName() {
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(0);
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT));
		req.addParameter(new D2DParameter(D2DParameterName.MOST_RECENT, 2));
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, false));
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.PATIENT_SUMMARY));
		D2DParameter parameterActual  =  req.getFirstOccurenceByName(D2DParameterName.CATEGORY);
		D2DParameter parameterExpected = new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT);
		//assertThat(parameterExpected).isEqualToComparingFieldByField(parameterActual);
		assertEquals(parameterExpected.toString(), parameterActual.toString());
	}
	
	@Test
	public void testGetFirstOccurenceValueByName() {
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(0);
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT));
		req.addParameter(new D2DParameter(D2DParameterName.MOST_RECENT, 2));
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, false));
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.PATIENT_SUMMARY));
		Object parameter = req.getFirstOccurenceValueByName(D2DParameterName.CATEGORY);
		
		assertEquals(DocumentCategory.LABORATORY_REPORT, parameter);
	}
	
	@Test
	public void testGetAllOccurencesByName() {
		D2DRequest req = new D2DRequest(D2DOperation.SEARCH);
		req.getHeader().setItemsPerPage(0);
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.IMAGE_REPORT));
		req.addParameter(new D2DParameter(D2DParameterName.MOST_RECENT, 4));
		req.addParameter(new D2DParameter(D2DParameterName.SUMMARY, true));
		req.addParameter(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT));
		List <D2DParameter> occurencesActual = new ArrayList<D2DParameter>(); 
		occurencesActual = req.getAllOccurencesByName(D2DParameterName.CATEGORY);
		List <D2DParameter> occurencesExpected = new ArrayList<D2DParameter>();
		occurencesExpected.add(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.IMAGE_REPORT));
		occurencesExpected.add(new D2DParameter(D2DParameterName.CATEGORY, DocumentCategory.LABORATORY_REPORT));
		
		for(int i=0; i<occurencesActual.size(); i++) {
			assertEquals(occurencesExpected.get(i).toString(), occurencesActual.get(i).toString());
		}
		
		
	}
	
//	@Test
//	public void testGettingBTadapterAddress() throws Exception {
//		
//		BluetoothConnection btc = new BluetoothConnection();
//		String realBtadapterAdress="74:70:FD:24:E5:13";//74:70:FD:24:E5:13 for PC home//00:1A:7D:DA:71:13 for PC in lab
//		String btAdapterAdress=btc.getBTadapterAddress().split("#")[0];
//		System.out.println(btAdapterAdress+"ADASDASDAS");
//		assertNotNull(btAdapterAdress);
//		assertEquals(realBtadapterAdress,btAdapterAdress);
//	}
	
//	@Test
//	public void testSerialization() {
//		FHIRObjectsFactory fhir = new FHIRObjectsFactory();
//		Practitioner practitionerBefore =fhir.buildPractitioner();
//		String encoded = FhirContext.forR4().newJsonParser().encodeResourceToString(practitionerBefore);
//        IParser parser = FhirContext.forR4().newJsonParser();
//        Practitioner practitionerAfter = parser.parseResource(Practitioner.class, encoded);
//		
////        assertEquals(practitionerBefore,practitionerAfter);
//        assertTrue(EqualsBuilder.reflectionEquals(practitionerBefore,practitionerAfter));
////        assertEquals(ReflectionToStringBuilder.toString(practitionerAfter), ReflectionToStringBuilder.toString(practitionerBefore));
////        assertReflectionEquals(practitionerAfter,practitionerAfter);
//	}

//	@Test
//	public void testDeserialize() throws URISyntaxException, IOException {
//        File file = new File(testingD2D.class.getClassLoader().getResource("correctPatientSummary.json").toURI());;
//        String  initialJsonFhir = Files.readString(file.toPath());
//        FhirContext fc = FhirContext.forR4();
//        IParser parser = fc.newJsonParser().setPrettyPrint(true);
//
//        Bundle patientSummaryBundle = (Bundle) parser.parseResource(initialJsonFhir);
//        String jsonFhir = parser.encodeResourceToString(patientSummaryBundle);
//
//        assertEquals(initialJsonFhir, jsonFhir);
//
//	}
	
//	@Test
//	public void errorOnPurpose() {
//		assertEquals(false,true);
//	}

}
