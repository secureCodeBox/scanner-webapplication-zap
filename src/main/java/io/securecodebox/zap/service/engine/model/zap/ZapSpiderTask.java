package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class ZapSpiderTask extends ExternalTask {

    public void setVariables(ZapSpiderFetchVariables variables) {
        this.variables = variables;
    }

    @JsonProperty("variables")
    private ZapSpiderFetchVariables variables;

    public ZapSpiderFetchVariables getVariables() {
        return variables;
    }

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

    public String getSpiderExcludeDuplicates() {
        return (variables.getSpiderExcludeDuplicates() != null && variables.getSpiderExcludeDuplicates().getValue() != null) ? variables.getSpiderExcludeDuplicates().getValue() : "";
    }

    public String getSpiderApiSpecUrl() {
        return variables.getSpiderApiSpecUrl() != null && variables.getSpiderApiSpecUrl().getValue() != null ? variables.getSpiderApiSpecUrl().getValue() : "";
    }

    public String getSpiderExcludeRegexes() {
        return (variables.getSpiderExcludeRegexes() != null && variables.getSpiderExcludeRegexes().getValue() != null) ? variables.getSpiderExcludeRegexes().getValue() : "";
    }

    public String getSpiderIncludeRegexes() {
        return (variables.getSpiderIncludeRegexes() != null && variables.getSpiderIncludeRegexes().getValue() != null) ? variables.getSpiderIncludeRegexes().getValue() : "";
    }

    public int getSpiderMaxDepth() {
        return (variables.getSpiderMaxDepth() != null && variables.getSpiderMaxDepth().getValue() != null && !variables.getSpiderMaxDepth().getValue().isEmpty()) ? Integer.parseInt(variables.getSpiderMaxDepth().getValue()) : 1;
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
