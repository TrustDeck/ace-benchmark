package org.trustdeck.benchmark.connector.ace;

import java.util.concurrent.locks.ReentrantLock;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;

public class ACETokenManager {

    private static ACETokenManager instance;
    private static final ReentrantLock lock = new ReentrantLock();

    private volatile Keycloak keycloakInstance;
    private volatile String accessToken;
    private long tokenExpiryTime; // Expiry time in milliseconds

    // Private constructor to prevent direct instantiation
    private ACETokenManager() {}

    // Get the singleton instance
    public static ACETokenManager getInstance() {
        if (instance == null) {
            synchronized (ACETokenManager.class) {
                if (instance == null) {
                    instance = new ACETokenManager();
                }
            }
        }
        return instance;
    }

    // Initialize the ACETokenManager with Keycloak parameters
    public synchronized void initialize(String username, String password, String clientId,
                                        String clientSecret, String keycloakAuthenticationURI,
                                        String keycloakRealmName) {
        if (this.keycloakInstance != null) {
            throw new IllegalStateException("ACETokenManager is already initialized.");
        }

        // Create the Keycloak instance
        this.keycloakInstance = Keycloak.getInstance(
                keycloakAuthenticationURI,
                keycloakRealmName,
                username,
                password,
                clientId,
                clientSecret
        );
    }

    // Get a valid token
    public String getToken() {
        if (keycloakInstance == null) {
            throw new IllegalStateException("ACETokenManager is not initialized.");
        }

        // Check if the token is still valid
        if (isTokenValid()) {
            return accessToken;
        }

        // Refresh the token in a thread-safe way
        lock.lock();
        try {
            if (!isTokenValid()) { // Double-check inside the lock
                refreshAccessToken();
            }
            return accessToken;
        } finally {
            lock.unlock();
        }
    }

    // Check if the current token is still valid
    private boolean isTokenValid() {
        return accessToken != null && System.currentTimeMillis() < tokenExpiryTime;
    }

    // Refresh the access token
    private void refreshAccessToken() {
        try {
            TokenManager tokenManager = keycloakInstance.tokenManager();
            AccessTokenResponse response = tokenManager.refreshToken();
            this.accessToken = response.getToken();
            // Reduce time to next refresh by 15 seconds. Convert seconds to milliseconds.
            this.tokenExpiryTime = System.currentTimeMillis() + ((response.getExpiresIn() - 15) * 1000L); 
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh the token: " + e.getMessage(), e);
        }
    }
}
