package io.securecodebox.zap.zap.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.rest.BasicAuthRestTemplate;
import io.securecodebox.zap.rest.LoggingRequestInterceptor;
import io.securecodebox.zap.zap.model.ZapApiCall;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ToString
public class ZapBaseApi {
    @Autowired
    private ZapConfiguration config;
    protected RestTemplate restTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(ZapBaseApi.class);

    @PostConstruct
    public void init() {
        restTemplate = (config.getCamundaUsername() != null && config.getCamundaPassword() != null) ?
                new BasicAuthRestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()), config.getCamundaUsername(), config.getCamundaPassword()) :
                new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

        restTemplate.setInterceptors(Collections.singletonList(new LoggingRequestInterceptor()));
    }


    String getZapApiJsonUrl(ZapApiCall apiCall) {
        return getZapApiJsonUrl(apiCall, Collections.emptyMap());
    }

    /**
     * Generates a complete request URL for the API, based on the given action and parameters.
     */
    String getZapApiJsonUrl(ZapApiCall apiCall, Map<String, String> parameters) {
        return config.getZapApiUrl() + "JSON/" + apiCall.getUrl() + generateZapApiQueryParameter(parameters);
    }

    private String getZapApiOtherUrl(ZapApiCall apiCall, Map<String, String> parameters) {
        return config.getZapApiUrl() + "OTHER/" + apiCall.getUrl() + generateZapApiQueryParameter(parameters);
    }

    /**
     * Generate a ZAP API query parameter string based on the given key/value map.
     * @param parameters The parameter map containing key/value pairs for the resulting query parameter string.
     * @return The ZAP API query parameter string.
     */
    private static String generateZapApiQueryParameter(Map<String, String> parameters) {
        StringBuilder result = new StringBuilder("?zapapiformat=JSON");
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            String value = parameter.getValue();
            if (value != null && !value.isEmpty()) {
                result.append('&').append(parameter.getKey()).append('=').append(value);
            }
        }
        return result.toString();
    }

    static Map<String, Object> convertJsonStringToMap(String json) {
        Map<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            LOG.error("Couldn't convert JSON string to map", e);
        }
        return map;
    }

    /**
     * Checks whether the call was successful. It's a mixture of status code test, {@link #isExecutionSuccessful(CharSequence)} and a workaround for the OpenAPI Extension as its response is different (and potentially misleading).
     * @see <a href="https://github.com/zaproxy/zaproxy/issues/3951">OpenAPI Extension: Action Returns Unexpected Response?</a>
     */
    private static boolean isSuccessful(ResponseEntity<String> response, ZapApiCall apiCall) {
        return ((response.getStatusCode().is2xxSuccessful() && response.getBody().contains("OK"))
                || (apiCall.getUrl().startsWith("openapi/") && response.getBody().equals("{\"importUrl\":[]}")));  // Defined in ZapSpiderApi.SpiderApi.OPTION_SET_OPENAPI_SPEC_URL
    }

    /**
     * Returns true if the given JSON response contains "OK".
     * @param response the response
     * @return true, if is execution successful
     */
    static boolean isExecutionSuccessful(CharSequence response) {
        return response.toString().contains("OK");
    }

    /**
     * Gets a single ZAP message in HAR format for the given messageId.
     */
    String getMessageHAR(String messageId) {
        Map<String, String> parameter = Collections.singletonMap("id", String.valueOf(messageId));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiOtherUrl(ZapBaseApiUrl.GET_HAR_MESSAGE, parameter), String.class, parameter);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.trace("Successfully retrieved spider message {}.", messageId);
            return response;
        } else {
            throw new IllegalStateException(String.format("Couldn't get message '%s', failed with response '%s' and HTTP code '%s'", messageId, response, responseEntity.getStatusCode()));
        }
    }

    void zapApiCall(ZapApiCall apiCall) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(apiCall), String.class, Collections.emptyMap());
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.debug("{} was successful.", apiCall);
        } else {
            throw new IllegalStateException(String.format("Couldn't %s, failed with response '%s' and HTTP code '%s'", apiCall, response, responseEntity.getStatusCode()));
        }
    }

    <T> T zapApiCall(ZapApiCall apiCall, Class<T> resultType) {
        ResponseEntity<T> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(apiCall), resultType, Collections.emptyMap());
        T response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("{} was successful.", apiCall);
            return response;
        } else {
            throw new IllegalStateException(String.format("Couldn't %s, failed with response '%s' and HTTP code '%s'", apiCall, response, responseEntity.getStatusCode()));
        }
    }

    void zapApiCall(ZapApiCall apiCall, String key, String value) {
        Map<String, String> parameter = Collections.singletonMap(key, value);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(apiCall, parameter), String.class, parameter);
        String response = responseEntity.getBody();

        if (isSuccessful(responseEntity, apiCall)) {  // TODO Workaround for unexpected response of OpenAPI extension
            LOG.debug("{} '{}' was successful.", apiCall, value);
        } else {
            throw new IllegalStateException(String.format("Couldn't %s '%s', failed with response '%s' and HTTP code '%s'", apiCall, value, response, responseEntity.getStatusCode()));
        }
    }

    <T> T zapApiCall(ZapApiCall apiCall, String key, String value, Class<T> resultType) {
        Map<String, String> parameter = Collections.singletonMap(key, value);

        ResponseEntity<T> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(apiCall, parameter), resultType, parameter);
        T response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("{} '{}' was successful.", apiCall, value);
            return response;
        } else {
            throw new IllegalStateException(String.format("Couldn't %s '%s', failed with response '%s' and HTTP code '%s'", apiCall, value, response, responseEntity.getStatusCode()));
        }
    }


    /**
     * The Enum ZapBaseApiUrl.
     */
    private enum ZapBaseApiUrl implements ZapApiCall {

        /** The view scan list URL. */
        GET_HAR_MESSAGE("core/other/messageHar/");

        /** The URL. */
        private final String url;

        /**
         * Instantiates a new spider api URL.
         *
         * @param url the url
         */
        ZapBaseApiUrl(final String url) {
            this.url = url;
        }

        /* (non-Javadoc)
         * @see de.iteratec.securebox.zap.service.zap.api.ZapApiCall#getUrl()
         */
        @Override
        public String getUrl() {
            return this.url;
        }
    }
}
