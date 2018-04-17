package io.securecodebox.zap.util;

import lombok.ToString;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;


public class BasicAuthRestTemplate extends RestTemplate {
    public BasicAuthRestTemplate(ClientHttpRequestFactory factory, String username, String password) {
        super(factory);
        addAuthentication(username, password);
    }


    private void addAuthentication(String username, String password) {
        if (username == null) {
            return;
        }
        List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(new BasicAuthorizationInterceptor(username, password));
        setRequestFactory(new InterceptingClientHttpRequestFactory(getRequestFactory(), interceptors));
    }


    @ToString
    private static class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor {
        private final String username;
        private final String password;


        BasicAuthorizationInterceptor(String username, String password) {
            this.username = username;
            this.password = (password == null ? "" : password);
        }


        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            byte[] token = Base64.getEncoder().encode((username + ':' + password).getBytes());
            request.getHeaders().add("Authorization", "Basic " + new String(token, "UTF-8"));
            return execution.execute(request, body);
        }
    }
}
