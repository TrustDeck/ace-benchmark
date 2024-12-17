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

package org.trustdeck.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.trustdeck.benchmark.connector.Connector;
import org.trustdeck.benchmark.connector.ConnectorException;
import org.trustdeck.benchmark.connector.ace.ACEConnector;
import org.trustdeck.benchmark.connector.ace.ClientManager;
import org.yaml.snakeyaml.Yaml;

/**
 * Main class of the benchmark driver.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException, ConnectorException {
    	
    	// Load configuration from file
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> yamlConfig = yaml.load(inputStream);

        // Extract the benchmark configuration from the loaded configuration file
        @SuppressWarnings("unchecked")
		Map<String, Object> benchmarkConfig = (Map<String, Object>) yamlConfig.get("benchmark");
        final int INITIAL_DB_SIZE = (int) benchmarkConfig.get("initialDbSize");
        final int MAX_TIME = (int) benchmarkConfig.get("maxTime");
        final int REPORTING_INTERVAL = (int) benchmarkConfig.get("reportingInterval");
        final boolean REPORT_DB_SPACE = (boolean) benchmarkConfig.get("reportDbSpace");
        final int REPORTING_INTERVAL_DB_SPACE = (int) benchmarkConfig.get("reportingIntervalDbSpace");
        final int NUM_THREADS = (int) benchmarkConfig.get("numThreads");
        final int NUMBER_OF_REPETITIONS = (int) benchmarkConfig.get("numberOfRepetitions");

        // Extract the scenario configurations from the loaded configuration file
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> scenarios = (List<Map<String, Object>>) benchmarkConfig.get("scenarios");

        // Create configs
        List<Configuration> configs = new ArrayList<>();
        for (Map<String, Object> scenario : scenarios) {
            String name = (String) scenario.get("name");
            int createRate = scenario.containsKey("createRate") ? (int) scenario.get("createRate") : 0;
            int readRate = scenario.containsKey("readRate") ? (int) scenario.get("readRate") : 0;
            int updateRate = scenario.containsKey("updateRate") ? (int) scenario.get("updateRate") : 0;
            int deleteRate = scenario.containsKey("deleteRate") ? (int) scenario.get("deleteRate") : 0;
            int pingRate = scenario.containsKey("pingRate") ? (int) scenario.get("pingRate") : 0;

            for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) {
                configs.add(Configuration.builder()
                        .setCreateRate(createRate)
                        .setReadRate(readRate)
                        .setUpdateRate(updateRate)
                        .setDeleteRate(deleteRate)
                        .setPingRate(pingRate)
                        .setInitialDBSize(INITIAL_DB_SIZE)
                        .setMaxTime(MAX_TIME)
                        .setName(name + "-" + NUM_THREADS + "-threads")
                        .setNumThreads(NUM_THREADS)
                        .setReportingInterval(REPORTING_INTERVAL)
                        .setReportingIntervalDBSpace(REPORTING_INTERVAL_DB_SPACE)
                        .setReportDBSpace(REPORT_DB_SPACE)
                        .build());
            }
        }

        // Create connector
        System.out.print("\r - Preparing service: creating authentication and service object");
        Connector connector = new ACEConnector();
        System.out.println("\r - Preparing service: creating authentication and service object\t[DONE]");

        // Execute
        for (Configuration config : configs) {
            execute(connector, config);
        }
    }
    
    /**
     * Executes a configuration.
     * 
     * @param connector the connector to use
     * @param config the configuration object that should be used to run the benchmark
     * @throws IOException
     * @throws URISyntaxException
     * @throws ConnectorException 
     */
    private static final void execute(Connector connector,
                                      Configuration config) throws IOException, ConnectorException {
               
        // Identifiers
        System.out.print("\r - Preparing benchmark: creating identifiers                      ");
        Identifiers identifiers = new Identifiers();
        System.out.println("\r - Preparing benchmark: creating identifiers\t\t\t\t[DONE]");

        // Statistics
        System.out.print("\r - Preparing benchmark: creating statistics                      ");
        Statistics statistics = new Statistics(config);
        System.out.println("\r - Preparing benchmark: creating statistics\t\t\t\t[DONE]");
        
        // Provider
        System.out.print("\r - Preparing benchmark: creating work provider                      ");
        WorkProvider provider = new WorkProvider(config, identifiers, statistics);
        System.out.println("\r - Preparing benchmark: creating work provider\t\t\t\t[DONE]");
        
        // Prepare
        System.out.print("\r - Preparing benchmark: purge database and re-initialize            ");
        provider.prepare(connector);
        System.out.println("\r - Preparing benchmark: purge database and re-initialize\t\t[DONE]");
        
        // Some logging
        System.out.println("\r - Preparing benchmark: Done");
        
        // Some logging
        System.out.println(" - Executing configuration: " + config.getName());
        
        // Start workers
        statistics.start();
        for (int i = 0; i < config.getNumThreads(); i++) {
            new Worker(provider).start();
        }
        
        // Some logging
        System.out.println("   - Number of workers launched: " + config.getNumThreads());
        
        // Files to write to
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(config.getName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".csv")));
        BufferedWriter dbWriter = config.isReportDBSpace() ? new BufferedWriter(new FileWriter(new File(config.getName() + "_DB_STORAGE-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".csv"))) : null;
        
        // Event and logging loop
        while (true) {
            
            // Reporting
            if (System.currentTimeMillis() - statistics.getLastTime() >= config.getReportingInterval()) {
                statistics.report(writer);
                writer.flush();
                
                // Print progress
                System.out.print("\r   - Progress: " + (double)((int)(((double)(System.currentTimeMillis() - statistics.getStartTime())/(double)config.getMaxTime()) * 1000d))/10d + " %");
            }
            
            // Reporting DB storage size
            if (config.isReportDBSpace() && System.currentTimeMillis() - statistics.getLastTimeDB() >= config.getReportingIntervalDBSpace()) {
                statistics.reportDBStorage(dbWriter, provider);
                dbWriter.flush();
            }
            
            // End of experiment
            if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
            	System.out.println("\r   - Progress: 100 % ");
                break;
            }
            
            // Sleep
            try {
                Thread.sleep(100); // 0.1 second
            } catch (InterruptedException e) {
                break;
            }
            
            // Interrupted?
            if (Thread.interrupted()) {
                break;
            }
        }
        
        // Close writer
        writer.close();
        if (config.isReportDBSpace()) {
        	dbWriter.close();
        }
        
        // Close client
        ClientManager.shutdown();
        
        // Some logging
        System.out.println(" - Done");
    }
}
