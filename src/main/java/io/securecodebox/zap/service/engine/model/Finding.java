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
import io.securecodebox.zap.service.engine.model.zap.ZapFields;
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
    @JsonIgnore
    private UUID uuid;
    private String name;
    private String description;
    private String category;
    @JsonProperty(value = "osi_layer", required = false)
    private String osiLayer;
    @JsonAlias("risk")
    private String severity;
    private Reference reference;
    @JsonAlias("solution")
    private String hint;
    @JsonAlias("url")
    private String location;
    private Map<String, Object> attributes = new HashMap<>();

    //todo: to also remove duplicates from spider, the relevant attributes for the spider must be added here
    private static final List<String> RELEVANT_ATTRIBUTES = Arrays.asList(ZapFields.ZAP_BASE_URL.name(),
            "alert", "attack", "confidence", "evidence", "other", "param", "reliability");

    @JsonAnySetter
    public void handleUnknownProperty(String key, String value){
        attributes.put(key, value);
    }

    @JsonProperty("id")
    public UUID getId() {
        if (uuid == null){
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    public String getSeverity() {
        return severity != null ? severity.toUpperCase() : "INFORMATIONAL";
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null){
            return false;
        }
        if(!(o instanceof Finding)){
            return false;
        }

        for(String s : RELEVANT_ATTRIBUTES){
            if(!equalsOrNull(this.getAttributes().get(s), ((Finding)o).getAttributes().get(s))){
                return false;
            }
        }
        return equalsOrNull(this.getCategory(), ((Finding) o).getCategory()) &&
                equalsOrNull(this.getDescription(), ((Finding) o).getDescription()) &&
                equalsOrNull(this.getHint(), ((Finding) o).getHint()) &&
                equalsOrNull(this.getLocation(), ((Finding) o).getLocation()) &&
                equalsOrNull(this.getName(), ((Finding) o).getName()) &&
                ((this.getReference() != null && ((Finding) o).getReference() != null &&
                        equalsOrNull(this.getReference().getSource(), ((Finding) o).getReference().getSource())) ||
                        (this.getReference() == null && ((Finding) o).getReference() == null)) &&
                equalsOrNull(this.getOsiLayer(), ((Finding) o).getOsiLayer()) &&
                equalsOrNull(this.getSeverity(), ((Finding) o).getSeverity());
    }

    private static boolean equalsOrNull(Object o1, Object o2){
        if(o1 == null){
            return o2 == null;
        }
        else {
            return o2 != null && o1.equals(o2);
        }
    }

    @Override
    public int hashCode() {

        List<Object> objects = new LinkedList<>();
        for(String s : RELEVANT_ATTRIBUTES){
            objects.add(getAttributes().get(s));
        }
        return Objects.hash(name, description, category, osiLayer, severity, reference.getSource(), hint, location, objects);
    }
}
