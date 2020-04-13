package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * All Zap Session Management Method which are also supported by the secureCodeBox Orchestration
 */
public enum ZapSessionManagement {
    @JsonProperty("script-based")
    ScriptBased,
    @JsonProperty("cookie-based")
    CookieBased,
}




