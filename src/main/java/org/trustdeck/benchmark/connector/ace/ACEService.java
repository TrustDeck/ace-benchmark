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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.trustdeck.benchmark.connector.PseudonymizationService;
import org.trustdeck.benchmark.connector.ace.HTTPRequest.HTTPMediaType;
import org.trustdeck.benchmark.connector.ace.HTTPRequest.HTTPRequestType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class implements the requests against ACE's API.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class ACEService implements PseudonymizationService<ACEToken, ACEDomain, ACEPseudonym> {
    
    /** Mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    /** The service represented by it's base URI. */
    private final URI service;
    
    /**
     * Creates a new instance.
     * 
     * @param service
     */
    public ACEService(URI service) {
        this.service = service;
    }
    
    /**
     * Create domain.
     * 
     * @param token
     * @param domain
     * @throws URISyntaxException
	 * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void createDomain(ACEToken token, ACEDomain domain) throws URISyntaxException, HTTPException, JsonProcessingException {
        // Build the request
        HTTPRequest request = new HTTPRequest(service,
                                              "/domain",
                                              HTTPRequestType.POST,
                                              token.getToken(),
                                              MAPPER.writer().writeValueAsString(domain),
                                              HTTPMediaType.APPLICATION_JSON);
        
        // Execute
        request.execute();
    }
    
    /**
     * Read domain.
     * 
     * @param token
     * @param domain
     * @throws URISyntaxException
     * @throws HTTPException
     */
    @Override
    public void readDomain(ACEToken token, ACEDomain domain) throws URISyntaxException, HTTPException {
        // Store query parameters
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("name", domain.getName());
        
        // Build the request
        HTTPRequest request = new HTTPRequest(service,
                                              "/domain",
                                              HTTPRequestType.GET,
                                              token.getToken(),
                                              parameters);
        
        // Execute
        request.execute();
    }
    
    /**
     * Update domain.
     * 
     * @param token
     * @param domain
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void updateDomain(ACEToken token, ACEDomain domain) throws URISyntaxException, HTTPException, JsonProcessingException {
    	// Build the request
        HTTPRequest request = new HTTPRequest(service,
                                              "/domain",
                                              HTTPRequestType.PUT,
                                              token.getToken(),
                                              MAPPER.writer().writeValueAsString(domain),
                                              HTTPMediaType.APPLICATION_JSON);
        
        // Execute
        request.execute();
    }
    
    /**
     * Delete domain.
     * 
     * @param token
     * @throws URISyntaxException
	 * @throws HTTPException
     */
    @Override
    public void deleteDomain(ACEToken token, ACEDomain domain) throws URISyntaxException, HTTPException {
    	// Store query parameters
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("name", domain.getName());
        
        // Build the request
        HTTPRequest request = new HTTPRequest(service,
                                              "/domain",
                                              HTTPRequestType.DELETE,
                                              token.getToken(),
                                              parameters);
        
        // Execute
        request.execute();
    }
    
    /**
     * Clear all tables and vacuum them.
     * 
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     */
    @Override
    public void clearTables(ACEToken token) throws URISyntaxException, HTTPException {
    	// Build the requests and execute them
    	HTTPRequest request = new HTTPRequest(service, "/table/pseudonym", HTTPRequestType.DELETE, token.getToken(), null);
        request.execute();
        
        request = new HTTPRequest(service, "/table/domain", HTTPRequestType.DELETE, token.getToken(), null);
        request.execute();
        
        request = new HTTPRequest(service, "/table/auditevent", HTTPRequestType.DELETE, token.getToken(), null);
        request.execute();
    }
    
    /**
     * Get table storage usage.
     * 
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     */
    @Override
    public String getStorage(ACEToken token, String tableName) throws URISyntaxException, HTTPException {
    	// Build the request and execute them
    	HTTPRequest request = new HTTPRequest(service, "/table/"+tableName+"/storage", HTTPRequestType.GET, token.getToken(), null);
        return request.execute();
    }

    /**
     * Create pseudonym.
     * 
     * @param token
     * @param domain
     * @param pseudonym
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void createPseudonym(ACEToken token, ACEDomain domain, ACEPseudonym pseudonym) throws URISyntaxException, HTTPException, JsonProcessingException {
    	// Build the request
    	HTTPRequest request = new HTTPRequest(service, 
											  "/domains/" + domain.getName() + "/pseudonym", 
											  HTTPRequestType.POST, 
											  token.getToken(), 
											  MAPPER.writer().writeValueAsString(pseudonym), 
											  HTTPMediaType.APPLICATION_JSON);
    	
    	// Execute
    	request.execute();
    }
    
    /**
     * Read pseudonym.
     * 
     * @param token
     * @param domain
     * @param pseudonym
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void readPseudonym(ACEToken token, ACEDomain domain, ACEPseudonym pseudonym) throws URISyntaxException, HTTPException, JsonProcessingException {
    	// Store query parameters
    	HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", pseudonym.getId());
        parameters.put("idType", pseudonym.getIdType());
        
        // Build the request
        HTTPRequest request = new HTTPRequest(service, 
											  "/domains/" + domain.getName() + "/pseudonym", 
											  HTTPRequestType.GET, 
											  token.getToken(),
											  parameters);
        
        // Execute
        request.execute();
    }

    /**
     * Update pseudonym.
     * 
     * @param token
     * @param domain
     * @param pseudonym
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void updatePseudonym(ACEToken token, ACEDomain domain, ACEPseudonym pseudonym) throws URISyntaxException, HTTPException, JsonProcessingException {
    	// Store query parameters
    	HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", pseudonym.getId());
        parameters.put("idType", pseudonym.getIdType());
        
        // Build the request
        HTTPRequest request = new HTTPRequest(service, 
        									  "/domains/" + domain.getName() + "/pseudonym", 
        									  HTTPRequestType.PUT, 
        									  token.getToken(), 
        									  MAPPER.writer().writeValueAsString(pseudonym), 
                HTTPMediaType.APPLICATION_JSON,
				  parameters);
        
        // Execute
        request.execute();
    }
    
    /**
     * Delete pseudonym.
     * 
     * @param token
     * @param domain
     * @param pseudonym
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void deletePseudonym(ACEToken token, ACEDomain domain, ACEPseudonym pseudonym) throws URISyntaxException, HTTPException, JsonProcessingException {
    	// Store query parameters
    	HashMap<String, String> parameters = new HashMap<>();
        parameters.put("id", pseudonym.getId());
        parameters.put("idType", pseudonym.getIdType());
        
        // Build the request
        HTTPRequest request = new HTTPRequest(service, 
        									  "/domains/" + domain.getName() + "/pseudonym", 
        									  HTTPRequestType.DELETE, 
        									  token.getToken(),
        									  parameters);
        
        // Execute
        request.execute();
    }
    
    /**
     * Ping.
     * 
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    @Override
    public void ping(ACEToken token) throws URISyntaxException, HTTPException, JsonProcessingException {
    	// Build the request
    	HTTPRequest request = new HTTPRequest(service, "/ping", HTTPRequestType.GET, token.getToken(), null);
        
        // Execute
        request.execute();
    }
}
