package io.securecodebox.zap.service.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.model.*;
import io.securecodebox.zap.service.engine.model.zap.ZapTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.togglz.ZapFeature;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.singletonMap;


@Service
@Slf4j
@ToString
public class ZapTaskService extends TaskService {

    @Autowired
    protected ZapConfiguration config;

    public int getZapTaskCountByTopic(ZapTopic topicName) {
        return taskApiClient.getTaskCountByTopic(topicName);
    }

    public ZapTask getTask(ZapTopic zapTopic){
        return taskApiClient.fetchAndLockTask(zapTopic, config.getAppId());
    }

    public CompleteTask completeTask(ZapTask zapTask, List<Finding> findings, String rawResult) {
        CompleteTask task = new CompleteTask(config.getAppId(), zapTask.getJobId(), config.getScannerType(), findings, rawResult);
        if (!ZapFeature.DISABLE_COMPLETE_ZAP_PROCESS_TASKS.isActive()) {
            taskApiClient.completeTask(task);
        }
        return task;
    }

    public List<Finding> createFindings(String zapResult) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            final List<Finding> scanResults = objectMapper.readValue(zapResult, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Finding.class));
            return scanResults;
        }
        catch (IOException e) {
            log.error("Cannot construct findings due to reason: {}", e);
        }
        return new LinkedList<>();
    }


    @Override
    //todo: Do we need this?
    public StatusDetail statusDetail() {
        try {
            int taskCountByTopic = getZapTaskCountByTopic(ZapTopic.ZAP_SCANNER);
            if (taskCountByTopic >= 0) {
                log.debug("Internal health check: OK");
                return StatusDetail.statusDetail("TaskService ZAP scanner", Status.OK, "up and running", singletonMap("Open ZAP Scanner tasks", String.valueOf(taskCountByTopic)));
            } else {
                return StatusDetail.statusDetail("TaskService ZAP scanner", Status.WARNING, "Couldn't find any ZAP scanner task", singletonMap("Open tasks", String.valueOf(taskCountByTopic)));
            }
        } catch (RuntimeException e) {
            log.debug("Error: Indicating a health problem!", e);
            return StatusDetail.statusDetail("TaskService ZAP Scanner", Status.ERROR, e.getMessage());
        }
    }
}
