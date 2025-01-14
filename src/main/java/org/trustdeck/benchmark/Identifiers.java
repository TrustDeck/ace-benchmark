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

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

/**
 * Creates and accesses IDs by incrementing from a start number.
 * 
 * @author Felix Wirth, Armin Müller
 *
 */
public class Identifiers {
    
    /** Provides randomness. */
    private static final Random RANDOM  = new Random(735576300);
    
    /** Prefix. */
    private static final String PREFIX  = "ID";
    
    /** Length. */
    private static final int LENGTH  = 32;
    
    /** Atomic counter. */
    private final AtomicLong counter = new AtomicLong(0);

    /**
     * Creates a new instance.
     */
    public Identifiers() {
        // Empty by design
    }

    /**
     * Create the next identifier.
     * 
     * @return the next number as a string, padded to the desired length
     */
    public String create() {
        return PREFIX + StringUtils.leftPad(String.valueOf(counter.incrementAndGet()), LENGTH - PREFIX.length(), "0");
    }

    /**
     * Read the next identifier.
     * 
     * @implNote this does not respect already deleted numbers so that this can return non-existing IDs
     * @return the next number as a string, padded to the desired length
     */
    public String read() {
        return PREFIX + StringUtils.leftPad(String.valueOf(RANDOM.nextInt((int) counter.get())), LENGTH - PREFIX.length(), "0");
    }
}
