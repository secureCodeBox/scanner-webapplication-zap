package io.securecodebox.zap.zap.api;

import io.securecodebox.zap.zap.model.ZapApiCall;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ZapSessionApi extends ZapBaseApi {

    private static final String ECMA_SCRIPT_ENGINE = "Oracle Nashorn";
    private static final String AUTHENTICATION_SCRIPT_TYPE = "authentication";
    public static final String CSRF_AUTH_SCRIPT_LOCATION = "scripts/templates/authentication/csrfAuthScript.js";

    private static final Logger LOG = LoggerFactory.getLogger(ZapSessionApi.class);


    public void sessionCreate(String sessionName) {

        Map<String, String> parameter = Collections.singletonMap("name", sessionName);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.CREATE_SESSION), String.class, parameter);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully created a new ZAP session with the name '{}'.", sessionName);
        } else {
            throw new IllegalStateException(String.format("Creation of ZAP session with name '%s' failed with response '%s' and HTTP code '%s'.", sessionName, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Creates a new zap context with the given contextName. A context always belongs to a underlying session which should be created before.
     */
    public int contextCreate(String contextName) {
        Map<String, String> parameter = Collections.singletonMap("contextName", contextName);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.CREATE_CONTEXT, parameter), String.class, parameter);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && response.contains("contextId")) {
            LOG.info("Successfully created a new ZAP context with name '{}'.", contextName);

            Map<String, Object> map = convertJsonStringToMap(response);
            return Integer.parseInt(map.get("contextId").toString());

        } else {
            throw new IllegalStateException(String.format("Creation of ZAP context with name '%s' failed with response '%s' and HTTP code '%s'.", contextName, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Creates a new zap context with the given contextName. A context always belongs to a underlying session which should be created before.
     */
    public void contextInclude(String contextName, String regex) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextName", contextName);
        parameters.put("regex", regex);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.OPTION_INCLUDE_IN_CONTEXT, parameters), String.class, parameters);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully included target '{}' in context '{}'.", regex, contextName);
        } else {
            throw new IllegalStateException(String.format("Couldn't include target '%s' in context '%s', failed with response '%s' and HTTP code '%s'.", regex, contextName, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Creates a new zap context with the given contextName. A context always belongs to a underlying session which should be created before.
     */
    public void contextExclude(String contextName, String regex) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextName", contextName);
        parameters.put("regex", regex);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.OPTION_EXCLUDE_FROM_CONTEXT, parameters), String.class, parameters);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully excluded target '{}' from context '{}'.", regex, contextName);
        } else {
            throw new IllegalStateException(String.format("Couldn't exclude target '%s' from context '%s', failed with response '%s' and HTTP code '%s'.", regex, contextName, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Creates a new zap context with the given contextName. A context always belongs to a underlying session which should be created before.
     */
    public void configureSessionManagement(int contextId, String methodName) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("methodName", methodName);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.SET_SESSION_MANAGEMENT, parameters), String.class, parameters);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully defined SessionManagement '{}' for context '{}'.", methodName, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't define SessionManagement '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", methodName, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public void createHttpSession(String targetUrl, String sessionName) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("site", targetUrl);
        parameters.put("session", sessionName);

        String restCall = getZapApiJsonUrl(SessionApiUrl.CREATE_HTTP_SESSION, parameters);
        LOG.info("Create Http Session with URL: " + restCall);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(restCall, String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully created a new empty session '{}' for target '{}'.", sessionName, sessionName);
        } else {
            throw new IllegalStateException(String.format("Couldn't create a new empty session '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", sessionName, sessionName, response, responseEntity.getStatusCode()));
        }
    }

    public void setActiveSession(String targetUrl, String sessionName) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("site", targetUrl);
        parameters.put("session", sessionName);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.ACTIVATE_HTTP_SESSION, parameters), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully activated the session '{}' for target '{}'.", sessionName, sessionName);
        } else {
            throw new IllegalStateException(String.format("Couldn't activate session '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", sessionName, sessionName, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Creates a new ZAP context with the given contextName. A context always belongs to a underlying session which should be created before.
     */
    public void configureFormBasedAuthentication(int contextId, String authMethodName, String LOGinUrl, String usernameFieldId, String passwordFieldId, String LOGinQueryExtension) {
        Map<String, String> parameters = new HashMap<>(3);
        parameters.put("contextId", String.valueOf(contextId));
        String authMethodConfigParams = "";
        try {
            authMethodConfigParams = "LOGinUrl=" + URLEncoder.encode(LOGinUrl, "UTF-8") + "&LOGinRequestData=" + URLEncoder.encode(usernameFieldId + "={%username%}&" + passwordFieldId + "={%password%}" + LOGinQueryExtension, "UTF-8");

            parameters.put("authMethodConfigParams", URLEncoder.encode(authMethodConfigParams, "UTF-8"));
            parameters.put("authMethodName", URLEncoder.encode(authMethodName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Couldn't escape the given authentication parameter. ", e);
        }

        String zapApiJsonUrl = getZapApiJsonUrl(SessionApiUrl.SET_AUTHENTICATION_METHOD, parameters);
        URI url = UriComponentsBuilder.fromHttpUrl(zapApiJsonUrl).build(true).toUri();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully defined FormBasedAuthentication '{}' with {} for context '{}'.", authMethodName, authMethodConfigParams, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't define SessionManagement '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", authMethodName, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public void configureFormBasedAuthenticationWithCsrfToken(int contextId, String authMethodName, String LOGinUrl, String usernameFieldId, String passwordFieldId, String csrfFieldId, String LOGinQueryExtension) {
        if (!scriptExists("csrfAuthScript")) {
            loadScript("csrfAuthScript", AUTHENTICATION_SCRIPT_TYPE, ECMA_SCRIPT_ENGINE, "csrfAuthScript.js");
        }
        Map<String, String> parameters = new HashMap<>(3);
        parameters.put("contextId", String.valueOf(contextId));
        String authMethodConfigParams = "";

        try {
            authMethodConfigParams = "scriptName=csrfAuthScript" + "&LoginURL=" + LOGinUrl + "&CSRFField=" + csrfFieldId + "&POSTData=" + URLEncoder.encode(usernameFieldId + "={%username%}&" + passwordFieldId + "={%password%}&" + csrfFieldId + "={%user_token%}", "UTF-8") + LOGinQueryExtension;

            parameters.put("authMethodConfigParams", URLEncoder.encode(authMethodConfigParams, "UTF-8"));
            parameters.put("authMethodName", URLEncoder.encode(authMethodName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Couldn't escape the given authentication parameter. ", e);
        }

        String zapApiJsonUrl = getZapApiJsonUrl(SessionApiUrl.SET_AUTHENTICATION_METHOD, parameters);
        URI url = UriComponentsBuilder.fromHttpUrl(zapApiJsonUrl).build(true).toUri();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully defined FormBasedAuthentication '{}' with {} for context '{}'.", authMethodName, authMethodConfigParams, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't define SessionManagement '%s' for context '%s', failed with response '%s' and HTTP-Code '%s'", authMethodName, contextId, response, responseEntity.getStatusCode()));
        }
    }


    public void setLoggedInIndicator(int contextId, String indicatorRegex) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("LOGgedInIndicatorRegex", "\\Q" + indicatorRegex + "\\E");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.SET_LOGGED_IN_INDICATOR, parameters), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully defined LoggedInIndicator '{}' for context '´{}'.", indicatorRegex, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't define LoggedInIndicator '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", indicatorRegex, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public void setLoggedOutIndicator(int contextId, String indicatorRegex) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("LOGgedOutIndicatorRegex", "\\Q" + indicatorRegex + "\\E");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.SET_LOGGED_OUT_INDICATOR, parameters), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully defined LoggedInIndicator '{}' for context '{}'.", indicatorRegex, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't define LoggedInIndicator '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", indicatorRegex, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public int newUser(int contextId, String username) {
        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("name", username);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.CREATE_USER, parameters), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.info("Successfully created a new User '%s' for context '%s'.", username, contextId);
            Map<String, Object> map = convertJsonStringToMap(response);
            return Integer.parseInt(map.get("userId").toString());
        } else {
            throw new IllegalStateException(String.format("Couldn't crete a new User '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", username, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public void setUserAuthenticationCredentials(int contextId, int userId, String user, String password) {
        String authCredentialsConfigParams = "";

        Map<String, String> parameters = new HashMap<>(2);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("userId", String.valueOf(userId));
        try {
            authCredentialsConfigParams = URLEncoder.encode("username=", "UTF-8") + user + URLEncoder.encode("&password=", "UTF-8") + password;
            parameters.put("authCredentialsConfigParams", authCredentialsConfigParams);
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Couldn't escape the given authentication parameter. ", e);
        }

        String zapApiJsonUrl = getZapApiJsonUrl(SessionApiUrl.SET_USER_CREDENTIALS, parameters);
        URI url = UriComponentsBuilder.fromHttpUrl(zapApiJsonUrl).build(true).toUri();

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully created a new User '{}' for context '{}'.", authCredentialsConfigParams, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't crete a new User '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", authCredentialsConfigParams, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public void setUserEnabled(int contextId, int userId, boolean enabled) {
        Map<String, String> parameters = new HashMap<>(3);
        parameters.put("contextId", String.valueOf(contextId));
        parameters.put("userId", String.valueOf(userId));
        parameters.put("enabled", String.valueOf(enabled));

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.SET_USER_ENABLED, parameters), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successful set enabled to {} for User '{}' for context '{}'.", enabled, userId, contextId);
        } else {
            throw new IllegalStateException(String.format("Couldn't set enabled to %s for User '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", enabled, userId, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public void addCSRFToken(String token) {
        Map<String, String> parameter = Collections.singletonMap("String", token);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.ADD_CSRF_TOKEN, parameter), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successful added CSRF token {}.", token);
        } else {
            throw new IllegalStateException(String.format("Couldn't add CSRF token %s .", token));
        }
    }

    private boolean scriptExists(String csrfAuthScript) {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.LIST_SCRIPTS), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.info("Existing Scripts: \n{}", response);
        } else {
            throw new IllegalStateException("Couldn't get list of scripts.");
        }

        try {
            JSONObject rootobj = (JSONObject) new JSONParser().parse(response);
            JSONArray list = (JSONArray) rootobj.get("listScripts");
            for (Object listobj : list) {
                JSONObject jsonobj = (JSONObject) listobj;
                String name = (String) jsonobj.get("name");
                if (name.equals(csrfAuthScript)) {
                    return true;
                }
            }
            return false;
        } catch (ParseException ignored) {
            throw new IllegalStateException("Couldn't get list of scripts");
        }
    }

    private void loadScript(String name, String type, String engine, String filename) {
        Map<String, String> parameters = new HashMap<>(5);
        parameters.put("scriptName", name);
        parameters.put("scriptType", type);
        parameters.put("scriptEngine", engine);
        parameters.put("fileName", filename);
        parameters.put("scriptDescription", "csrfLOGinscript");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SessionApiUrl.LOAD_SCRIPT, parameters), String.class);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful() && isExecutionSuccessful(response)) {
            LOG.info("Successfully loaded script {} .", filename);
        } else {
            throw new IllegalStateException(String.format("Couldn't load script %s", filename));
        }
    }


    public String getZapVersion() {
        return zapApiCall(SessionApiUrl.GET_VERSION, String.class);
    }


    private enum SessionApiUrl implements ZapApiCall {
        CREATE_SESSION("core/action/newSession/"),
        CREATE_CONTEXT("context/action/newContext/"),
        VIEW_CONTEXT("context/view/context/"),
        VIEW_CONTEXT_LIST("context/view/contextList/"),
        OPTION_INCLUDE_IN_CONTEXT("context/action/includeInContext/"),
        OPTION_EXCLUDE_FROM_CONTEXT("context/action/excludeFromContext/"),
        SET_SESSION_MANAGEMENT("sessionManagement/action/setSessionManagementMethod/"),
        SET_AUTHENTICATION_METHOD("authentication/action/setAuthenticationMethod/"),
        SET_LOGGED_IN_INDICATOR("authentication/action/setLoggedInIndicator/"),
        SET_LOGGED_OUT_INDICATOR("authentication/action/setLoggedOutIndicator/"),
        CREATE_USER("users/action/newUser/"),
        SET_USER_CREDENTIALS("users/action/setAuthenticationCredentials/"),
        SET_USER_ENABLED("users/action/setUserEnabled/"),
        GET_USERS("users/view/usersList/"),
        GET_VERSION("core/view/version/"),
        CREATE_HTTP_SESSION("httpSessions/action/createEmptySession/"),
        ACTIVATE_HTTP_SESSION("httpSessions/action/setActiveSession/"),
        ADD_CSRF_TOKEN("acsrf/action/addOptionToken/"),
        LOAD_SCRIPT("script/action/load/"),
        LIST_SCRIPTS("script/view/listScripts/");

        private final String url = null;

        private String s;


        SessionApiUrl(String s) {
            this.s = s;
        }

        @Override
        public String getUrl() {
            return s;
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
