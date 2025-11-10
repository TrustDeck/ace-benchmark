/*
 * ACE-Benchmark Driver
 * Copyright 2024-2025 Armin Müller and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.benchmark.connector.mainzelliste;


import org.apache.commons.lang3.RandomStringUtils;
import org.benchmark.connector.BenchmarkException;
import org.benchmark.connector.MConnector;
import de.pseudonymisierung.mainzelliste.client.*;
import de.pseudonymisierung.mainzelliste.client.MainzellisteConnection.RequestMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Slf4j
public class MainzellisteConnector implements MConnector {


    private final MainzellisteConnection connection;

    private final Session session;

    private final Validator validator;


    // Constructor
    public MainzellisteConnector(MainzellisteConnection connection, Session session, Validator validator) {
        this.connection = connection;
        this.session = session;
        this.validator = validator;
    }

    public void prepare() throws BenchmarkException {

        try {
//            this.clearTables();
            Thread.sleep(15000);
            log.info("Tables cleared successfully");
        } catch (TrustDeckClientLibraryException | InterruptedException e) {
            // Ignore
        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }
    public JSONObject addPatient(String idType, String idString)  {
        // Prepare AddPatientToken Object
        try {
            AddPatientToken addPatToken = new AddPatientToken();
            addPatToken.addExternalId(idType, idString);
            // Add sureness parameter to force creation even with possible matches
            Map<String, String> newPatientFields = new HashMap<>();
            newPatientFields.put("vorname", RandomStringUtils.randomAlphabetic(5));
            newPatientFields.put("nachname",RandomStringUtils.randomAlphabetic(5));
            newPatientFields.put("geburtstag", "02");
            newPatientFields.put("geburtsmonat", "03");
            newPatientFields.put("geburtsjahr", "1990");

            for(Map.Entry<String,String> entry : newPatientFields.entrySet()){
                addPatToken.addField(entry.getKey(), entry.getValue());
            }
            // Convert token to JSON object
            JSONObject addPatTokenJSON = addPatToken.toJSON();

            addPatTokenJSON.put("sureness",true);

            // Create and fetch the token from the server
            String addPatTokenId = session.getToken(addPatToken);
            log.info("AddPatientToken ID received: {}", addPatTokenId);

            // Send API request to create Patient

            MainzellisteResponse addPatientResponse = connection.doRequest(
                    RequestMethod.POST,
                    "patients?tokenId=" + addPatTokenId,
                    String.valueOf(addPatTokenJSON)
            );
            log.info("add patient response :{}", addPatientResponse.getData());

            // Get the first object from the array response
            JSONArray responseArray = new JSONArray(addPatientResponse.getData());
            return responseArray.getJSONObject(0);
        }catch(Exception e){
            return null;
        }
    }
    public String readPatient(ID patientId) throws InvalidSessionException, MainzellisteNetworkException {
       try{ ReadPatientsToken readPatientsToken = new ReadPatientsToken();
        readPatientsToken.addSearchId(patientId);

        // Add the fields you want to retrieve
        readPatientsToken.setResultFields(Arrays.asList("vorname", "nachname", "geburtstag", "geburtsmonat", "geburtsjahr"));

        // Get the token
        String readPatientstokenId = session.getToken(readPatientsToken);
        log.debug("ReadPatientsToken ID received: {}", readPatientstokenId);

        // Make the GET request
        MainzellisteResponse readPatientResponse = connection.doRequest(RequestMethod.GET, "patients?tokenId=" + readPatientstokenId, null);

        log.info("Read patient response: {}", readPatientResponse.getData());
        return readPatientResponse.getData();
    }catch(Exception e){
        return null;
    }
}


    public String editPatient(ID id) throws InvalidSessionException, MainzellisteNetworkException, JSONException {
try{
        // Create edit token
        EditPatientToken editPatientToken = new EditPatientToken(id);
        log.debug("id debug-{}", id.getIdString());
        List<String> fieldsToEdit = List.of("ort");
        editPatientToken.setFieldsToEdit(fieldsToEdit);
        //  Create Patient data to call editPatient method
        JSONObject updateBody = new JSONObject();
        updateBody.put("ort", "newOrt");

        // Get token
        String editPatientTokenId = session.getToken(editPatientToken);
        log.debug("Edit patient token ID: {}", editPatientTokenId);

        // Make request
        MainzellisteResponse editPatientResponse = connection.doRequest(RequestMethod.PUT, "patients/tokenId/" + editPatientTokenId, updateBody.toString());

        log.debug("Edit Patient response: {}", editPatientResponse.getStatusCode());
        return String.valueOf(editPatientResponse.getStatusCode());
    }catch(Exception e){
        return null;
        }
        }



    public String deletePatient(ID id) throws InvalidSessionException, MainzellisteNetworkException {

try{
        // create deletePatient token
        DeletePatientToken deletePatienttoken = new DeletePatientToken(id);

        // create and fetch the created token
        String deletePatientsTokenId = session.getToken(deletePatienttoken);
        log.debug("deletePatientsToken ID received: {}", deletePatientsTokenId);

        // Delete patient using the token
        MainzellisteResponse deletePatientResponse =
                connection.doRequest(RequestMethod.DELETE, "patients/" + deletePatientsTokenId + "/" + id.getIdType() + "/" + id.getIdString(), deletePatientsTokenId);
        log.info("deletePatient Response :{}", deletePatientResponse.getStatusCode());

        //additional step-verify if patient is successfully deleted.
        String verificationReadPatient = this.readPatient(id);
        log.info("Verification read patient after deletion response: {}", verificationReadPatient);


        return String.valueOf(deletePatientResponse.getStatusCode());
    }catch(Exception e){
        return null;
    }
}

    public int ping() throws MainzellisteNetworkException {
        MainzellisteResponse response = connection.doRequest(RequestMethod.DELETE, "", null);
        return response.getStatusCode();
    }

}



//Session session = mainzelliste.getSession();}
//}