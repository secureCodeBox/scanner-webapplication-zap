package io.securecodebox.zap.zap.model;


/**
 * Represents a single API call to the OWASP ZAP API.
 */
public interface ZapApiCall {
    /**
     * Gets the url part.
     */
    String getUrl();
}
