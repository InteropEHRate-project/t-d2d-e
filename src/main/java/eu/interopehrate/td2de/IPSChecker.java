package eu.interopehrate.td2de;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.validation.CachingValidationSupport;
import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.r4.hapi.validation.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.CodeSystem;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IContextValidationSupport;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.*;

class IPSChecker {
	
    private FhirContext ctx;
    private FhirValidator validator;
    
    public IPSChecker(FhirContext fContext, String structureDefinitionsPath) throws DataFormatException, FileNotFoundException, URISyntaxException {
    	this.ctx = fContext;
		
    	ValidationSupportChain supportChain = new ValidationSupportChain();
		//adding default profile validation support
		DefaultProfileValidationSupport defaultSupport = new DefaultProfileValidationSupport();
		supportChain.addValidationSupport(defaultSupport);
		
		//adding structures valuesets and codeSystems of own profiles
		PrePopulatedValidationSupport prePopulatedSupport = new PrePopulatedValidationSupport();
		IParser jsonParser = ctx.newJsonParser();
		jsonParser.setParserErrorHandler(new StrictErrorHandler());

		File f = new File(structureDefinitionsPath);
	
		
		File[] files = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.getName().endsWith(".json") ?  true :  false);
			}
		});
			
		for (File file : files) {
			if (file.getName().startsWith("StructureDefinition-")) {
				prePopulatedSupport.addStructureDefinition(jsonParser.parseResource(
						StructureDefinition.class, new FileReader(file.getPath())));
			} else if (file.getName().startsWith("ValueSet-")) {
				prePopulatedSupport.addValueSet(jsonParser.parseResource(
						ValueSet.class, new FileReader(file.getPath())));
			} else if (file.getName().startsWith("CodeSystem-")) {
				prePopulatedSupport.addCodeSystem(jsonParser.parseResource(
						CodeSystem.class, new FileReader(file.getPath())));
			}	
		} 

		// Adds validator pack profiles to validation
		supportChain.addValidationSupport(prePopulatedSupport);
		// Adds support for caching 
		CachingValidationSupport cache = new CachingValidationSupport(supportChain);
		
		// Creation of validators
		FhirInstanceValidator validatorModule = new FhirInstanceValidator(cache); 
		validator = ctx.newValidator();
		validator.registerValidatorModule(validatorModule);
		validatorModule.setAnyExtensionsAllowed(true);
		validatorModule.setNoTerminologyChecks(true);
    }
    
    
    public boolean validateProfile(IBaseResource resource) {
		ValidationResult result = validator.validateWithResult(resource);
		// Show the issues
		for (SingleValidationMessage next : result.getMessages()) {
		   System.out.println(" Next issue " + next.getSeverity() + " - " + next.getLocationString() + " - " + next.getMessage());
		}
	    return result.isSuccessful();    
    }
}
