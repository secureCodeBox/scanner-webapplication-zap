package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


/**
 * DTO representing the "complete" step of Camunda's REST API.
 *
 * @see <a href="https://docs.camunda.org/manual/7.8/reference/rest/external-task/post-complete/">Camunda: Complete External Task</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteTask {

    private String workerId;
    private Variables variables;
}
