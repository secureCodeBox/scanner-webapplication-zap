package io.securecodebox.zap.service.engine;

import de.otto.edison.status.indicator.StatusDetailIndicator;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Responsible for task handling with the process engine. The Camunda process engine is the owner of all tasks and processes. The external task API {@link EngineTaskApiClient} to retrieve new tasks and complete work.
 */
@Service
@Slf4j
@ToString
public abstract class TaskService implements StatusDetailIndicator {
    @Autowired
    protected EngineTaskApiClient taskApiClient;


    public ExternalTask getTask(int taskId) {
        return taskApiClient.getTask(taskId);
    }
}
