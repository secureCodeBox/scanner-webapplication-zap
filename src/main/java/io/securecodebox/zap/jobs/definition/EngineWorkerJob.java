package io.securecodebox.zap.jobs.definition;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.EngineTaskApiClient;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.ExternalTask;
import io.securecodebox.zap.service.engine.model.zap.ZapScannerTask;
import io.securecodebox.zap.service.engine.model.zap.ZapSpiderTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.retryableCronJobDefinition;
import static java.time.Duration.ofMinutes;

/**
 * Die ExternalTask Implementation vo Camunda
 * Schaut obs Tasks gibt
 *
 * De is wichtig fürn funktionierenden Prozess
 */

@Component
@Slf4j
@ToString
public class EngineWorkerJob implements JobRunnable {
    public static final String JOB_TYPE = "engine/worker/owasp/zap";

    private static final Logger LOG = LoggerFactory.getLogger(EngineWorkerJob.class);

    @Autowired
    private ZapConfiguration config;
    @Autowired
    private ZapTaskService taskService;
    @Autowired
    private ZapService zapService;


    @Override
    public JobDefinition getJobDefinition() {
        return retryableCronJobDefinition(JOB_TYPE, "ZAP Task-Fetch Job",
                "This Job checks periodicaly all process queued working units for a OWASP Scan.",
                config.getJobsSchedulerCron(), 3, 3, ofMinutes(1), Optional.of(ofMinutes(59)));
    }

    @Override
    public void execute(JobEventPublisher jobEventPublisher) {
        jobEventPublisher.info("Starting a new OWASP ZAP Job");
        executeSpiderService(jobEventPublisher);
        executeScannerService(jobEventPublisher);
    }

    /**
     * Trying to executes a ZAP Spider based on tasks.
     */
    private void executeSpiderService(JobEventPublisher publisher) {
        try {
            publisher.info("List all open worker Tasks for the topic: " + ZapTopic.ZAP_SPIDER);
            ExternalTask[] spiderTasks = taskService.getZapTasksByTopic(ZapTopic.ZAP_SPIDER);

            if (spiderTasks.length > 0) {
                int count = 1;

                // debug example: show all tasks
                for (ExternalTask externalTask : spiderTasks) {
                    publisher.info(String.format("Found spider tasks (%s/%s): %s", count, spiderTasks.length, externalTask.toString()));
                    count++;
                }
                ZapSpiderTask[] fetchedSpiderTasks = taskService.fetchAndLockSpiderTasks(config.getMaximumTasksToFetchByJob(), ZapTopic.ZAP_SPIDER);

                if (fetchedSpiderTasks.length > 0) {
                    count = 1;
                    // debug example: show all tasks
                    for (ZapSpiderTask zapSpiderTask : fetchedSpiderTasks) {
                        publisher.info(String.format("Fetched spider tasks (%s/%s): %s", count, fetchedSpiderTasks.length, zapSpiderTask.toString()));

                        String spiderRequests = runSpiderByTask(zapSpiderTask);

                        if (spiderRequests != null) {
                            CompleteTask completedTask = taskService.completeZapSpiderTask(zapSpiderTask, spiderRequests);
                            publisher.info(String.format("Completed task: %s", completedTask));
                        } else {
                            publisher.warn("Skipped task completion due to a missing nmap scan result");
                        }
                        count++;

                        // clear Session
                        zapService.createNewSession();
                    }
                } else {
                    publisher.info("No tasks fetched");
                }
            }
        } catch (RuntimeException e) {
            LOG.error("JOB Excecution Error!", e);
        }
    }

    /**
     * Trying to executes a ZAP Scan based on tasks.
     */
    private void executeScannerService(JobEventPublisher publisher) {
        try {
            publisher.info("List all open worker Tasks for the topic: " + ZapTopic.ZAP_SCANNER);
            ExternalTask[] scannerTasks = taskService.getZapTasksByTopic(ZapTopic.ZAP_SCANNER);

            if (scannerTasks.length > 0) {
                int count = 1;

                // debug example: show all tasks
                for (ExternalTask externalTask : scannerTasks) {
                    publisher.info(String.format("Found scanner tasks (%s/%s): %s", count, scannerTasks.length, externalTask.toString()));
                    count++;
                }
                ZapScannerTask[] fetchedScannerTasks = taskService.fetchAndLockScannerTasks(config.getMaximumTasksToFetchByJob(),
                        ZapTopic.ZAP_SCANNER);

                if (fetchedScannerTasks.length > 0) {
                    count = 1;
                    for (ZapScannerTask zapTask : fetchedScannerTasks) {
                        publisher.info(String.format("Fetched scanner tasks (%s/%s): %s", count, fetchedScannerTasks.length, zapTask.toString()));

                        String scannerAlerts = runScanByTask(zapTask);

                        if (scannerAlerts != null) {
                            CompleteTask completedTask = taskService.completeZapScannerTask(zapTask, scannerAlerts);
                            publisher.info(String.format("Completed scanner task: %s", completedTask));
                        } else {
                            publisher.warn("Skipped task completion due to a missing nmap scan result");
                        }
                        count++;

                        zapService.createNewSession();  // Clear session
                    }
                } else {
                    publisher.info("No tasks fetched");
                }
            }
        } catch (RuntimeException e) {
            LOG.error("JOB Excecution Error!", e);
        }
    }

    private String runScanByTask(ZapScannerTask zapTask) {
        String targetUrl = zapTask.getTargetUrl();
        String includeRegex = zapTask.getScannerIncludeRegexes();
        String excludeRegex = zapTask.getScannerExcludeRegexes();
        boolean useAuthentication = zapTask.getAuthentication();
        int contextId = createSessionWithContext(targetUrl, includeRegex, excludeRegex);
        int userId = -1;

        if (useAuthentication) {
            String loginUrl = zapTask.getLoginSite();
            String usernameFieldId = zapTask.getUsernameFieldId();
            String passwordFieldId = zapTask.getPasswordFieldId();
            String loggedInIndicator = zapTask.getLoggedInIndicator();
            String loggedOutIndicator = zapTask.getLoggedOutIndicator();
            String username = zapTask.getLoginUser();
            String password = zapTask.getLoginPassword();
            String loginQueryExtension = "";
            String tokenId = zapTask.getCsrfTokenId();

            userId = zapService.configureAuthentication(contextId, loginUrl, usernameFieldId, passwordFieldId, username, password, loginQueryExtension, loggedInIndicator, loggedOutIndicator, tokenId);
        }

        String spiderResults = zapTask.getSpiderResult();
        zapService.recallSpiderUrlsAsJsonToScanner(spiderResults);

        int scannerScanId = useAuthentication ? zapService.startScannerAsUser(contextId, userId, targetUrl) : zapService.startScanner(targetUrl);

        zapService.waitUntilScannerFinished(scannerScanId);
        zapService.getScannerScans();
        return zapService.getScannerResultAsJson(targetUrl);
    }

    private int createSessionWithContext(String targetUrl, String includeRegex, String excludeRegex) {
        return (includeRegex != null && !includeRegex.isEmpty()) ? zapService.createNewSessionWithContext(targetUrl, includeRegex, excludeRegex) : zapService.createNewSessionWithContext(targetUrl);
    }

    private String runSpiderByTask(ZapSpiderTask zapSpiderTask) {
        String targetUrl = zapSpiderTask.getTargetUrl();
        String apiSpecUrl = zapSpiderTask.getSpiderApiSpecUrl();
        String includeRegex = zapSpiderTask.getSpiderIncludeRegexes();
        String excludeRegex = zapSpiderTask.getSpiderExcludeRegexes();
        int maxDepth = zapSpiderTask.getSpiderMaxDepth();
        boolean useAuthentication = zapSpiderTask.getAuthentication();
        int contextId = createSessionWithContext(targetUrl, includeRegex, excludeRegex);
        int spiderScanId;

        if (useAuthentication) {
            String loginUrl = zapSpiderTask.getLoginSite();
            String usernameFieldId = zapSpiderTask.getUsernameFieldId();
            String passwordFieldId = zapSpiderTask.getPasswordFieldId();
            String loggedInIndicator = zapSpiderTask.getLoggedInIndicator();
            String loggedOutIndicator = zapSpiderTask.getLoggedOutIndicator();
            String username = zapSpiderTask.getLoginUser();
            String password = zapSpiderTask.getLoginPassword();
            String loginQueryExtension = "";
            String tokenId = zapSpiderTask.getCsrfTokenId();

            int userId = zapService.configureAuthentication(contextId, loginUrl, usernameFieldId, passwordFieldId, username, password, loginQueryExtension, loggedInIndicator, loggedOutIndicator, tokenId);

            spiderScanId = zapService.startSpiderAsUser(contextId, userId, targetUrl, apiSpecUrl, maxDepth);
        } else {
            spiderScanId = zapService.startSpider(targetUrl, apiSpecUrl, maxDepth);
        }

        zapService.waitUntilSpiderFinished(spiderScanId);
        zapService.getSpiderScans();
        return zapService.getSpiderResultAsJson(spiderScanId);
    }
}
