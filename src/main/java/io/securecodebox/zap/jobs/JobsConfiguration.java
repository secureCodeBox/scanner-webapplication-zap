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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JobsConfiguration {
    @Bean
    public AsyncHttpClient httpClient() {
        return new AsyncHttpClient(new Builder().build());
    }

    @Bean
    public KeepLastJobs keepLast10CleanupStrategy() {
        return new KeepLastJobs(10);
    }
}
