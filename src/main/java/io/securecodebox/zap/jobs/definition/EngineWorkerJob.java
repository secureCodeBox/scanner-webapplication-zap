package io.securecodebox.zap.jobs.definition;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.service.JobRunnable;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.service.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.*;
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

        ZapTask spiderTask = taskService.getTask(ZapTopic.ZAP_SPIDER);
        ZapTask scannerTask = taskService.getTask(ZapTopic.ZAP_SCANNER);
        try {
            if (spiderTask != null) {
                publisher.info(String.format("Fetched spider task: %s", spiderTask.toString()));
                performTask(publisher, spiderTask, ZapTopic.ZAP_SPIDER);
            } else {
                publisher.info("No spider tasks fetched.");
            }

            if (scannerTask != null) {
                publisher.info(String.format("Fetched scanner task: %s", scannerTask.toString()));
                performTask(publisher, scannerTask, ZapTopic.ZAP_SCANNER);
            } else {
                publisher.info("No scanner tasks fetched.");
            }
        }
        catch (ClientApiException | RuntimeException e) {
            log.error("Job execution error!", e);
        }
        catch (UnsupportedEncodingException e) {
            log.error("Couldn't define session management!", e);
        }
    }


    private void performTask(JobEventPublisher publisher, ZapTask task, ZapTopic zapTopic) throws ClientApiException, UnsupportedEncodingException {
        String contextId, userId;
        List<Finding> resultFindings = new LinkedList<>();
        StringBuilder rawFindings = new StringBuilder("[");

        log.info("Starting Task for topic {} with targets: {}", zapTopic, task.getTargets());

        for (Target target : task.getTargets()) {
            String includeRegex = (String) target.getAttributes().get("includeRegex");
            String excludeRegex = (String) target.getAttributes().get("excludeRegex");
            Boolean authentication = (Boolean) target.getAttributes().get("ZAP_AUTHENTICATION");
            authentication = (authentication != null) ? authentication : false;
            String loginSite = (String) target.getAttributes().get("loginSite");
            String usernameFieldId = (String) target.getAttributes().get("usernameFieldId");
            String passwordFieldId = (String) target.getAttributes().get("passwordFieldId");
            String loginUser = (String) target.getAttributes().get("loginUser");
            String password = (String) target.getAttributes().get("password");
            String loggedInIndicator = (String) target.getAttributes().get("loggedInIndicator");
            String loggedOutIndicator = (String) target.getAttributes().get("loggedOutIndicator");
            String csrfToken = (String) target.getAttributes().get("csrfToken");

            contextId = service.createContext((String)target.getAttributes().get("ZAP_BASE_URL"), includeRegex, excludeRegex);
            if (authentication) {
                userId = service.configureAuthentication(
                        contextId, loginSite,
                        usernameFieldId, passwordFieldId,
                        loginUser, password,
                        "", loggedInIndicator,
                        loggedOutIndicator, csrfToken);
            } else {
                contextId = "-1";
                userId = "-1";
            }

            String result;
            if(zapTopic == ZapTopic.ZAP_SPIDER) {
                String spiderApiSpecUrl = (String) target.getAttributes().get("spiderApiSpecUrl");
                Integer spiderMaxDepth = (Integer) target.getAttributes().get("spiderMaxDepth");
                spiderMaxDepth = (spiderMaxDepth != null) ? spiderMaxDepth : 1;
                log.info("Start Spider with URL: " + target.getLocation());
                String scanId = (String) service.startSpiderAsUser(target.getLocation(), spiderApiSpecUrl,
                        spiderMaxDepth, contextId, userId);
                result = service.retrieveSpiderResult(scanId);
            }
            else {
                log.info("Start Scanner with URL: " + target.getLocation());
                service.recallTarget(target);
                String scanId = (String) service.startScannerAsUser(target.getLocation(), contextId, userId);
                result = service.retrieveScannerResult(scanId, target.getLocation());
            }
            if (!"{}".equals(result)) {  // Scanner didn't fail?
                List<Finding> scannerResults = taskService.createFindings(result);
                scannerResults.forEach(f -> f.getAttributes().put("ZAP_BASE_URL", target.getAttributes().get("ZAP_BASE_URL")));
                resultFindings.addAll(scannerResults);
                rawFindings.append(result).append(",");

                log.info("Scan Results for target {}: {}", target.getLocation(), resultFindings);
            } else {
                publisher.warn("Skipped target processing due to a missing ZAP scan result.");
            }
        }

        int lastIndex = rawFindings.lastIndexOf(",");
        if(lastIndex != -1) {
            rawFindings.deleteCharAt(lastIndex);
        }
        rawFindings.append("]");
        CompleteTask completedTask = taskService.completeTask(task, resultFindings, rawFindings.toString());
        publisher.info("Completed scanner task: " + completedTask);

        service.clearSession();
    }
}
