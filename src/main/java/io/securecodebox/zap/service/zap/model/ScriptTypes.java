package io.securecodebox.zap.service.zap.model;

import lombok.AllArgsConstructor;

/**
 * Script Types supported by ZAP
 */
@AllArgsConstructor
public enum ScriptTypes {
    FuzzerHTTPProcessor("httpfuzzerprocessor"),
    FuzzerWebSocetProcessor("websocketfuzzerprocessor"),
    SessionManagment("session"),
    HTTPSender("httpsender"),
    StandAlone("standalone"),
    PayloadProcessor("payloadprocessor"),
    ActiveRules("active"),
    PayloadGenerator("payloadgenerator"),
    PassiveRules("passive"),
    Extender("extender"),
    Proxy("proxy"),
    Selenium("selenium"),
    WebSocketSender("websocketsender"),
    HUD("hud"),
    ScriptInputVector("variant"),
    Targeted("targeted"),
    WebSocketPassiveRules("websocketpassive"),
    Authentication("authentication");

    private final String zapReference;

    @Override
    public String toString() {
        return this.zapReference;
    }
}
