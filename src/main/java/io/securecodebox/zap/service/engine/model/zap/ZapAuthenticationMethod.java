package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ZapAuthenticationMethod {
    @JsonProperty("form-based")
    FormBased,
    @JsonProperty("script-based")
    ScriptBased,
    @JsonProperty("csrf-script")
    CsrfLoginScript;
}
