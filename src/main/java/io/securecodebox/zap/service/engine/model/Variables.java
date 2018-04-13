package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties()
public class Variables {


    private ProcessVariable lastServiceMessage;
    private ProcessVariable processUuid;
    @JsonProperty("ZAP_AUTHENTICATION")
    private ProcessVariable authentication;
    private ProcessVariable loggedInIndicator;
    private ProcessVariable loggedOutIndicator;
    private ProcessVariable loginPassword;
    private ProcessVariable loginSite;
    private ProcessVariable loginUser;
    private ProcessVariable passwordFieldId;
    @JsonProperty("ZAP_TARGET_URL")
    private ProcessVariable targetUrl;
    private ProcessVariable usernameFieldId;
    private ProcessVariable csrfTokenId;

    private ProcessVariable spiderResult;
    private ProcessVariable spiderMicroserviceId;
    @JsonProperty("ZAP_SPIDER_EXCLUDE_DUPLICATES")
    private ProcessVariable spiderExcludeDuplicates;
    private ProcessVariable spiderApiSpecUrl;
    @JsonProperty("ZAP_SPIDER_EXCLUDE_REGEX")
    private ProcessVariable spiderExcludeRegexes;
    @JsonProperty("ZAP_SPIDER_INCLUDE_REGEX")
    private ProcessVariable spiderIncludeRegexes;
    @JsonProperty("ZAP_SPIDER_MAX_DEPTH")
    private ProcessVariable spiderMaxDepth;
    @JsonProperty("ZAP_SPIDER_TARGET_URL")
    private ProcessVariable spiderTargetUrl;

    private ProcessVariable scannerMicroserviceId;
    private ProcessVariable scannerResult;
    @JsonProperty("ZAP_SCANNER_EXLUDE_REGEXX")
    private ProcessVariable scannerExcludeRegexes;
    @JsonProperty("ZAP_SCANNER_INCLUDE_REGEX")
    private ProcessVariable scannerIncludeRegexes;
    @JsonProperty("ZAP_SCANNER_TARGET_URL")
    private ProcessVariable scannerTargetUrl;

    public ProcessVariable getSpiderMicroserviceId() {
        return spiderMicroserviceId;
    }

    public void setSpiderMicroserviceId(ProcessVariable spiderMicroserviceId) {
        this.spiderMicroserviceId = spiderMicroserviceId;
    }

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

    public ProcessVariable getLastServiceMessage() {
        return lastServiceMessage;
    }

    public void setLastServiceMessage(ProcessVariable lastServiceMessage) {
        this.lastServiceMessage = lastServiceMessage;
    }

    public ProcessVariable getProcessUuid() {
        return processUuid;
    }

    public void setProcessUuid(ProcessVariable processUuid) {
        this.processUuid = processUuid;
    }

    public ProcessVariable getAuthentication() {
        return authentication;
    }

    public void setAuthentication(ProcessVariable authentication) {
        this.authentication = authentication;
    }

    public ProcessVariable getLoggedInIndicator() {
        return loggedInIndicator;
    }

    public void setLoggedInIndicator(ProcessVariable loggedInIndicator) {
        this.loggedInIndicator = loggedInIndicator;
    }

    public ProcessVariable getLoggedOutIndicator() {
        return loggedOutIndicator;
    }

    public void setLoggedOutIndicator(ProcessVariable loggedOutIndicator) {
        this.loggedOutIndicator = loggedOutIndicator;
    }

    public ProcessVariable getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(ProcessVariable loginPassword) {
        this.loginPassword = loginPassword;
    }

    public ProcessVariable getLoginSite() {
        return loginSite;
    }

    public void setLoginSite(ProcessVariable loginSite) {
        this.loginSite = loginSite;
    }

    public ProcessVariable getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(ProcessVariable loginUser) {
        this.loginUser = loginUser;
    }

    public ProcessVariable getPasswordFieldId() {
        return passwordFieldId;
    }

    public void setPasswordFieldId(ProcessVariable passwordFieldId) {
        this.passwordFieldId = passwordFieldId;
    }

    public ProcessVariable getScannerExcludeRegexes() {
        return scannerExcludeRegexes;
    }

    public void setScannerExcludeRegexes(ProcessVariable scannerExcludeRegexes) {
        this.scannerExcludeRegexes = scannerExcludeRegexes;
    }

    public ProcessVariable getScannerIncludeRegexes() {
        return scannerIncludeRegexes;
    }

    public void setScannerIncludeRegexes(ProcessVariable scannerIncludeRegexes) {
        this.scannerIncludeRegexes = scannerIncludeRegexes;
    }

    public ProcessVariable getScannerTargetUrl() {
        return scannerTargetUrl;
    }

    public void setScannerTargetUrl(ProcessVariable scannerTargetUrl) {
        this.scannerTargetUrl = scannerTargetUrl;
    }

    public ProcessVariable getSpiderResult() {
        return spiderResult;
    }

    public void setSpiderResult(ProcessVariable spiderResult) {
        this.spiderResult = spiderResult;
    }

    public ProcessVariable getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(ProcessVariable targetUrl) {
        this.targetUrl = targetUrl;
    }

    public ProcessVariable getUsernameFieldId() {
        return usernameFieldId;
    }

    public void setUsernameFieldId(ProcessVariable usernameFieldId) {
        this.usernameFieldId = usernameFieldId;
    }

    public ProcessVariable getCsrfTokenId() {
        return csrfTokenId;
    }

    public void setCsrfTokenId(ProcessVariable csrfTokenId) {
        this.csrfTokenId = csrfTokenId;
    }

    public ProcessVariable getSpiderExcludeDuplicates() {
        return spiderExcludeDuplicates;
    }

    public void setSpiderExcludeDuplicates(ProcessVariable spiderExcludeDuplicates) {
        this.spiderExcludeDuplicates = spiderExcludeDuplicates;
    }

    public ProcessVariable getSpiderApiSpecUrl() {
        return spiderApiSpecUrl;
    }

    public void setSpiderApiSpecUrl(ProcessVariable spiderApiSpecUrl) {
        this.spiderApiSpecUrl = spiderApiSpecUrl;
    }

    public ProcessVariable getSpiderExcludeRegexes() {
        return spiderExcludeRegexes;
    }

    public void setSpiderExcludeRegexes(ProcessVariable spiderExcludeRegexes) {
        this.spiderExcludeRegexes = spiderExcludeRegexes;
    }

    public ProcessVariable getSpiderIncludeRegexes() {
        return spiderIncludeRegexes;
    }

    public void setSpiderIncludeRegexes(ProcessVariable spiderIncludeRegexes) {
        this.spiderIncludeRegexes = spiderIncludeRegexes;
    }

    public ProcessVariable getSpiderMaxDepth() {
        return spiderMaxDepth;
    }

    public void setSpiderMaxDepth(ProcessVariable spiderMaxDepth) {
        this.spiderMaxDepth = spiderMaxDepth;
    }

    public ProcessVariable getSpiderTargetUrl() {
        return spiderTargetUrl;
    }

    public void setSpiderTargetUrl(ProcessVariable spiderTargetUrl) {
        this.spiderTargetUrl = spiderTargetUrl;
    }

    public static List<String> getVariablesAsStringArray(){

        List<String> result = new LinkedList<>();

        for(Field field : Variables.class.getDeclaredFields()){
            if(field.getAnnotationsByType(JsonProperty.class).length != 0){
                result.add(field.getAnnotation(JsonProperty.class).value());
            }
            else {
                result.add(field.getName());
            }
        }
        return result;
    }
}
