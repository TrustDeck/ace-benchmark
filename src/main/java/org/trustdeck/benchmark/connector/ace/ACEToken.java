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

import org.trustdeck.benchmark.connector.Token;

/**
 * Authentication token.
 * 
 * @author Fabian Prasser
 */
public class ACEToken implements Token<String> {
   
    /** Authentication token. */
    private String token;
    
    /**
     * Create a new instance.
     * 
     * @param token
     */
    public ACEToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return this.token;
    }
}
