package io.securecodebox.zap.configuration;

import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.service.JobService;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZapJobsConfiguration {

    @Autowired
    JobService jobService;

    @Bean
    public AsyncHttpClient httpClient() {
        return new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().build());
    }

    @Bean
    public KeepLastJobs keepLast10FooJobsCleanupStrategy(final JobRepository jobRepository) {
        return new KeepLastJobs(jobRepository, 10);
    }

    @Bean
    public StopDeadJobs stopDeadJobsStrategy() {
        return new StopDeadJobs(jobService, 60);
    }
}