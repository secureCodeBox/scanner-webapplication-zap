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

package io.securecodebox.zap.jobs;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.service.JobMutexGroup;
import de.otto.edison.jobs.service.JobService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;


@Configuration
public class JobsConfiguration {

    @Autowired
    JobService jobService;

    @Bean
    public AsyncHttpClient httpClient() {
        return new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().build());
    }

    @Bean
    public KeepLastJobs keepLast10CleanupStrategy(final JobRepository jobRepository) {
        return new KeepLastJobs(jobRepository, 10);
    }

    @Bean
    public StopDeadJobs stopDeadJobsStrategy() {
        return new StopDeadJobs(jobService, 60);
    }

    @Bean
    public JobMutexGroup mutualExclusion() {
        return new JobMutexGroup("barFizzle", "Bar", "Fizzle");
    }

}
