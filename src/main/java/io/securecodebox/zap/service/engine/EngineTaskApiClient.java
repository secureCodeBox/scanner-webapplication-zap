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

import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.ScanFailure;
import io.securecodebox.zap.service.engine.model.zap.ZapTask;
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
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * Consumes and integrates the Camunda process engine API for external tasks.
 * Executes the REST calls
 */
@Service
@Slf4j
@ToString
public class EngineTaskApiClient {
    private final ZapConfiguration config;
    private RestTemplate restTemplate;

    /**
     * Request Timeout duration in milli sec.
     */
    static final int REQUEST_TIMEOUT = 5 * 1000;


    @Autowired
    public EngineTaskApiClient(ZapConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {

        log.info("initiating REST template for user {}", config.getCamundaUsername());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(REQUEST_TIMEOUT);
        BufferingClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);

        if (config.getCamundaUsername() != null && config.getCamundaPassword() != null) {
            restTemplate = new BasicAuthRestTemplate(bufferingRequestFactory, config.getCamundaUsername(), config.getCamundaPassword());
        } else {
            restTemplate = new RestTemplate(bufferingRequestFactory);
        }

        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));

        log.info("EngineApiClient is using {} as Engine Base URL.", config.getProcessEngineApiUrl());
    }

    /**
     * @return Returns true if the configured SCB Engine API is available and at least one processModell is deployed.
     */
    boolean isApiAvailable() {
        return (this.countProcesses() > 0);
    }

    /**
     * @return Returns the number of currently deployed process models which are available at the SCB Engine.
     */
    int countProcesses() {

        String url = config.getProcessEngineApiUrl() + "/box/processes/";
        log.debug("Call countProcesses() via {}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        log.debug(String.format("Result for countProcesses(): %s", response));

        if (response.getStatusCode().is2xxSuccessful() && response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON) && !response.toString().isEmpty()) {
            return response.toString().split("id").length;
        } else {
            return 0;
        }
    }

    ZapTask fetchAndLockTask(ZapTopic zapTopic, String jobId) {
        String url = config.getProcessEngineApiUrl() + "/box/jobs/lock/" + zapTopic.getName() + "/" + jobId;
        log.info(String.format("Trying to fetch task for the topic: %s via %s", zapTopic, url));

        ResponseEntity<ZapTask> task = restTemplate.postForEntity(url, null, ZapTask.class);
        if (task.getBody() != null && task.getStatusCode().is2xxSuccessful() && task.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
            log.debug("HTTP Response Success");
        } else {
            log.debug("Currently nothing todo, no task found!");
        }
        return task.getBody();
    }

    void completeTask(CompleteTask task) {

        String url = config.getProcessEngineApiUrl() + "/box/jobs/" + task.getJobId() + "/result";
        log.debug("Post completeTask({}) via {}", task.getJobId(), url);

        ResponseEntity<String> completedTask = restTemplate.postForEntity(url, task, String.class);
        log.info(String.format("Completed the task: %s", task.getJobId()));

        HttpStatus statusCode = completedTask.getStatusCode();

        if (statusCode.is2xxSuccessful()) {
            log.info(String.format("Successfully completed the task: %s: ", task.getJobId()));
        } else {
            log.error(String.format("Couldn't complete the task: %s, the return code is: %s with result: %s", task.getJobId(), statusCode, completedTask.getBody()));
        }
    }

    void reportFailure(ScanFailure failure) {

        String url = config.getProcessEngineApiUrl() + "/box/jobs/" + failure.getJobId() + "/failure";

        ResponseEntity<String> completedTask = restTemplate.postForEntity(url, failure, String.class);
        log.info(String.format("Reported failure: %s", failure));

        HttpStatus statusCode = completedTask.getStatusCode();

        if (statusCode.is2xxSuccessful()) {
            log.info(String.format("Successfully reported the failure: %s: ", failure));
        } else {
            log.error(String.format("Error reporting failure: %s, the return code is: %s with result: %s", failure, statusCode, completedTask.getBody()));
        }

    }
}
