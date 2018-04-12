package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchVariables {
    protected ProcessVariable processUuid;

    public ProcessVariable getProcessUuid() {
        return processUuid;
    }

    public void setProcessUuid(ProcessVariable processUuid) {
        this.processUuid = processUuid;
    }
}
