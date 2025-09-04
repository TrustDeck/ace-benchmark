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
package org.trustdeck.benchmark.connector.trustdeck;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.trustdeck.benchmark.connector.BenchmarkException;
import org.trustdeck.benchmark.connector.Connector;
import org.trustdeck.client.TrustDeckClient;
import org.trustdeck.client.exception.TrustDeckClientLibraryException;
import org.trustdeck.client.exception.TrustDeckResponseException;
import org.trustdeck.client.model.Domain;
import org.trustdeck.client.model.IdentifierItem;
import org.trustdeck.client.model.Pseudonym;

import java.time.LocalDateTime;


/**
 * Connector to ACE.
 *
 * @author Fabian Prasser, Armin Müller, Chethan Nagaraj
 */
@Slf4j
public class TrustDeckConnector implements Connector {

    /**
     * Default domain prefix.
     */
    private static final String DEFAULT_DOMAIN_PREFIX = "PA-";

    /**
     * Default start time for the domain's validity period.
     */
    private static final LocalDateTime DEFAULT_DOMAIN_VALID_FROM = LocalDateTime.parse("2000-01-01T18:00:00");

    /**
     * Default idType.
     */
    private static final String DEFAULT_ID_TYPE = "TestType";

    /**
     * Default start time for the pseudonym's validity period.
     */
    private static final LocalDateTime DEFAULT_PSEUDONYM_VALID_FROM = LocalDateTime.parse("2001-01-01T18:00:00");

    /**
     * The domain used for all pseudonym operations
     */
    private final Domain domain;

    /**
     * TrustDeck client instance for interacting with the TrustDeck API
     */
    @Getter
    private final TrustDeckClient trustDeckClient;


    /**
     * Create a new instance of the connector.
     */
    public TrustDeckConnector(TrustDeckClient trustDeckClient, String serviceDomainName) {

        // Prepare domain
        this.domain = new Domain();
        this.domain.setName(serviceDomainName);
        this.domain.setPrefix(DEFAULT_DOMAIN_PREFIX);
        this.domain.setValidFrom(DEFAULT_DOMAIN_VALID_FROM);
        this.trustDeckClient = trustDeckClient;
    }

    /**
     * Prepare for benchmark.
     * Remove old data.
     */
    public void prepare() throws BenchmarkException {
        try {
            try {
                this.clearTables();
                Thread.sleep(15000);
                log.info("Tables cleared successfully");
                this.deleteDomainRightsAndRoles(this.domain);
                log.info("Domain rights and roles cleared successfully");
                this.createDomain(this.domain);
                log.info("Domain - {} was created successfully", domain.getName());
            } catch (TrustDeckClientLibraryException | InterruptedException e) {
                // Ignore
            }
        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }

    /**
     * Performs a ping test to check TrustDeck connectivity.
     */
    @Override
    public void ping() throws BenchmarkException {
        try {
            this.trustDeckClient.ping();
            log.debug("\nPing successful");
        } catch (TrustDeckClientLibraryException e) {
                if (e.getResponseStatusCode().value() == 404) {
                    // Ignore 404 errors
                    return;
                }
                log.info("\nPing failed with error status code :{}",e.getResponseStatusCode());
            } catch (Exception e) {
                throw new BenchmarkException(e);
            }
        }


//------------------------------------- Domain Operations---------------------------------------------------------------------------------------

    /**
     * Creates a new domain in TrustDeck.
     *
     * @param domain the object to be created
     */
    @Override
    public void createDomain(Domain domain) {
        this.trustDeckClient.domains().create(domain); //Exception handled by caller
    }


    /**
     * Reads a domain from TrustDeck.
     *
     * @param domain the domain object containing the name to be read
     */
    @Override
    public void readDomain(Domain domain) throws BenchmarkException {
        try {
            this.trustDeckClient.domains().get(domain.getName());
        } catch (TrustDeckClientLibraryException e) {
            log.info("\nDomain read failed for domain:{}", domain.getName());
        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }

    /**
     * Updates an existing domain in TrustDeck.
     *
     * @param domainName the name of the domain to update
     * @param domain     the domain object containing updated information
     */
    @Override
    public void updateDomain(String domainName, Domain domain) throws BenchmarkException {
        try {
            this.trustDeckClient.domains().update(domainName, domain);
        } catch (TrustDeckClientLibraryException e) {
            log.info("\nDomain update failed for domain:{}", domainName);
        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }

    /**
     * Deletes a domain from TrustDeck.
     *
     * @param domain the domain to be deleted
     */
    @Override
    public void deleteDomain(Domain domain) throws BenchmarkException {

        try {
            this.trustDeckClient.domains().delete(domain.getName(), true);
        } catch (TrustDeckClientLibraryException e) {
            log.info("\nDomain deletion failed for domain:{}", domain.getName());
        } catch (Exception e) {
            {
                throw new BenchmarkException(e);
            }
        }
    }


//------------------------------Pseudonym Operations--------------------------------------------------------


    /**
     * Creates a new pseudonym in the specified domain.
     *
     * @param id the identifier used for creating the pseudonym
     */
    @Override
    public void createPseudonym(String id) throws BenchmarkException {
        // Build identifier object
        IdentifierItem identifierItem = IdentifierItem.builder().identifier(id).idType(DEFAULT_ID_TYPE).build();
        // Create Pseudonym by providing the identifier item
        try {
            this.trustDeckClient.pseudonyms(this.domain.getName()).create(identifierItem, false);
        } catch (TrustDeckClientLibraryException e) {
            if (e.getResponseStatusCode().value() == 404) {
                // Ignore 404 errors
                return;
            }
            log.info("\nPseudonym creation failed with error {}", e.getResponseStatusCode());

        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }


    /**
     * Reads a pseudonym from the specified domain.
     *
     * @param id the identifier used to retrieve the pseudonym
     */
    @Override
    public void readPseudonym(String id) throws BenchmarkException {

        // Build identifier object
        IdentifierItem identifierItem = IdentifierItem.builder().identifier(id).idType(DEFAULT_ID_TYPE).build();

        try {
            this.trustDeckClient.pseudonyms(this.domain.getName()).get(identifierItem);
        } catch (TrustDeckClientLibraryException e) {
            if (e.getResponseStatusCode().value() == 404) {
                // Ignore 404 errors
                return;
            }
            log.info("\nPseudonym retrieval for id:{} failed with error {}",id, e.getResponseStatusCode());

        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }


    /**
     * Updates an existing pseudonym in the specified domain.
     *
     * @param id the identifier of the pseudonym to update
     */
    @Override
    public void updatePseudonym(String id) throws BenchmarkException {
        try {
            IdentifierItem identifierItem = IdentifierItem.builder().identifier(id).idType(DEFAULT_ID_TYPE).build();
            Pseudonym updatePseudonym = Pseudonym.builder().identifierItem(identifierItem).validFrom(DEFAULT_PSEUDONYM_VALID_FROM).build();
            this.trustDeckClient.pseudonyms(this.domain.getName()).update(identifierItem, updatePseudonym);
            log.debug(" Pseudonym Update successful for identifier :{} ", id);
        } catch (TrustDeckClientLibraryException e) {
            if (e.getResponseStatusCode().value() == 404) {
                //ignore
                return;
            }
            log.info("\nPseudonym update for id:{} failed with error {}",id, e.getResponseStatusCode());
        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }

    /**
     * Deletes a pseudonym from the specified domain.
     *
     * @param id the identifier of the pseudonym to delete
     */
    @Override
    public void deletePseudonym(String id) throws BenchmarkException {
        try {
            IdentifierItem identifierItem = IdentifierItem.builder().identifier(id).idType(DEFAULT_ID_TYPE).build();
            this.trustDeckClient.pseudonyms(domain.getName()).delete(identifierItem);
        } catch (TrustDeckClientLibraryException e) {
            if (e.getResponseStatusCode().value() == 404) {
                //ignore
                return;
            }
            log.info("\nPseudonym deletion for id:{} failed with error {}",id, e.getResponseStatusCode());
        } catch (Exception e) {
            throw new BenchmarkException(e);
        }
    }


    //-------------------------    //DB Maintenance Operations   --------------------------------------------------------------------------------------------------------------


    /**
     * Clears all tables in the database.
     */
    @Override
    public void clearTables() throws TrustDeckClientLibraryException {
        this.trustDeckClient.dbMaintenance().clearTables();
    }

    /**
     * Deletes domain-specific rights and roles from the database.
     *
     * @param domain the domain whose rights and roles should be deleted
     * @throws TrustDeckClientLibraryException if there is an error in the TrustDeck client
     * @throws TrustDeckResponseException      if there is an error in the TrustDeck response
     */
    @Override
    public void deleteDomainRightsAndRoles(Domain domain) throws TrustDeckClientLibraryException {
        this.trustDeckClient.dbMaintenance().deleteDomainRightsAndRoles(domain);
    }

    /**
     * Retrieves the size of a database table
     *
     * @param tableName (required) the name of the table from which the user wants to read the table size
     */
    @Override
    public String getStorageConsumption(String tableName) throws BenchmarkException {
        try {
            return this.trustDeckClient.dbMaintenance().getStorage(tableName);
        } catch (Exception e) {
            if (e instanceof TrustDeckClientLibraryException) {
                // Ignore TrustDeck client exceptions
                return "";
            }
            throw new BenchmarkException(e);
        }
    }
}

