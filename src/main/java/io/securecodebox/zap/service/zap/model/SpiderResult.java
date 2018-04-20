package io.securecodebox.zap.service.zap.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.zaproxy.clientapi.core.ApiResponseSet;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpiderResult {
    private String statusCode;
    private String statusReason;
    private String method;
    private String messageId;
    private String url;
    private String requestDateTime;
    private long responseTime;
    private String postData;
    private String headers;
    private String queryString;
    private String cookies;


    public SpiderResult() {}

    /**
     * @param response {@code ApiResponseSet} returned from the related ZAP API call
     * @see org.zaproxy.clientapi.core.Alert
     */
    public SpiderResult(ApiResponseSet response) {
        statusCode = response.getStringValue("statusCode");
        statusReason = response.getStringValue("statusReason");
        method = response.getStringValue("method");
        messageId = response.getStringValue("messageId");
        url = response.getStringValue("url");
        requestDateTime = response.getStringValue("requestDateTime");
        responseTime = response.getStringValue("responseTime") != null ? Long.parseLong(response.getStringValue("responseTime")) : -1;
        postData = response.getStringValue("postData");
        headers = response.getStringValue("headers");
        queryString = response.getStringValue("queryString");
        cookies = response.getStringValue("cookies");
    }
}
