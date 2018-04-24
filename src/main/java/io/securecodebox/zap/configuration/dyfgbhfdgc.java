//package io.securecodebox.zap.configuration;
//
//import io.securecodebox.zap.service.engine.model.zap.ZapTask;
//import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
//import io.securecodebox.zap.util.BasicAuthRestTemplate;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.client.BufferingClientHttpRequestFactory;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.UUID;
//
//public class dyfgbhfdgc {
//
//    private RestTemplate restTemplate;
//
//    ZapTask fetchAndLockTasks(ZapTopic zapTopic, String jobId) {
//        restTemplate = ("kermit" != null && "a" != null)
//                ? new BasicAuthRestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), "kermit", "a")
//                : new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
//
//        String url = "http://localhost:8080/box/jobs/lock/" + zapTopic.getName() + "/" + jobId;
//        System.out.println((String.format("Trying to fetch task for the topic: %s via %s", zapTopic, url)));
//
//        ResponseEntity<ZapTask> task = restTemplate.postForEntity(url, null, ZapTask.class);
//        if (task.getBody() != null && task.getStatusCode().is2xxSuccessful() && task.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)) {
//            System.out.println("HTTP Response Success");
//        } else {
//            System.out.println("Currently nothing todo, no tasks found!");
//        }
//        return task.getBody();
//    }
//
//    public static void main(String[] args) {
//
//        dyfgbhfdgc f = new dyfgbhfdgc();
//
//
//
//        System.out.println(f.fetchAndLockTasks(ZapTopic.ZAP_SPIDER, UUID.randomUUID().toString()));
//    }
//}
