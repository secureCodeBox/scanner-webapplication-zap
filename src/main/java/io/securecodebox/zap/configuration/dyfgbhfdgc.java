//package io.securecodebox.zap.configuration;
//
//import io.securecodebox.zap.service.engine.model.Target;
//import io.securecodebox.zap.service.engine.model.zap.ZapTask;
//import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
//import io.securecodebox.zap.util.BasicAuthRestTemplate;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.client.BufferingClientHttpRequestFactory;
//import org.springframework.http.client.SimpleClientHttpRequestFactory;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.*;
//import java.util.stream.Collectors;
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
//        String url = "http://localhost:9000/box/jobs/lock/" + zapTopic.getName() + "/" + jobId;
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
//    private Map<String, List<Target>> groupTargetsByContext(List<Target> targets) {
//
//        return targets.stream()
//                .filter(target -> target.getAttributes().get("baseUrl") != null)
//                .collect(Collectors.groupingBy(target -> (String) target.getAttributes().get("baseUrl")));
//    }
//
//    public static void main(String[] args) {
//
//        dyfgbhfdgc f = new dyfgbhfdgc();
//        Target t = new Target("http://x.com");
//        t.getAttributes().put("baseUrl", "http://x.com");
//        Target t1 = new Target("http://x.com/1");
//        t1.getAttributes().put("baseUrl", "http://x.com");
//        Target t4 = new Target("http://x.com/sdf");
//        t4.getAttributes().put("baseUrl", "http://x.com");
//        Target t2 = new Target("http://y.com");
//        t2.getAttributes().put("baseUrl", "http://y.com");
//        Target t3 = new Target("http://null");
//        Target t5 = new Target("http://y.com/sdf");
//        t5.getAttributes().put("baseUrl", "http://y.com");
//        Target t6 = new Target("http://y.com/sdf");
//        t6.getAttributes().put("baseUrl", "http://y.com");
//        Target t7 = new Target("http://null/1");
//        t7.getAttributes().put("baseUrl", "http://null");
//
//        List<Target> targets = Arrays.asList(t, t1, t2, t3, t4, t5, t6, t7);
//
//        System.out.println(f.fetchAndLockTasks(ZapTopic.ZAP_SPIDER, UUID.randomUUID().toString()));
//
//
//    }
//}
