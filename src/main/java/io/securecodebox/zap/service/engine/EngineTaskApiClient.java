package io.securecodebox.zap.service.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import io.securecodebox.zap.service.engine.model.FetchTasks;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.util.BasicAuthRestTemplate;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Consumes and integrates the Camunda process engine API for external tasks.
 */
@Service
@Slf4j
@ToString
public class EngineTaskApiClient {
    private final ZapConfiguration config;
    private RestTemplate restTemplate;


    @Autowired
    public EngineTaskApiClient(ZapConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        log.info("initiating REST template for user {}", config.getCamundaUsername());

        restTemplate = (config.getCamundaUsername() != null && config.getCamundaPassword() != null)
                ? new BasicAuthRestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), config.getCamundaUsername(), config.getCamundaPassword())
                : new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));

        log.info("EngineApiClient is using {} as Engine Base URL.", config.getProcessEngineApiUrl());
    }


    /**
     * Retrieves the task object for the given taskId from the Process Engine, based on the REST-API:
     * <a>https://docs.camunda.org/manual/7.5/reference/rest/external-task/get/</a>.
     *
     * @param taskId ID to return the task object for.
     * @return Task object for the given task ID from the Process Engine.
     */
    ExternalTask getTask(int taskId) {
        String url = config.getProcessEngineApiUrl() + '/' + taskId;
        log.debug("Call getTask() via {}", url);

        ResponseEntity<ExternalTask> task = restTemplate.getForEntity(url, ExternalTask.class);
        if (task.getStatusCode().is2xxSuccessful() && task.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return task.getBody();
        } else {
            throw new ResourceAccessException(String.format("Couldn't retrieve the task with ID %s! The Engine Service returned status %s!", taskId, task.getStatusCode()));
        }
    }

    ExternalTask[] getTasksByTopic(ZapTopic topicName) {
        String url = config.getProcessEngineApiUrl() + "?topicName=" + topicName;
        log.debug("Call getTasksByTopic() via {}", url);

        ResponseEntity<ExternalTask[]> task = restTemplate.getForEntity(url, ExternalTask[].class);
        if (task.getStatusCode().is2xxSuccessful() && task.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return task.getBody();
        } else {
            throw new ResourceAccessException(String.format("Couldn't retrieve the tasks for topic %s! The Engine Service returned HTTP %s", topicName, task.getStatusCode()));
        }
    }

    int getTaskCountByTopic(ZapTopic topicName) {
        String url = config.getProcessEngineApiUrl() + "/count?topicName=" + topicName;
        log.debug("Call getTaskCountByTopic() via {}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        Map<String, Object> result = jsonStringToMap(response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
            if (result.size() == 1 && result.containsKey("count")) {
                return Integer.parseInt(result.get("count").toString());
            } else {
                throw new ResourceAccessException("Status Code");
            }
        } else {
            log.error("HTTP response error: {}", response.getStatusCode());
            throw new ResourceAccessException("Status Code");
        }
    }

    <T> T fetchAndLockTasks(FetchTasks fetchTask, Class<T> resultTaskType) {
        String url = config.getProcessEngineApiUrl() + "/fetchAndLock";
        log.info(String.format("Trying to fetch %s open worker tasks for the topics: %s via %s", fetchTask.getMaxTasks(), fetchTask.getTopics(), url));

        ResponseEntity<T> tasks = restTemplate.postForEntity(url, fetchTask, resultTaskType);
        if (tasks.getStatusCode().is2xxSuccessful() && tasks.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
            log.debug("HTTP Response Success");
        } else {
            log.debug("Currently nothing todo, no tasks found!");
        }
        return tasks.getBody();
    }

    void completeTask(String taskId, CompleteTask completeTask) {
        String url = config.getProcessEngineApiUrl() + '/' + taskId + "/complete";
        log.info("Post completeTask({}) via {}", taskId, url);
        log.info("Trying to complete the CompleteTask: {}", completeTask);

        ResponseEntity<String> completeTasks = restTemplate.postForEntity(url, completeTask, String.class);
        log.info(String.format("Completed the task: %s as workerId: %s", taskId, completeTask.getWorkerId()));

        HttpStatus statusCode = completeTasks.getStatusCode();

        if (statusCode.is2xxSuccessful()) {
            log.info(String.format("Successful completed the task: %s as workerId: %s", taskId, completeTask.getWorkerId()));
        } else {
            log.error(String.format("Couldn't completed the task: %s, the return code is: %s with result: %s", taskId, statusCode, completeTasks.getBody()));
        }
    }


    private static Map<String, Object> jsonStringToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>(16);

        try {
            map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            log.error("Couldn't parse object to map", e);
        }
        return map;
    }
}
