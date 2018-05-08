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

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapFields;
import io.securecodebox.zap.service.engine.model.zap.ZapTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.service.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        try {

            //if there is a spider task available
            if (spiderTask != null) {
                publisher.info(String.format("Fetched spider task: %s", spiderTask.toString()));

                //execute the spider task
                performSpiderTask(publisher, spiderTask);
            } else {
                publisher.info("No spider tasks fetched.");
            }

            if (scannerTask != null) {
                publisher.info(String.format("Fetched scanner task: %s", scannerTask.toString()));
                performScannerTask(publisher, scannerTask);
            } else {
                publisher.info("No scanner tasks fetched.");
            }
        } catch (ClientApiException | RuntimeException e) {
            log.error("Job execution error!", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Couldn't define session management!", e);
        }
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

        List<Finding> resultFindings = new LinkedList<>();
        StringBuilder rawFindings = new StringBuilder("[");

        log.info("Starting Scanner Task with targets: {}", task.getTargets());

        Map<String, List<Target>> targetsGroupedByContext = task.getTargets().stream()
                .filter(target -> target.getAttributes().get(ZapFields.ZAP_BASE_URL.name()) != null)
                .collect(Collectors.groupingBy(target -> (String) target.getAttributes().get(ZapFields.ZAP_BASE_URL.name())));

        log.info("Grouped Urls by Base Url: {}", targetsGroupedByContext);

        /* This loop searches through the list of targets belonging to one context looking for an include Regex and an
           exclude regex to define the context of the zap scanner. It takes the first value found for each of the regexes
           To ensure proper functionality of the scanner, all targets belonging to a context should have the same
           include and exclude regexes
         */
        for (String context : targetsGroupedByContext.keySet()) {

            List<Target> targets = targetsGroupedByContext.get(context);
            String contextId = configureScannerContext(context, targets);

            for (Target target : targets) {

                String userId = configureAuthentication(target, contextId);
                String result = executeScanner(target, contextId, userId);

                if (!"{}".equals(result)) {  // Scanner didn't fail?
                    addFindingsToResult(target, resultFindings, rawFindings, result);

                } else {
                    publisher.warn("Skipped target processing due to a missing ZAP scan result.");
                }
            }
        }

        //Finish the scanner task and post findings to the engine
        completeTask(task, publisher, resultFindings, rawFindings, ZapTopic.ZAP_SCANNER);
    }

    private String configureScannerContext(String context, List<Target> targets) throws ClientApiException {

        //Get configuration settings from the target
        String includeRegex = null;
        String excludeRegex = null;

        for (Target target : targets) {
            if (includeRegex == null) {
                includeRegex = (String) target.getAttributes().get(ZapFields.ZAP_SCANNER_INCLUDE_REGEX.name());
            }
            if (excludeRegex == null) {
                excludeRegex = (String) target.getAttributes().get(ZapFields.ZAP_SCANNER_EXCLUDE_REGEX.name());
            }
            if (includeRegex != null && excludeRegex != null) {
                break;
            }
        }

        //Create a new Context for all the targets belonging to this context
        return service.createContext(context, includeRegex, excludeRegex);
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

        List<Finding> resultFindings = new LinkedList<>();
        StringBuilder rawFindings = new StringBuilder("[");

        log.info("Starting Spider Task with targets: {}", task.getTargets());

        for (Target target : task.getTargets()) {

            String includeRegex = (String) target.getAttributes().get(ZapFields.ZAP_SCANNER_INCLUDE_REGEX.name());
            String excludeRegex = (String) target.getAttributes().get(ZapFields.ZAP_SCANNER_EXCLUDE_REGEX.name());

            //Create a new Context for each target
            String contextId = service.createContext((String) target.getAttributes().get(ZapFields.ZAP_BASE_URL.name()), includeRegex, excludeRegex);

            String userId = configureAuthentication(target, contextId);
            String result = executeSpider(target, contextId, userId);

            if (!"{}".equals(result)) {  // Scanner didn't fail?
                addFindingsToResult(target, resultFindings, rawFindings, result);

            } else {
                publisher.warn("Skipped target processing due to a missing ZAP scan result.");
            }
        }

        //Finish the spider task and post findings to the engine
        completeTask(task, publisher, resultFindings, rawFindings, ZapTopic.ZAP_SPIDER);
    }

    private String configureAuthentication(Target target, String contextId) throws ClientApiException, UnsupportedEncodingException {

        Boolean authentication = (Boolean) target.getAttributes().get(ZapFields.ZAP_AUTHENTICATION.name());
        authentication = (authentication != null) ? authentication : false;
        String loginSite = (String) target.getAttributes().get(ZapFields.ZAP_LOGIN_SITE.name());
        String usernameFieldId = (String) target.getAttributes().get(ZapFields.ZAP_USERNAME_FIELD_ID.name());
        String passwordFieldId = (String) target.getAttributes().get(ZapFields.ZAP_PW_FIELD_ID.name());
        String loginUser = (String) target.getAttributes().get(ZapFields.ZAP_LOGIN_USER.name());
        String password = (String) target.getAttributes().get(ZapFields.ZAP_LOGIN_PW.name());
        String loggedInIndicator = (String) target.getAttributes().get(ZapFields.ZAP_LOGGED_IN_INDICATOR.name());
        String loggedOutIndicator = (String) target.getAttributes().get(ZapFields.LOGGED_OUT_INDICATOR.name());
        String csrfToken = (String) target.getAttributes().get(ZapFields.ZAP_CSRF_TOKEN_ID.name());

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

    private String executeSpider(Target target, String contextId, String userId) throws ClientApiException {


        String spiderApiSpecUrl = (target.getAttributes().get(ZapFields.ZAP_SPIDER_API_SPEC_URL.name()) != null)
                ?
                config.getProcessEngineApiUrl() + "/rest/" + target.getAttributes().get(ZapFields.ZAP_SPIDER_API_SPEC_URL.name())
                :
                null;
        Integer spiderMaxDepth = (Integer) target.getAttributes().get(ZapFields.ZAP_SPIDER_MAX_DEPTH.name());
        spiderMaxDepth = (spiderMaxDepth != null) ? spiderMaxDepth : 1;
        log.info("Start Spider with URL: " + target.getLocation());
        String scanId = (String) service.startSpiderAsUser(target.getLocation(), spiderApiSpecUrl,
                spiderMaxDepth, contextId, userId);
        return service.retrieveSpiderResult(scanId);
    }

    private String executeScanner(Target target, String contextId, String userId) throws ClientApiException {
        log.info("Start Scanner with URL: " + target.getLocation());
        service.recallTarget(target);
        String scanId = (String) service.startScannerAsUser(target.getLocation(), contextId, userId);
        return service.retrieveScannerResult(scanId, target.getLocation());
    }

    private void addFindingsToResult(final Target target, List<Finding> resultFindings, StringBuilder rawFindings, String result) {
        //Combining results of all targets in one list
        List<Finding> scannerResults = taskService.createFindings(result);
        scannerResults.forEach(f -> f.getAttributes().put(ZapFields.ZAP_BASE_URL.name(), target.getAttributes().get(ZapFields.ZAP_BASE_URL.name())));
        resultFindings.addAll(scannerResults);
        rawFindings.append(result).append(",");

        log.info("Scan Results for target {}: {}", target.getLocation(), resultFindings);
    }

    private void completeTask(ZapTask task, JobEventPublisher publisher, List<Finding> findings, StringBuilder rawFindings, ZapTopic zapTopic) throws ClientApiException {
        int lastIndex = rawFindings.lastIndexOf(",");
        if (lastIndex != -1) {
            rawFindings.deleteCharAt(lastIndex);
        }
        rawFindings.append("]");
        CompleteTask completedTask = taskService.completeTask(task, findings, rawFindings.toString(), zapTopic);
        publisher.info("Completed scanner task: " + completedTask);

        service.clearSession();
    }
}
