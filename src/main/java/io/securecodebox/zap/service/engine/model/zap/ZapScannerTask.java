package io.securecodebox.zap.service.engine.model.zap;

import io.securecodebox.zap.service.engine.model.ExternalTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapScannerTask extends ExternalTask {
    private ZapScannerFetchVariables variables;

    public boolean getAuthentication() {
        return (variables.getAuthentication() != null && variables.getAuthentication().getValue() != null && !variables.getAuthentication().getValue().isEmpty()) && Boolean.parseBoolean(variables.getAuthentication().getValue());
    }

    public String getLoggedInIndicator() {
        return (variables.getLoggedInIndicator() != null && variables.getLoggedInIndicator().getValue() != null) ? variables.getLoggedInIndicator().getValue() : "";
    }

    public String getLoggedOutIndicator() {
        return (variables.getLoggedOutIndicator() != null && variables.getLoggedOutIndicator().getValue() != null) ? variables.getLoggedOutIndicator().getValue() : "";
    }

    public String getLoginPassword() {
        return (variables.getLoginPassword() != null && variables.getLoginPassword().getValue() != null) ? variables.getLoginPassword().getValue() : "";
    }

    public String getLoginSite() {
        return (variables.getLoginSite() != null && variables.getLoginSite().getValue() != null) ? variables.getLoginSite().getValue() : "";
    }

    public String getLoginUser() {
        return (variables.getLoginUser() != null && variables.getLoginUser().getValue() != null) ? variables.getLoginUser().getValue() : "";
    }

    public String getPasswordFieldId() {
        return (variables.getPasswordFieldId() != null && variables.getPasswordFieldId().getValue() != null) ? variables.getPasswordFieldId().getValue() : "";
    }

    public String getScannerExcludeRegexes() {
        return (variables.getScannerExcludeRegexes() != null && variables.getScannerExcludeRegexes().getValue() != null) ? variables.getScannerExcludeRegexes().getValue() : "";
    }

    public String getScannerIncludeRegexes() {
        return (variables.getScannerIncludeRegexes() != null && variables.getScannerIncludeRegexes().getValue() != null) ? variables.getScannerIncludeRegexes().getValue() : "";
    }

    public String getScannerTargetUrl() {
        return (variables.getScannerTargetUrl() != null && variables.getScannerTargetUrl().getValue() != null) ? variables.getScannerTargetUrl().getValue() : "";
    }

    public String getSpiderResult() {
        return (variables.getSpiderResult() != null && variables.getSpiderResult().getValue() != null) ? variables.getSpiderResult().getValue() : "";
    }

    public String getTargetUrl() {
        return (variables.getTargetUrl() != null && variables.getTargetUrl().getValue() != null) ? variables.getTargetUrl().getValue() : "";
    }

    public String getUsernameFieldId() {
        return (variables.getUsernameFieldId() != null && variables.getUsernameFieldId().getValue() != null) ? variables.getUsernameFieldId().getValue() : "";
    }

    public String getCsrfTokenId() {
        return (variables.getCsrfTokenId() != null && variables.getCsrfTokenId().getValue() != null) ? variables.getCsrfTokenId().getValue() : "";
    }
}
