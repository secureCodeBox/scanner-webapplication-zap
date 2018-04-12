package io.securecodebox.zap.service.engine.model.zap;

import io.securecodebox.zap.service.engine.model.FetchVariables;
import io.securecodebox.zap.service.engine.model.ProcessVariable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ZapScannerFetchVariables extends FetchVariables {
    public static final List<String> TASK_VARIABLES = Collections.unmodifiableList(Arrays.asList("processUuid", "targetUrl", "scannerTargetUrl", "scannerIncludeRegexes", "scannerExcludeRegexes", "spiderResult", "authentication", "loginSite", "loginUser", "usernameFieldId", "loginPassword", "passwordFieldId", "loggedInIndicator", "loggedOutIndicator", "csrfTokenId"));

    private ProcessVariable authentication;
    private ProcessVariable loggedInIndicator;
    private ProcessVariable loggedOutIndicator;
    private ProcessVariable loginPassword;
    private ProcessVariable loginSite;
    private ProcessVariable loginUser;
    private ProcessVariable passwordFieldId;
    private ProcessVariable scannerExcludeRegexes;
    private ProcessVariable scannerIncludeRegexes;
    private ProcessVariable scannerTargetUrl;
    private ProcessVariable spiderResult;
    private ProcessVariable targetUrl;
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
}
