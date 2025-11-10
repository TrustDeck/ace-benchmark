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
import org.apache.commons.lang3.RandomStringUtils;
import de.pseudonymisierung.mainzelliste.client.InvalidSessionException;
import de.pseudonymisierung.mainzelliste.client.MainzellisteNetworkException;
import org.benchmark.connector.*;
import de.pseudonymisierung.mainzelliste.client.ID;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that provides the work for the worker threads.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class WorkProvider {

	/**
	 * The benchmark driver's configuration object.
	 */
	private Configuration config;

	/**
	 * The work distribution according to the scenario.
	 */
	private WorkDistribution distribution;

	private final List<String> randomFirstNames;
	private final List<String> randomLastNames;
	private final int poolSize = 10000;

	// TODO-check if needed for Mainz
	/**
	 * The set of identifiers used to create/access pseudonym-objects.
	 */
	private Identifiers identifiers;

	// TODO-might have to modify for Mainz
	/**
	 * The statistics object.
	 */
	private Statistics statistics;

//    /** Thread local connectors*/
//    private ThreadLocal<TConnector> threadLocalConnectors;

	private ThreadLocal<MConnector> threadLocalConnectors;

	/**
	 * Creates a new instance.
	 *
	 * @param config
	 * @param identifiers
	 * @param statistics
	 * @param factory
	 */
//    public WorkProvider(Configuration config,
//                        Identifiers identifiers,
//                        Statistics statistics,
//                        TConnectorFactory factory) {
	public WorkProvider(Configuration config,
						Identifiers identifiers,
						Statistics statistics,
						MConnectorFactory factory) {

		// Store config
		this.config = config;
		this.identifiers = identifiers;
		this.statistics = statistics;
		this.randomFirstNames = new ArrayList<>(poolSize);
		this.randomLastNames = new ArrayList<>(poolSize);


		for(int i=0; i<poolSize;i++){
			 randomFirstNames.add(RandomStringUtils.randomAlphabetic(5));
			 randomLastNames.add(RandomStringUtils.randomAlphabetic(5));
		}
		// Prepare thread-local instances
		this.threadLocalConnectors =
				ThreadLocal.withInitial(() -> {
					try {
						return factory.create();
					} catch (BenchmarkException e) {
						throw new RuntimeException(e);
					}
				});
		// Distribution of work
		this.distribution = new WorkDistribution(config.getCreateRate(),
				config.getReadRate(),
				config.getUpdateRate(),
				config.getDeleteRate(),
				config.getPingRate());
	}

	/**
	 * Prepare the benchmark run.
	 *
	 * @throws BenchmarkException
	 */
	public void prepare() throws BenchmarkException {
		// Remove old data and create benchmark table
		threadLocalConnectors.get().prepare();

//        // Create initial pseudonym pool
//        for (int i = 0; i < config.getInitialDBSize(); i++) {
//            threadLocalConnectors.get().addPatient(identifiers.create());
//        }
	}

//    /**
//     * Get storage metrics.
//     *
//     * @param storageIdentifier
//     * @throws BenchmarkException
//     */
//    public String getDBStorageMetrics(String storageIdentifier) throws BenchmarkException {
//        return threadLocalConnectors.get().getStorageConsumption(storageIdentifier);
//    }

	/**
	 * Returns the next work item.
	 *
	 * @return the work
	 */
	public Runnable getWork() {

//        // Obtain thread-local mainzelliste
//        TConnector connector = threadLocalConnectors.get();

		MConnector connector = threadLocalConnectors.get();

		// Get the template according to the defined distribution
		switch (distribution.sample()) {
			case CREATE:
				return new Runnable() {

					@Override
					public void run() {

						try {

							String uniqueIdType = identifiers.create();


							// Pass both the ID and patient fields
							connector.addPatient("extid", uniqueIdType);
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
							connector.readPatient(new ID("extid", identifiers.read()));
						} catch (BenchmarkException | InvalidSessionException | MainzellisteNetworkException e) {
							if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
								// Work submitted shortly before the benchmark was terminated might still be processed.
								// Exceptions thrown by those requests can be ignored.
							} else {
								throw new RuntimeException(e);
							}
						}
						statistics.addRead();
					}
				};
			case UPDATE:
				return new Runnable() {
					@Override
					public void run() {
						try {
							connector.editPatient(new ID("extid", identifiers.read()));
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
				return new Runnable(){
						@Override
						public void run () {
							try {
								connector.deletePatient(new ID("extid",identifiers.read()));
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
					}

					;
	        case PING:
							return new Runnable() {
						@Override
						public void run () {
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
					}

					;
				};

			// Sanity check
			throw new IllegalStateException("No work can be provided.");
		}
	}
