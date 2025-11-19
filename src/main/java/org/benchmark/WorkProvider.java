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

package org.benchmark;

import de.pseudonymisierung.mainzelliste.client.ID;
import de.pseudonymisierung.mainzelliste.client.InvalidSessionException;
import de.pseudonymisierung.mainzelliste.client.MainzellisteNetworkException;
import org.benchmark.connector.BenchmarkException;
import org.benchmark.connector.MConnector;
import org.benchmark.connector.MConnectorFactory;
import org.codehaus.jettison.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class that provides the work for the worker threads.
 *
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class WorkProvider {

    // List to store created patient IDs
    private final List<Map<String, String>> createdIds = new CopyOnWriteArrayList<>();
    /**
     * The benchmark driver's configuration object.
     */
    private Configuration config;


    // TODO-check if needed for Mainz
    /**
     * The work distribution according to the scenario.
     */
    private WorkDistribution distribution;

    /**
     * The set of identifiers used to create/access pseudonym-objects.
     */
    private Identifiers identifiers;

// TODO-move to seperate

//    /** Thread local connectors*/
//    private ThreadLocal<TConnector> threadLocalConnectors;

    /**
     * The statistics object.
     */
    private Statistics statistics;
    private ThreadLocal<MConnector> threadLocalConnectors;

    /**
     * Creates a new instance.
     *
     * @param config
     * @param identifiers
     * @param statistics
     * @param factory
     */

    // TODO-move to seperate

//    public WorkProvider(Configuration config,
//                        Identifiers identifiers,
//                        Statistics statistics,
//                        TConnectorFactory factory) {
    public WorkProvider(Configuration config, Identifiers identifiers, Statistics statistics, MConnectorFactory factory) {

        // Store config
        this.config = config;
        this.identifiers = identifiers;
        this.statistics = statistics;


        // Prepare thread-local instances
        this.threadLocalConnectors = ThreadLocal.withInitial(() -> {
            try {
                return factory.create();
            } catch (BenchmarkException e) {
                throw new RuntimeException(e);
            }
        });
        // Distribution of work
        this.distribution = new WorkDistribution(config.getCreateRate(), config.getReadRate(), config.getUpdateRate(), config.getDeleteRate(), config.getPingRate());
    }

    /**
     * Prepare the benchmark run.
     *
     * @throws BenchmarkException
     */
    public void prepare() throws BenchmarkException, InvalidSessionException, MainzellisteNetworkException, JSONException {
        // Remove old data and create benchmark table
        threadLocalConnectors.get().prepare();

        //TODO- check this once

        // Create initial patient pool
        for (int i = 0; i < config.getInitialDBSize(); i++) {
            threadLocalConnectors.get().addPatient();
        }
    }



    /**
     * Returns the next work item.
     *
     * @return the work
     */
    public Runnable getWork() {

        //TODO-move to seperate
//        TConnector connector = threadLocalConnectors.get();

        //  Obtain thread-local mainzelliste connector instances
        MConnector connector = threadLocalConnectors.get();


        // Get the template according to the defined distribution
        switch (distribution.sample()) {
            case CREATE:
                return new Runnable() {

                    @Override
                    public void run() {

                        try {

                            // Pass both the ID and patient fields
                            Map<String, String> createdId = connector.addPatient();
                            createdIds.add(createdId);
                        } catch (Exception e) {
                            if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
                                // Work submitted shortly before the benchmark was terminated might still be processed.
                                // Exceptions thrown by those requests can be ignored.
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                        statistics.addCreate();
                    }
                };
            case READ:
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (Map<String, String> x : createdIds) {
                                String idType = x.get("idType");
                                String idString = x.get("idString");
                                connector.readPatient(new ID(idType, idString));
                            }

                        } catch (BenchmarkException | InvalidSessionException | MainzellisteNetworkException e) {
                            if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
                                // Work submitted shortly before the benchmark was terminated might still be processed.
                                // Exceptions thrown by those requests can be ignored.
                            } else {
                                throw new RuntimeException(e);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        statistics.addRead();
                    }
                };
            case UPDATE:
                return new Runnable() {
                    @Override
                    public void run() {
                        try {

                            for (Map<String, String> x : createdIds) {
                                String idType = x.get("idType");
                                String idString = x.get("idString");
                                connector.editPatient(new ID(idType, idString));
                            }
                        } catch (Exception e) {
                            if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
                                // Work submitted shortly before the benchmark was terminated might still be processed.
                                // Exceptions thrown by those requests can be ignored.
                            } else {
                                throw new RuntimeException(e);
                            }

                            statistics.addUpdate();
                        }
                    }
                };

            case DELETE:
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (Map<String, String> x : createdIds) {
                                String idType = x.get("idType");
                                String idString = x.get("idString");
                                connector.deletePatient(new ID(idType, idString));
                            }
                        } catch (Exception e) {
                            if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
                                // Work submitted shortly before the benchmark was terminated might still be processed.
                                // Exceptions thrown by those requests can be ignored.
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                        statistics.addDelete();
                    }
                };
            case PING:
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            connector.ping();
                        } catch (MainzellisteNetworkException e) {
                            if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
                                // Work submitted shortly before the benchmark was terminated might still be processed.
                                // Exceptions thrown by those requests can be ignored.
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                        statistics.addPing();
                    }
                };
        }

        // Sanity check
        throw new IllegalStateException("No work can be provided.");
    }
}
