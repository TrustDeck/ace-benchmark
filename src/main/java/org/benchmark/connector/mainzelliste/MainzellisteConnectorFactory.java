package org.benchmark.connector.mainzelliste;

import de.pseudonymisierung.mainzelliste.client.*;
import org.benchmark.connector.BenchmarkException;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;


/**
 * Factory for creating {@link MainzellisteConnector} instances.
 * Loads configuration from a YAML file and initializes the connection and session.
 *
 *  @author Armin Müller, Felix N. Wirth, Chethan C. Nagaraj and Fabian Prasser
 */
@Slf4j
public class MainzellisteConnectorFactory implements MConnectorFactory {

    Session session;

    /**
     * Creates a new {@link MainzellisteConnector} using configuration from config.yaml.
     *
     * @return a new MainzellisteConnector instance, or null if creation fails
     */
    public  MainzellisteConnector create() throws BenchmarkException {
        /// 1. Create connection instance and session

        try {

            Yaml yaml = new Yaml();
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
            Map<String, Object> yamlConfig = yaml.load(inputStream);
            @SuppressWarnings("unchecked") Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("mainzelliste");
            log.debug("Config created");


            MainzellisteConnection connection = new MainzellisteConnection(toolConfig.get("uri"), toolConfig.get("apiKey"));
            log.info("connection created");

            this.session = connection.createSession();
            log.info("Session created with ID: {}", session.getURI());


            return new MainzellisteConnector(connection, session);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }
    // This can be used for the clear table implementation as it removes all the tokens related to a session.
//    public String destroySession() throws MainzellisteNetworkException {
//        this.session.destroy();
//        return "Session destroyed";
    }




