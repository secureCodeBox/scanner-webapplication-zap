package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * DTO representing the "fetch and lock" step of Camunda's REST API.
 * @see <a href="https://docs.camunda.org/manual/7.8/reference/rest/external-task/fetch/">Fetch and Lock External Tasks</a>
 */
@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchTasks {

    private String workerId;
    private Number maxTasks;
    private boolean usePriority;
    private List<TaskTopic> topics;


    public FetchTasks() {
        topics = new ArrayList<>(5);
    }

    public String getWorkerId() {
        return workerId;
    }

    public Number getMaxTasks() {
        return maxTasks;
    }

    public boolean isUsePriority() {
        return usePriority;
    }

    public List<TaskTopic> getTopics() {
        return topics;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public void setMaxTasks(Number maxTasks) {
        this.maxTasks = maxTasks;
    }

    public void setUsePriority(boolean usePriority) {
        this.usePriority = usePriority;
    }

    public void setTopics(List<TaskTopic> topics) {
        this.topics = topics;
    }
}
