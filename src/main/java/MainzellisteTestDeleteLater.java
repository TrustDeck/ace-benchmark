///*
// * ACE-Benchmark Driver
// * Copyright 2024-2025 Armin Müller and contributors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//
//import de.pseudonymisierung.mainzelliste.client.ID;
//import de.pseudonymisierung.mainzelliste.client.InvalidSessionException;
//import de.pseudonymisierung.mainzelliste.client.MainzellisteNetworkException;
//import lombok.extern.slf4j.Slf4j;
//import org.benchmark.connector.mainzelliste.MainzellisteConnector;
//import org.benchmark.connector.mainzelliste.MainzellisteConnectorFactory;
//import org.codehaus.jettison.json.JSONException;
//import org.codehaus.jettison.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//@Slf4j
//public class MainzellisteTestDeleteLater {
//
//    public static void main(String[] args) throws InvalidSessionException, MainzellisteNetworkException, JSONException {
//        System.out.println("Setup successful !");
//
//        MainzellisteConnectorFactory MFactory = new MainzellisteConnectorFactory();
//        MainzellisteConnector MConnector = MFactory.create();
//
//        if (MConnector != null) {
//            System.out.println("Mainz Connector successfully initialised");
//        } else {
//            System.out.println("Error-Mainz Connector initialisation failed");
//        }
//
//        // 1. call addPatient method
//        Map<String,String> newPatientFields = new HashMap<>();
//        newPatientFields.put("vorname", "Mark");
//        newPatientFields.put("nachname", "Spencer");
//        newPatientFields.put("geburtstag", "02");
//        newPatientFields.put("geburtsmonat", "03");
//        newPatientFields.put("geburtsjahr", "1990");
////        AddPatientToken addPatToken = new AddPatientToken();
////        addPatToken.addExternalId("extId", "yourExternalId");
////        addPatToken.addExternalId("pid", "yourPID");
//
//        JSONObject createdPatientResponse = MConnector.addPatient("extid","TEST");
//        log.info("addPatient Response :{}", createdPatientResponse);
//        // fetch idType and IdString from response
//        String createdPatientIdType = createdPatientResponse.get("idType").toString();
//        String createdPatientIdString = createdPatientResponse.get("idString").toString();
//
//
//        // 2. call readPatient method
//        String readPatResponse = MConnector.readPatient(new ID("pid", createdPatientIdString));
//        log.info("readPatient Response :{}", readPatResponse);
//
//
//        //  Create Patient data to call editPatient method
//        JSONObject editDataJson = new JSONObject();
//        editDataJson.put("ort", "newOrt");
//
//        // 3. call editPatient method
//        String editPatResponseStatusCode = MConnector.editPatient(new ID(createdPatientIdType,createdPatientIdString));
//        log.info("editPatient Response :{}", editPatResponseStatusCode);
//
//
//        // 3. call deletePatient method
//        String deletePatResponse = MConnector.deletePatient(new ID(createdPatientIdType,createdPatientIdString));
//        log.debug("Patient deleted successfully :{}",deletePatResponse);
//
//
//    }
//}
