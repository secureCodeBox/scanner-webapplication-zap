package io.securecodebox.zap.service.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Responsible for task handling with the process engine. The Camunda process engine is the owner of all tasks and processes. The external task API {@link EngineTaskApiClient} to retrieve new tasks and complete work.
 */
@Service
@Slf4j
@ToString
public abstract class TaskService implements StatusDetailIndicator {

    @Autowired
    protected EngineTaskApiClient taskApiClient;

    private static final Logger LOG = LoggerFactory.getLogger(EngineTaskApiClient.class);

    public ExternalTask getTask(int taskId) {
        return taskApiClient.getTask(taskId);
    }

    protected static Map<String, Object> jsonStringToMap(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();

        try {
            map = mapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            LOG.error("Couldn't parse object to map", e);
        }
        return map;
    }
}
