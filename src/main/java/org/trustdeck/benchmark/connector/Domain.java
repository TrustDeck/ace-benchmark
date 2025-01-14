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
package org.trustdeck.benchmark.connector;

/**
 * Generic domain interface.
 * 
 * @author Fabian Prasser
 */
public interface Domain {

    /**
     * Name
     * @return
     */
    String getName();

    /**
     * Name
     * @param name
     */
    void setName(String name);

    /**
     * Name
     * @param name
     * @return
     */
    Domain withName(String name);

    /**
     * Prefix
     * @return
     */
    String getPrefix();

    /**
     * Prefix
     * @param prefix
     */
    void setPrefix(String prefix);

    /**
     * Prefix
     * @param prefix
     * @return
     */
    Domain withPrefix(String prefix);

    /**
     * Validity
     * @return
     */
    String getValidFrom();

    /**
     * Validity
     * @param validFrom
     */
    void setValidFrom(String validFrom);

    /**
     * Validity
     * @param validFrom
     * @return
     */
    Domain withValidFrom(String validFrom);

    /**
     * Validity
     * @return
     */
    String getValidityTime();

    /**
     * Validity
     * @param validityTime
     */
    void setValidityTime(String validityTime);

    /**
     * Validity
     * @param validityTime
     * @return
     */
    Domain withValidityTime(String validityTime);

    /**
     * Validity
     * @return
     */
    String getDescription();

    /**
     * Validity
     * @param description
     */
    void setDescription(String description);

    /**
     * Validity
     * @param description
     * @return
     */
    Domain withDescription(String description);
}
