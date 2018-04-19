package io.securecodebox.zap.service.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.model.*;
import io.securecodebox.zap.service.engine.model.zap.ZapScannerTask;
import io.securecodebox.zap.service.engine.model.zap.ZapSpiderTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.togglz.ZapFeature;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zaproxy.zap.spider.SpiderTask;

import java.io.IOException;
import java.util.*;

import static java.util.Collections.singletonMap;


@Service
@Slf4j
@ToString
public class ZapTaskService extends TaskService {

    @Autowired
    protected ZapConfiguration config;


    public ExternalTask[] getZapTasksByTopic(ZapTopic topicName) {
        return taskApiClient.getTasksByTopic(topicName);
    }

    public int getZapTaskCountByTopic(ZapTopic topicName) {
        return taskApiClient.getTaskCountByTopic(topicName);
    }

    /**
     * Fetch and lock scanner tasks with the given maximum count.
     */
    public ZapScannerTask[] fetchAndLockScannerTasks(int maxTasks, ZapTopic zapTopic) {
        FetchTasks fetchTask = createZapFetchTasks(maxTasks, zapTopic, Variables.getVariablesAsStringArray());
        return taskApiClient.fetchAndLockTasks(fetchTask, ZapScannerTask[].class);
    }

    /**
     * Fetch and lock spider tasks with the given maximum count.
     */
    public ZapSpiderTask[] fetchAndLockSpiderTasks(int maxTasks, ZapTopic zapTopic) {
        FetchTasks fetchTask = createZapFetchTasks(maxTasks, zapTopic, Variables.getVariablesAsStringArray());
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

    public CompleteTask completeTask(ExternalTask fetchedTask, List<Finding> findings, String rawResult) {
        CompleteTask task = createCompleteTask(fetchedTask, findings, rawResult);
        if (!ZapFeature.DISABLE_COMPLETE_ZAP_PROCESS_TASKS.isActive()) {
            taskApiClient.completeTask(fetchedTask.getId(), task);
        }
        return task;
    }

    private CompleteTask createCompleteTask(ExternalTask zapTask, List<Finding> findings, String rawResult) {

        ObjectMapper objectMapper = new ObjectMapper();
        String findingsAsJson;
        try {
            findingsAsJson = objectMapper.writeValueAsString(objectMapper.writeValueAsString(findings));
        }
        catch (JsonProcessingException e){
            log.error(e.getMessage());
            findingsAsJson = "";
        }

        Variables vars = new Variables();
        HashMap<String, String> valueInfoContent = new HashMap<>();
        valueInfoContent.put("objectTypeName", "java.lang.String");
        valueInfoContent.put("serializationDataFormat", "application/json");
        JSONObject valueInfo = new JSONObject();
        valueInfo.put("valueInfo", new JSONObject(valueInfoContent));

        //todo: Remove the instanceof checks when spider and scanner have the same data model
        if(zapTask instanceof ZapSpiderTask) {
            vars.setLastServiceMessage(new ProcessVariable("String", "ZAP spider task finished :-)", null));
            vars.setSpiderType(new ProcessVariable("String", config.getSpiderType(), null));
            vars.setSpiderMicroserviceId(new ProcessVariable("String", config.getAppId(), null));
            vars.setSpiderResult(new ProcessVariable("Object", findingsAsJson, new JSONObject(valueInfoContent)));
            vars.setSpiderRawResult(new ProcessVariable("Object", rawResult, new JSONObject(valueInfoContent)));
        }
        if(zapTask instanceof ZapScannerTask){
            vars.setLastServiceMessage(new ProcessVariable("String", "ZAP scanner task finished :-)", null));
            vars.setScannerType(new ProcessVariable("String", config.getScannerType(), null));
            vars.setScannerMicroserviceId(new ProcessVariable("String", config.getAppId(), null));
            vars.setScannerResult(new ProcessVariable("Object", findingsAsJson, new JSONObject(valueInfoContent)));
            vars.setRawScannerResult(new ProcessVariable("Object", rawResult, new JSONObject(valueInfoContent)));
        }

        CompleteTask result = new CompleteTask();
        result.setWorkerId(zapTask.getWorkerId());

        log.info("########################################################Task WorkerId: " + zapTask.getWorkerId() + "#################################################");
        result.setVariables(vars);
        return result;
    }

    public List<Finding> createFindings(String zapResult) {

        JSONParser jsonParser = new JSONParser();
        final List<Finding> scanResults = new ArrayList<>();
        try {
            JSONArray jsonResultFindingArray = (JSONArray) jsonParser.parse(zapResult);
            jsonResultFindingArray.forEach(obj -> {

                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    Finding f = objectMapper.readValue(((JSONObject) obj).toJSONString(), Finding.class);
                    scanResults.add(f);
                }
                catch (IOException e){
                    //should not occur, if it does, ignore the finding
                    e.printStackTrace();
                }
            });
        }
        catch (ParseException e){
            //should not occur, if it does, ignore
            e.printStackTrace();
        }
        return scanResults;
    }


    @Override
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
