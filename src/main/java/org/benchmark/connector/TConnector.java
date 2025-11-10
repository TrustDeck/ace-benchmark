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
package org.benchmark.connector;

import org.trustdeck.client.model.Domain;

/**
 * Connector interface for benchmark operations.
 * Defines methods for domain and pseudonym management, service health check, and storage metrics retrieval.
 *
 * @author Fabian Prasser, Chethan Nagaraj
 */
public interface TConnector {

    /** Prepares the benchmark environment. */
    public void prepare() throws BenchmarkException;



    /** Creates a new domain. */
    public void createDomain(Domain domain) throws BenchmarkException;

    /** Reads an existing domain. */
    public void readDomain(Domain domain) throws BenchmarkException;

    /** Updates an existing domain. */
    public void updateDomain(String domainName, Domain domain) throws BenchmarkException;

    /** Deletes a domain.*/
    public void deleteDomain(Domain domain) throws BenchmarkException;



    /** Create pseudonym. */
    public void createPseudonym(String id) throws BenchmarkException;

    /** Read pseudonym. */
    public void readPseudonym(String string) throws BenchmarkException;
    
    /** Update pseudonym. */
    public void updatePseudonym(String string) throws BenchmarkException;
    
    /*** Delete pseudonym.*/
    public void deletePseudonym(String string) throws BenchmarkException;



    /** Ping the service. */
    public void ping() throws BenchmarkException;

    public void clearTables() throws BenchmarkException;

    public void deleteDomainRightsAndRoles(Domain domain) throws BenchmarkException;

    /*** Fetches Storage Metrics.*/
    public String getStorageConsumption(String storageIdentifier) throws BenchmarkException;



}
