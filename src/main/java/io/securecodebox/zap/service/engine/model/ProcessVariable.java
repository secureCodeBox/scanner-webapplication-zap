package io.securecodebox.zap.service.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessVariable {
    private String type;
    private String value;
    private Object valueInfo;

    public ProcessVariable() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Object getValueInfo() {
        return valueInfo;
    }

    public void setValueInfo(Object valueInfo) {
        this.valueInfo = valueInfo;
    }

    public String getValue() {
        return (value != null) ? value : "";
    }
}
