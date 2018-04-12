package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.securecodebox.zap.service.engine.model.FetchVariables;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class ZapSpiderFetchVariables extends FetchVariables {
    public static final List<String> TASK_VARIABLES = Collections.unmodifiableList(Arrays.asList("PROCESS_AUTOMATED", "ZAP_SPIDER_TARGET_URL", "spiderApiSpecUrl", "spiderIncludeRegexes", "spiderExcludeRegexes", "spiderExcludeDuplicates", "spiderMaxDepth", "authentication", "loginSite", "loginUser", "usernameFieldId", "loginPassword", "passwordFieldId", "loggedInIndicator", "loggedOutIndicator", "csrfTokenId"));

    private ProcessVariable authentication;
    private ProcessVariable loggedInIndicator;
    private ProcessVariable loggedOutIndicator;
    private ProcessVariable loginPassword;
    private ProcessVariable loginSite;

    public ZapSpiderFetchVariables() {
    }

    private ProcessVariable loginUser;
    private ProcessVariable passwordFieldId;
    private ProcessVariable spiderExcludeDuplicates;
    private ProcessVariable spiderApiSpecUrl;
    private ProcessVariable spiderExcludeRegexes;
    private ProcessVariable spiderIncludeRegexes;
    private ProcessVariable spiderMaxDepth;

    public ProcessVariable getZAP_SPIDER_TARGET_URL() {
        return ZAP_SPIDER_TARGET_URL;
    }

    public void setZAP_SPIDER_TARGET_URL(ProcessVariable ZAP_SPIDER_TARGET_URL) {
        this.ZAP_SPIDER_TARGET_URL = ZAP_SPIDER_TARGET_URL;
    }

    @JsonProperty("ZAP_SPIDER_TARGET_URL")
    private ProcessVariable ZAP_SPIDER_TARGET_URL;
    private ProcessVariable PROCESS_AUTOMATED;

    public ProcessVariable getPROCESS_AUTOMATED() {
        return PROCESS_AUTOMATED;
    }

    public void setPROCESS_AUTOMATED(ProcessVariable PROCESS_AUTOMATED) {

        this.PROCESS_AUTOMATED = PROCESS_AUTOMATED;
    }

    private ProcessVariable usernameFieldId;
    private ProcessVariable csrfTokenId;

    public static List<String> getTaskVariables() {
        return TASK_VARIABLES;
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

    public ProcessVariable getTargetUrl() {
        return getZAP_SPIDER_TARGET_URL();
    }

    public void setTargetUrl(ProcessVariable targetUrl) {
        setZAP_SPIDER_TARGET_URL(targetUrl);
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
}
