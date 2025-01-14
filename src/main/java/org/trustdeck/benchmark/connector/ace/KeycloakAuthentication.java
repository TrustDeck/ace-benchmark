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

import java.util.concurrent.locks.ReentrantLock;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;

/**
 * Information needed for authentication.
 * 
 * @author Fabian Prasser and Armin Müller
 */
public class KeycloakAuthentication {
    
    /** Name of the user utilized for the benchmarking process. */
    protected String username;
    
    /** The user's password. */
    protected String password;
    
    /** The Keycloak instance's clientID representing ACE. */
    protected String clientId;
    
    /** The client secret for the above clientID. */
    protected String clientSecret;
    
    /** The URI of the Keycloak authentication server. */
    protected String keycloakAuthenticationURI;
    
    /** The name of Keycloak realm. */
    protected String keycloakRealmName;
	
	/** TokenManager: handles Keycloak's access and refresh tokens. */
	protected TokenManager tokenManager;
	
	/** Was a token already initially issued. */
	private volatile boolean tokenIssued = false;
    
    /** Singleton Keycloak instance. */
    private volatile Keycloak keycloakInstance;
    
    /** Lock to avoid multiple initializations of Keycloak. */
    private final ReentrantLock lock = new ReentrantLock();

    /** Thread-local TokenManager; handles Keycloak's access and refresh tokens. */
    //private ThreadLocal<TokenManager> threadLocalTokenManager = ThreadLocal.withInitial(() -> {
    //    initializeKeycloak();

	//if (keycloakInstance == null) {
    //        throw new IllegalStateException("Keycloak instance is not initialized.");
    //    }
        
    //    return keycloakInstance.tokenManager();
    //});
    
    /**
     * Basic constructor.
     */
    public KeycloakAuthentication() {
        // Empty by design
    }

    /**
     * @param username the username to set
     */
    public KeycloakAuthentication setUsername(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * @param password the password to set
     */
    public KeycloakAuthentication setPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * @param clientId the clientId to set
     */
    public KeycloakAuthentication setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
    
    /**
     * @param clientSecret the clientSecret to set
     */
    public KeycloakAuthentication setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
    
    /**
     * @param keycloakAuthenticationURI the keycloakAuthenticationURI to set
     */
    public KeycloakAuthentication setKeycloakAuthenticationURI(String keycloakAuthenticationURI) {
        this.keycloakAuthenticationURI = keycloakAuthenticationURI;
        return this;
    }
    
    /**
     * @param keycloakRealmName the keycloakRealmName to set
     */
    public KeycloakAuthentication setKeycloakRealmName(String keycloakRealmName) {
        this.keycloakRealmName = keycloakRealmName;
        return this;
    }
    
    /**
     *  Initialize Keycloak singleton instance.
     */
    public KeycloakAuthentication initialize() {
        validateParameters();
		
		if (this.keycloakInstance == null) {
        	// Not yet available. Create a new instance. Ensure only one is created.
            this.lock.lock();
            
            try {
                if (this.keycloakInstance == null) {
                    this.keycloakInstance = Keycloak.getInstance(
                            keycloakAuthenticationURI,
                            keycloakRealmName,
                            username,
                            password,
                            clientId,
                            clientSecret
                    );
                }
            } finally {
                this.lock.unlock();
            }
        }
		
		// Create token manager
		this.tokenManager = this.keycloakInstance.tokenManager();
		
		return this;
    }
    
    /**
     *  Parameter validation.
     */
    private void validateParameters() {
        if (username == null || password == null || clientId == null || clientSecret == null || keycloakAuthenticationURI == null || keycloakRealmName == null) {
            throw new IllegalArgumentException("Missing required authentication parameters.");
        }
    }
    
    /**
     * Returns an authentication token.
     * 
     * @return the authentication token as a string.
     */
    public String authenticate() throws HTTPException {
    	//validateParameters();
        //initializeKeycloak();
		if (!tokenIssued) {
			// Double checked locking
			this.lock.lock();
			
			// Retrieve access token
			try {
				String token = this.tokenManager.grantToken().getToken();
				tokenIssued = true;
				
				return token;
			} catch (Exception e) {
				//System.out.println("KC auth failed: " + e.getMessage());
				//e.printStackTrace();
				return null;
				//return this.tokenManager.getAccessTokenString();
				//throw new HTTPException("Keycloak authentication failed. Cause: " + e.getMessage(), e);
			} finally {
				this.lock.unlock();
			}
		} else {
			return this.tokenManager.getAccessTokenString();
		}
    }
    
    /**
     * Refreshes an authentication token.
     * 
     * @return a refreshed access token
     */
    public String refreshToken() {
    	//validateParameters();
        //initializeKeycloak();
        
        // Refresh access token
        try {
            return this.tokenManager.refreshToken().getToken();
        } catch (Exception e) {
            throw new HTTPException("Failed to refresh Keycloak token. Cause: " + e.getMessage(), e);
        }
    }
}
