package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.Variables;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Data
@Slf4j
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
        return getValue(variables.getSpiderRawResult());
    }

    public String getTargetUrl() {
        return getValue(variables.getTargetUrl());
    }

    public List<Target> getTargets(){
        String targetString = getValue(variables.getProcessTargets());
        if(targetString != null && !targetString.isEmpty()){
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(targetString,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Target.class));
            }
            catch (IOException e){
                log.error("Cannot parse targets due to reason: " + e + "\nTargets: " + targetString);
            }
        }
        return new LinkedList<>();
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
