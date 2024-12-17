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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.trustdeck.benchmark.Main;
import org.trustdeck.benchmark.connector.Connector;
import org.trustdeck.benchmark.connector.ConnectorException;
import org.yaml.snakeyaml.Yaml;

/**
 * Connector to ACE.
 * 
 * @author Fabian Prasser, Armin Müller
 */
public class ACEConnector implements Connector{

    /** Default domain prefix. */
    private static final String DEFAULT_DOMAIN_PREFIX = "TST";
    
    /** Default idType. */
    private static final String DEFAULT_ID_TYPE = "ID";
    
    /** Default start time for the domain's validity period. */
    private static final String DEFAULT_DOMAIN_VALID_FROM = "2000-01-01T18:00:00";
    
    /** Default start time for the pseudonym's validity period. */
    private static final String DEFAULT_PSEUDONYM_VALID_FROM = "2001-01-01T18:00:00";
    
    /** Token lifetime. */
    private static final long DEFAULT_TOKEN_LIFETIME = 290000;

    /** ACE service. */
    private ACEService service;
    
    /** Authentication. */
    private KeycloakAuthentication authentication;
    
    /** Access token. */
    private ACEToken token;
    
    /** To track token validity. */
    private long lastAuthenticated;
    
    /** Domain to use for the benchmarking in ACE. */
    private ACEDomain domain;
    
    /**
     * Create a new instance of the connector.
     * 
     * @throws URISyntaxException
     */
    @SuppressWarnings("unchecked")
    public ACEConnector() throws URISyntaxException {
        
        // Extract the tool configuration from the loaded configuration file
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> yamlConfig = yaml.load(inputStream);
        Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("ace");
 
        // Authentication
        this.authentication = new KeycloakAuthentication()
                .setClientId(toolConfig.get("clientId"))
                .setClientSecret(toolConfig.get("clientSecret"))
                .setKeycloakAuthenticationURI(toolConfig.get("keycloakAuthUri"))
                .setKeycloakRealmName(toolConfig.get("keycloakRealmName"))
                .setUsername(toolConfig.get("username"))
                .setPassword(toolConfig.get("password"));
        
        // Instantiate service
        this.service = new ACEService(new URI(toolConfig.get("uri")));
        
        // Prepare domain
        this.domain = new ACEDomain((String) toolConfig.get("domainName"), DEFAULT_DOMAIN_PREFIX);
        this.domain.setValidFrom(DEFAULT_DOMAIN_VALID_FROM);
    }
    
    /**
     * Authentication mechanism. Retrieves a new token or refreshes an existing one. 
     */
    private void authenticate() {

        // Retrieve an access token
        if (this.token == null) {
            this.token = new ACEToken(authentication.authenticate());
            this.lastAuthenticated = System.currentTimeMillis();
            
        // Refresh token
        } else if (System.currentTimeMillis() - lastAuthenticated > DEFAULT_TOKEN_LIFETIME) {
            this.token = new ACEToken(authentication.refreshToken());
            this.lastAuthenticated = System.currentTimeMillis();
        }
    }
    
    /**
     * Prepare for benchmark.
     * Authenticate and remove old data.
     */
    public void prepare() throws ConnectorException {
        try {
            // Authenticate
            authenticate();
    
            // Remove old data from ACE
            try {
                service.clearTables(this.token);
            } catch (HTTPException e) {
                // Ignore
            }
    
            // Refresh access token (since the old-data-removal can take a while) and create the domain
            authenticate();
            service.createDomain(this.token, this.domain);
            
        // Catch and forward errors
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }
    
    /**
     * Create pseudonym.
     * 
     * @param id the identifier used for creating the pseudonym.
     */
    public void createPseudonym(String id) throws ConnectorException {
        try {
            authenticate();
            service.createPseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE));
            
        // Catch and forward errors
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }
    
    /**
     * Retrieve storage metrics.
     * 
     * @param storageIdentifier the name of the database that should be queried.
     * @return the raw http response containing the storage metrics
     */
    public String getStorageConsumption(String storageIdentifier) throws ConnectorException {
        try {
            // Authenticate
            authenticate();

            // Gather storage information
            String response = "";
            try {
                response = service.getStorage(token, storageIdentifier);
            } catch (HTTPException e) {
                // Ignore
            }
            
            return response;
            
        // Catch and forward errors
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }

    /**
     * Read pseudonym.
     * 
     * @param id the identifier used for reading the pseudonym.
     */
    @Override
    public void readPseudonym(String id) throws ConnectorException {
		try {
			// Authenticate
		    authenticate();
		    service.readPseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE));
		    
		// Catch and forward errors
		} catch (Exception e) {
		    // It is ok if the pseudonym does not exist
		    if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
		        throw new ConnectorException(e);
		    } 
		}
    }

    /**
     * Update pseudonym.
     * 
     * @param id the identifier used for updating the pseudonym.
     */
    @Override
    public void updatePseudonym(String id) throws ConnectorException {
        try {
        	// Authenticate
            authenticate();
            service.updatePseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE).withValidFrom(DEFAULT_PSEUDONYM_VALID_FROM));
            
        // Catch and forward errors
        } catch (Exception e) {
            // It is ok if the pseudonym does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            } 
        }
    }

    /**
     * Delete pseudonym.
     * 
     * @param id the identifier used for deleting the pseudonym.
     */
    @Override
    public void deletePseudonym(String id) throws ConnectorException {
        try {
        	// Authenticate
            authenticate();
            service.deletePseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE));
            
        // Catch and forward errors
        } catch (Exception e) {
            // It is ok if the pseudonym does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            } 
        }
    }

    /**
     * Ping ACE.
     */
    @Override
    public void ping() throws ConnectorException {
        try {
        	// Authenticate
        	authenticate();
        	
            service.ping(this.token);
        // Catch and forward errors
        } catch (Exception e) {
            // It is ok if the endpoint does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            }
        }
    }
}
