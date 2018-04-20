package io.securecodebox.zap.jobs.definition;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapScannerTask;
import io.securecodebox.zap.service.engine.model.zap.ZapSpiderTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.service.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableCronJobDefinition;
import static java.time.Duration.ofMinutes;


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
                "This Job checks periodically all process queued working units for a OWASP Scan.",
                config.getJobsSchedulerCron(), 3, 3, ofMinutes(1), Optional.of(ofMinutes(59)));
    }

    @Override
    public void execute(JobEventPublisher publisher) {
        publisher.info("Starting a new OWASP ZAP job.");

        try {
            publisher.info("Listing all open worker tasks for the topic: " + ZapTopic.ZAP_SPIDER);
            if (taskService.getZapTasksByTopic(ZapTopic.ZAP_SPIDER).length > 0) {
                ZapSpiderTask[] spiderTasks = taskService.fetchAndLockSpiderTasks(config.getMaximumTasksToFetchByJob(), ZapTopic.ZAP_SPIDER);
                if (spiderTasks.length > 0) {
                    for (int i = 0; i < spiderTasks.length; i++) {
                        ZapSpiderTask task = spiderTasks[i];
                        publisher.info(String.format("Fetched spider tasks (%s/%s): %s", i + 1, spiderTasks.length, task.toString()));
                        performSpiderTask(publisher, task);
                    }
                } else {
                    publisher.info("No tasks fetched.");
                }
            }

            publisher.info("Listing all open worker tasks for the topic: " + ZapTopic.ZAP_SCANNER);
            if (taskService.getZapTasksByTopic(ZapTopic.ZAP_SCANNER).length > 0) {
                ZapScannerTask[] scannerTasks = taskService.fetchAndLockScannerTasks(config.getMaximumTasksToFetchByJob(), ZapTopic.ZAP_SCANNER);
                if (scannerTasks.length > 0) {
                    for (int i = 0; i < scannerTasks.length; i++) {
                        ZapScannerTask task = scannerTasks[i];
                        publisher.info(String.format("Fetched scanner tasks (%s/%s): %s", i + 1, scannerTasks.length, task.toString()));
                        performScannerTask(publisher, task);
                    }
                } else {
                    publisher.info("No tasks fetched.");
                }
            }
        } catch (ClientApiException | RuntimeException e) {
            log.error("Job execution error!", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Couldn't define session management!", e);
        }
    }


    private void performSpiderTask(JobEventPublisher publisher, ZapSpiderTask task) throws ClientApiException, UnsupportedEncodingException {
        String contextId, userId;
        List<Finding> resultFindings = new LinkedList<>();
        StringBuilder rawFindings = new StringBuilder("[");

        contextId = service.createContext(task.getTargetUrl(), task.getSpiderIncludeRegexes(), task.getSpiderExcludeRegexes());
        if (task.getAuthentication()) {
            userId = service.configureAuthentication(contextId, task.getLoginSite(), task.getUsernameFieldId(), task.getPasswordFieldId(), task.getLoginUser(), task.getLoginPassword(), "", task.getLoggedInIndicator(), task.getLoggedOutIndicator(), task.getCsrfTokenId());
        } else {
            contextId = "-1";
            userId = "-1";
        }

        for (Target target : task.getTargets()) {

            String scanId = (String) service.startSpiderAsUser(target.getLocation(),task.getSpiderApiSpecUrl(),
                    task.getSpiderMaxDepth(), contextId, userId);

            String result = service.retrieveSpiderResult(scanId);
            if (!"{}".equals(result)) {  // Scanner didn't fail?
                resultFindings.addAll(taskService.createFindings(result));
                rawFindings.append(result).append(",");
            } else {
                publisher.warn("Skipped target processing due to a missing ZAP scan result.");
            }
        }

        rawFindings.deleteCharAt(rawFindings.lastIndexOf(","));
        rawFindings.append("]");
        CompleteTask completedTask = taskService.completeTask(task, resultFindings, rawFindings.toString());
        publisher.info("Completed scanner task: " + completedTask);

        service.clearSession();
    }

    private void performScannerTask(JobEventPublisher publisher, ZapScannerTask task) throws ClientApiException, UnsupportedEncodingException {

        String contextId, userId;
        List<Finding> resultFindings = new LinkedList<>();
        StringBuilder rawFindings = new StringBuilder("[");

        contextId = service.createContext(task.getTargetUrl(), task.getScannerIncludeRegexes(), task.getScannerExcludeRegexes());
        if (task.getAuthentication()) {
            userId = service.configureAuthentication(contextId, task.getLoginSite(), task.getUsernameFieldId(), task.getPasswordFieldId(), task.getLoginUser(), task.getLoginPassword(), "", task.getLoggedInIndicator(), task.getLoggedOutIndicator(), task.getCsrfTokenId());
        } else {
            contextId = "-1";
            userId = "-1";
        }

        for (Target target : task.getTargets()) {

            service.recallTarget(target);

            String scanId = (String) service.startScannerAsUser(target.getLocation(), contextId, userId);

            String result = service.retrieveScannerResult(scanId, target.getLocation());
            if (!"{}".equals(result)) {  // Scanner didn't fail?
                resultFindings.addAll(taskService.createFindings(result));
                rawFindings.append(result).append(",");
            } else {
                publisher.warn("Skipped target processing due to a missing ZAP scan result.");
            }
        }

        rawFindings.deleteCharAt(rawFindings.lastIndexOf(","));
        rawFindings.append("]");
        CompleteTask completedTask = taskService.completeTask(task, resultFindings, rawFindings.toString());
        publisher.info("Completed scanner task: " + completedTask);

        service.clearSession();
    }
}
