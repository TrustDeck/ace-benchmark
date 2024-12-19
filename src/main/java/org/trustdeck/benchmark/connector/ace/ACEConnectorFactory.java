/*
 * ACE-Benchmark Driver
 * Copyright 2024 Armin Müller and contributors.
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
package org.trustdeck.benchmark.connector.ace;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.trustdeck.benchmark.Main;
import org.trustdeck.benchmark.connector.ConnectorException;
import org.trustdeck.benchmark.connector.ConnectorFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * ACE connector factory
 */
public class ACEConnectorFactory implements ConnectorFactory {

    /**
     * Creates a new connector
     * @return
     * @throws URISyntaxException 
     * @throws ConnectorException 
     */
    public ACEConnector create() throws ConnectorException {

        // Extract the tool configuration from the loaded configuration file
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> yamlConfig = yaml.load(inputStream);
        @SuppressWarnings("unchecked")
        Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("ace");
        
        String authClientId = toolConfig.get("clientId");
        String authClientSecret = toolConfig.get("clientSecret");
        String authKeycloakURI = toolConfig.get("keycloakAuthUri");
        String authKeycloakRealmName = toolConfig.get("keycloakRealmName");
        String authUsername = toolConfig.get("username");
        String authPassword = toolConfig.get("password");
 
        String serviceURI = toolConfig.get("uri");
        String serviceDomainName = toolConfig.get("domainName");
        
        // Create connector
        ACEConnector connector;
        try {
            connector = new ACEConnector(authClientId,
                                                      authClientSecret,
                                                      authKeycloakURI,
                                                      authKeycloakRealmName,
                                                      authUsername,
                                                      authPassword,
                                                      serviceURI,
                                                      serviceDomainName);
        } catch (URISyntaxException e) {
            throw new ConnectorException(e);
        }

        // Prepare and return
        connector.prepare();
        return connector;
    }

    @Override
    public void shutdown() {
        HTTPClientManager.shutdown();
    }
}
