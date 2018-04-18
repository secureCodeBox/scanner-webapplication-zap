package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


/**
 * DTO representing the result of Camunda's REST API.
 * @see <a href="https://docs.camunda.org/manual/7.8/reference/rest/external-task/get/">Get External Task</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalTask {

    private String activityId;
    private String activityInstanceId;
    private String errorMessage;
    private String executionId;
    private String id;
    private String lockExpirationTime;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processInstanceId;
    private Number retries;
    private boolean suspended;
    private String workerId;
    private String topicName;
}
