package io.securecodebox.zap.service.engine;

import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.model.*;
import io.securecodebox.zap.service.engine.model.zap.*;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonMap;


@Service
@Slf4j
@ToString
public class ZapTaskService extends TaskService {

    @Autowired
    protected ZapConfiguration config;

    private static final Logger LOG = LoggerFactory.getLogger(ZapTaskService.class);

    public ExternalTask[] getZapTasksByTopic(ZapTopic topicName) {
        return taskApiClient.getTasksByTopic(topicName);
    }

    public int getZapTaskCountByTopic(ZapTopic topicName) {
        return taskApiClient.getTaskCountByTopic(topicName.getName());
    }

    /**
     * Fetch and lock scanner tasks with the given maximum count.
     */
    public ZapScannerTask[] fetchAndLockScannerTasks(int maxTasks, ZapTopic zapTopic) {
        FetchTasks fetchTask = createZapFetchTasks(maxTasks, zapTopic, ZapScannerFetchVariables.TASK_VARIABLES);
        return taskApiClient.fetchAndLockTasks(fetchTask, ZapScannerTask[].class);
    }

    /**
     * Fetch and lock spider tasks with the given maximum count.
     */
    public ZapSpiderTask[] fetchAndLockSpiderTasks(int maxTasks, ZapTopic zapTopic) {
        FetchTasks fetchTask = createZapFetchTasks(maxTasks, zapTopic, ZapSpiderFetchVariables.TASK_VARIABLES);
        return taskApiClient.fetchAndLockTasks(fetchTask, ZapSpiderTask[].class);
    }

    private FetchTasks createZapFetchTasks(int maxTasks, ZapTopic zapTopic, List<String> variablesToFetch) {
        TaskTopic topic = new TaskTopic();
        topic.setTopicName(zapTopic.getName());
        topic.setLockDuration(config.getTaskLockDurationInMs());
        topic.setVariables(variablesToFetch);

        FetchTasks result = new FetchTasks();
        result.setWorkerId(config.getAppId());
        result.setMaxTasks(maxTasks);
        result.setTopics(Collections.singletonList(topic));
        return result;
    }

    public CompleteTask completeZapScannerTask(ZapScannerTask fetchedTask, String result) {
        CompleteTask task = createZapScannerCompleteTask(fetchedTask, result);
        taskApiClient.completeTask(fetchedTask.getId(), task);
        return task;
    }

    public CompleteTask completeZapSpiderTask(ZapSpiderTask fetchedTask, String result) {
        CompleteTask task = createZapSpiderCompleteTask(fetchedTask, result);
        taskApiClient.completeTask(fetchedTask.getId(), task);
        return task;
    }

    private CompleteTask createZapSpiderCompleteTask(ZapSpiderTask zapTask, String zapResult) {
        ZapSpiderCompleteVariables vars = new ZapSpiderCompleteVariables();
        vars.setSpiderResult(new ProcessVariable("json", zapResult, null));
        vars.setSpiderMicroserviceId(new ProcessVariable("String", config.getAppId(), null));
//        vars.setLastServiceMessage(new ProcessVariable("String", "ZAP spider task finished :-)", null));

        CompleteTask result = new CompleteTask();
        result.setWorkerId(config.getAppId());
        result.setVariables(vars);
        return result;
    }

    private CompleteTask createZapScannerCompleteTask(ZapScannerTask zapTask, String zapResult) {
        ZapScannerCompleteVariables vars = new ZapScannerCompleteVariables();
        vars.setScannerResult(new ProcessVariable("json", zapResult, null));
        vars.setScannerMicroserviceId(new ProcessVariable("String", config.getAppId(), null));
//        vars.setLastServiceMessage(new ProcessVariable("String", "ZAP scanner task finished :-)", null));

        CompleteTask result = new CompleteTask();
        result.setWorkerId(config.getAppId());
        result.setVariables(vars);
        return result;
    }


    @Override
    public StatusDetail statusDetail() {
        try {
            int taskCountByTopic = getZapTaskCountByTopic(ZapTopic.ZAP_SCANNER);
            if (taskCountByTopic >= 0) {
                LOG.debug("Internal health check: OK");
                return StatusDetail.statusDetail("TaskService ZAP scanner", Status.OK, "up and running", singletonMap("Open ZAP Scanner tasks", String.valueOf(taskCountByTopic)));
            } else {
                return StatusDetail.statusDetail("TaskService ZAP scanner", Status.WARNING, "Couldn't find any ZAP scanner task", singletonMap("Open tasks", String.valueOf(taskCountByTopic)));
            }
        } catch (RuntimeException e) {
            LOG.debug("Error: Indicating a health problem!", e);
            return StatusDetail.statusDetail("TaskService ZAP Scanner", Status.ERROR, e.getMessage());
        }
    }
}
