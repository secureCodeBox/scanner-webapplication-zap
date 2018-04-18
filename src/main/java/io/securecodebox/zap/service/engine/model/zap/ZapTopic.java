package io.securecodebox.zap.service.engine.model.zap;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents all ZAP specific topic names used to identify the external tasks for this service.
 */
@AllArgsConstructor
public enum ZapTopic {
    ZAP_SPIDER("zap_spider"),
    ZAP_SCANNER("zap_scan");

    @Getter
    private String name;

    @Override
    public String toString() {
        return getName();
    }
}
