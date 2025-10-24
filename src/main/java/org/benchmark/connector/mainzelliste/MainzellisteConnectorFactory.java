package org.benchmark.connector.mainzelliste;

import de.pseudonymisierung.mainzelliste.client.*;
import org.codehaus.jettison.json.JSONObject;
import org.benchmark.Main;
import org.benchmark.connector.BenchmarkException;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MainzellisteConnectorFactory {

    public static MainzellisteConnector create() throws BenchmarkException {
        /// 1. Create connection instance and session

        try {

            Yaml yaml = new Yaml();
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
            Map<String, Object> yamlConfig = yaml.load(inputStream);
            @SuppressWarnings("unchecked") Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("mainzelliste");
            log.info("Config created");


            MainzellisteConnection connection = new MainzellisteConnection(toolConfig.get("uri"), toolConfig.get("apiKey"));
            log.info("connection created");

            Session session = connection.createSession();
            log.info("Session created with ID: {}", session.getURI());

            Validator validator = connection.createValidator();
            log.info("validator created {}", validator);


            return new MainzellisteConnector(connection, session,validator);
        } catch (Exception e) {
            log.info("URI syntax exception occurred, please check the URI ;{}", e.getMessage());
        }
        return null;
    }





    public static void main(String[] args) throws Exception {
        MainzellisteConnector connector = create();
        if (connector != null) {
//            Session session = mainzelliste.getSession();

            // 1. call addPatient method
            Map<String,String> newPatientFields = new HashMap<>();
            newPatientFields.put("vorname", "Mark");
            newPatientFields.put("nachname", "Spencer");
            newPatientFields.put("geburtstag", "02");
            newPatientFields.put("geburtsmonat", "03");
            newPatientFields.put("geburtsjahr", "1990");

            JSONObject createdPatientResponse = connector.addPatient("pid",newPatientFields);
            log.info("addPatient Response :{}", createdPatientResponse);
            // fetch idType and IdString from response
            String createdPatientIdType = createdPatientResponse.get("idType").toString();
            String createdPatientIdString = createdPatientResponse.get("idString").toString();


            // 2. call readPatient method
            String readPatResponse = connector.readPatient(new ID("pid", createdPatientIdString));
            log.info("readPatient Response :{}", readPatResponse);


            //  Create Patient data to call editPatient method
            JSONObject editDataJson = new JSONObject();
            editDataJson.put("ort", "newOrt");

            // 3. call editPatient method
            String editPatResponseStatusCode = connector.editPatient(new ID(createdPatientIdType,createdPatientIdString),editDataJson);
            log.info("editPatient Response :{}", editPatResponseStatusCode);


            // 3. call deletePatient method
            String deletePatResponse = connector.deletePatient(new ID(createdPatientIdType,createdPatientIdString));
            log.debug("Patient deleted successfully :{}",deletePatResponse);



        }

    }

}
