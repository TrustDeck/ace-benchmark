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

import java.util.Random;

/**
 * This class handles the distribution of requests according to the scenario-configuration.
 * 
 * @author Fabian Prasser and Armin Müller
 */
public class WorkDistribution {
    
    /** Provides randomness. */
    private static final Random RANDOM = new Random(677321100);
    
    /**
     * Types of work.
     */
    public static enum WorkType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        PING
    }
    
    /** Number of create operations. */
    private final int c;
    
    /** Number of create and read operations. */
    private final int cr;
    
    /** Number of create, read, and update operations. */
    private final int cru;
    
    /** Number of create, read, update, and delete operations. */
    private final int crud;
    
    /** Number of create, read, update, delete, and ping operations. */
    private final int crudp;
    
    /**
     * Creates a new instance.
     * 
     * @param creates
     * @param reads
     * @param updates
     * @param deletes
     * @param pings
     */
    public WorkDistribution(int creates, int reads, int updates, int deletes, int pings) {
        this.c = creates;
        this.cr = creates + reads;
        this.cru = creates + reads + updates;
        this.crud = creates + reads + updates + deletes;
        this.crudp = creates + reads + updates + deletes + pings;
    }
    
    /**
     * Returns a type of work sampled according to the given distribution.
     * 
     * @return a work type
     */
    public WorkType sample() {
        int number = RANDOM.nextInt(crudp);
        
        if (number < c) {
            return WorkType.CREATE;
        } else if (number < cr) {
            return WorkType.READ;
        } else if (number < cru) {
            return WorkType.UPDATE;
        } else if (number < crud) {
            return WorkType.DELETE;
        } else {
        	return WorkType.PING;
        }
    }
}
