/*
 *
 *  *
 *  * SecureCodeBox (SCB)
 *  * Copyright 2015-2018 iteratec GmbH
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

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

    public CompleteTask completeTask(ZapTask zapTask, List<Finding> findings, String rawResult, ZapTopic zapTopic) {
        String scannerType = (zapTopic == ZapTopic.ZAP_SCANNER) ? config.getScannerType() : config.getSpiderType();
        CompleteTask task = new CompleteTask(config.getAppId(), zapTask.getJobId(), scannerType, findings, rawResult);
        if (!ZapFeature.DISABLE_COMPLETE_ZAP_PROCESS_TASKS.isActive()) {
            taskApiClient.completeTask(task);
        }
        return task;
    }

    public void reportFailure(Exception exception, ZapTask zapTask){

        if(exception != null) {
            ScanFailure failure = new ScanFailure(config.getAppId(), zapTask.getJobId(), exception.getMessage(), "Cause: " + exception.getCause());

            taskApiClient.reportFailure(failure);
        }
    }

    public List<Finding> createFindings(String zapResult) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(zapResult, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, Finding.class));
        }
        catch (IOException e) {
            log.error("Cannot construct findings due to reason: {}", e);
        }
        return new LinkedList<>();
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
