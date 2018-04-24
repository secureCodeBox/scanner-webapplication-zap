package io.securecodebox.zap.jobs;

import de.otto.edison.jobs.service.JobService;
import io.securecodebox.zap.jobs.definition.EngineWorkerJob;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.togglz.ZapFeature;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;


@Component
@Slf4j
@ToString
public class JobScheduler {
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_TIME)
            .toFormatter(Locale.ENGLISH);

    private final JobService jobService;
    private final ZapTaskService taskService;


    @Autowired
    public JobScheduler(JobService jobService, ZapTaskService taskService) {
        this.jobService = jobService;
        this.taskService = taskService;
    }


    /**
     * Starts the {@link EngineWorkerJob} every minute if {@link ZapFeature#DISABLE_TRIGGER_ALL_JOBS} is inactive.
     */
    @Scheduled(cron = "${securecodebox.zap.jobsSchedulerCron}")
    public void scheduleEngineWorkerJob() {
        String now = dateFormatter.format(LocalDateTime.now());
        if (ZapFeature.DISABLE_TRIGGER_ALL_JOBS.isActive()) {
            log.info("The Job trigger time is inactive: {}", now);
        } else {
            if(taskService.getTask(ZapTopic.ZAP_SPIDER) != null || taskService.getTask(ZapTopic.ZAP_SCANNER) != null){
                log.info("The Job trigger time is active: {}", now);
                jobService.startAsyncJob(EngineWorkerJob.JOB_TYPE);
            } else {
                log.info("The Job trigger time is active, but no waiting tasks found: {}", now);
            }
        }
    }
}
