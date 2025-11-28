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

package org.benchmark.connector.mainzelliste;

import de.pseudonymisierung.mainzelliste.client.InvalidSessionException;
import de.pseudonymisierung.mainzelliste.client.MainzellisteNetworkException;
import org.benchmark.connector.BenchmarkException;
import org.benchmark.connector.Configuration;
import org.benchmark.connector.mainzelliste.Statistics;
import org.codehaus.jettison.json.JSONException;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Main class of the benchmark driver.
 *
 * @author Armin Müller, Felix N. Wirth, Chethan C. Nagaraj and Fabian Prasser
 */

    // make a document if 1. changes to main benchmark, creation of new benchmark and the mainzelliste benchmark!
public class Main {

    public static void main(String[] args) throws IOException, BenchmarkException, InvalidSessionException, MainzellisteNetworkException, JSONException {

        // Load configuration from file
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> yamlConfig = yaml.load(inputStream);

        // Extract the benchmark configuration from the loaded configuration file
        @SuppressWarnings("unchecked") Map<String, Object> benchmarkConfig = (Map<String, Object>) yamlConfig.get("benchmark");
        final int INITIAL_DB_SIZE = (int) benchmarkConfig.get("initialDbSize");
        final int MAX_TIME = (int) benchmarkConfig.get("maxTime");
        final int REPORTING_INTERVAL = (int) benchmarkConfig.get("reportingInterval");
        final boolean REPORT_DB_SPACE = (boolean) benchmarkConfig.get("reportDbSpace");
        final int REPORTING_INTERVAL_DB_SPACE = (int) benchmarkConfig.get("reportingIntervalDbSpace");
        final int NUM_THREADS = (int) benchmarkConfig.get("numThreads");
        final int NUMBER_OF_REPETITIONS = (int) benchmarkConfig.get("numberOfRepetitions");

        // Extract the scenario configurations from the loaded configuration file
        @SuppressWarnings("unchecked") List<Map<String, Object>> scenarios = (List<Map<String, Object>>) benchmarkConfig.get("scenarios");

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
                configs.add(Configuration.builder().setCreateRate(createRate).setReadRate(readRate).setUpdateRate(updateRate).setDeleteRate(deleteRate).setPingRate(pingRate).setInitialDBSize(INITIAL_DB_SIZE).setMaxTime(MAX_TIME).setName(name + "-" + NUM_THREADS + "-threads").setNumThreads(NUM_THREADS).setReportingInterval(REPORTING_INTERVAL).setReportingIntervalDBSpace(REPORTING_INTERVAL_DB_SPACE).setReportDBSpace(REPORT_DB_SPACE).build());
            }
        }

        // Some logging
        System.out.println("\n++++++++++++++++++++++++++++ ACE Benchmark ++++++++++++++++++++++++++++\n");

        // Log total number of configs
        System.out.println("Total configurations to run: " + configs.size());

        

        MConnectorFactory factory = new MainzellisteConnectorFactory();
        for (int i = 0; i < configs.size(); i++) {
            Configuration config = configs.get(i);
            System.out.println("Running configuration " + (i + 1) + " of " + configs.size() + ": " + config.getName() + " (" + (configs.size() - i - 1) + " left after this)");
            execute(config, factory);
        }

    }

    /**
     * Executes a configuration.
     *
     * @param config The configuration object that should be used to run the benchmark
     * @throws IOException
     * @throws URISyntaxException
     * @throws BenchmarkException
     */
    private static final void execute(Configuration config, MConnectorFactory factory) throws IOException, BenchmarkException, InvalidSessionException, MainzellisteNetworkException, JSONException {



        // Statistics
        System.out.print("\r - Preparing benchmark: creating statistics                      ");
        Statistics statistics = new Statistics(config);
        System.out.println("\r - Preparing benchmark: creating statistics\t\t\t[DONE]");

        // Provider
        System.out.print("\r - Preparing benchmark: creating work provider                      ");
        MWorkProvider provider = new MWorkProvider(config, statistics, factory);
        System.out.println("\r - Preparing benchmark: creating work provider\t\t\t[DONE]");


        // Prepare
        System.out.print("\r - Preparing benchmark: purge database and re-initialize        ");
        provider.prepare();
        System.out.println("\r - Preparing benchmark: purge database and re-initialize\t[DONE]");

        // Some logging
        System.out.println("\r - Preparing benchmark: Done");

        // Some logging
        System.out.println("\n - Executing configuration: " + config.getName());



        // Start workers and keep references
        statistics.start();
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < config.getNumThreads(); i++) {
            Thread t = new Worker(provider);
            workers.add(t);
            t.start();
        }


        // Some logging
        System.out.println("   - Number of workers launched: " + config.getNumThreads());

        // Files to write to
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(config.getName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".csv")));
        BufferedWriter dbWriter = config.isReportDBSpace() ? new BufferedWriter(new FileWriter(new File(config.getName() + "_DB_STORAGE-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".csv"))) : null;

        try {
            // Event and logging loop
            while (true) {
                // Reporting
                if (System.currentTimeMillis() - statistics.getLastTime() >= config.getReportingInterval()) {
                    statistics.report(writer);
                    writer.flush();

                    // Calculate Progress
                    double progress = (double) ((int) (((double) (System.currentTimeMillis() - statistics.getStartTime())
                            / (double) config.getMaxTime()) * 1000d)) / 10d;

                    // Print progress
                    System.out.print("\r   - Progress: " + progress + " % (currently " + statistics.getLastOverallTPS() + " TPS)       ");
                }

                // Note - The DB storage reporting function not implemented

//                // Reporting DB storage size
//                if (config.isReportDBSpace()
//                        && System.currentTimeMillis() - statistics.getLastTimeDB() >= config.getReportingIntervalDBSpace()) {
//                    statistics.reportDBStorage(dbWriter, provider);
//                    dbWriter.flush();
//                }

                // End of experiment
                if (System.currentTimeMillis() - statistics.getStartTime() >= config.getMaxTime()) {
                    System.out.println("\r   - Progress: 100 %                             ");
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
        } finally {
            // Stop and join workers before returning (prevents overlap with the next scenario's prepare)
            for (Thread t : workers) t.interrupt();
            for (Thread t : workers) {
                try {
                    t.join(5000); // waits 5 secs for each worker thread to stop
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Always close writers here
            try {
                writer.close();
            } catch (IOException e) { /* ignore */ }

            if (dbWriter != null) {
                try {
                    dbWriter.close();
                } catch (IOException e) { /* ignore */ }
            }
            System.out.println(" - Done\n");
        }
    }
}
