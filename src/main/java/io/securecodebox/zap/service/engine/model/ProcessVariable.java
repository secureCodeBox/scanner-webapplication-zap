package io.securecodebox.zap.service.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ProcessVariable {
    private String type;
    private String value;
    private Object valueInfo;


    public String getValue() {
        return (value != null) ? value : "";
    }
}
