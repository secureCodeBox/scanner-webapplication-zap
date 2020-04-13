package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ZapAuthenticationMethod {
    @JsonProperty("form-based")
    FormBased("formBasedAuthentication"),
    @JsonProperty("script-based")
    ScriptBased("scriptBasedAuthentication");

    /**
     * String reference used in the ZAP API.
     *
     * See all at your local zap api:
     * http://zap-address/JSON/authentication/view/getSupportedAuthenticationMethods/
     */
    private final String zapReference;

    ZapAuthenticationMethod(String zapReference) {
        this.zapReference = zapReference;
    }

    @Override
    public String toString() {
        return this.zapReference;
    }
}
