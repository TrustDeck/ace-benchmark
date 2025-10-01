package org.mainzelliste.benchmark.connector;

import de.pseudonymisierung.mainzelliste.client.MainzellisteConnection;
import de.pseudonymisierung.mainzelliste.client.MainzellisteNetworkException;
import de.pseudonymisierung.mainzelliste.client.Session;
import de.pseudonymisierung.mainzelliste.client.Validator;
import org.mainzelliste.benchmark.Connector;
import org.mainzelliste.benchmark.ConnectorFactory;
import org.trustdeck.benchmark.Main;
import org.trustdeck.benchmark.connector.BenchmarkException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
public class MainzellisteConnectorFactory implements ConnectorFactory {


    @Override
    public Connector create() throws BenchmarkException {
        /// 1. Create connection instance and session

        try {

            Yaml yaml = new Yaml();
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
            Map<String, Object> yamlConfig = yaml.load(inputStream);
            @SuppressWarnings("unchecked") Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("trustdeck");


            MainzellisteConnection connection = new MainzellisteConnection("http://localhost:8080", "1234");

            Session session = connection.createSession();
            log.info("Session created with ID: {}", session.getId());

            Validator validator = connection.createValidator();
        }catch(URISyntaxException | MainzellisteNetworkException e){
            log.trace("URI syntax exception occurred, please check the URI ;{}",e.getMessage());
        }
return null;
    }

}
