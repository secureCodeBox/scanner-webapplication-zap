package io.securecodebox.zap.jobs;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JobsConfiguration {
    @Bean
    public AsyncHttpClient httpClient() {
        return new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build());
    }

    @Bean
    public KeepLastJobs keepLast10CleanupStrategy() {
        return new KeepLastJobs(10);
    }
}
