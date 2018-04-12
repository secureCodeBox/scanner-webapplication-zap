package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


/**
 * DTO representing the topics of Camunda's REST API.
 * @see <a href="https://docs.camunda.org/manual/7.8/reference/rest/external-task/get/">Get External Task</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskTopic {
    private String topicName;
    private Number lockDuration;
    private List<String> variables;

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Number getLockDuration() {
        return lockDuration;
    }

    public void setLockDuration(Number lockDuration) {
        this.lockDuration = lockDuration;
    }

    public List<String> getVariables() {
        return variables;
    }

    public void setVariables(List<String> variables) {
        this.variables = variables;
    }
}
