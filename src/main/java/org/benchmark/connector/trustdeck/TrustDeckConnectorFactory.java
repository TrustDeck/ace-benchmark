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
package org.benchmark.connector.trustdeck;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import lombok.extern.slf4j.Slf4j;

import org.benchmark.connector.mainzelliste.Main;
import org.benchmark.connector.BenchmarkException;

import org.trustdeck.client.TrustDeckClient;
import org.trustdeck.client.config.TrustDeckClientConfig;

/**
 * ACE mainzelliste factory.
 *
 * @author Fabian Prasser, Armin Müller, Chethan N. Nagaraj
 */
@Slf4j
public class TrustDeckConnectorFactory implements TConnectorFactory {


    /**
     * Creates a new mainzelliste.
     *
     * @return the initialized mainzelliste
     * @throws BenchmarkException
     */

    public TrustDeckConnector create() throws BenchmarkException {


        try {
            // 1. Extract the tool configuration from the loaded configuration file
            Yaml yaml = new Yaml();
            InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
            Map<String, Object> yamlConfig = yaml.load(inputStream);
            @SuppressWarnings("unchecked") Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("trustdeck");

   
            // 2. Create TrustDeck Config object
            TrustDeckClientConfig trustDeckClientConfig = TrustDeckClientConfig.builder()
                    .serviceUrl(toolConfig.get("uri"))
                    .keycloakUrl(toolConfig.get("keycloakAuthUri"))
                    .realm(toolConfig.get("keycloakRealmName"))
                    .clientId(toolConfig.get("clientId"))
                    .clientSecret(toolConfig.get("clientSecret"))
                    .userName(toolConfig.get("username"))
                    .password(toolConfig.get("password"))
                    .build();
            log.debug(" Creation of configuration object successful");

            // 3. Create TrustDeck client instance which internally initialises TrustDeck token service, domain service, pseudonym service, and util service
            TrustDeckClient trustDeckClient = new TrustDeckClient(trustDeckClientConfig);
            log.debug(" Creation of TrustDeck client instance successful");

            // 4. Create TrustDeck Domain object to use for the benchmarking in ACE.
            String serviceDomainName = toolConfig.get("domainName");

            // 5. Create TrustDeck Connector instance
            return new TrustDeckConnector(trustDeckClient, serviceDomainName);


        } catch (Exception e) {
            throw new BenchmarkException("Cannot create the TrustDeck Connector instance", e);
        }


    }


//Not neccessary as RestTemplate does not require explicit shutdown


    /*
     * Not necessary as RestTemplate does not require explicit shutdown.
     * @Override
     * public void shutdown() {
     * HTTPClientManager.shutdown();
    }
     */
}
