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

/**
 * This class represents a thread that performs dedicated work.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class Worker extends Thread {
    
    /** Work provider. */
    private WorkProvider provider;
    
    /**
     * Creates a new instance.

     * @param provider
     */
    public Worker(WorkProvider provider) {
        this.provider = provider;
        this.setDaemon(true);
    }
    
    @Override
    public void run() {
        
        // Do forever
        while (true) {
            
            // Next work package
            Runnable work = this.provider.getWork();
            
            // Perform work
            work.run();
            
            // See if it's time to stop
            if (Thread.interrupted()) {
                return;
            }
        }
    }
}