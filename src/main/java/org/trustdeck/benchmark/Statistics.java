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

package org.trustdeck.benchmark;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;

import org.trustdeck.benchmark.connector.BenchmarkException;

/**
 * This class contains the statistics collected for benchmarking.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class Statistics {

    /** The configuration object. */
    private final Configuration config;

    /** The start time of the benchmark run. */
    @Getter
    private long startTime;

    /** Atomic number of creates. */
    private final AtomicInteger creates = new AtomicInteger();
    
    /** Atomic number of reads. */
    private final AtomicInteger reads   = new AtomicInteger();
    
    /** Atomic number of updates. */
    private final AtomicInteger updates = new AtomicInteger();
    
    /** Atomic number of deletes. */
    private final AtomicInteger deletes = new AtomicInteger();
    
    /** Atomic number of pings. */
    private final AtomicInteger pings 	= new AtomicInteger();

    /** Last time the statistics were gathered. */
    @Getter
    private long lastTime = 0;
    
    /** Last time the database statistics were gathered. */
    @Getter
    private long lastTimeDB = 0;
    
    /** Number of creates from last statistic-gathering. */
    private int lastCreates = 0;
    
    /** Number of reads from last statistic-gathering */
    private int lastReads = 0;
    
    /** Number of updates from last statistic-gathering */
    private int lastUpdates = 0;
    
    /** Number of deletes from last statistic-gathering */
    private int lastDeletes = 0;
    
    /** Number of pings from last statistic-gathering */
    private int lastPings = 0;
    
    /** Number of combined creates, reads, updates, and deletes from last statistic-gathering */
    private int lastCRUDs = 0;
    
    /** Number of transactions per second in the last reporting step. */
    @Getter
    private int lastOverallTPS = 0;
   
    /**
     * Creates a new instance.
     * 
     * @param configuration
     */
    public Statistics(Configuration configuration) {
        this.config = configuration;
    }
    
    /**
     * Performance tracking. Thread safe.
     */
    public void addCreate() {
        this.creates.incrementAndGet();
    }
    
    /**
     * Performance tracking. Thread safe.
     */
    public void addDelete() {
        this.deletes.incrementAndGet();
    }
    
    /**
     * Performance tracking. Thread safe.
     */
    public void addRead() {
        this.reads.incrementAndGet();
    }
    
    /**
     * Performance tracking. Thread safe.
     */
    public void addUpdate() {
        this.updates.incrementAndGet();
    }
    
    /**
     * Performance tracking. Thread safe.
     */
    public void addPing() {
        this.pings.incrementAndGet();
    }

    /**
     * Reporting. NOT thread safe.
     * 
     * @throws IOException 
     */
    public void report(Writer writer) throws IOException {
        
        // Collect data
        long currentTime = System.currentTimeMillis();
        int currentCreates = creates.get();
        int currentReads = reads.get(); 
        int currentUpdates = updates.get();
        int currentDeletes = deletes.get();
        int currentPings = pings.get();
        int currentCRUDs = currentCreates + currentReads + currentUpdates + currentDeletes + currentPings;
        
        // Derive parameters
        double tpsCreate = (double)(currentCreates - lastCreates) / (double)(currentTime - lastTime) * 1000d;
        double tpsRead = (double)(currentReads - lastReads) / (double)(currentTime - lastTime) * 1000d;
        double tpsUpdate = (double)(currentUpdates - lastUpdates) / (double)(currentTime - lastTime) * 1000d;
        double tpsDelete = (double)(currentDeletes - lastDeletes) / (double)(currentTime - lastTime) * 1000d;
        double tpsPing = (double)(currentPings - lastPings) / (double)(currentTime - lastTime) * 1000d;
        double tpsOverall = (double)(currentCRUDs - lastCRUDs) / (double)(currentTime - lastTime) * 1000d;
        
        // Print header
        if (lastTime == 0) {
            
            // Print parameters
            StringBuilder builder = new StringBuilder();
            builder.append("Name").append(";");
            builder.append("Threads").append(";");
            builder.append("Initial size").append(";");
            builder.append("Time").append(";");
            builder.append("Num creates").append(";");
            builder.append("Num reads").append(";");
            builder.append("Num updates").append(";");
            builder.append("Num deletes").append(";");
            builder.append("TPS create").append(";");
            builder.append("TPS read").append(";");
            builder.append("TPS update").append(";");
            builder.append("TPS delete").append(";");
            builder.append("TPS ping").append(";");
            builder.append("TPS overall").append("\n");
            writer.write(builder.toString());
        }
        
        // Print parameters
        StringBuilder builder = new StringBuilder();
        builder.append(config.getName()).append(";");
        builder.append(config.getNumThreads()).append(";");
        builder.append(config.getInitialDBSize()).append(";");
        builder.append(String.valueOf((double)(currentTime - startTime)/1000d).replace('.', ',')).append(";");
        builder.append(currentCreates).append(";");
        builder.append(currentReads).append(";");
        builder.append(currentUpdates).append(";");
        builder.append(currentDeletes).append(";");
        builder.append((int)tpsCreate).append(";");
        builder.append((int)tpsRead).append(";");
        builder.append((int)tpsUpdate).append(";");
        builder.append((int)tpsDelete).append(";");
        builder.append((int)tpsPing).append(";");
        builder.append((int)tpsOverall).append("\n");
        writer.write(builder.toString());
        
        // Store
        this.lastTime = currentTime;
        this.lastCreates = currentCreates;
        this.lastReads = currentReads;
        this.lastUpdates = currentUpdates;
        this.lastDeletes = currentDeletes;
        this.lastPings = currentPings;
        this.lastCRUDs = currentCRUDs;
        this.lastOverallTPS = (int) tpsOverall;
    }
    
    /**
    * Reporting DB storage. NOT thread safe.
    *
    * @throws IOException
     * @throws BenchmarkException
    */
   public void reportDBStorage(Writer writer, WorkProvider provider) throws BenchmarkException, IOException {

       // Collect data
       long currentTime = System.currentTimeMillis();

       String d = provider.getDBStorageMetrics("domain");
       String p = provider.getDBStorageMetrics("pseudonym");
       String a = provider.getDBStorageMetrics("auditevent");

       // Derive parameters
       long domainSize, domainRecordCount, domainDBSize, pseudonymSize, pseudonymRecordCount, pseudonymDBSize, auditeventSize, auditeventRecordCount, auditeventDBSize;
       double domainBytesPerRecord, pseudonymBytesPerRecord, auditeventBytesPerRecord;

           // Process domain metrics
           domainSize = Long.parseLong(d.substring(d.indexOf("tableSize: ") + "tableSize: ".length(), d.indexOf(", recordCount:")).trim());
           domainRecordCount = Long.parseLong(d.substring(d.indexOf("recordCount: ") + "recordCount: ".length(), d.indexOf(", totalSize:")).trim());
           domainDBSize = Long.parseLong(d.substring(d.indexOf("totalSize: ") + "totalSize: ".length()).trim());
           domainBytesPerRecord = (double) domainSize / (double) domainRecordCount;

           // Process pseudonym metrics
           pseudonymSize = Long.parseLong(p.substring(p.indexOf("tableSize: ") + "tableSize: ".length(), p.indexOf(", recordCount:")).trim());
           pseudonymRecordCount = Long.parseLong(p.substring(p.indexOf("recordCount: ") + "recordCount: ".length(), p.indexOf(", totalSize:")).trim());
           pseudonymDBSize = Long.parseLong(p.substring(p.indexOf("totalSize: ") + "totalSize: ".length()).trim());
           pseudonymBytesPerRecord = (double) pseudonymSize / (double) pseudonymRecordCount;

           // Process audit event metrics
           auditeventSize = Long.parseLong(a.substring(a.indexOf("tableSize: ") + "tableSize: ".length(), a.indexOf(", recordCount:")).trim());
           auditeventRecordCount = Long.parseLong(a.substring(a.indexOf("recordCount: ") + "recordCount: ".length(), a.indexOf(", totalSize:")).trim());
           auditeventDBSize = Long.parseLong(a.substring(a.indexOf("totalSize: ") + "totalSize: ".length()).trim());
           auditeventBytesPerRecord = (double) auditeventSize / (double) auditeventRecordCount;

       // Print header
       if (lastTimeDB == 0) {

           // Print parameters
           StringBuilder builder = new StringBuilder();
           builder.append("Time").append(";");
           builder.append("Table name").append(";");
           builder.append("Table size").append(";");
           builder.append("Number of records").append(";");
           builder.append("Bytes per record").append(";");
           builder.append("Database size").append("\n");
           writer.write(builder.toString());
       }

       // Print parameters
       StringBuilder builder = new StringBuilder();
       builder.append(String.valueOf((double)(currentTime - startTime)/1000d).replace('.', ',')).append(";");
       builder.append("domain").append(";");
       builder.append(domainSize).append(";");
       builder.append(domainRecordCount).append(";");
       builder.append(domainBytesPerRecord).append(";");
       builder.append(domainDBSize).append("\n");

       builder.append(String.valueOf((double)(currentTime - startTime)/1000d).replace('.', ',')).append(";");
       builder.append("pseudonym").append(";");
       builder.append(pseudonymSize).append(";");
       builder.append(pseudonymRecordCount).append(";");
       builder.append(pseudonymBytesPerRecord).append(";");
       builder.append(pseudonymDBSize).append("\n");

       builder.append(String.valueOf((double)(currentTime - startTime)/1000d).replace('.', ',')).append(";");
       builder.append("auditevent").append(";");
       builder.append(auditeventSize).append(";");
       builder.append(auditeventRecordCount).append(";");
       builder.append(auditeventBytesPerRecord).append(";");
       builder.append(auditeventDBSize).append("\n");

       writer.write(builder.toString());

       // Store
       this.lastTimeDB = currentTime;
   }

    /**
     * Stores the start time.
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
    }
}
