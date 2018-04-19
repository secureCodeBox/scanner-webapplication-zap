package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import io.securecodebox.zap.service.engine.model.Variables;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZapSpiderTask extends ExternalTask {
    @JsonProperty("variables")
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

    public String getSpiderExcludeDuplicates() {
        return getValue(variables.getSpiderExcludeDuplicates());
    }

    public String getSpiderApiSpecUrl() {
        return getValue(variables.getSpiderApiSpecUrl());
    }

    public String getSpiderExcludeRegexes() {
        return getValue(variables.getSpiderExcludeRegexes());
    }

    public String getSpiderIncludeRegexes() {
        return getValue(variables.getSpiderIncludeRegexes());
    }

    public int getSpiderMaxDepth() {
        try {
            return Integer.parseInt(getValue(variables.getSpiderMaxDepth()));
        } catch (NumberFormatException ignored) {
            return 1;
        }
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
