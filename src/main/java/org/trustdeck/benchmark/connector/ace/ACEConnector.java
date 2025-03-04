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
package org.trustdeck.benchmark.connector.ace;

import java.net.URI;
import java.net.URISyntaxException;

import org.trustdeck.benchmark.connector.Connector;
import org.trustdeck.benchmark.connector.ConnectorException;

/**
 * Connector to ACE.
 * 
 * @author Fabian Prasser, Armin Müller
 */
public class ACEConnector implements Connector {

    /** Default domain prefix. */
    private static final String DEFAULT_DOMAIN_PREFIX = "TST";
    
    /** Default idType. */
    private static final String DEFAULT_ID_TYPE = "ID";
    
    /** Default start time for the domain's validity period. */
    private static final String DEFAULT_DOMAIN_VALID_FROM = "2000-01-01T18:00:00";
    
    /** Default start time for the pseudonym's validity period. */
    private static final String DEFAULT_PSEUDONYM_VALID_FROM = "2001-01-01T18:00:00";

    /** ACE service. */
    private ACEService service;
    
    /** Access token. */
    private ACEToken token;
    
    /** Domain to use for the benchmarking in ACE. */
    private ACEDomain domain;
    
    /**
     * Create a new instance of the connector.
     * 
     * @throws URISyntaxException
     */
    public ACEConnector(String serviceURI, String serviceDomainName) throws URISyntaxException {

        // Instantiate service
        this.service = new ACEService(new URI(serviceURI));
        
        // Prepare domain
        this.domain = new ACEDomain(serviceDomainName, DEFAULT_DOMAIN_PREFIX);
        this.domain.setValidFrom(DEFAULT_DOMAIN_VALID_FROM);
    }

    /**
     * Authentication mechanism. Retrieves an access token. 
     */
    private void authenticate() {
    	this.token = new ACEToken(ACETokenManager.getInstance().getToken());
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
		Thread.sleep(15000);
		service.deleteRoles(this.token, this.domain);
            } catch (HTTPException e) {
                // Ignore
            } catch (InterruptedException f) {
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
     * Create a pseudonym.
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
}
