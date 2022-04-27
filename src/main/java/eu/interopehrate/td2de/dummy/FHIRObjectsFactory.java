package eu.interopehrate.td2de.dummy;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.hl7.fhir.r4.model.*;

public class FHIRObjectsFactory {

    public Patient buildPatient(/*Practitioner gp*/) {
        Patient p = new Patient();
        p.setId(UUID.randomUUID().toString());

        GregorianCalendar bd = new GregorianCalendar(1963, Calendar.MARCH, 24);
        p.setBirthDate(bd.getTime());

        HumanName name = new HumanName();
        name.setFamily("Rossi").addGiven("Maria");
        p.addName(name);

        p.setGender(Enumerations.AdministrativeGender.FEMALE);

        p.addAddress().addLine("Piazza di Spagna")
                .setCity("Roma")
                .setState("Italia")
                .setPostalCode("87654")
                .setUse(Address.AddressUse.HOME);

        return p;
    }


    public Practitioner buildPractitioner() {
        Practitioner p = new Practitioner();
        p.setId(UUID.randomUUID().toString());

        GregorianCalendar bd = new GregorianCalendar(1970, Calendar.SEPTEMBER, 9);
        p.setBirthDate(bd.getTime());

        HumanName name = new HumanName();
        name.setFamily("Bianchi").addGiven("Antonio");
        p.addName(name);

        p.setGender(Enumerations.AdministrativeGender.MALE);

        p.addAddress().addLine("Piazza Navona")
                .setCity("Roma")
                .setState("Italia")
                .setPostalCode("13456")
                .setUse(Address.AddressUse.WORK);

        return p;
    }
    
    public Organization buildOrganization() {
        Organization org = new Organization();
        org.setId(UUID.randomUUID().toString());

        org.setActive(true);

        org.setName("Azienda ospedaliera San Camillo-Forlanini");

        org.addAddress().addLine("Circonvallazione Gianicolense, 87")
                .setCity("Roma")
                .setState("Italia")
                .setPostalCode("00152")
                .setUse(Address.AddressUse.WORK);

        org.addTelecom()
                .setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue("+39-6-58701")
                .setUse(ContactPoint.ContactPointUse.WORK);

        return org;
    }
    
    public org.hl7.fhir.r4.model.Bundle buildBundle(){
        org.hl7.fhir.r4.model.Bundle bundle = new org.hl7.fhir.r4.model.Bundle();
        return bundle;
    }
}