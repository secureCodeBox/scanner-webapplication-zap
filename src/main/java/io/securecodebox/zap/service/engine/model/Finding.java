package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        return severity != null ? severity.toUpperCase() : "null";
    }
}
