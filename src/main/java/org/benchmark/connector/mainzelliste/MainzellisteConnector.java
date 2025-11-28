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
import de.pseudonymisierung.mainzelliste.client.*;
import de.pseudonymisierung.mainzelliste.client.MainzellisteConnection.RequestMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.benchmark.connector.BenchmarkException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@Slf4j
public class MainzellisteConnector implements MConnector {


    private final MainzellisteConnection connection;

    private final Session session;


    // Constructor
    public MainzellisteConnector(MainzellisteConnection connection, Session session
    ) {
        this.connection = connection;
        this.session = session;

    }

// NOTE: Clear tables not implemented, Truncate tables as a workaround, Implementation to be considered for Future Works.
    public void prepare() throws BenchmarkException {
//        try {
////            this.connection.clearTables(this.session);
////            Thread.sleep(5000);
////            log.debug("Tables cleared successfully");
//        } catch (TrustDeckClientLibraryException | InterruptedException e) {
//            // Ignore
//        } catch (Exception e) {
//            throw new BenchmarkException(e);
//        }
    }


    public Map<String, String> addPatient() throws InvalidSessionException, MainzellisteNetworkException, JSONException {

        // Prepare AddPatientToken Object
        AddPatientToken addPatToken = new AddPatientToken();

        Random random = new Random();
        addPatToken.addField("vorname", RandomStringUtils.randomAlphabetic(5));
        addPatToken.addField("nachname", RandomStringUtils.randomAlphabetic(5));
        addPatToken.addField("geburtstag", String.format("%02d", 1 + random.nextInt(30) + 1));
        addPatToken.addField("geburtsmonat", String.format("%02d", random.nextInt(12 ) + 1));
        addPatToken.addField("geburtsjahr", "1989");

        // convert token to JSON object.
        JSONObject addPatTokenJSON = addPatToken.toJSON();


        //Create and fetch the token from the server.
        String addPatTokenId = this.session.getToken(addPatToken);
        log.debug("AddPatientToken ID received: {}", addPatTokenId);

        // Using Client library, send API request to create Patient using token id and token Json object.
        MainzellisteResponse addPatientResponse = connection.doRequest(RequestMethod.POST, "patients?tokenId=" + addPatTokenId, String.valueOf(addPatTokenJSON));
        log.debug("Created Patient details -{} ", addPatientResponse.getStatusCode());
        try{
        JSONArray responseJsonArray = new JSONArray(addPatientResponse.getData());
        JSONObject responseJsonObject = responseJsonArray.getJSONObject(0);

        Map<String, String> createdPatientIds = new ConcurrentHashMap<>();
        createdPatientIds.put("idType", responseJsonObject.getString("idType"));
        createdPatientIds.put("idString", responseJsonObject.getString("idString"));
        log.debug("Created Map of IDS {}", createdPatientIds);
        return createdPatientIds;}
        catch(Exception e){
            log.warn(e.getMessage());
            return null;
        }
    }

    public String readPatient(ID patientId) throws InvalidSessionException, MainzellisteNetworkException {
        try {
            ReadPatientsToken readPatientsToken = new ReadPatientsToken();
            readPatientsToken.addSearchId(patientId);

            // Add the fields you want to retrieve
            readPatientsToken.setResultFields(Arrays.asList("vorname", "nachname", "geburtstag", "geburtsmonat", "geburtsjahr"));

            // Get the token
            String readPatientstokenId = session.getToken(readPatientsToken);
            log.debug("ReadPatientsToken ID received: {}", readPatientstokenId);

            // Make the GET request
            MainzellisteResponse readPatientResponse = connection.doRequest(RequestMethod.GET, "patients?tokenId=" + readPatientstokenId, null);

            log.debug("Read patient response: {}", readPatientResponse.getData());
            return readPatientResponse.getData();
        } catch (Exception e) {
            log.warn("READ FAILED {}", e.getMessage());
            return null;
        }
    }


    public String editPatient(ID id) throws InvalidSessionException, MainzellisteNetworkException, JSONException {
        try {
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

            log.info("Edit Patient response: {}", editPatientResponse.getStatusCode());
            return String.valueOf(editPatientResponse.getStatusCode());
        } catch (Exception e) {
            log.warn("Edit failed due to " + e.getMessage());
            return null;
        }
    }

    public String deletePatient(ID id) throws InvalidSessionException, MainzellisteNetworkException {
        try {
            // Create deletePatient token
            DeletePatientToken deletePatientToken = new DeletePatientToken(id);
            log.info("deletePatientsToken JSON: {}", deletePatientToken.toJSON());

            // Fetch the created token
            String deletePatientsTokenId = session.getToken(deletePatientToken);
            log.info("deletePatientsToken ID received: {}", deletePatientsTokenId);

            // Delete patient using the token
            String url = "patients?tokenId=" + deletePatientsTokenId;
            MainzellisteResponse deletePatientResponse =
                    connection.doRequest(RequestMethod.DELETE, url, null);

            log.info("deletePatient Response: {}", deletePatientResponse.getStatusCode());
            return String.valueOf(deletePatientResponse.getStatusCode());
        } catch (Exception e) {
            log.warn("Deletion failed due to", e);
            return null;
        }
    }
    public int ping() throws MainzellisteNetworkException {
        MainzellisteResponse response = connection.doRequest(RequestMethod.DELETE, "", null);
        return response.getStatusCode();
    }

}
