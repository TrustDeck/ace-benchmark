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

/**
 * This class represents a general error state for the benchmark driver.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class HTTPException extends RuntimeException {
    
    /** SVUID */
    private static final long serialVersionUID = 2548768954338424491L;
    
    /** Status code. */
    private int statusCode = 0;
    
    /**
     * Creates a new instance.
     * @param message the exception's message
     */
    public HTTPException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     * @param message the exception's message
     * @param statusCode the exception's status code 
     */
    public HTTPException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Creates a new instance.
     * @param message the exception's message
     * @param wrappedException the exception that was initially thrown
     */
    public HTTPException(String message, Exception wrappedException) {
        super(message, wrappedException);
    }

    /**
     * Retrieves the status code from this exception.
     * @return the exception's status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}
