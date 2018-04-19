package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;


/**
 * DTO representing the topics of Camunda's REST API.
 *
 * @see <a href="https://docs.camunda.org/manual/7.8/reference/rest/external-task/get/">Get External Task</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskTopic {
    private String topicName;
    private Number lockDuration;
    private List<String> variables;
}
