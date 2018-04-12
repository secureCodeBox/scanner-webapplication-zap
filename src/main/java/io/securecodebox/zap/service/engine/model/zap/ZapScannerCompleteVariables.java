package io.securecodebox.zap.service.engine.model.zap;

import io.securecodebox.zap.service.engine.model.CompleteVariables;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapScannerCompleteVariables extends CompleteVariables {
    private ProcessVariable scannerMicroserviceId;
    private ProcessVariable scannerResult;

    public ProcessVariable getScannerMicroserviceId() {
        return scannerMicroserviceId;
    }

    public void setScannerMicroserviceId(ProcessVariable scannerMicroserviceId) {
        this.scannerMicroserviceId = scannerMicroserviceId;
    }

    public ProcessVariable getScannerResult() {
        return scannerResult;
    }

    public void setScannerResult(ProcessVariable scannerResult) {
        this.scannerResult = scannerResult;
    }
}
