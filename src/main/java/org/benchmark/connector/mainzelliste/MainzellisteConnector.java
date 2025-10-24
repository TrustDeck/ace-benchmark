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

import de.pseudonymisierung.mainzelliste.client.*;
import de.pseudonymisierung.mainzelliste.client.MainzellisteConnection.RequestMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Getter
@Slf4j
public class MainzellisteConnector {


    private final MainzellisteConnection connection;

    private final Session session;

    private final Validator validator;


    // Constructor
    public MainzellisteConnector(MainzellisteConnection connection, Session session, Validator validator) {
        this.connection = connection;
        this.session = session;
        this.validator = validator;
    }


    public JSONObject addPatient(String idType, Map<String,String> body) throws InvalidSessionException, MainzellisteNetworkException, JSONException {

        // Prepare AddPatientToken Object
        AddPatientToken addPatToken = new AddPatientToken();

        // add new Patient data to the token
        List<String> additionFields = Arrays.asList("vorname", "nachname", "geburtstag", "geburtsmonat", "geburtsjahr");
        for(String field:additionFields){
            addPatToken.addField(field,body.get(field));
        }

        // convert token to JSON object.
        JSONObject addPatTokenJSON = addPatToken.toJSON();


        //Create and fetch the token from the server.
        String addPatTokenId = session.getToken(addPatToken);
        log.debug("AddPatientToken ID received: {}", addPatTokenId);

        // Using Client library, send API request to create Patient using token id and token Json object.
        MainzellisteResponse addPatientResponse = connection.doRequest(RequestMethod.POST, "patients?tokenId=" + addPatTokenId, String.valueOf(addPatTokenJSON));
        log.debug("Created Patient details -{} ", addPatientResponse.getData());

        JSONArray responseArray = new JSONArray(addPatientResponse.getData());
        JSONObject responseObject = responseArray.getJSONObject(0);

        log.debug("Created Patient with IdString-{} ", responseObject);


        return responseObject;

    }

    public String readPatient(ID patientId) throws InvalidSessionException, MainzellisteNetworkException {
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
    }

    public String editPatient(ID id, JSONObject updateBody) throws Exception {

        // Create edit token
        EditPatientToken editPatientToken = new EditPatientToken(id);
        log.debug("id debug-{}", id.getIdString());
        List<String> fieldsToEdit = List.of("ort");
        editPatientToken.setFieldsToEdit(fieldsToEdit);

        // Get token
        String editPatientTokenId = session.getToken(editPatientToken);
        log.debug("Edit patient token ID: {}", editPatientTokenId);

        // Make request
        MainzellisteResponse editPatientResponse = connection.doRequest(RequestMethod.PUT, "patients/tokenId/" + editPatientTokenId, updateBody.toString());

        log.debug("Edit Patient response: {}", editPatientResponse.getStatusCode());
        return String.valueOf(editPatientResponse.getStatusCode());


    }

    public String deletePatient(ID id) throws MainzellisteNetworkException, InvalidSessionException {



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
    }
}