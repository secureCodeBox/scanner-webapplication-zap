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

package io.securecodebox.zap.jobs.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapPartialResult;
import io.securecodebox.zap.service.engine.model.zap.ZapTargetAttributes;
import io.securecodebox.zap.service.engine.model.zap.ZapTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.service.zap.deduplication.ScanDuplicateReducer;
import io.securecodebox.zap.service.zap.deduplication.SpiderDuplicateReducer;
import io.securecodebox.zap.service.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableCronJobDefinition;
import static java.time.Duration.ofMinutes;

/**
 * Polls the engine for Spider and Scanner Tasks and executes them if available
 */
@Component
@Slf4j
@ToString
public class EngineWorkerJob implements JobRunnable {
    public static final String JOB_TYPE = "engine/worker/owasp/zap";

    @Autowired
    private ZapConfiguration config;
    @Autowired
    private ZapTaskService taskService;
    @Autowired
    private ZapService service;

    @Override
    public JobDefinition getJobDefinition() {
        return retryableCronJobDefinition(JOB_TYPE, "ZAP Task-Fetch Job",
                "This Job checks periodically all process queued working units for an OWASP Scan.",
                config.getJobsSchedulerCron(), 3, 3, ofMinutes(1), Optional.of(ofMinutes(59)));
    }

    /**
     * Main entry point
     *
     * @param publisher
     */
    @Override
    public void execute(JobEventPublisher publisher) {

        //fetch and lock tasks
        ZapTask spiderTask = taskService.getTask(ZapTopic.ZAP_SPIDER);
        ZapTask scannerTask = taskService.getTask(ZapTopic.ZAP_SCANNER);

        //if there is a spider task available
        if (spiderTask != null) {
            publisher.info(String.format("Fetched spider task: %s", spiderTask.getJobId()));

            //execute the spider task
            try {
                performSpiderTask(publisher, spiderTask);
            } catch (ClientApiException | RuntimeException e) {
                log.error("Job execution error!", e);
                taskService.reportFailure(spiderTask.getJobId(), "Spider Job execution error");
            } catch (UnsupportedEncodingException e) {
                log.error("Couldn't define session management!", e);
                taskService.reportFailure(spiderTask.getJobId(), "Could not define session management");
            }
        } else {
            publisher.info("No spider tasks fetched.");
        }

        if (scannerTask != null) {
            publisher.info(String.format("Fetched scanner task: %s", scannerTask.getJobId()));
            try {
                performScannerTask(publisher, scannerTask);
            } catch (ClientApiException | RuntimeException e) {
                log.error("Job execution error!", e);
                taskService.reportFailure(scannerTask.getJobId(), "Scanner Job Execution error");
            } catch (UnsupportedEncodingException e) {
                log.error("Couldn't define session management!", e);
                taskService.reportFailure(scannerTask.getJobId(), "Could not define session management");
            }
        } else {
            publisher.info("No scanner tasks fetched.");
        }
    }

    /**
     * Perform spider tasks and post the result back to the engine
     *
     * @param publisher
     * @param task      the task to be executed
     * @throws ClientApiException
     * @throws UnsupportedEncodingException
     */
    private void performSpiderTask(JobEventPublisher publisher, ZapTask task) throws ClientApiException, UnsupportedEncodingException {

        List<Finding> findings = new LinkedList<>();
        List<String> rawFindings = new LinkedList<>();

        for (Target target : task.getTargets()) {
            log.info("Starting Spider Task with targets: '{}'", target.getLocation());

            ZapTargetAttributes attributes = target.getAttributes();

            //Create a new Context for each target
            String contextId = service.createContext(attributes.getBaseUrl(),
                    attributes.getSpiderIncludeRegex(), attributes.getSpiderExcludeRegex());

            String userId = configureAuthentication(target, contextId);
            ZapPartialResult result = executeSpider(target, contextId, userId);

            addBaseUrlToFindings(result.getFindings(), target.getAttributes().getBaseUrl());

            if (config.isFilterSpiderResults()) {
                new SpiderDuplicateReducer().reduce(result.getFindings(), target);
            }

            findings.addAll(result.getFindings());
            rawFindings.add(result.getRawFindings());
        }

        //Finish the spider task and post findings to the engine
        completeTask(task, publisher, findings, rawFindings, ZapTopic.ZAP_SPIDER);
    }

    /**
     * Perform scanner tasks and post the result back to the engine
     *
     * @param publisher
     * @param task      the task to be executed
     * @throws ClientApiException
     * @throws UnsupportedEncodingException
     */
    private void performScannerTask(JobEventPublisher publisher, ZapTask task) throws ClientApiException, UnsupportedEncodingException {

        List<Finding> findings = new LinkedList<>();
        List<String> rawFindings = new LinkedList<>();

        for (Target target : task.getTargets()) {
            log.info("Starting Scanner Task against target: '{}'", target.getLocation());

            String contextId = configureScannerContext(target.getAttributes().getBaseUrl(), target);

            service.recallTarget(target);

            String userId = configureAuthentication(target, contextId);
            ZapPartialResult result = executeScanner(target, contextId, userId);

            addBaseUrlToFindings(result.getFindings(), target.getAttributes().getBaseUrl());

            findings.addAll(result.getFindings());
            rawFindings.add(result.getRawFindings());
        }

        if (config.isFilterScannerResults()) {
            log.info("Removing duplicate findings");
            new ScanDuplicateReducer().reduce(findings);
        }

        //Finish the scanner task and post findings to the engine
        completeTask(task, publisher, findings, rawFindings, ZapTopic.ZAP_SCANNER);
    }

    /**
     * Adds a ZAP_BASE_URL attribute to each finding.
     * @param findings
     * @param baseUrl
     */
    private void addBaseUrlToFindings(List<Finding> findings, String baseUrl){
        findings.forEach(f -> f.getAttributes().put("ZAP_BASE_URL", baseUrl));
    }

    private String computeSpiderApiSpecUrl(String specUrlVariable) {
        if (specUrlVariable == null) {
            return null;
        } else if (specUrlVariable.startsWith("http://") || specUrlVariable.startsWith("https://")) {
            return specUrlVariable;
        } else {
            return config.getProcessEngineApiUrl() + "/rest/" + specUrlVariable;
        }
    }

    private ZapPartialResult executeSpider(Target target, String contextId, String userId) throws ClientApiException {
        String spiderApiSpecUrl = computeSpiderApiSpecUrl(
                target.getAttributes().getSpiderApiSpecUrl()
        );
        Integer spiderMaxDepth = target.getAttributes().getSpiderMaxDepth();
        spiderMaxDepth = (spiderMaxDepth != null) ? spiderMaxDepth : 1;
        log.info("Start Spider with URL: " + target.getLocation());
        String scanId = (String) service.startSpiderAsUser(target.getLocation(), spiderApiSpecUrl,
                spiderMaxDepth, contextId, userId);
        return service.retrieveSpiderResult(scanId);
    }

    private ZapPartialResult executeScanner(Target target, String contextId, String userId) throws ClientApiException {
        log.info("Start Scanner with URL: " + target.getLocation());
        service.recallTarget(target);
        String scanId = (String) service.startScannerAsUser(target.getLocation(), contextId, userId);
        return service.retrieveScannerResult(scanId, target.getLocation());
    }

    private String configureScannerContext(String targetUrl, Target target) throws ClientApiException {
        ZapTargetAttributes attributes = target.getAttributes();
        //Create a new Context for all the targets belonging to this context
        return service.createContext(targetUrl, attributes.getScannerIncludeRegex(), attributes.getScannerExcludeRegex());
    }

    private String configureAuthentication(Target target, String contextId) throws ClientApiException, UnsupportedEncodingException {

        Boolean authentication = target.getAttributes().getAuthentication();
        authentication = (authentication != null) ? authentication : false;
        String loginSite = target.getAttributes().getLoginSite();
        String usernameFieldId = target.getAttributes().getLoginUsernameFieldId();
        String passwordFieldId = target.getAttributes().getPwFieldId();
        String loginUser = target.getAttributes().getLoginUser();
        String password = target.getAttributes().getLoginPw();
        String loggedInIndicator = target.getAttributes().getLoggedInIndicator();
        String loggedOutIndicator = target.getAttributes().getLoggedOutIndicator();
        String csrfToken = target.getAttributes().getCsrfTokenId();

        if (authentication) {
            return service.configureAuthentication(
                    contextId, loginSite,
                    usernameFieldId, passwordFieldId,
                    loginUser, password,
                    "", loggedInIndicator,
                    loggedOutIndicator, csrfToken);
        } else {
            return "-1";
        }
    }

    private void completeTask(ZapTask task, JobEventPublisher publisher, List<Finding> findings, List<String> rawFindings, ZapTopic zapTopic) throws ClientApiException {
        try {
            String rawFindingsString = new ObjectMapper().writeValueAsString(rawFindings);

            CompleteTask completedTask = taskService.completeTask(task, findings, rawFindingsString, zapTopic);
            publisher.info("Completed " + ((zapTopic == ZapTopic.ZAP_SCANNER) ? "scanner" : "spider") + " task: " + completedTask.getJobId());

            service.clearSession();
        } catch (JsonProcessingException e) {
            log.warn("Could not persist rawFindings");
        }
    }
}
