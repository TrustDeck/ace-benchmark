/*
 * ACE-Benchmark Driver
 * Copyright 2024 Armin M�ller and contributors.
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

import java.net.URI;
import java.net.URISyntaxException;

import org.trustdeck.benchmark.connector.Connector;
import org.trustdeck.benchmark.connector.ConnectorException;

/**
 * Connector to ACE
 * @author Fabian Prasser
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
    
    /** Token lifetime*/
    private static final long DEFAULT_TOKEN_LIFETIME = 290000;

    /** Service*/
    private ACEService service;
    
    /** Authentication*/
    private KeycloakAuthentication authentication;
    
    /** Token*/
    private ACEToken token;
    
    /** To track token validity*/
    private long lastAuthenticated;
    
    /** Domain to use by ACE*/
    private ACEDomain domain;
    
    /**
     * Create a new instance of the service
     * @param serviceURI
     * @param domainName
     * @param clientId
     * @param clientSecret
     * @param keycloakAuthURI
     * @param keycloakRealmName
     * @param username
     * @param password
     * @param uRI
     * @throws URISyntaxException
     */
    public ACEConnector(String serviceURI,
                        String domainName,
                        String clientId,
                        String clientSecret,
                        String keycloakAuthURI,
                        String keycloakRealmName,
                        String username,
                        String password,
                        String uRI) throws URISyntaxException {

        // Authentication
        this.authentication = new KeycloakAuthentication()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setKeycloakAuthenticationURI(keycloakAuthURI)
                .setKeycloakRealmName(keycloakRealmName)
                .setUsername(username)
                .setPassword(password);
        
        // Instantiate service
        this.service = new ACEService(new URI(serviceURI));
        
        // Prepare domain
        this.domain = new ACEDomain(domainName, DEFAULT_DOMAIN_PREFIX);
        this.domain.setValidFrom(DEFAULT_DOMAIN_VALID_FROM);
    }
    
    /**
     * Authentication mechanism
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
     * Prepare for benchmark
     */
    public void prepare() throws ConnectorException {
        
        try {
            
            // Authenticate
            authenticate();
    
            // Remove old data from ACE
            try {
                service.clearTables(this.token);
            } catch(HTTPException e) {
                // Ignore
            }
    
            // Refresh access token (since the old-data-removal can take a while) and create the domain
            authenticate();
            service.createDomain(this.token, this.domain);
            
        // Catch errors
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }
    
    /**
     * Create pseudonym
     */
    public void createPseudonym(String id) throws ConnectorException {
        
        try {
            authenticate();
            service.createPseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE));
            
        // Catch errors
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }
    
    /**
     * Return storage metrics
     */
    public String getStorageConsumption(String storageIdentifier) throws ConnectorException {
        
        try {
            
            // Authenticate
            authenticate();

            // Gather storage information
            String response = "";
            try {
                response = service.getStorage(token, storageIdentifier);
            } catch(HTTPException e) {
                // Ignore
            }
            
            return response;
            
        // Catch errors
        } catch (Exception e) {
            throw new ConnectorException(e);
        }
    }

    /**
     * Read pseudonym
     */
    public void readPseudonym(String id) throws ConnectorException {
        
        try {
            authenticate();
            service.readPseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE));
            
        // Catch errors
         } catch (Exception e) {
            // It is ok if the pseudonym does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            } 
        }
    }

    @Override
    public void updatePseudonym(String id) throws ConnectorException {
        
        try {
            authenticate();
            service.updatePseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE).withValidFrom(DEFAULT_PSEUDONYM_VALID_FROM));
            
        // Catch errors
         } catch (Exception e) {
            // It is ok if the pseudonym does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            } 
        }
    }

    @Override
    public void deletePseudonym(String id) throws ConnectorException {
        

        try {
            authenticate();
            service.deletePseudonym(this.token, this.domain, new ACEPseudonym(id, DEFAULT_ID_TYPE));
            
        // Catch errors
         } catch (Exception e) {
            // It is ok if the pseudonym does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            } 
        }
    }

    @Override
    public void ping() throws ConnectorException {
        try {
            service.ping(token);
        } catch (Exception e) {
            // It is ok if the endpoint does not exist
            if (!(e instanceof HTTPException && ((HTTPException) e).getStatusCode() == 404)) {
                throw new ConnectorException(e);
            }
        }
    }
}