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
package org.trustdeck.benchmark.connector.ace;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

/**
 * Singleton class for managing the client object needed for generating requests.
 * 
 * @author Armin Müller
 */
public class HTTPClientManager {
    
	/** The web client object needed to create requests. */
    private static volatile Client client;

    /**
     *  Private constructor prevents instantiation.
     */
    private HTTPClientManager() {}

    /**
     * Retrieve or create the client object.
     * 
     * @return the client object
     */
    static Client getClient() {
        // Double-checked-locking improves performance since thread 
    	// safety is only needed when creating the client for the first time.
    	if (client == null) {
            synchronized (HTTPClientManager.class) {
                if (client == null) {
                    client = ClientBuilder.newClient();
                }
            }
        }
        
        return client;
    }

    /**
     * Closes the client and unsets the class object.
     */
    static void shutdown() {
        if (client != null) {
            client.close();
            client = null;
        }
    }
}
