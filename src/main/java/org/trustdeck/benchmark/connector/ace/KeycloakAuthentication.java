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
    
    /** The token manager handles Keycloak's access and refresh tokens. */
    private TokenManager tokenmanager;
    
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
     * Returns an authentication token.
     * 
     * @return the authentication token as a string.
     */
    public String authenticate() throws HTTPException {
        // Check that all necessary parameters are set
        if (username == null || password == null || clientId == null || clientSecret == null || keycloakAuthenticationURI == null || keycloakRealmName == null) {
            throw new NullPointerException("All parameters must not be null!");
        }
        
        // Retrieve access token
        Keycloak instance = Keycloak.getInstance(keycloakAuthenticationURI, keycloakRealmName, username, password, clientId, clientSecret);
        tokenmanager = instance.tokenManager();
        String accessToken = tokenmanager.getAccessTokenString();

    	return accessToken;
    }
    
    /**
     * Refreshes an authentication token.
     * 
     * @return a refreshed access token
     */
    public String refreshToken() {
    	return tokenmanager.refreshToken().getToken();
    }
}
