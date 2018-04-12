package io.securecodebox.zap.zap;

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
import io.securecodebox.zap.zap.api.ZapScannerApi;
import io.securecodebox.zap.zap.api.ZapSessionApi;
import io.securecodebox.zap.zap.api.ZapSpiderApi;
import io.securecodebox.zap.zap.model.Scans;
import io.securecodebox.zap.zap.model.scanner.Alert;
import io.securecodebox.zap.zap.model.scanner.Alerts;
import io.securecodebox.zap.zap.model.spider.FullResults;
import io.securecodebox.zap.zap.model.spider.SpiderResult;
import io.securecodebox.zap.zap.model.spider.SpiderResultUrl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zaproxy.clientapi.core.ClientApi;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonMap;


/**
 * Encapsulates all relevant OWASP ZAP methods.
 */
@Service
@Slf4j
public class ZapService implements StatusDetailIndicator {
    private static final int SPIDER_POLLER_WAIT_UNTIL_TIME_IN_MS = 1000;
    private static final int SCANNER_POLLER_WAIT_UNTIL_TIME_IN_MS = 5000;

    private static final String SESSION_NAME = "secureCodeBoxSession";
    private static final String CONTEXT_NAME = "secureCodeBoxContext";
    private static final String SESSION_MANAGEMENT_METHOD = "cookieBasedSessionManagement";
    private static final String FORM_BASED_AUTHENTICATION = "formBasedAuthentication";
    private static final String SCRIPT_BASED_AUTHENTICATION = "scriptBasedAuthentication";
    private static final int SPIDER_MAX_CHILDREN = -1;

    private static final Logger LOG = LoggerFactory.getLogger(ZapService.class);


    @Autowired
    private ZapConfiguration config;
    @Autowired
    private ZapSessionApi sessionApi;
    @Autowired
    private ZapSpiderApi spiderApi;
    @Autowired
    private ZapScannerApi scanApi;

    private ClientApi zapClient;

    @PostConstruct
    public void init(){
       zapClient = new ClientApi(config.getZapHost(), config.getZapPort());
    }


    public void createNewSession() {
        LOG.info("Starting to create a new ZAP session.");
        sessionApi.sessionCreate(SESSION_NAME);
    }

    public int createNewSessionWithContext(String targetUrl) {
        return createNewSessionWithContext(targetUrl, "\\Q" + targetUrl + "\\E.*", null);
    }

    public int createNewSessionWithContext(String targetUrl, String contextIncludeRegex, String contextExcludeRegex) {
        return createNewSessionWithContext(targetUrl, Collections.singletonList(contextIncludeRegex), Collections.singletonList(contextExcludeRegex));
    }

    /**
     * Creates the new session and a new context based on the given URL.
     * @param targetUrl Target URL to create a new session and a new context for.
     * @return The contextId
     */
    public int createNewSessionWithContext(String targetUrl, Iterable<String> contextIncludeRegexList, Iterable<String> contextExcludeRegexList) {
        LOG.info("Starting to create a new ZAP session '{}' and context '{}'.", SESSION_NAME, CONTEXT_NAME);

        sessionApi.sessionCreate(SESSION_NAME);
        int contextId = sessionApi.contextCreate(CONTEXT_NAME);

        for (String contextIncludeRegex : contextIncludeRegexList) {
            if (contextIncludeRegex != null && !contextIncludeRegex.isEmpty()) {
                sessionApi.contextInclude(CONTEXT_NAME, contextIncludeRegex);
            }
        }
        for (String contextExcludeRegex : contextExcludeRegexList) {
            if (contextExcludeRegex != null && !contextExcludeRegex.isEmpty()) {
                sessionApi.contextExclude(CONTEXT_NAME, contextExcludeRegex);
            }
        }

        sessionApi.configureSessionManagement(contextId, SESSION_MANAGEMENT_METHOD);
        sessionApi.createHttpSession(targetUrl, SESSION_NAME);
        sessionApi.setActiveSession(targetUrl, SESSION_NAME);

        return contextId;
    }

    /**
     * Configure the authentication based on the given user name and password field.
     */
    public int configureAuthentication(int contextId, String LOGinUrl, String usernameFieldId, String passwordFieldId, String username, String password, String LOGinQueryExtension, String LOGgedInIndicator, String LOGgedOutIndicator, String tokenId) {

        LOG.info("Configuring ZAP based authentication for user '{}' and LoginUrl '{}'", username, LOGinUrl);

        if (tokenId.isEmpty()) {
            sessionApi.configureFormBasedAuthentication(contextId, FORM_BASED_AUTHENTICATION, LOGinUrl, usernameFieldId, passwordFieldId, LOGinQueryExtension);
        } else {
            sessionApi.addCSRFToken(tokenId);
            sessionApi.configureFormBasedAuthenticationWithCsrfToken(contextId, SCRIPT_BASED_AUTHENTICATION, LOGinUrl, usernameFieldId, passwordFieldId, tokenId, LOGinQueryExtension);
        }

        if (LOGgedInIndicator != null && !LOGgedInIndicator.isEmpty()) {
            sessionApi.setLoggedInIndicator(contextId, LOGgedInIndicator);
        }
        if (LOGgedOutIndicator != null && !LOGgedOutIndicator.isEmpty()) {
            sessionApi.setLoggedOutIndicator(contextId, LOGgedOutIndicator);
        }

        int userId = sessionApi.newUser(contextId, "Testuser");
        sessionApi.setUserAuthenticationCredentials(contextId, userId, username, password);
        sessionApi.setUserEnabled(contextId, userId, true);

        return userId;
    }


    /**
     * Start a new ZAP spider/crawler against the targetUrl, without authentication.
     */
    int startSpider(String targetUrl, int maxDepth) {
        return startSpider(targetUrl, "", maxDepth);
    }

    public int startSpider(String targetUrl, String apiSpecUrl, int maxDepth) {
        LOG.info("Starting spider (without authentication) for targetUrl '{}' and with apiSpecUrl '{}' and maxDepth '{}'", targetUrl, apiSpecUrl, maxDepth);

        spiderApi.optionSetApiSpecUrl(apiSpecUrl);
        spiderApi.optionMaxDepth(maxDepth);
        spiderApi.optionParseComments(true);
        spiderApi.optionParseGit(true);
        spiderApi.optionParseSvn(true);
        spiderApi.optionParseSitemap(true);
        spiderApi.optionParseRobotsTxt(true);

        return spiderApi.startSpider(CONTEXT_NAME, targetUrl, SPIDER_MAX_CHILDREN);
    }

    /**
     * Start a new ZAP spider/crawler against the targetUrl with authentication.
     */
    public int startSpiderAsUser(int contextId, int userId, String targetUrl, int maxDepth) {
        return startSpiderAsUser(contextId, userId, targetUrl, "", maxDepth);
    }

    public int startSpiderAsUser(int contextId, int userId, String targetUrl, String apiSpecUrl, int maxDepth) {
        LOG.info("Starting spider (with authentication) for targetUrl '{}' and with apiSpecUrl '{}' and maxDepth '{}'.", targetUrl, apiSpecUrl, maxDepth);

        spiderApi.optionSetApiSpecUrl(apiSpecUrl);
        spiderApi.optionMaxDepth(maxDepth);
        spiderApi.optionParseComments(true);
        spiderApi.optionParseGit(true);
        spiderApi.optionParseSvn(true);
        spiderApi.optionParseSitemap(true);
        spiderApi.optionParseRobotsTxt(true);

        return spiderApi.startSpiderAsUser(contextId, targetUrl, userId, SPIDER_MAX_CHILDREN);
    }

    public List<SpiderResultUrl> getSpiderResults(int scanId) {
        SpiderResult spiderResult = spiderApi.getSpiderResults(scanId);
        List<SpiderResultUrl> spiderResultUrlList;

        if (spiderResult.getFullResults() != null && !spiderResult.getFullResults().isEmpty()) {
            FullResults urlsInScope = spiderResult.getFullResults().get(0);
            spiderResultUrlList = spiderApi.filterAndExtendSpiderResults(urlsInScope.getUrlsInScope());
        } else {
            spiderResultUrlList = Collections.emptyList();
        }

        LOG.info("Found #{} spider URLs for the scanId:{}", spiderResultUrlList.size(), scanId);

        return spiderResultUrlList;
    }

    public String getSpiderResultAsJson(int scanId) {
        List<SpiderResultUrl> urls = getSpiderResults(scanId);
        ObjectMapper mapper = new ObjectMapper();
        String result = "";

        if (urls.isEmpty()) {
            result = "{}";
        } else {
            try {
                // Convert List to JSON
                result = mapper.writeValueAsString(urls);
            } catch (JsonProcessingException e) {
                LOG.error("Couldn't convert JSON String to List<SpiderResultUrl> object", e);
            }
        }

        return result;
    }

    public void recallSpiderUrlsAsJsonToScanner(String json) {
        List<SpiderResultUrl> list;
        ObjectMapper mapper = new ObjectMapper();

        // Convert JSON to List
        try {
            list = mapper.readValue(json, new TypeReference<List<SpiderResultUrl>>() {
            });

            recallSpiderUrlsToScanner(list);
        } catch (IOException e) {
            LOG.error("Couldn't convert JSON String to List<SpiderResultUrl> object", e);
        }
    }

    public void recallSpiderUrlsToScanner(List<SpiderResultUrl> requests) {
        recallSpiderUrlsToScanner(requests, "");
    }

    public void recallSpiderUrlsToScanner(List<SpiderResultUrl> requests, String targetUrl) {
        Collection<Cookie> cookies = null;

        // generate a first request to enforce a session cookie (if existing)
        // to use this for all following requests
        if (targetUrl != null && !targetUrl.isEmpty()) {
            cookies = recallSpiderUrlToScanner(targetUrl);
        } else if (requests.size() >= 1) {
            cookies = recallSpiderUrlToScanner(requests.get(0).getUrl());
        }

        recallSpiderUrlsToScanner(requests, cookies);
    }

    private Collection<Cookie> recallSpiderUrlToScanner(String requestUrl) {
        AsyncHttpClient client = new AsyncHttpClient();
        List<Cookie> resultCookies = null;

        if (!requestUrl.isEmpty()) {
            String host = config.getZapHost();
            int port = config.getZapPort();

            Response response;
            LOG.debug("Call sync to retrieve SESSION COOKIE with GET:{} via ZAP Proxy: {}:{}", requestUrl, host, port);
            try {
                response = client.prepareGet(requestUrl)
                        .setProxyServer(new ProxyServer(host, port))
                        .execute(new AsyncCompletionHandler<Response>() {
                            @Override
                            public Response onCompleted(Response response) {
                                LOG.debug("GET response: {} {}", response.getStatusCode(), response.getUri());

                                if (!response.getCookies().isEmpty()) {
                                    for (Cookie cookie : response.getCookies()) {
                                        LOG.info("Found cookie: {} {}", cookie.getName(), cookie.getValue());
                                    }
                                }
                                return response;
                            }

                            @Override
                            public void onThrowable(Throwable t) {
                                LOG.error("Error", t);

                            }
                        }).get();

                if (response != null && response.getCookies() != null) {
                    resultCookies = response.getCookies();
                }
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        client.close();

        return resultCookies;
    }

    public void recallSpiderUrlsToScanner(List<SpiderResultUrl> requests, Collection<Cookie> cookies) {
        AsyncHttpClient client = new AsyncHttpClient();

        LOG.debug("Trying to recall #{} requests.", requests.size());

        for (SpiderResultUrl request : requests) {
            try {
                recallSpiderUrlToScanner(request, client, cookies);
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Error", e);
            }
        }

        client.close();
    }

    public Collection<Cookie> recallSpiderAuthentication(String LOGinUrl, String usernameFieldId, String passwordFieldId, String user, String password, String LOGinQueryExtension, String LOGgedInIndicator, String LOGgedOutIndicator) {
        AsyncHttpClient client = new AsyncHttpClient();
        List<Cookie> cookies = null;

        String host = config.getZapHost();
        int port = config.getZapPort();
        Response response;

        LOG.info("Trying to initialize Zap with a new httpSession based on cookies");

        // Sync HTTP Call
        try {
            // Open Login page
            response = client.prepareGet(LOGinUrl)
                    .setProxyServer(new ProxyServer(host, port))
                    .execute(new AsyncCompletionHandler<Response>() {
                        @Override
                        public Response onCompleted(Response response) {
                            LOG.debug("GET initialize Zap response: {} {}", response.getStatusCode(), response.getUri());
                            if (!response.getCookies().isEmpty()) {
                                for (Cookie cookie : response.getCookies()) {
                                    LOG.info("Found initialize Zap cookie: {} {}", cookie.getName(), cookie.getValue());
                                }
                            }
                            return response;
                        }

                        @Override
                        public void onThrowable(Throwable t) {
                            LOG.error("Error while initialize Zap GET Request ", t);
                        }
                    }).get();

            // post LOGin credentials
            if (response != null && response.getCookies() != null) {
                cookies = response.getCookies();
                String LOGinPostData = usernameFieldId + '=' + user + '&' + passwordFieldId + '=' + password + LOGinQueryExtension;
                client.preparePost(LOGinUrl).setProxyServer(new ProxyServer(host, port)).setBody(LOGinPostData).setCookies(cookies).setHeader("Content-Type", "application/x-www-form-urlencoded").execute().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while initialize Zap GET Request ", e);
        }

        client.close();

        return cookies;
    }

    /**
     * @see <a href="https://asynchttpclient.github.io/async-http-client/proxy.html">AsyncHttpClient</a>
     */
    private void recallSpiderUrlToScanner(SpiderResultUrl request, AsyncHttpClient client, Collection<Cookie> cookies) throws InterruptedException, ExecutionException {
        if (!request.getUrl().isEmpty()) {
            String host = config.getZapHost();
            int port = config.getZapPort();
            int cookieSize = 0;

            if (cookies == null) {
                cookies = new HashSet<>();
            } else {
                cookieSize = cookies.size();
            }

            switch (request.getMethod()) {
                case "GET":
                    LOG.debug("Call async GET:{} with ZAP Proxy: {}:{} and #cookes: {}", request.getUrl(), host, port, cookieSize);
                    client.prepareGet(request.getUrl())
                            .setProxyServer(new ProxyServer(host, port))
                            .setCookies(cookies)
                            .execute(new AsyncCompletionHandler<Response>() {
                                @Override
                                public Response onCompleted(Response response) {
                                    LOG.debug("GET response: {} {}", response.getStatusCode(), response.getUri());
                                    if (!response.getCookies().isEmpty()) {
                                        for (Cookie cookie : response.getCookies()) {
                                            LOG.info("Found cookie: {} {}", cookie.getName(), cookie.getValue());
                                        }
                                    }
                                    return response;
                                }

                                @Override
                                public void onThrowable(Throwable t) {
                                    LOG.error("Error", t);
                                }
                            }).get();
                    break;
                case "POST":
                    LOG.debug("Call async POST:{} with ZAP Proxy: {}:{} and Post-Data: {}", request.getUrl(), host, port, request.getPostData());
                    client.preparePost(request.getUrl())
                            .setProxyServer(new ProxyServer(host, port))
                            .setBody(request.getPostData())
                            .setCookies(cookies).setHeader("Content-Type", "application/x-www-form-urlencoded").execute(new AsyncCompletionHandler<Response>() {
                        @Override
                        public Response onCompleted(Response response) {
                            LOG.debug("POST response: {}", response);
                            return response;
                        }

                        @Override
                        public void onThrowable(Throwable t) {
                            LOG.error("Error", t);
                        }
                    });

                    break;
                default:
                    LOG.debug("Nothing to do, methood: {} URL:{}", request.getMethod(), request.getUrl());
                    break;
            }
        }
    }

    public Scans getSpiderScans() {
        Scans scans = spiderApi.getScans();
        LOG.info(scans.toString());
        return scans;
    }

    /**
     * Start a new ZAP scanner against the targetUrl, without authentication.
     */
    public int startScanner(String targetUrl) {
        LOG.info("Starting scanner (without authentication) for targetUrl '{}'.", targetUrl);

        scanApi.optionEnableAllScanners();
        scanApi.optionHandleAntiCfrs(true);
        return scanApi.startScanner(targetUrl, false);
    }

    public int startScannerAsUser(int contextId, int userId, String targetUrl) {
        LOG.info("Starting scanner (with authentication) for targetUrl '{}'", targetUrl);

        scanApi.optionEnableAllScanners();
        scanApi.optionHandleAntiCfrs(true);
        return scanApi.startScannerAsUser(contextId, targetUrl, userId);
    }

    public List<Alert> getScannerResult(String targetUrl) {
        Alerts alerts = scanApi.getScanResults(targetUrl);

        LOG.info("Found #{} alerts for targetUrl: {}", alerts.getAlerts().size(), targetUrl);

        return alerts.getAlerts();
    }

    public String getScannerResultAsJson(String targetUrl) {
        List<Alert> urls = getScannerResult(targetUrl);

        ObjectMapper mapper = new ObjectMapper();
        String result = "";

        if (urls.isEmpty()) {
            result = "{}";
        } else {
            try {
                // Convert List to JSON
                result = mapper.writeValueAsString(urls);
            } catch (JsonProcessingException e) {
                LOG.error("Couldn't convert JSON String to List<Alert> object", e);
            }
        }

        return result;
    }

    public int getScannerProgressInPercent(int scanId) {
        return scanApi.getStatus(scanId).getStatusInPercent();
    }

    public Scans getScannerScans() {
        Scans scans = scanApi.getScans();
        LOG.info(scans.toString());
        return scans;
    }

    public void waitUntilScannerFinished(int scanId) {
        int progress = scanApi.getStatus(scanId).getStatusInPercent();
        try {
            while (progress < 100) {
                progress = scanApi.getStatus(scanId).getStatusInPercent();
                LOG.info("Scanner (ID: {}) progress: {}%", scanId, progress);
                Thread.sleep(SCANNER_POLLER_WAIT_UNTIL_TIME_IN_MS);
            }
        } catch (InterruptedException e) {
            LOG.error("Couldn't wait until scanner finished", e);
        }
    }

    public void waitUntilSpiderFinished(int scanId) {
        int progress = spiderApi.getStatus(scanId).getStatusInPercent();
        try {
            while (progress < 100) {
                progress = spiderApi.getStatus(scanId).getStatusInPercent();
                LOG.info("Spider (ID: {}) progress: {}%", scanId, progress);
                Thread.sleep(SPIDER_POLLER_WAIT_UNTIL_TIME_IN_MS);
            }
        } catch (InterruptedException e) {
            LOG.error("Couldn't wait until scanner finished", e);
        }
    }

    public String getVersion() {
        return sessionApi.getZapVersion();
    }


    @Override
    public StatusDetail statusDetail() {
        try {
            String zapVersion = getVersion();
            if (zapVersion != null && !zapVersion.isEmpty() && zapVersion.contains("version")) {
                LOG.debug("Internal status check: ok");
                return StatusDetail.statusDetail("ZapService", Status.OK, "up and running", singletonMap("ZAP Version", zapVersion));
            } else {
                return StatusDetail.statusDetail("ZapService", Status.WARNING, "Warning");
            }

        } catch (RuntimeException e) {
            LOG.debug("Error: indicating a status problem!", e);
            return StatusDetail.statusDetail("ZapService", Status.ERROR, e.getMessage());
        }
    }
}
