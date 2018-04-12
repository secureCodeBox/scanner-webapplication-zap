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

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLockExpirationTime() {
        return lockExpirationTime;
    }

    public void setLockExpirationTime(String lockExpirationTime) {
        this.lockExpirationTime = lockExpirationTime;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Number getRetries() {
        return retries;
    }

    public void setRetries(Number retries) {
        this.retries = retries;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}
