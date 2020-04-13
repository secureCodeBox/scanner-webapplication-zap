package io.securecodebox.zap.service.zap.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ScriptEngines {
    OracleNashorn("Oracle Nashorn");

    private final String zapReference;

    @Override
    public String toString() {
        return this.zapReference;
    }
}
