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
package org.trustdeck.benchmark.connector;

import java.net.URISyntaxException;
import org.trustdeck.benchmark.connector.ace.HTTPException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Interface for pseudonymization service
 * @param <S>
 * @param <T>
 * @param <U>
 * @author Fabian Prasser
 */
public interface PseudonymizationService<S extends Token<?>, T extends Domain, U extends Pseudonym> {

    /**
     * Create domain.
     * 
     * @param token
     * @param domain
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    void createDomain(S token, T domain) throws URISyntaxException,
                                                  HTTPException,
                                                  JsonProcessingException;

    /**
     * Read domain.
     * 
     * @param token
     * @param domain
     * @throws URISyntaxException
     * @throws HTTPException
     */
    void readDomain(S token, T domain) throws URISyntaxException, HTTPException;

    /**
     * Update domain.
     * 
     * @param token
     * @param domain
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    void updateDomain(S token, T domain) throws URISyntaxException,
                                                  HTTPException,
                                                  JsonProcessingException;

    /**
     * Delete domain.
     * 
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     */
    void deleteDomain(S token, T domain) throws URISyntaxException, HTTPException;

    /**
     * Clear all tables and vacuum them.
     * 
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     */
    void clearTables(S token) throws URISyntaxException, HTTPException;

    /**
     * Get table storage usage.
     * 
     * @param token
     * @param storageIdentifier
     * @throws URISyntaxException
     * @throws HTTPException
     */
    String getStorage(S token, String storageIdentifier) throws URISyntaxException, HTTPException;

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
    void createPseudonym(S token, T domain, U pseudonym) throws URISyntaxException,
                                                                          HTTPException,
                                                                          JsonProcessingException;

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
    void readPseudonym(S token, T domain, U pseudonym) throws URISyntaxException,
                                                                        HTTPException,
                                                                        JsonProcessingException;

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
    void updatePseudonym(S token, T domain, U pseudonym) throws URISyntaxException,
                                                                          HTTPException,
                                                                          JsonProcessingException;

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
    void deletePseudonym(S token, T domain, U pseudonym) throws URISyntaxException,
                                                                          HTTPException,
                                                                          JsonProcessingException;

    /**
     * Ping.
     * 
     * @param token
     * @throws URISyntaxException
     * @throws HTTPException
     * @throws JsonProcessingException
     */
    void ping(S token) throws URISyntaxException, HTTPException, JsonProcessingException;

}
