package io.securecodebox.zap.service.engine.model.zap;

import io.securecodebox.zap.service.engine.model.ExternalTask;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import io.securecodebox.zap.service.engine.model.Variables;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ZapScannerTask extends ExternalTask {

    private Variables variables;

    public boolean getAuthentication() {
        return Boolean.parseBoolean(getValue(variables.getAuthentication()));
    }

    public String getLoggedInIndicator() {
        return getValue(variables.getLoggedInIndicator());
    }

    public String getLoggedOutIndicator() {
        return getValue(variables.getLoggedOutIndicator());
    }

    public String getLoginPassword() {
        return getValue(variables.getLoginPassword());
    }

    public String getLoginSite() {
        return getValue(variables.getLoginSite());
    }

    public String getLoginUser() {
        return getValue(variables.getLoginUser());
    }

    public String getPasswordFieldId() {
        return getValue(variables.getPasswordFieldId());
    }

    public String getScannerExcludeRegexes() {
        return getValue(variables.getScannerExcludeRegexes());
    }

    public String getScannerIncludeRegexes() {
        return getValue(variables.getScannerIncludeRegexes());
    }

    public String getScannerTargetUrl() {
        return getValue(variables.getScannerTargetUrl());
    }

    public String getSpiderResult() {
        return getValue(variables.getSpiderResult());
    }

    public String getTargetUrl() {
        return getValue(variables.getTargetUrl());
    }

    public String getUsernameFieldId() {
        return getValue(variables.getUsernameFieldId());
    }

    public String getCsrfTokenId() {
        return getValue(variables.getCsrfTokenId());
    }


    private static String getValue(ProcessVariable var) {
        return (var != null && var.getValue() != null) ? var.getValue() : "";
    }
}
