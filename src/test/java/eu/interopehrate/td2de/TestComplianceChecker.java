package eu.interopehrate.td2de;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

import org.hl7.fhir.r4.model.Bundle;
import org.junit.Before;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

public class TestComplianceChecker {
	
	String ipsValidatorPack;
	
	@Before
	public void init() {
		ipsValidatorPack= "C:\\development\\InteropEHRate\\terminal-d2d-hr-exchange\\src\\main\\resources\\ipsValidatorPack";
	}

	@Test
	public void testWrongPatientSummary() throws DataFormatException, FileNotFoundException, URISyntaxException {
		FhirContext ctx = FhirContext.forR4();
		IPSChecker ipsChecker = new IPSChecker(ctx,ipsValidatorPack);
		Bundle wrongPatientSummary = new Bundle();
		boolean conformant=ipsChecker.validateProfile(wrongPatientSummary);
		System.out.println(conformant);
		assertTrue(!(conformant));
	}
	
	@Test
	public void testCorrectPatientSummary()throws DataFormatException, FileNotFoundException, URISyntaxException {
		FhirContext ctx = FhirContext.forR4();
		IPSChecker ipsChecker = new IPSChecker(ctx,ipsValidatorPack);
		URL is = TestComplianceChecker.class.getClassLoader().getResource("correctPatientSummary.json");
		File f = new File(is.toURI());
		Scanner sc = new Scanner(f);
    	String content = sc.useDelimiter("\\Z").next();
    	IParser parser = ctx.newJsonParser();
      	Bundle correctPatientSummary = parser.parseResource(Bundle.class, content);
		boolean conformant=ipsChecker.validateProfile(correctPatientSummary);
		System.out.println(conformant);
		assertTrue(conformant);
		sc.close();
		
	}
	
	
	@Test
	public void testIPSchecker() throws FileNotFoundException, URISyntaxException {
	    FhirContext ctx = FhirContext.forR4();
	    IPSChecker ipsChecker = new IPSChecker(ctx,ipsValidatorPack);
	}
	
}
