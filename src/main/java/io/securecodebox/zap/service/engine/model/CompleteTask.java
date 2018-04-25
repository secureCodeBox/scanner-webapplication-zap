package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;


/**
 * DTO representing the "complete" step of Camunda's REST API.
 *
 * @see <a href="https://docs.camunda.org/manual/7.8/reference/rest/external-task/post-complete/">Camunda: Complete External Task</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class CompleteTask {

    private String scannerId;
    private String jobId;
    private String scannerType;
    private List<Finding> findings;
    private String rawFindings;

}
