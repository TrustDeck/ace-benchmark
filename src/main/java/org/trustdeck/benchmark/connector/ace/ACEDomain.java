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

import org.trustdeck.benchmark.connector.Domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents a domain.
 * 
 * @author Felix Wirth
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "prefix", "validFrom", "validityTime", "description" })
public class ACEDomain implements Domain {
	
	/** The domain's . */
    @JsonProperty("name")
    private String name;
    
    /** The domain's prefix. */
    @JsonProperty("prefix")
    private String prefix;
    
    /** The domain's valid from date-time. */
    @JsonProperty("validFrom")
    private String validFrom;
    
    /** The domain's validity period. */
    @JsonProperty("validityTime")
    private String validityTime;
    
    /** The domain's description. */
    @JsonProperty("description")
    private String description;

    /**
     * Creates a new instance.
     */
    public ACEDomain() {
    	// Empty by design
    }

    /**
     * Creates a new domain object.
     *
     * @param validityTime
     * @param prefix
     * @param name
     * @param description
     * @param validFrom
     */
    public ACEDomain(String name,
                  String prefix,
                  String validFrom,
                  String validityTime,
                  String description) {
        super();
        this.name = name;
        this.prefix = prefix;
        this.validFrom = validFrom;
        this.validityTime = validityTime;
        this.description = description;
    }

    /**
     * Creates a new instance.
     * 
     * @param name
     * @param prefix
     */
    public ACEDomain(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    @Override
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Domain withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    @Override
    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Domain withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    @JsonProperty("validFrom")
    public String getValidFrom() {
        return validFrom;
    }

    @Override
    @JsonProperty("validFrom")
    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public Domain withValidFrom(String validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    @Override
    @JsonProperty("validityTime")
    public String getValidityTime() {
        return validityTime;
    }

    @Override
    @JsonProperty("validityTime")
    public void setValidityTime(String validityTime) {
        this.validityTime = validityTime;
    }

    @Override
    public Domain withValidityTime(String validityTime) {
        this.validityTime = validityTime;
        return this;
    }

    @Override
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @Override
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Domain withDescription(String description) {
        this.description = description;
        return this;
    }
}
