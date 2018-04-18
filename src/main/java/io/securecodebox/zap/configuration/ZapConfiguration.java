package io.securecodebox.zap.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


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
