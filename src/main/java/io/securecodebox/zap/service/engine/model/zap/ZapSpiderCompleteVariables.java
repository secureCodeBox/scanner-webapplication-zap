package io.securecodebox.zap.service.engine.model.zap;

import io.securecodebox.zap.service.engine.model.CompleteVariables;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapSpiderCompleteVariables extends CompleteVariables {
    private ProcessVariable spiderMicroserviceId;
    private ProcessVariable spiderResult;

    public ProcessVariable getSpiderMicroserviceId() {
        return spiderMicroserviceId;
    }

    public void setSpiderMicroserviceId(ProcessVariable spiderMicroserviceId) {
        this.spiderMicroserviceId = spiderMicroserviceId;
    }

    public ProcessVariable getSpiderResult() {
        return spiderResult;
    }

    public void setSpiderResult(ProcessVariable spiderResult) {
        this.spiderResult = spiderResult;
    }
}
