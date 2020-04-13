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
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapReplacerRule;
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
    private ZapService zapService;

    @Override
    public JobDefinition getJobDefinition() {
        return retryableCronJobDefinition(JOB_TYPE, "ZAP Task-Fetch Job",
                "This Job checks periodically all process queued working units for an OWASP Scan.",
                config.getJobsSchedulerCron(), 3, 3, ofMinutes(1), Optional.of(ofMinutes(59)));
    }

    /**
     * Main entry point
     */
    @Override
    public boolean execute() {
        //fetch and lock tasks
        ZapTask spiderTask = taskService.getTask(ZapTopic.ZAP_SPIDER);
        ZapTask scannerTask = taskService.getTask(ZapTopic.ZAP_SCANNER);

        boolean skippedSpider = false;
        boolean skippedScanner = false;

        //if there is a spider task available
        if (spiderTask != null) {
            log.info(String.format("Fetched spider task: %s", spiderTask.getJobId()));

            //execute the spider task
            try {
                performSpiderTask(spiderTask);
            } catch (ClientApiException | RuntimeException e) {
                log.error("Job execution error!", e);
                taskService.reportFailure(spiderTask.getJobId(), "Spider Job execution error");
            } catch (UnsupportedEncodingException e) {
                log.error("Couldn't define session management!", e);
                taskService.reportFailure(spiderTask.getJobId(), "Could not define session management");
            }
        } else {
            log.debug("No spider tasks fetched.");
            skippedSpider = true;
        }

        if (scannerTask != null) {
            log.info(String.format("Fetched scanner task: %s", scannerTask.getJobId()));
            try {
                performScannerTask(scannerTask);
            } catch (ClientApiException | RuntimeException e) {
                log.error("Job execution error!", e);
                taskService.reportFailure(scannerTask.getJobId(), "Scanner Job Execution error");
            } catch (UnsupportedEncodingException e) {
                log.error("Couldn't define session management!", e);
                taskService.reportFailure(scannerTask.getJobId(), "Could not define session management");
            }
        } else {
            log.debug("No scanner tasks fetched.");
            skippedScanner = true;
        }

        return skippedSpider || skippedScanner;
    }

    /**
     * Perform spider tasks and post the result back to the engine
     *
     * @param task the task to be executed
     * @throws ClientApiException
     * @throws UnsupportedEncodingException
     */
    private void performSpiderTask(ZapTask task) throws ClientApiException, UnsupportedEncodingException {

        List<Finding> findings = new LinkedList<>();
        // RawFindings will remain empty in spider Task as it isn't usefull here.
        List<String> rawFindings = new LinkedList<>();

        for (Target target : task.getTargets()) {
            log.info("Starting Spider Task with targets: '{}'", target.getLocation());

            ZapTargetAttributes attributes = target.getAttributes();

            //Create a new Context for each target
            String contextId = zapService.createContext(attributes.getBaseUrl(),
                    attributes.getSpiderIncludeRegex(), attributes.getSpiderExcludeRegex());

            String userId = configureAuthentication(target, contextId);
            List<Finding> result = executeSpider(target, contextId, userId);

            addBaseUrlToFindings(result, target.getAttributes().getBaseUrl());

            if (config.isFilterSpiderResults()) {
                new SpiderDuplicateReducer().reduce(result, target);
            }

            findings.addAll(result);
        }

        //Finish the spider task and post findings to the engine
        completeTask(task, findings, rawFindings, ZapTopic.ZAP_SPIDER);
    }

    /**
     * Perform scanner tasks and post the result back to the engine
     *
     * @param task the task to be executed
     * @throws ClientApiException
     * @throws UnsupportedEncodingException
     */
    private void performScannerTask(ZapTask task) throws ClientApiException, UnsupportedEncodingException {

        List<Finding> findings = new LinkedList<>();
        List<String> rawFindings = new LinkedList<>();

        for (Target target : task.getTargets()) {
            log.info("Starting Scanner Task against target: '{}'", target.getLocation());

            String contextId = configureScannerContext(target.getAttributes().getBaseUrl(), target);

            String userId = configureAuthentication(target, contextId);
            configureSessionManagement(target, contextId);
            List<Finding> result = executeScanner(target, contextId, userId);

            addBaseUrlToFindings(result, target.getAttributes().getBaseUrl());

            findings.addAll(result);
        }

        // Save only one Raw Report, as zap doesn't support to get reports on the individual targets.
        rawFindings.add(zapService.getRawReport());

        if (config.isFilterScannerResults()) {
            log.info("Removing duplicate findings");
            new ScanDuplicateReducer().reduce(findings);
        }

        //Finish the scanner task and post findings to the engine
        completeTask(task, findings, rawFindings, ZapTopic.ZAP_SCANNER);
    }

    /**
     * Adds a ZAP_BASE_URL attribute to each finding.
     *
     * @param findings
     * @param baseUrl
     */
    private void addBaseUrlToFindings(List<Finding> findings, String baseUrl) {
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

    private List<Finding> executeSpider(Target target, String contextId, String userId) throws ClientApiException {
        String spiderApiSpecUrl = computeSpiderApiSpecUrl(
                target.getAttributes().getSpiderApiSpecUrl()
        );
        Integer spiderMaxDepth = target.getAttributes().getSpiderMaxDepth();
        spiderMaxDepth = (spiderMaxDepth != null) ? spiderMaxDepth : 1;
        ZapReplacerRule[] zapReplacerRules = target.getAttributes().getZapReplacerRules();

        log.debug("Start Spider with URL: " + target.getLocation());
        String scanId = zapService.startSpiderAsUser(target.getLocation(), spiderApiSpecUrl,
                spiderMaxDepth, contextId, userId, zapReplacerRules);
        return zapService.retrieveSpiderResult(scanId);
    }

    private List<Finding> executeScanner(Target target, String contextId, String userId) throws ClientApiException {
        log.debug("Start Sitemap recreation");
        zapService.recallTarget(target, target.getAttributes().getZapReplacerRules());

        log.debug("Start Scanner with URL: " + target.getLocation());
        String scanId = zapService.startScannerAsUser(target.getLocation(), contextId, userId, target.getAttributes());
        return zapService.retrieveScannerResult(scanId, target.getLocation());
    }

    private String configureScannerContext(String targetUrl, Target target) throws ClientApiException {
        ZapTargetAttributes attributes = target.getAttributes();
        // Create a new Context for all the targets belonging to this context
        return zapService.createContext(targetUrl, attributes.getScannerIncludeRegex(), attributes.getScannerExcludeRegex());
    }

    private String configureAuthentication(Target target, String contextId) throws ClientApiException, UnsupportedEncodingException {

        Boolean authentication = target.getAttributes().getAuthentication();
        authentication = (authentication != null) ? authentication : false;


        if (authentication) {
            return zapService.configureAuthentication(contextId, target);
        } else {
            return "-1";
        }
    }

    private void configureSessionManagement(Target target, String contextId) throws ClientApiException {
        zapService.configureSessionManagement(contextId, target);
    }

    private void completeTask(ZapTask task, List<Finding> findings, List<String> rawFindings, ZapTopic zapTopic) throws ClientApiException {
        try {
            String rawFindingsString = new ObjectMapper().writeValueAsString(rawFindings);

            CompleteTask completedTask = taskService.completeTask(task, findings, rawFindingsString, zapTopic);
            log.info("Completed " + ((zapTopic == ZapTopic.ZAP_SCANNER) ? "scanner" : "spider") + " task: " + completedTask.getJobId());

            zapService.clearSession();
        } catch (JsonProcessingException e) {
            log.warn("Could not persist rawFindings");
        }
    }
}
