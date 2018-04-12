package io.securecodebox.zap.zap.model.scanner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


/**
 * List of {@link Alert}s returned for a single OWASP Zap scan by API.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alerts {
    private List<Alert> alerts;

    public List<Alert> getAlerts() {
        return alerts;
    }

    @Override
    public String toString() {
        return "Alerts [alerts=" + (alerts != null ? alerts.subList(0, Math.min(alerts.size(), 10)) : null) + ']';
    }
}
