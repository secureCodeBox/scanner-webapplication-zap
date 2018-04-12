package io.securecodebox.zap.zap.model.scanner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


/**
 * Represents a single alert result returned from the OWASP ZAP scanner API. Each alert is technical identified by id.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    private String other;
    private String param;
    private String alert;
    private String evidence;
    private String confidence;
    private String solution;
    private String url;
    private String reference;
    private String id;
    private String risk;
    private String description;
    private String attack;
    private String messageId;
    private String cweid;
    private String wascid;
}
