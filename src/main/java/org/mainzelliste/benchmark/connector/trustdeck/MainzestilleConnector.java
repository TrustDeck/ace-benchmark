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
package org.mainzelliste.benchmark.connector.trustdeck;

import java.net.URISyntaxException;
import java.util.Arrays;

import java.util.List;

import de.pseudonymisierung.mainzelliste.client.*;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jettison.json.JSONObject;


import de.pseudonymisierung.mainzelliste.client.MainzellisteConnection.RequestMethod;


@Slf4j
public class MainzestilleConnector {
    public static void main(String[] args) throws URISyntaxException {
        try {

            /// 1. Create connection instance and session
            MainzellisteConnection connection = new MainzellisteConnection("http://localhost:8080", "1234");
            Session session = connection.createSession();
            log.info("Session created with ID: {}", session.getId());

            Validator validator = connection.createValidator();
            log.debug(" Validator created with ID:{}",validator);

            /// 2. Add new Patient

            // Create AddPatientToken
            AddPatientToken addPatienttoken = new AddPatientToken();
            addPatienttoken.addIdType("pid");

            String addPatienttokenId = session.getToken(addPatienttoken);
            log.info("AddPatientToken ID received: {}", addPatienttokenId);

            // Create URL for creating new patient
            String addPatienturl = "http://localhost:8080/patients?tokenId=" + addPatienttokenId;


            // Create patient data JSON object.
            JSONObject requestBody = new JSONObject();
            JSONObject fields = new JSONObject();
            fields.put("vorname", "John");
            fields.put("nachname", "Doe");
            fields.put("geburtstag", "01");
            fields.put("geburtsmonat", "01");
            fields.put("geburtsjahr", "1980");
            requestBody.put("fields", fields);
            requestBody.put("sureness", false);



          /* Using Client library, send API request to create Patient
             using AddPatientToken and Patient data */
            MainzellisteResponse addPatientResponse = connection.doRequest(RequestMethod.POST, addPatienturl, String.valueOf(requestBody));
            log.info("Created Patient details -{} ", addPatientResponse.getData());


            /// 3. Read  Patient
          ReadPatientsToken readPatientsToken = new ReadPatientsToken();
          ID id = new ID("pid", "0003Y0WZ");
          readPatientsToken.addSearchId(id);

          // Add the fields you want to retrieve
          readPatientsToken.setResultFields(Arrays.asList("vorname", "nachname", "geburtsname",
                  "geburtstag", "geburtsmonat", "geburtsjahr", "plz", "ort"));
          readPatientsToken.setResultIds(Arrays.asList("pid"));

          // Get the token
          String readPatientstokenId = session.getToken(readPatientsToken);
          log.info("ReadPatientsToken ID received: {}", readPatientstokenId);

          // Make the GET request
          MainzellisteResponse readPatientResponse = connection.doRequest(
                  RequestMethod.GET,
                  "patients?tokenId=" + readPatientstokenId,
                  null
          );

          log.info("Read patient response: {}", readPatientResponse.getData());

            /// 4. Edit patient


            // Create edit token
            EditPatientToken editPatientToken = new EditPatientToken(id);
            log.info("id debug-{}",id.getIdString());
            List<String> fieldsToEdit = Arrays.asList("ort");
            editPatientToken.setFieldsToEdit(fieldsToEdit);

            // Get token
            String editPatientTokenId = session.getToken(editPatientToken);
            log.info("Edit patient token ID: {}", editPatientTokenId);

            // Create edit data
            JSONObject editDataJson = new JSONObject();
            editDataJson.put("ort", "newOrt");

            // Construct URL carefully
            String editPatientUrl = String.format("patients/tokenId/%s",
                    editPatientTokenId);
            // Validate token
            log.info("ID {}",editPatientUrl);
            // Make request
            MainzellisteResponse editResponse = connection.doRequest(
                    RequestMethod.PUT,
                    editPatientUrl,
                    editDataJson.toString()
            );

            log.info("Edit Patient response: {} {}", editResponse.getStatusCode(),editResponse.getData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//
//            // Create the HTTP client and request
//            CloseableHttpClient client = HttpClients.createDefault();
//            HttpPost request = new HttpPost(url);
//
//
//            // Add all required headers exactly as in curl
//            request.setHeader("Content-Type", "application/json");

//            // Log the exact request details for debugging
//            log.info("Request URL: {}", request.getURI());
//            log.info("Request headers:");
//            for (org.apache.http.Header header : request.getAllHeaders()) {
//                log.info("{}: {}", header.getName(), header.getValue());
//            }
//            log.info("Request body: {}", requestBody.toString(2));
//
//            // Set the request body
//            StringEntity entity = new StringEntity(requestBody.toString());
//            request.setEntity(entity);


//            // Execute the request
//            try (CloseableHttpResponse response = client.execute(request)) {
//                String responseBody = EntityUtils.toString(response.getEntity());
//                log.info("Response status: {}", response.getStatusLine().getStatusCode());
//                log.info("Response body: {}", responseBody);
//            }

