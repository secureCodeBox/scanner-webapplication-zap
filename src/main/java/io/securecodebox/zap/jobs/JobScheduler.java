package io.securecodebox.zap.jobs;

import de.otto.edison.jobs.service.JobService;
import io.securecodebox.zap.jobs.definition.EngineWorkerJob;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

/**
 * Startet den EngineWorkerJob jede Minute und schaut ob a Task von da Camunda Engine do is
 * <p>
 * De is ah wichtig
 */


@Component
@Slf4j
@ToString
public class JobScheduler {
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter(Locale.ENGLISH);

    private static final Logger LOG = LoggerFactory.getLogger(JobScheduler.class);


    @Autowired
    private JobService jobService;
    @Autowired
    private ZapTaskService taskService;


    /**
     * Starts the {@link EngineWorkerJob} every minute
     */
    @Scheduled(cron = "${securecodebox.zap.jobsSchedulerCron}")
    public void scheduleEngineWorkerJob() {
        String now = dateFormatter.format(LocalDateTime.now());
//        if (ZapFeature.DISABLE_TRIGGER_ALL_JOBS.isActive()) {
//            LOG.info("The Job trigger time is inactive: {}", now);
//        } else {
        int spiderTasksCount = taskService.getZapTaskCountByTopic(ZapTopic.ZAP_SPIDER);
        int scannerTasksCount = taskService.getZapTaskCountByTopic(ZapTopic.ZAP_SCANNER);
        if ((spiderTasksCount + scannerTasksCount) > 0) {
            LOG.info("The Job trigger time is active: " + now);
            jobService.startAsyncJob(EngineWorkerJob.JOB_TYPE);
        } else {
            LOG.info("The Job trigger time is active, but no waiting tasks found: " + now);
        }
    }
//    }
}
