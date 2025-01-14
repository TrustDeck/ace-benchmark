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
package org.trustdeck.benchmark.connector.ace;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.trustdeck.benchmark.Main;
import org.yaml.snakeyaml.Yaml;

/**
 * Singleton class that handles retrieving and refreshing access tokens from keycloak.
 * 
 * @author Armin Müller
 */
public class ACETokenManager {

	/** Singleton instance of the ACE token manager. */
    private static ACETokenManager instance;
    
    /** Lock used to ensure that only one thread refreshes a token simultaneously. */
    private static final ReentrantLock lock = new ReentrantLock();

    /** Instance of the keycloak object used to handle communication with keycloak. */
    private volatile Keycloak keycloakInstance;

    /** The currently valid access token. */
    private volatile String accessToken;

    /** Expiry time of the access token in Unix time (in milliseconds). */
    private long tokenExpiryTime;

    // Private constructor to prevent direct instantiation
    private ACETokenManager() {}

    /**
     *  Get or create the singleton token manager instance.
     *  @return the singleton instance
     */
    public static ACETokenManager getInstance() {
    	// Double checked locking to ensure this will only be created once.
        if (instance == null) {
            synchronized (ACETokenManager.class) {
                if (instance == null) {
                    instance = new ACETokenManager();
                }
            }
        }
        
        return instance;
    }

    /**
     * Initialize the token manager by creating a keycloak instance object.
     * This is used to handle communication between the benchmark and the keycloak API.
     */
    public synchronized void initialize() {
    	// Check if keycloak was already initialized
        if (this.keycloakInstance != null) {
            throw new IllegalStateException("ACETokenManager is already initialized.");
        }
        
        // Extract the configuration from the loaded configuration file
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> yamlConfig = yaml.load(inputStream);
        @SuppressWarnings("unchecked")
        Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("ace");
        
        // Create the Keycloak instance
        this.keycloakInstance = Keycloak.getInstance(
        		toolConfig.get("keycloakAuthUri"),
        		toolConfig.get("keycloakRealmName"),
        		toolConfig.get("username"),
        		toolConfig.get("password"),
        		toolConfig.get("clientId"),
        		toolConfig.get("clientSecret")
        );
    }

    /**
     * Get a valid token.
     * 
     * @return the access token as a String
     */
    public String getToken() {
    	// Check if a keycloak instance object is available
        if (keycloakInstance == null) {
            throw new IllegalStateException("ACETokenManager is not initialized.");
        }

        // Check if the current token is still valid
        if (isTokenValid()) {
            return accessToken;
        }

        // Refresh the token in a thread-safe way
        lock.lock();
        try {
        	// Double-check inside the lock
            if (!isTokenValid()) {
                refreshAccessToken();
            }
            
            return accessToken;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Check if the current token is still valid if there is one.
     * 
     * @return {@code true} if there is an access token available and it's not yet expired, {@code false} otherwise
     */
    private boolean isTokenValid() {
        return accessToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }

    /**
     * Refresh the access token or create a new one if necessary.
     */
    private void refreshAccessToken() {
        TokenManager tokenManager = keycloakInstance.tokenManager();
        long expiresIn = 0;
        
    	try {
            AccessTokenResponse response = tokenManager.refreshToken();
            this.accessToken = response.getToken();
            expiresIn = response.getExpiresIn();
        } catch (NullPointerException e) {
        	// The keycloak code contains a bug: we need to catch this NPE here when there was no token previously generated
            AccessTokenResponse response = tokenManager.grantToken();
            this.accessToken = response.getToken();
            expiresIn = response.getExpiresIn();
    	} catch (Exception f) {
            throw new RuntimeException("Failed to refresh the token: " + f.getMessage(), f);
        }
    	
    	// Reduce time to next refresh by 10 seconds, so we always preemptively refresh. Convert seconds to milliseconds.
        this.tokenExpiryTime = System.currentTimeMillis() + ((expiresIn - 10) * 1000L);
    }
}
