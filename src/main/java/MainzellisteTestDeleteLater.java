import de.pseudonymisierung.mainzelliste.client.AddPatientToken;

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

import de.pseudonymisierung.mainzelliste.client.*;
import lombok.extern.slf4j.Slf4j;
import org.benchmark.Main;
import org.benchmark.connector.mainzelliste.MainzellisteConnector;
import org.benchmark.connector.mainzelliste.MainzellisteConnectorFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class MainzellisteTestDeleteLater {

    public static void main(String[] args) throws InvalidSessionException, MainzellisteNetworkException, JSONException, URISyntaxException {
        System.out.println("Setup successful !");


        MainzellisteConnectorFactory factory = new MainzellisteConnectorFactory();
        MainzellisteConnector Mconnector = factory.create();
        Map<String,String> response = Mconnector.addPatient();
        log.info("response from add patient: {}", response.toString());



//
//        Optional<Map<String,String>> createdPatientResponse = Optional.of(MainzellisteConnector.addPatient());
//        log.info("addPatient Response :{}", createdPatientResponse);
////         fetch idType and IdString from response
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

        // 3. call deletePatient method
        String idType = response.get("idType");
        log.info("IDTYPE GIVEN:{}",idType);

        String idString = response.get("idString");
        log.info("idString GIVEN:{}",idString);
        String deleteResponse = Mconnector.deletePatient(new ID(idType,idString));

        log.info("Patient deleted successfully :{}",deleteResponse);


        log.info("Session destroyed {}",factory.destroySession());
//
    }
}
