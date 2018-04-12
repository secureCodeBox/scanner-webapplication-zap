package io.securecodebox.zap.rest;

import io.securecodebox.zap.jobs.JobScheduler;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


/**
 * @see <a href="https://stackoverflow.com/questions/7952154">Spring RestTemplate - how to enable full debugging/LOGging of requests/responses?</a>
 *  Nur LOGging, in erster Linie unwichtig
 * */
@Slf4j
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {
        
    private static final Logger LOG = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private static void traceRequest(HttpRequest request, byte[] body) throws java.io.UnsupportedEncodingException {
        LOG.trace("===========================request begin================================================");
        LOG.trace("URI : {}", request.getURI());
        LOG.trace("Method : {}", request.getMethod());
        LOG.trace("Request Body : {}", new String(body, "UTF-8"));
        LOG.trace("==========================request end================================================");
    }

    private static void traceResponse(ClientHttpResponse response) throws IOException {
        String lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"))) {
            lines = reader.lines().collect(Collectors.joining("\n"));
        }

        LOG.trace("============================response begin==========================================");
        LOG.trace("status code: {}", response.getStatusCode());
        LOG.trace("status text: {}", response.getStatusText());
        LOG.trace("Response Body : {}", lines);
        LOG.trace("=======================response end=================================================");
    }
}
