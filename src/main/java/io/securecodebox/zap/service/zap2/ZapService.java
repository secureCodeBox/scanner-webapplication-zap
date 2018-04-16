package io.securecodebox.zap.service.zap2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.zap2.model.SpiderResult;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.zaproxy.clientapi.core.*;
import org.zaproxy.clientapi.gen.Context;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;


/**
 * Encapsulates all relevant OWASP ZAP methods.
 */
@Service
@Slf4j
@ToString
public class ZapService implements StatusDetailIndicator {
    private static final String SESSION_NAME = "secureCodeBoxSession";
    private static final String CONTEXT_NAME = "secureCodeBoxContext";
    private static final String AUTH_USER = "Testuser";
    private static final String AUTH_FORM_BASED = "formBasedAuthentication";
    private static final String AUTH_SCRIPT_BASED = "scriptBasedAuthentication";

    /**
     * The config containing all service specific properties.
     */
    @Autowired
    private ZapConfiguration config;

    private ClientApi api;


    @PostConstruct
    public void init() {
        api = new ClientApi(config.getZapHost(), config.getZapPort());
    }


    public String getVersion() throws ClientApiException {
        return getSingleResult(api.core.version());
    }


    /**
     * Create a new session and context based on the given URL.
     *
     * @param targetUrl Target URL to create a new session and context for
     * @return Created context ID
     */
    public String createContext(String targetUrl, String contextIncludeRegex, String contextExcludeRegex) throws ClientApiException {
        log.info("Starting to create a new ZAP session '{}' and context '{}'.", SESSION_NAME, CONTEXT_NAME);

        if (contextIncludeRegex == null || contextIncludeRegex.isEmpty()) {
            contextIncludeRegex = "\\Q" + targetUrl + "\\E.*";
            contextExcludeRegex = null;
        }

        api.core.newSession(SESSION_NAME, null);

        Context context = new Context(api);
        String contextId = getSingleResult(context.newContext(CONTEXT_NAME));
        context.includeInContext(CONTEXT_NAME, contextIncludeRegex);
        context.excludeFromContext(CONTEXT_NAME, contextExcludeRegex);

        api.sessionManagement.setSessionManagementMethod(contextId, "cookieBasedSessionManagement", null);

        api.httpSessions.createEmptySession(targetUrl, SESSION_NAME);
        api.httpSessions.setActiveSession(targetUrl, SESSION_NAME);

        return contextId;
    }

    public void clearSession() throws ClientApiException {
        api.core.newSession(SESSION_NAME, null);
    }

    /**
     * Configure the authentication based on the given user name and password field.
     *
     * @param tokenId If non-empty the authentication is script-based instead of form-based
     * @return New user ID
     */
    public String configureAuthentication(String contextId, String loginUrl, String usernameFieldId, String passwordFieldId, String username, String password, String loginQueryExtension, String loggedInIndicator, String loggedOutIndicator, String tokenId) throws ClientApiException, UnsupportedEncodingException {
        log.info("Configuring ZAP based authentication for user '{}' and loginUrl '{}'", username, loginUrl);

        if (tokenId.isEmpty()) {
            api.authentication.setAuthenticationMethod(contextId, AUTH_FORM_BASED, "loginUrl=" + URLEncoder.encode(loginUrl, "UTF-8") + "&loginRequestData=" + URLEncoder.encode(usernameFieldId + "={%username%}&" + passwordFieldId + "={%password%}" + loginQueryExtension, "UTF-8"));
        } else {
            api.authentication.setAuthenticationMethod(contextId, AUTH_SCRIPT_BASED, "scriptName=csrfAuthScript" + "&LoginURL=" + loginUrl + "&CSRFField=" + tokenId + "&POSTData=" + URLEncoder.encode(usernameFieldId + "={%username%}&" + passwordFieldId + "={%password%}&" + tokenId + "={%user_token%}", "UTF-8") + loginQueryExtension);
            api.acsrf.addOptionToken(tokenId);
            api.script.load("csrfAuthScript", "authentication", "Oracle Nashorn", "csrfAuthScript.js", "csrfloginscript");  // TODO First check if api.script.listScripts() contains "csrfAuthScript" ?
        }

        if (loggedInIndicator != null && !loggedInIndicator.isEmpty()) {
            api.authentication.setLoggedInIndicator(contextId, "\\Q" + loggedInIndicator + "\\E");
        }
        if (loggedOutIndicator != null && !loggedOutIndicator.isEmpty()) {
            api.authentication.setLoggedOutIndicator(contextId, "\\Q" + loggedOutIndicator + "\\E");
        }

        String userId = getSingleResult(api.users.newUser(contextId, AUTH_USER));
        api.users.setAuthenticationCredentials(contextId, userId, URLEncoder.encode("username=", "UTF-8") + username + URLEncoder.encode("&password=", "UTF-8") + password);
        api.users.setUserEnabled(contextId, userId, "true");

        return userId;
    }


    public void recallSpiderToScanner(String json) {
        List<SpiderResult> requests;
        try {
            requests = new ObjectMapper().readValue(json, new TypeReference<List<SpiderResult>>() {});  // Convert JSON to list
            requests = requests.stream().filter(l -> !l.getUrl().isEmpty()).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Couldn't convert JSON string to List<SpiderResult>", e);
            return;
        }

        if (!requests.isEmpty()) {
            Collection<Cookie> cookies = enforceSessionCookie(requests.get(0).getUrl());

            log.debug("Trying to recall #{} requests.", requests.size());
            try (AsyncHttpClient client = new AsyncHttpClient()) {  // https://asynchttpclient.github.io/async-http-client/proxy.html
                for (SpiderResult request : requests) {
                    switch (request.getMethod()) {
                        case "GET":
                            callAsyncGetRequest(client, request, cookies);
                            break;
                        case "POST":
                            callAsyncPostRequest(client, request, cookies);
                            break;
                        default:
                            log.debug("Nothing to do, method: {} URL:{}", request.getMethod(), request.getUrl());
                            break;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error", e);
            }
        }
    }

    /**
     * @param userId User ID to start the spider scan as, "-1" to ignore
     * @return New spider scan ID
     */
    public Object startSpiderAsUser(String targetUrl, String apiSpecUrl, int maxDepth, String contextId, String userId) throws ClientApiException {
        log.info("Starting spider for targetUrl '{}' and with apiSpecUrl '{}' and maxDepth '{}'", targetUrl, apiSpecUrl, maxDepth);

        api.openapi.importUrl(apiSpecUrl, "false");
        api.spider.setOptionMaxDepth(maxDepth);
        api.spider.setOptionParseComments(true);
        api.spider.setOptionParseGit(true);
        api.spider.setOptionParseSVNEntries(true);
        api.spider.setOptionParseSitemapXml(true);
        api.spider.setOptionParseRobotsTxt(true);

        ApiResponse response = ("-1".equals(userId))
                ? api.spider.scan(targetUrl, "-1", null, CONTEXT_NAME, null)
                : api.spider.scanAsUser(contextId, userId, targetUrl, "-1", null, null);
        return getSingleResult(response);
    }

    /**
     * @param userId User ID to start the scan as, "-1" to ignore
     * @return New scanner scan ID
     */
    public Object startScannerAsUser(String targetUrl, String contextId, String userId) throws ClientApiException {
        log.info("Starting scanner for targetUrl '{}'.", targetUrl);

        api.ascan.enableAllScanners(null);
        api.ascan.setOptionHandleAntiCSRFTokens(true);

        ApiResponse response = ("-1".equals(userId))
                ? api.ascan.scan(targetUrl, "true", "false", null, null, null)
                : api.ascan.scanAsUser(targetUrl, contextId, userId, "true", null, null, null);
        return getSingleResult(response);
    }

    /**
     * Start a new spider scan followed by a regular scan and wait until both have finished.
     *
     * @return Scan results as JSON string
     */
    public String scan(String targetUrl, String apiSpecUrl, int maxDepth) throws ClientApiException {
        createContext(targetUrl, null, null);

        String id = (String) startSpiderAsUser(targetUrl, apiSpecUrl, maxDepth, "-1", "-1");
        retrieveSpiderResult(id);
        id = (String) startScannerAsUser(targetUrl, "-1", "-1");
        String result = retrieveScannerResult(id, targetUrl);

        return result;
    }


    /**
     * Wait until the spider scan finished, then return its result.
     *
     * @return JSON string
     */
    public String retrieveSpiderResult(String scanId) throws ClientApiException {
        try {
            int progress = 0;
            while (progress < 100) {
                progress = Integer.parseInt(getSingleResult(api.spider.status(scanId)));
                log.info("Spider (ID: {}) progress: {}%", scanId, progress);
                Thread.sleep(1000);
            }
            log.info("Spider (ID: {}) completed.", scanId);
        } catch (InterruptedException e) {
            log.error("Couldn't wait until spider finished!", e);
        }

        Collection<SpiderResult> spiderResult = new ArrayList<>(256);
        ApiResponse response = api.spider.fullResults(scanId);
        if (response instanceof ApiResponseList) {
            spiderResult = ((ApiResponseList) response)
                    .getItems().stream()
                    .map(r -> new SpiderResult((ApiResponseSet) r))
                    .collect(Collectors.toList());
        }

        List<SpiderResult> result = spiderResult.isEmpty()
                ? Collections.emptyList()
                : filterAndExtendSpiderResults(spiderResult);
        log.info("Found #{} spider URLs for the scanId:{}", result.size(), scanId);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Couldn't convert List<SpiderResult> to JSON string!", e);
            return "{}";
        }
    }

    /**
     * Wait until the scanner scan finished, then return its result.
     *
     * @return JSON string
     */
    public String retrieveScannerResult(String scanId, String targetUrl) throws ClientApiException {
        try {
            int progress = 0;
            while (progress < 100) {
                progress = Integer.parseInt(getSingleResult(api.ascan.status(scanId)));
                log.info("Scanner (ID: {}) progress: {}%", scanId, progress);
                Thread.sleep(5000);
            }
            log.info("Scanner (ID: {}) completed.", scanId);
        } catch (InterruptedException e) {
            log.error("Couldn't wait until scanner finished!", e);
        }

        List<Alert> result = api.getAlerts(targetUrl, -1, -1);
        log.info("Found #{} alerts for targetUrl: {}", result.size(), targetUrl);

        try {
            return new ObjectMapper().writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Couldn't convert List<Alert> to JSON string!", e);
            return "{}";
        }
    }


    /**
     * Generate first request to enforce a session cookie (if existing) to use for all following requests.
     */
    private Collection<Cookie> enforceSessionCookie(String url) {
        Collection<Cookie> result = new HashSet<>(5);

        try (AsyncHttpClient client = new AsyncHttpClient()) {
            log.debug("Call sync to retrieve session cookie with GET:{} via ZAP: {}:{}", url, config.getZapHost(), config.getZapPort());

            Response response = client.prepareGet(url)
                    .setProxyServer(new ProxyServer(config.getZapHost(), config.getZapPort()))
                    .execute(new AsyncCompletionHandler<Response>() {
                        @Override
                        public Response onCompleted(Response r) {
                            log.debug("GET response: {} {}", r.getStatusCode(), r.getUri());
                            r.getCookies().forEach(c -> log.info("Found cookie: {} {}", c.getName(), c.getValue()));
                            return r;
                        }

                        @Override
                        public void onThrowable(Throwable t) {
                            log.error("Error", t);
                        }
                    })
                    .get();

            if (response != null && response.getCookies() != null) {
                result = response.getCookies();
            }
        } catch (InterruptedException | ExecutionException ignored) {
        }

        return result;
    }

    private void callAsyncGetRequest(AsyncHttpClient client, SpiderResult request, Collection<Cookie> cookies) throws InterruptedException, ExecutionException {
        log.debug("Call async GET:{} with ZAP: {}:{} and #cookies: {}", request.getUrl(), config.getZapHost(), config.getZapPort(), cookies.size());

        client.prepareGet(request.getUrl())
                .setProxyServer(new ProxyServer(config.getZapHost(), config.getZapPort()))
                .setCookies(cookies)
                .execute(new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(Response r) {
                        log.debug("GET response: {} {}", r.getStatusCode(), r.getUri());
                        r.getCookies().forEach(c -> log.info("Found cookie: {} {}", c.getName(), c.getValue()));
                        return r;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        log.error("Error", t);
                    }
                }).get();
    }

    private void callAsyncPostRequest(AsyncHttpClient client, SpiderResult request, Collection<Cookie> cookies) {
        log.debug("Call async POST:{} with ZAP: {}:{} and Post-Data: {}", request.getUrl(), config.getZapHost(), config.getZapPort(), request.getPostData());

        client.preparePost(request.getUrl())
                .setProxyServer(new ProxyServer(config.getZapHost(), config.getZapPort()))
                .setBody(request.getPostData())
                .setCookies(cookies)
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .execute(new AsyncCompletionHandler<Response>() {
                    @Override
                    public Response onCompleted(Response r) {
                        log.debug("POST response: {}", r);
                        return r;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        log.error("Error", t);
                    }
                });
    }

    private static String getSingleResult(ApiResponse response) {
        if (response instanceof ApiResponseElement) {
            return ((ApiResponseElement) response).getValue();
        }
        return "";
    }

    /**
     * Keep all result URLs with HTTP status code 2xx/3xx/1xx and extend them with additional information.
     * TODO Why exactly is this necessary?!
     */
    private List<SpiderResult> filterAndExtendSpiderResults(Collection<SpiderResult> urls) throws ClientApiException {
        JSONParser parser = new JSONParser();
        List<SpiderResult> result = new ArrayList<>(urls.size());

        for (SpiderResult url : urls) {
            HttpStatus status = HttpStatus.valueOf(Integer.parseInt(url.getStatusCode()));
            if (status.is2xxSuccessful() || status.is3xxRedirection() || status.is1xxInformational()) {
                String msg = Arrays.toString(api.core.messageHar(url.getMessageId()));
                try {
                    JSONArray entries = (JSONArray) ((JSONObject) ((JSONObject) parser.parse(msg)).get("log")).get("entries");
                    if (entries.size() == 1) {
                        JSONObject entry = (JSONObject) entries.get(0);
                        JSONObject request = (JSONObject) entry.get("request");

                        url.setResponseTime((Long) entry.get("time"));
                        url.setRequestDateTime((String) entry.get("startedDateTime"));
                        url.setPostData(formatAsString(((JSONObject) request.get("postData")).get("params"), "&"));
                        url.setHeaders(formatAsString(request.get("headers"), "&"));
                        url.setQueryString(formatAsString(request.get("queryString"), "&"));
                        url.setCookies(formatAsString(request.get("cookies"), ";"));

                        log.debug("Successfully extended spider result with messageId {} to '{}'.", url, url.getMessageId());
                    }
                } catch (ParseException e) {
                    throw new RuntimeException("Couldn't parse message with id " + url.getMessageId(), e);
                }
                result.add(url);
            }
        }
        return result;
    }

    /**
     * Reformat the parameters from JSON to flat string.
     *
     * @param parameters Should be compatible to {@link JSONArray}
     */
    private static String formatAsString(Object parameters, CharSequence concatChar) {
        Function<JSONObject, String> transform = j -> {
            try {
                return URLEncoder.encode(j.get("name").toString(), "UTF-8") + '=' + URLEncoder.encode(j.get("value").toString(), "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                return "";
            }
        };

        return ((Collection<Object>) parameters).stream()
                .map(p -> (JSONObject) p)
                .map(transform)
                .collect(Collectors.joining(concatChar));
    }


    @Override
    public StatusDetail statusDetail() {
        try {
            String v = getVersion();
            if (v != null && !v.isEmpty() && v.contains("version")) {
                log.debug("Internal status check: ok");
                return StatusDetail.statusDetail(getClass().getSimpleName(), Status.OK, "up and running", singletonMap("ZAP Version", v));
            } else {
                return StatusDetail.statusDetail(getClass().getSimpleName(), Status.WARNING, "Warning");
            }
        } catch (ClientApiException e) {
            log.debug("Error: indicating a status problem!", e);
            return StatusDetail.statusDetail(getClass().getSimpleName(), Status.ERROR, e.getMessage());
        }
    }
}
