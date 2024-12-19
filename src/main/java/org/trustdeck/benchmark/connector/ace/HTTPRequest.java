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

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This class is used to build and execute HTTP requests.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class HTTPRequest {
    
    /**
     * Type of HTTP request.
     */
    public enum HTTPRequestType {
                                 GET,
                                 POST,
                                 PUT,
                                 DELETE
    };
    
    /**
     * MediaType of the HTTP request.
     */
    public enum HTTPMediaType {
        TEXT_PLAIN,
        APPLICATION_JSON
    };
    
    /** The web client object needed to generate and send requests. */
    private final Client client;
    
    /** The server to where the request should go to. */
    private final URI server;
    
    /** The path on the server where the request should go to. */
    private final String path;
    
    /** The type of the request (GET, POST, ...). */
    private final HTTPRequestType requestType;
    
    /** The authentication token needed to authenticate the request. */
    private final String authToken;
    
    /** The request body if needed. */
    private final String body;
    
    /** The request body's mediaType if applicable. */
    private final HTTPMediaType bodyMediaType;
    
    /** Represents the request parameters. */
    private final Map<String, String> parameters;
    
    /**
     * Creates a new instance
     * @param server
     * @param path
     * @param requestType
     * @param authToken
     * @param parameters
     */
    public HTTPRequest(URI server,
                       String path,
                       HTTPRequestType requestType,
                       String authToken,
                       Map<String, String> parameters) {
        this(server, path, requestType, authToken, null, null, parameters);
    }

    /**
     * Creates a new instance.
     * 
     * @param server
     * @param path
     * @param requestType
     * @param authToken
     * @param body
     * @param bodyMediaType
     */
    public HTTPRequest(URI server, String path, HTTPRequestType requestType, String authToken, String body, HTTPMediaType bodyMediaType) {
    	this(server, path, requestType, authToken, body, 
        		bodyMediaType != null ? bodyMediaType : 
        			(requestType == HTTPRequestType.POST || requestType == HTTPRequestType.PUT ? HTTPMediaType.APPLICATION_JSON : HTTPMediaType.TEXT_PLAIN),
        		null);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param server
     * @param path
     * @param requestType
     * @param authToken
     * @param body
     * @param bodyMediaType
     * @param parameters
     */
    public HTTPRequest(URI server, String path, HTTPRequestType requestType, String authToken, String body, HTTPMediaType bodyMediaType, Map<String, String> parameters) {
        this.client = HTTPClientManager.getClient();
        this.server = server;
        this.path = path;
        this.requestType = requestType;
        this.authToken = authToken;
        this.body = body;
        this.bodyMediaType =  bodyMediaType != null ? bodyMediaType :
                (requestType == HTTPRequestType.POST || requestType == HTTPRequestType.PUT) ? 
                        HTTPMediaType.APPLICATION_JSON : HTTPMediaType.TEXT_PLAIN;
        this.parameters = parameters;
    }
    
    /**
     * Execute the request.
     * 
     * @return the request's response as a string
     */
    public String execute() {

        WebTarget target = this.client.target(server).path(path);

        if (parameters != null && !parameters.isEmpty()) {
            for (Entry<String, String> parameter : parameters.entrySet()) {
                target = target.queryParam(parameter.getKey(), parameter.getValue());
            }
        }

        // Build request
        Builder builder = target.request();
        builder.header("Authorization", String.format("Bearer %s", authToken));

        // Handle media type
        String type = null;

        switch (bodyMediaType) {
        case APPLICATION_JSON:
            type = MediaType.APPLICATION_JSON;
            break;
        case TEXT_PLAIN:
            type = MediaType.TEXT_PLAIN;
            break;
        default:
            throw new IllegalStateException("Unknown media type");
        }

        // Execute request
        Response response = null;

        switch (requestType) {
        case GET:
            response = builder.get(Response.class);
            break;
        case POST:
            if (body == null || type == null) {
                throw new IllegalArgumentException("Body and media type must not be null.");
            }
            response = builder.post(Entity.entity(body, type));
            break;
        case PUT:
            if (body == null || type == null) {
                throw new IllegalArgumentException("Body and media type must not be null.");
            }
            response = builder.put(Entity.entity(body, type));
            break;
        case DELETE:
            response = builder.delete(Response.class);
            break;
        default:
            throw new IllegalStateException("Unknown request type.");
        }

        // Read and return the response entity
        return response.readEntity(String.class);
    }
}
