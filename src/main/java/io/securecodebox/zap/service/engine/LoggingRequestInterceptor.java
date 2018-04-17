package io.securecodebox.zap.service.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


/**
 * @see <a href="https://stackoverflow.com/questions/7952154">Spring RestTemplate - how to enable full debugging/logging of requests/responses?</a>
 */
@Slf4j
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private static void traceRequest(HttpRequest request, byte[] body) throws java.io.UnsupportedEncodingException {
        log.trace("===========================request begin================================================");
        log.trace("URI : {}", request.getURI());
        log.trace("Method : {}", request.getMethod());
        log.trace("Request Body : {}", new String(body, "UTF-8"));
        log.trace("==========================request end================================================");
    }

    private static void traceResponse(ClientHttpResponse response) throws IOException {
        String lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"))) {
            lines = reader.lines().collect(Collectors.joining("\n"));
        }

        log.trace("============================response begin==========================================");
        log.trace("status code: {}", response.getStatusCode());
        log.trace("status text: {}", response.getStatusText());
        log.trace("Response Body : {}", lines);
        log.trace("=======================response end=================================================");
    }
}
