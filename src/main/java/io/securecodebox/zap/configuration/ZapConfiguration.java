package io.securecodebox.zap.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
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

    public String getAppId() {
        return appId;
    }

    public String getJobsSchedulerCron() {
        return jobsSchedulerCron;
    }

    public int getMaximumTasksToFetchByJob() {
        return maximumTasksToFetchByJob;
    }

    public String getServiceDiscoveryApiUrl() {
        return serviceDiscoveryApiUrl;
    }

    public String getZapHost() {
        return zapHost;
    }

    public String getCamundaUsername() {
        return camundaUsername;
    }

    public String getCamundaPassword() {
        return camundaPassword;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setJobsSchedulerCron(String jobsSchedulerCron) {
        this.jobsSchedulerCron = jobsSchedulerCron;
    }

    public void setMaximumTasksToFetchByJob(int maximumTasksToFetchByJob) {
        this.maximumTasksToFetchByJob = maximumTasksToFetchByJob;
    }

    public void setProcessEngineApiUrl(String processEngineApiUrl) {
        this.processEngineApiUrl = processEngineApiUrl;
    }

    public void setServiceDiscoveryApiUrl(String serviceDiscoveryApiUrl) {
        this.serviceDiscoveryApiUrl = serviceDiscoveryApiUrl;
    }

    public void setTaskLockDurationInMs(String taskLockDurationInMs) {
        this.taskLockDurationInMs = taskLockDurationInMs;
    }

    public void setZapHost(String zapHost) {
        this.zapHost = zapHost;
    }

    public void setZapPort(String zapPort) {
        this.zapPort = zapPort;
    }

    public void setCamundaUsername(String camundaUsername) {
        this.camundaUsername = camundaUsername;
    }

    public void setCamundaPassword(String camundaPassword) {
        this.camundaPassword = camundaPassword;
    }
}
