package io.securecodebox.zap.service.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.rest.BasicAuthRestTemplate;
import io.securecodebox.zap.rest.LoggingRequestInterceptor;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import io.securecodebox.zap.service.engine.model.FetchTasks;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 * Hoid si die ExternalTasks vo Camunda über Rest
 *
 * De is wichtig
 */
@Service
@Slf4j
@ToString
public class EngineTaskApiClient {
    
    @Autowired
    private ZapConfiguration config;
    private RestTemplate restTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(EngineTaskApiClient.class);

    @PostConstruct
    public void init() {
        LOG.info("initiating rest template : {} and {}", config.getCamundaUsername(), config.getCamundaPassword());
        restTemplate = config.getCamundaUsername() != null && config.getCamundaPassword() != null 
                ? 
                new BasicAuthRestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), 
                        config.getCamundaUsername(), config.getCamundaPassword()) 
                : 
                new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));

        LOG.info("EngineApiClient is using {} as Engine Base URL.", config.getProcessEngineApiUrl());
    }

    /**
     * Retrieves the task object for the given taskId from the Process Engine, based on the REST-API:
     * <a>https://docs.camunda.org/manual/7.5/reference/rest/external-task/get/</a>.
     * @param taskId The taskId to return the task object for.
     * @return The task object for the given taskId from the Process Engine.
     */
    public ExternalTask getTask(int taskId) {
        String externalTasksUrl = config.getProcessEngineApiUrl() + '/' + taskId;
        LOG.debug("Call getTask() via {}", externalTasksUrl);
        ResponseEntity<ExternalTask> responseTasks = restTemplate.getForEntity(externalTasksUrl, ExternalTask.class);

        ExternalTask task = responseTasks.getBody();
        MediaType contentType = responseTasks.getHeaders().getContentType();
        HttpStatus statusCode = responseTasks.getStatusCode();

        if (statusCode.is2xxSuccessful() && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            LOG.trace("HTTP Response Success: {}", statusCode);
            return task;
        } else {
            throw new ResourceAccessException(String.format("Couldn't retrieve the task with taskId %s! The Engine Service returned HTTP %s", taskId, statusCode));
        }
    }

    public ExternalTask[] getTasksByTopic(ZapTopic topicName) {
        return getTasksByTopic(topicName, false);
    }

    private ExternalTask[] getTasksByTopic(ZapTopic topicName, boolean includeLockedTasks) {
        String externalTasksUrl = config.getProcessEngineApiUrl() + "?topicName=" + topicName;
        LOG.debug("Call getTasksByTopic() via {}", externalTasksUrl);

        ResponseEntity<ExternalTask[]> responseTasks = restTemplate.getForEntity(externalTasksUrl, ExternalTask[].class);

        ExternalTask[] tasks = responseTasks.getBody();
        MediaType contentType = responseTasks.getHeaders().getContentType();
        HttpStatus statusCode = responseTasks.getStatusCode();

        if (statusCode.is2xxSuccessful() && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            LOG.trace("HTTP Response Success: {}", statusCode);
            return tasks;
        } else {
            throw new ResourceAccessException(String.format("Couldn't retrieve the tasks for topic %s! The Engine Service returned HTTP %s", topicName, statusCode));
        }
    }

    public int getTaskCountByTopic(String topicName) {
        return getTaskCountByTopic(topicName, false);
    }

    /**
     * @param includeLockedTasks Ensure to include locked tasks also or not.
     */
    private int getTaskCountByTopic(String topicName, boolean includeLockedTasks) {
        String externalTasksUrl = config.getProcessEngineApiUrl() + "/count?topicName=" + topicName;
        LOG.debug("Call getTaskCountByTopic() via {}", externalTasksUrl);

        ResponseEntity<String> responseJson = restTemplate.getForEntity(externalTasksUrl, String.class);
        String json = responseJson.getBody();
        MediaType contentType = responseJson.getHeaders().getContentType();
        HttpStatus statusCode = responseJson.getStatusCode();

        LOG.debug("Result:{} HTTP-Status:{}", json, responseJson.getStatusCode());

        Map<String, Object> map = jsonStringToMap(json);

        if (statusCode.is2xxSuccessful() && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            LOG.trace("HTTP Response Success: {}", statusCode);
            if (map.size() == 1 && map.containsKey("count")) {
                return Integer.parseInt(map.get("count").toString());
            } else {
                throw new ResourceAccessException("Status Code");
            }
        } else {
            LOG.error("HTTP Response Error: {}", statusCode);
            throw new ResourceAccessException("Status Code");
        }
    }

    public <T> T fetchAndLockTasks(FetchTasks fetchTask, Class<T> resultTaskType) {
        String engineFetchTasksUrl = config.getProcessEngineApiUrl() + "/fetchAndLock";
        LOG.info(String.format("Trying to fetch %s open worker tasks for the topics: %s via %s", fetchTask.getMaxTasks(), fetchTask.getTopics(), engineFetchTasksUrl));

        ResponseEntity<T> responseFetchedTasks = restTemplate.postForEntity(engineFetchTasksUrl, fetchTask, resultTaskType);
        ResponseEntity<String> string = restTemplate.postForEntity(engineFetchTasksUrl, fetchTask, String.class);

        T fetchedTasks = responseFetchedTasks.getBody();
        MediaType contentType = responseFetchedTasks.getHeaders().getContentType();
        HttpStatus statusCode = responseFetchedTasks.getStatusCode();

        if (statusCode.is2xxSuccessful() && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            LOG.debug(String.format("HTTP Response Success: %s", statusCode.toString()));
        } else {
            LOG.debug("Currently nothing todo, no tasks found!");
        }
        return fetchedTasks;
    }

    public void completeTask(String taskId, CompleteTask completeTask) {
        String completeTasksUrl = config.getProcessEngineApiUrl() + '/' + taskId + "/complete";
        LOG.info("Post completeTask({}) via {}", taskId, completeTasksUrl);
        LOG.info("Trying to complete the CompleteTask: {}", completeTask);

        ResponseEntity<String> responseCompleteTasks = restTemplate.postForEntity(completeTasksUrl, completeTask, String.class);
        LOG.info(String.format("Completed the task: %s as workerId: %s", taskId, completeTask.getWorkerId()));

        HttpStatus statusCode = responseCompleteTasks.getStatusCode();

        if (statusCode.is2xxSuccessful()) {
            LOG.info(String.format("Successful completed the task: %s as workerId: %s", taskId, completeTask.getWorkerId()));
        } else {
            LOG.error(String.format("Couldn't completed the task: %s, the return code is: %s with result: %s", taskId, statusCode, responseCompleteTasks.getBody()));
        }
    }

    private Map<String, Object> jsonStringToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();

        try {
            map = mapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            LOG.error("Couldnt parse object to map", e);
        }
        return map;
    }
}
