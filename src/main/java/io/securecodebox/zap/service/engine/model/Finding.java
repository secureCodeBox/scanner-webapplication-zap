/*
 *
 *  *
 *  * SecureCodeBox (SCB)
 *  * Copyright 2015-2018 iteratec GmbH
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.util.*;

/**
 * A single result of a scan job
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@ToString
public class Finding {

    /**
     * Id of the finding. Must be unique for every finding.
     */
    private UUID id;
    private String name;
    private String description;
    @JsonAlias("alert")
    private String category;
    @JsonProperty(value = "osi_layer", required = false)
    private String osiLayer = "APPLICATION";
    @JsonAlias("risk")
    private String severity;
    private Reference reference;
    @JsonAlias("solution")
    private String hint;
    @JsonAlias("url")
    private String location;
    private Map<String, Object> attributes = new HashMap<>();

    @JsonAnySetter
    public void handleUnknownProperty(String key, Object value){
        attributes.put(key, value);
    }

    @JsonProperty("id")
    public UUID getId() {
        if (id == null){
            id = UUID.randomUUID();
        }
        return id;
    }

    public String getSeverity() {
        return severity != null ? severity.toUpperCase() : "INFORMATIONAL";
    }
}
