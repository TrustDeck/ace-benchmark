package org.trustdeck.benchmark.connector.ace;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.trustdeck.benchmark.Main;
import org.yaml.snakeyaml.Yaml;

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
    public synchronized void initialize() {
//    									String username, String password, String clientId,
//                                        String clientSecret, String keycloakAuthenticationURI,
//                                        String keycloakRealmName) {
        if (this.keycloakInstance != null) {
            throw new IllegalStateException("ACETokenManager is already initialized.");
        }
        
        // Extract the tool configuration from the loaded configuration file
        Yaml yaml = new Yaml();
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.yaml");
        Map<String, Object> yamlConfig = yaml.load(inputStream);
        @SuppressWarnings("unchecked")
        Map<String, String> toolConfig = (Map<String, String>) yamlConfig.get("ace");
        
        String authClientId = toolConfig.get("clientId");
        String authClientSecret = toolConfig.get("clientSecret");
        String authKeycloakURI = toolConfig.get("keycloakAuthUri");
        String authKeycloakRealmName = toolConfig.get("keycloakRealmName");
        String authUsername = toolConfig.get("username");
        String authPassword = toolConfig.get("password");

        // Create the Keycloak instance
        this.keycloakInstance = Keycloak.getInstance(
        		authKeycloakURI,
        		authKeycloakRealmName,
        		authUsername,
        		authPassword,
        		authClientId,
        		authClientSecret
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
        TokenManager tokenManager = keycloakInstance.tokenManager();
        long expiresIn = 0;
        
    	try {
            AccessTokenResponse response = tokenManager.refreshToken();
            this.accessToken = response.getToken();
            expiresIn = response.getExpiresIn();
            // Reduce time to next refresh by 15 seconds. Convert seconds to milliseconds.
            //this.tokenExpiryTime = System.currentTimeMillis() + ((response.getExpiresIn() - 15) * 1000L); 
        } catch (NullPointerException e) {
        	// The keycloak code contains a bug: we need to catch this NPE here when there was no token previously generated
            AccessTokenResponse response = tokenManager.grantToken();
            this.accessToken = response.getToken();
            expiresIn = response.getExpiresIn();
            // Reduce time to next refresh by 15 seconds. Convert seconds to milliseconds.
            //this.tokenExpiryTime = System.currentTimeMillis() + ((response.getExpiresIn() - 15) * 1000L);
    	} catch (Exception f) {
            throw new RuntimeException("Failed to refresh the token: " + f.getMessage(), f);
        }
    	
    	// Reduce time to next refresh by 15 seconds. Convert seconds to milliseconds.
        this.tokenExpiryTime = System.currentTimeMillis() + ((expiresIn - 15) * 1000L);
    }
}
