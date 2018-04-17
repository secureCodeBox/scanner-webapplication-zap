package io.securecodebox.zap.jobs.definition;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.zap.ZapScannerTask;
import io.securecodebox.zap.service.engine.model.zap.ZapSpiderTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.service.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

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
        contextId = service.createContext(task.getTargetUrl(), task.getSpiderIncludeRegexes(), task.getSpiderExcludeRegexes());
        if (task.getAuthentication()) {
            userId = service.configureAuthentication(contextId, task.getLoginSite(), task.getUsernameFieldId(), task.getPasswordFieldId(), task.getLoginUser(), task.getLoginPassword(), "", task.getLoggedInIndicator(), task.getLoggedOutIndicator(), task.getCsrfTokenId());
        } else {
            contextId = "-1";
            userId = "-1";
        }

        String scanId = (String) service.startSpiderAsUser(task.getTargetUrl(), task.getSpiderApiSpecUrl(), task.getSpiderMaxDepth(), contextId, userId);

        String result = service.retrieveSpiderResult(scanId);
        if (result != null) {
            CompleteTask completedTask = taskService.completeZapSpiderTask(task, result);
            publisher.info("Completed spider task: " + completedTask);
        } else {
            publisher.warn("Skipped task completion due to a missing ZAP scan result.");
        }

        service.clearSession();
    }

    private void performScannerTask(JobEventPublisher publisher, ZapScannerTask task) throws ClientApiException, UnsupportedEncodingException {
        String contextId, userId;
        contextId = service.createContext(task.getTargetUrl(), task.getScannerIncludeRegexes(), task.getScannerExcludeRegexes());
        if (task.getAuthentication()) {
            userId = service.configureAuthentication(contextId, task.getLoginSite(), task.getUsernameFieldId(), task.getPasswordFieldId(), task.getLoginUser(), task.getLoginPassword(), "", task.getLoggedInIndicator(), task.getLoggedOutIndicator(), task.getCsrfTokenId());
        } else {
            contextId = "-1";
            userId = "-1";
        }

        service.recallSpiderToScanner(task.getSpiderResult());
        String scanId = (String) service.startScannerAsUser(task.getTargetUrl(), contextId, userId);

        String result = service.retrieveScannerResult(scanId, task.getTargetUrl());
        if (!"{}".equals(result)) {  // Scanner didn't fail?
            CompleteTask completedTask = taskService.completeZapScannerTask(task, result);
            publisher.info("Completed scanner task: " + completedTask);
        } else {
            publisher.warn("Skipped task completion due to a missing ZAP scan result.");
        }

        service.clearSession();
    }
}
