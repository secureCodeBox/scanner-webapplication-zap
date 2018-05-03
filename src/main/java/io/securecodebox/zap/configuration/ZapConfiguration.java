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

package io.securecodebox.zap.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * Configuration class for the application defining various properties which can be overridden by setting environment variables
 */
@Component
@ConfigurationProperties(prefix = "securecodebox.zap")
@Data
public class ZapConfiguration {
    private String appId;
    private String jobsSchedulerCron;
    private int maximumTasksToFetchByJob;
    private String processEngineApiUrl;
    private String serviceDiscoveryApiUrl;
    private String taskLockDurationInMs;
    private String zapHost;
    private String zapPort;
    private String camundaUsername;
    private String camundaPassword;
    private String spiderType;
    private String scannerType;


    /**
     * Note: If the "ENGINE_ADDRESS" environment variable is set, it will override the configuration.
     */
    public String getProcessEngineApiUrl() {
        String env = System.getenv("ENGINE_ADDRESS");
        return (env != null) ? env : processEngineApiUrl;
    }

    public int getTaskLockDurationInMs() {
        return Integer.parseInt(taskLockDurationInMs);
    }

    public String getZapApiUrl() {
        return "http://" + zapHost + ':' + zapPort + '/';
    }

    public int getZapPort() {
        return Integer.parseInt(zapPort);
    }
}
