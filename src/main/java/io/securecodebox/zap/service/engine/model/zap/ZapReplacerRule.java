package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonPropertyOrder(alphabetic = true)
@Data
public class ZapReplacerRule {

    // "matchType":"RESP_HEADER",
    @JsonProperty("matchType")
    private String matchType;

    // "description":"Remove CSP",
    @JsonProperty("description")
    private String description;

    // "matchString":"Content-Security Policy",
    @JsonProperty("matchString")
    private String matchString;

    // "initiators":"",
    @JsonProperty("initiators")
    private String initiators;

    // "matchRegex":"false",
    @JsonProperty("matchRegex")
    private String matchRegex;

    // "replacement":"",
    @JsonProperty("replacement")
    private String replacement;

    // "enabled":"true"
    @JsonProperty("enabled")
    private String enabled;


}
