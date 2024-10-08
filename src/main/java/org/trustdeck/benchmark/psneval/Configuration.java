/*
 * ACE-Benchmark Driver
 * Copyright 2024 Armin M�ller and contributors.
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

package org.trustdeck.benchmark.psneval;

import lombok.Getter;

/**
 * This class represents the configuration object for a benchmarking run.
 * 
 * @author Felix Wirth and Armin M�ller
 */
@Getter
public class Configuration {
    
    /** Create rate in percent. */
    private final int createRate;
    
    /** Read rate in percent. */
    private final int readRate;
    
    /** Update rate in percent. */
    private final int updateRate;
    
    /** Delete rate in percent. */
    private final int deleteRate;
    
    /** Ping rate in percent. */
    private final int pingRate;
    
    /** Number of threads. */
    private final int numThreads;
    
    /** Maximal time until evaluation stops in milliseconds. */
    private final int maxTime;
    
    /** Name of the benchmark run. */
    private final String name;
    
    /** Name of the domain in which the benchmarking operations should be performed. */
    private final String domainName;
    
    /** Number of records created already at preparation stage. */
    private final int initialDBSize;
    
    /** Interval of performance recording in milliseconds. */
    private final int reportingInterval;
    
    /** Interval of database storage check recording in milliseconds. */
    private final int reportingIntervalDBSpace;
    
    /**
     * Creates a new instance.
     * 
     * @param createRate
     * @param readRate
     * @param upateRate
     * @param deleteRate
     * @param pingRate
     * @param numThreads
     * @param maxTime
     * @param name
     * @param domainName
     * @param initialDBSize
     * @param reportingInterval
     * @param reportingIntervalDBSpace
     */
    private Configuration(int createRate,
                          int readRate,
                          int upateRate,
                          int deleteRate,
                          int pingRate,
                          int numThreads,
                          int maxTime,
                          String name,
                          String domainName,
                          int initialDBSize,
                          int reportingInterval,
                          int reportingIntervalDBSpace) {
        this.readRate = readRate;
        this.createRate = createRate;
        this.updateRate = upateRate;
        this.deleteRate = deleteRate;
        this.pingRate = pingRate;
        this.numThreads = numThreads;
        this.maxTime = maxTime;
        this.name = name;
        this.domainName = domainName;
        this.initialDBSize = initialDBSize;
        this.reportingInterval = reportingInterval;
        this.reportingIntervalDBSpace = reportingIntervalDBSpace;
    }
    
    /**
     * Return builder
     * @return
     */
    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }
    
    /** 
     * Builder for a configuration object.
     */
    @Getter
    public static class ConfigurationBuilder {
        
        /** Create rate. */
        private int createRate;
        
        /** Read rate. */
        private int readRate;
        
        /** Update rate. */
        private int updateRate;
        
        /** Delete rate. */
        private int deleteRate;
        
        /** Ping rate. */
        private int pingRate;
        
        /** Number of threads. */
        private int numThreads;
        
        /** Maximal time until evaluation stops in milliseconds. */
        private int maxTime;
        
        /** Name of the benchmark run. */
        private String name;
        
        /** Name of the domain in which the benchmarking operations should be performed. */
        private String domainName;
        
        /** Number of records created already at preparation stage. */
        private int initialDBSize;
        
        /** Interval of performance recording in milliseconds. */
        private int reportingInterval;
        
        /** Interval of database storage check recording in milliseconds. */
        private int reportingIntervalDBSpace;
        
        /**
         * Build the configuration.
         * 
         * @return a configuration object
         */
        public Configuration build() {
            // Checks
            if (createRate < 0 || readRate < 0 || updateRate < 0 || deleteRate < 0 || pingRate < 0 || numThreads < 0 || maxTime < 0 || initialDBSize < 0) {
                throw new IllegalStateException("All number values must be zero or positive!");
            }
            
            if (readRate + createRate + updateRate + deleteRate + pingRate != 100) {
                throw new IllegalStateException("All rates combined must add up to exactly one hundred!");
            }
            
            if (!((maxTime > 0) || (maxTime == 0))) {
                throw new IllegalStateException("Max time must be 0 and the other must be positive ");
            }
            
            if (name == null || domainName == null) {
                throw new IllegalStateException("Domain name and name must not be null!");
            }
            
            if (initialDBSize == 0 && (readRate > 0 || updateRate > 0 || deleteRate > 0)) {
                throw new IllegalStateException("If read, update or delete is set, the number of pre-created records must not be null.");
            }
            
            if (this.reportingInterval <= 0) {
                throw new IllegalStateException("Recorder interval must be greater than zero.");
            }
            
            if (this.reportingIntervalDBSpace <= 0) {
                throw new IllegalStateException("Database storage recorder interval must be greater than zero.");
            }
            
            // Create object
            return new Configuration(createRate, readRate, updateRate, deleteRate, pingRate, numThreads, maxTime, name, domainName, initialDBSize, reportingInterval, reportingIntervalDBSpace);
        }
        
        // SETTERS SECTION (these allow chaining).
        
        /**
         * @param readRate the readRate to set - if provided please also configure a number of records to be created while preparing
         */
        public ConfigurationBuilder setReadRate(int readRate) {
            this.readRate = readRate;
            return this;
        }

        /**
         * @param createRate the createRate to set
         */
        public ConfigurationBuilder setCreateRate(int createRate) {
            this.createRate = createRate;
            return this;
        }

        /**
         * @param updateRate the updateRate to set
         */
        public ConfigurationBuilder setUpdateRate(int updateRate) {
            this.updateRate = updateRate;
            return this;
        }

        /**
         * @param deleteRate the deleteRate to set
         */
        public ConfigurationBuilder setDeleteRate(int deleteRate) {
            this.deleteRate = deleteRate;
            return this;
        }

        /**
         * @param pingRate the pingRate to set
         */
        public ConfigurationBuilder setPingRate(int pingRate) {
            this.pingRate = pingRate;
            return this;
        }

        /**
         * @param numThreads the numThreads to set
         */
        public ConfigurationBuilder setNumThreads(int numThreads) {
            this.numThreads = numThreads;
            return this;
        }
    
        /**
         * @param maxTime the maxTime to set
         */
        public ConfigurationBuilder setMaxTime(int maxTime) {
            this.maxTime = maxTime;
            return this;
        }
    
        /**
         * @param name the name to set
         */
        public ConfigurationBuilder setName(String name) {
            this.name = name;
            return this;
        }
    
        /**
         * @param domainName the domainName to set
         */
        public ConfigurationBuilder setDomainName(String domainName) {
            this.domainName = domainName;
            return this;
        }
        
        /**
         * @param initialDBSize The number of records created before the evaluation starts
         */
        public ConfigurationBuilder setInitialDBSize(int initialDBSize) {
            this.initialDBSize = initialDBSize;
            return this;
        }
        
        /**
         * @param reportingInterval the recorderInterval to set
         * @return 
         */
        public ConfigurationBuilder setReportingInterval(int reportingInterval) {
            this.reportingInterval = reportingInterval;
            return this;
        }
        
        /**
         * @param reportingIntervalDBSpace the recorderIntervalDBSpace to set
         * @return 
         */
        public ConfigurationBuilder setReportingIntervalDBSpace(int reportingIntervalDBSpace) {
            this.reportingIntervalDBSpace = reportingIntervalDBSpace;
            return this;
        }
    }
}
