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

import jakarta.ws.rs.core.Response;

/**
 * Utility class.
 * 
 * @author Armin Müller, Felix N. Wirth, and Fabian Prasser
 */
public class HTTPUtil {

    /**
     * Method to throw an exception in case of an unreadable response.
     * 
     * @param response the original response from the request
     * @throws HTTPException
     */
    public static void raiseException(Response response) throws HTTPException {
        String body = "Body not readable";
        
        try {
            body = response.readEntity(String.class);
        } catch (Exception e) {
            // Ignore all other exceptions
        }
        
        throw new HTTPException(String.format("Error executing HTTP request with return code %s and body %s",
                                response.getStatus(), body));
    }
}
