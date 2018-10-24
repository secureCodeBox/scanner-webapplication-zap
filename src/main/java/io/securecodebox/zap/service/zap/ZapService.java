/*
 *
 *  *
 *  * SecureCodeBox (SCB)
 *  * Copyright 2015-2018 iteratec GmbH
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * 	http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package io.securecodebox.zap.service.zap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarRequest;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Reference;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapReplacerRule;
import io.securecodebox.zap.service.engine.model.zap.ZapSitemapEntry;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zaproxy.clientapi.core.*;
import org.zaproxy.clientapi.gen.Context;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
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

    private static ZapReplacerRule[] defaultRules = null;

    private final ZapConfiguration config;
    private ClientApi api;


    @Autowired
    public ZapService(ZapConfiguration config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        api = new ClientApi(config.getZapHost(), config.getZapPort());
    }

    private void resetDefaultRules () throws ClientApiException {
        if (defaultRules == null) {
            defaultRules = getCurrentReplacerRules();
        } else {
            Arrays.stream(getCurrentReplacerRules()).forEach(rule -> {
                try {
                    api.replacer.removeRule(rule.getDescription());
                } catch (ClientApiException e) {
                    log.error("Unable to remove replacer rule", e);
                }
            });
            Arrays.stream(defaultRules).forEach(rule -> {
                try {
                    api.replacer.addRule(
                            rule.getDescription(),
                            rule.getEnabled(),
                            rule.getMatchType(),
                            rule.getMatchRegex(),
                            rule.getMatchString(),
                            rule.getReplacement(),
                            rule.getInitiators()
                    );
                } catch (ClientApiException e) {
                    log.error("Unable to add default replacer rule", e);
                }
            });
        }
    }

    /**
     * Create a new session and context based on the given URL.
     *
     * @param targetUrl Target URL to create a new session and context for
     * @return Created context ID
     */
    public String createContext(String targetUrl, List<String> contextIncludeRegex, List<String> contextExcludeRegex) throws ClientApiException {
        log.info("Starting to create a new ZAP session '{}' and context '{}'.", SESSION_NAME, CONTEXT_NAME);

        contextIncludeRegex.add("\\Q" + targetUrl + "\\E.*");

        api.core.newSession(SESSION_NAME, "true");

        Context context = new Context(api);
        String contextId = getSingleResult(context.newContext(CONTEXT_NAME));
        for (String regex : contextIncludeRegex) {
            if (regex != null && !regex.isEmpty()) {
                context.includeInContext(CONTEXT_NAME, regex);
            }
        }

        for (String regex : contextExcludeRegex) {
            context.excludeFromContext(CONTEXT_NAME, regex);
        }

        api.sessionManagement.setSessionManagementMethod(contextId, "cookieBasedSessionManagement", null);
        api.httpSessions.createEmptySession(targetUrl, SESSION_NAME);
        api.httpSessions.setActiveSession(targetUrl, SESSION_NAME);

        return contextId;
    }

    public void clearSession() throws ClientApiException {
        api.core.newSession(SESSION_NAME, "true");
    }

    /**
     * Configure the authentication based on the given user name and password field.
     *
     * @param tokenId If non-empty the authentication is script-based instead of form-based
     * @return New user ID
     */
    public String configureAuthentication(String contextId, String loginUrl, String usernameFieldId, String passwordFieldId,
                                          String username, String password, String loginQueryExtension, String loggedInIndicator,
                                          String loggedOutIndicator, String tokenId) throws ClientApiException, UnsupportedEncodingException {
        log.info("Configuring ZAP based authentication for user '{}' and loginUrl '{}'", username, loginUrl);

        if (tokenId == null || tokenId.isEmpty()) {
            api.authentication.setAuthenticationMethod(contextId, AUTH_FORM_BASED, "loginUrl=" +
                    URLEncoder.encode(loginUrl, "UTF-8") + "&loginRequestData=" +
                    URLEncoder.encode(usernameFieldId + "={%username%}&" + passwordFieldId + "={%password%}" +
                            loginQueryExtension, "UTF-8"));
        } else {
            api.authentication.setAuthenticationMethod(contextId, AUTH_SCRIPT_BASED,
                    "scriptName=csrfAuthScript" + "&LoginURL=" + loginUrl + "&CSRFField=" +
                            tokenId + "&POSTData=" + URLEncoder.encode(usernameFieldId + "={%username%}&" +
                            passwordFieldId + "={%password%}&" + tokenId + "={%user_token%}", "UTF-8") + loginQueryExtension);
            api.acsrf.addOptionToken(tokenId);
            api.script.load("csrfAuthScript", "authentication", "Oracle Nashorn",
                    "csrfAuthScript.js", "csrfloginscript");
            // TODO First check if api.script.listScripts() contains "csrfAuthScript" ?
        }

        if (loggedInIndicator != null && !loggedInIndicator.isEmpty()) {
            api.authentication.setLoggedInIndicator(contextId, "\\Q" + loggedInIndicator + "\\E");
        }
        if (loggedOutIndicator != null && !loggedOutIndicator.isEmpty()) {
            api.authentication.setLoggedOutIndicator(contextId, "\\Q" + loggedOutIndicator + "\\E");
        }

        String userId = getSingleResult(api.users.newUser(contextId, AUTH_USER));
        api.users.setAuthenticationCredentials(contextId, userId, "username=" + username + "&password=" + password);
        api.users.setUserEnabled(contextId, userId, "true");
        api.forcedUser.setForcedUser(contextId, userId);
        api.forcedUser.setForcedUserModeEnabled(true);

        return userId;
    }

    /**
     * Recalls a request for putting it in the ZAP cache
     * This sends out all spider requests again proxied by the zap proxy.
     *
     * @param target the Target containing a sitemap with all requests to recall
     */
    public void recallTarget(Target target) {
        List<ZapSitemapEntry> sitemap = target.getAttributes().getSitemap();
        if (sitemap == null) {
            log.warn("Tried to recall a empty sitemap to Zap. The scan will fail as it will not have any targets to scan!");
            return;
        }

        ObjectMapper objMapper = new ObjectMapper();

        log.info("Recalling {} requests to zap.", sitemap.size());

        for (ZapSitemapEntry entry : sitemap) {
            try {
                String requestHar = objMapper.writeValueAsString(entry);
                byte[] response = api.core.sendHarRequest(requestHar, "");
                String msg = new String(response);
                log.debug("Recalled target to ZAP with following response {}", msg);
            } catch (JsonProcessingException e) {
                log.error("Couldn't convert Har Request Object to JSON string!", e);
            } catch (ClientApiException e) {
                log.error("Couldn't upload Har Request to ZAP!", e);
            }
        }
    }


    /**
     * Gets and converts the API wrapper request "replacer.rules()"
     * @return array of replacer rules
     * @throws ClientApiException can be thrown for any api request
     */
    private final ZapReplacerRule[] getCurrentReplacerRules () throws ClientApiException {
        return ((ApiResponseList) api.replacer.rules()).getItems().stream()
                .map(i -> ((ApiResponseSet) i))
                .map(i -> {
                    ZapReplacerRule rule = new ZapReplacerRule();
                    rule.setMatchType(i.getStringValue("matchType"));
                    rule.setDescription(i.getStringValue("description"));
                    rule.setMatchString(i.getStringValue("matchString"));
                    rule.setInitiators(i.getStringValue("initiators"));
                    rule.setMatchRegex(i.getStringValue("matchRegex"));
                    rule.setReplacement(i.getStringValue("replacement"));
                    rule.setEnabled(i.getStringValue("enabled"));
                    return rule;
                })
                .toArray(ZapReplacerRule[]::new);
    }

    /**
     * @param userId User ID to start the spider scan as, "-1" to ignore
     * @return New spider scan ID
     */
    public Object startSpiderAsUser(String targetUrl, String apiSpecUrl, int maxDepth, String contextId, String userId, ZapReplacerRule[] replacerRules) throws ClientApiException {
        log.info("Starting spider for targetUrl '{}' and with apiSpecUrl '{}' and maxDepth '{}'", targetUrl, apiSpecUrl, maxDepth);

        // store current rules (later resets to these default rules)
        resetDefaultRules();
        // then add any specified replacer rules
        if (replacerRules != null && replacerRules.length > 0) {
            for (ZapReplacerRule rule : replacerRules) {
                log.info("Found custom rule: " + rule.getDescription()
                        + "\n  | enabled: " + rule.getEnabled()
                        + "\n  | matchType: " + rule.getMatchType()
                        + "\n  | matchRegex: " + rule.getMatchRegex()
                        + "\n  | matchString: " + rule.getMatchString()
                        + "\n  | replacement: " + rule.getReplacement()
                        + "\n  | initiators: " + rule.getInitiators());
                api.replacer.addRule(rule.getDescription(), rule.getEnabled(), rule.getMatchType(), rule.getMatchRegex(), rule.getMatchString(), rule.getReplacement(), rule.getInitiators());
            }
        }

        if (apiSpecUrl != null && !apiSpecUrl.isEmpty()) {
            api.openapi.importUrl(apiSpecUrl, null);
        }
        api.spider.setOptionMaxDepth(maxDepth);
        api.spider.setOptionParseComments(true);
        api.spider.setOptionParseGit(true);
        api.spider.setOptionParseSVNEntries(true);
        api.spider.setOptionParseSitemapXml(true);
        api.spider.setOptionParseRobotsTxt(true);

        ApiResponse response = ("-1".equals(userId))
                ? api.spider.scan(targetUrl, "-1", null, CONTEXT_NAME, null)
                : api.spider.scanAsUser(contextId, userId, targetUrl, "-1", null, null);

        // removeCustomReplacerRules(defaultReplacerRules, replacerRules);

        return getSingleResult(response);
    }


    /**
     * @param userId User ID to start the scan as, "-1" to ignore
     * @param delayInMs delay between reuests (optional)
     * @param threadsPerHost maximum number of concurrent connections to host (optional)
     * @param replacerRules replacer plugin rules, see
     *                         https://github.com/zaproxy/zap-extensions/wiki/HelpAddonsReplacerReplacer
     * @return New scanner scan ID
     */
    public Object startScannerAsUser(String targetUrl, String contextId, String userId, Integer delayInMs, Integer threadsPerHost, ZapReplacerRule[] replacerRules) throws ClientApiException {
        log.info("Starting scanner for targetUrl '{}' and userId {}.", targetUrl, userId);

        Integer defaultDelay = Integer.valueOf(api.ascan.optionDelayInMs().toString());
        Integer defaultThreads = Integer.valueOf(api.ascan.optionThreadPerHost().toString());

        resetDefaultRules();

        api.ascan.enableAllScanners(null);
        api.ascan.setOptionHandleAntiCSRFTokens(true);
        if (delayInMs != null) {
            api.ascan.setOptionDelayInMs(delayInMs);
        }
        if (threadsPerHost != null) {
            api.ascan.setOptionThreadPerHost(threadsPerHost);
        }
        if (replacerRules != null && replacerRules.length > 0) {
            for (ZapReplacerRule rule : replacerRules) {
                api.replacer.addRule(
                        rule.getDescription(),
                        rule.getEnabled(),
                        rule.getMatchType(),
                        rule.getMatchRegex(),
                        rule.getMatchString(),
                        rule.getReplacement(),
                        rule.getInitiators());
            }
        }

        ApiResponse response = ("-1".equals(userId))
                ? api.ascan.scan(targetUrl, "true", "false", null, null, null)
                : api.ascan.scanAsUser(targetUrl, contextId, userId, "true", null, null, null);

        api.ascan.setOptionThreadPerHost(defaultThreads);
        api.ascan.setOptionDelayInMs(defaultDelay);

        // removeCustomReplacerRules(defaultReplacerRules, replacerRules);

        return getSingleResult(response);
    }

    private final void removeCustomReplacerRules (final ZapReplacerRule[] defaultRules, final ZapReplacerRule[] customRules) throws ClientApiException {
        if (customRules != null && customRules.length > 0) {
            for (ZapReplacerRule rule : customRules) {
                // remove custom rule
                api.replacer.removeRule(rule.getDescription());
                // reset to default: if a default rule exists with the same description, add the default rule
                for (ZapReplacerRule defaultReplacerRule : defaultRules) {
                    if (rule.getDescription().equals(defaultReplacerRule.getDescription())) {
                        api.replacer.addRule(
                                defaultReplacerRule.getDescription(),
                                defaultReplacerRule.getEnabled(),
                                defaultReplacerRule.getMatchType(),
                                defaultReplacerRule.getMatchRegex(),
                                defaultReplacerRule.getMatchString(),
                                defaultReplacerRule.getReplacement(),
                                defaultReplacerRule.getInitiators());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Wait until the spider scan finished, then return its result.
     *
     * @return JSON string
     */
    public List<Finding> retrieveSpiderResult(String scanId) throws ClientApiException {
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

        ApiResponse response = api.spider.fullResults(scanId);
        List<Finding> findings = new ArrayList<>(1);
        if (response instanceof ApiResponseList) {
            findings = ((ApiResponseList) response).getItems().stream()
                    .map(i -> ((ApiResponseList) i).getItems())
                    .flatMap(Collection::stream)
                    .filter(r -> r instanceof ApiResponseSet)
                    .map(r -> {
                        Finding finding = new Finding();
                        HarRequest harRequest = getHarRequestPortionForRequest(((ApiResponseSet)r).getStringValue("messageId"));
                        finding.setLocation(harRequest.getUrl());
                        finding.getAttributes().put("request", harRequest);
                        return finding;
                    })
                    .collect(Collectors.toList());
        }

        log.info("Found #{} spider URLs for the scanId:{}", findings.size(), scanId);

        return findings;
    }

    /**
     * Wait until the scanner scan finished, then return its result.
     *
     * @return JSON string
     */
    public List<Finding> retrieveScannerResult(String scanId, String targetUrl) throws ClientApiException {
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

        List<Finding> findings = result.stream().map(alert -> {
            Finding finding = new Finding();
            finding.setLocation(alert.getUrl());
            finding.setName(alert.getName());
            finding.setSeverity(alert.getRisk().name());
            finding.setDescription(alert.getDescription());
            finding.setHint(alert.getSolution());
            finding.setCategory(alert.getName());

            finding.getAttributes().put("HAR", getHarForRequest(alert.getMessageId()));
            finding.getAttributes().put("OTHER", alert.getOther());
            finding.getAttributes().put("ATTACK", alert.getAttack());
            finding.getAttributes().put("CONFIDENCE", alert.getConfidence().name());
            finding.getAttributes().put("EVIDENCE", alert.getEvidence());
            finding.getAttributes().put("WASC_ID", alert.getWascId());
            finding.getAttributes().put("PLUGIN_ID", alert.getPluginId());
            finding.getAttributes().put("OTHER_REFERENCES", alert.getReference().split("\n"));

            Reference reference = new Reference();
            reference.setId("CVE-"+ alert.getCweId());
            reference.setSource("https://cwe.mitre.org/data/definitions/" + alert.getCweId() +".html");
            finding.setReference(reference);

            return finding;
        }).collect(Collectors.toList());
        log.info("Found #{} alerts for targetUrl: {}", result.size(), targetUrl);

        return findings;
    }

    private static String getSingleResult(ApiResponse response) {
        if (response instanceof ApiResponseElement) {
            return ((ApiResponseElement) response).getValue();
        }
        return "";
    }

    /**
     * Returns the HAR (HTTP Archive) of a single entry in the ZAP Tree.
     * Currently only the request portion is required and used.
     *
     * This sends a Request to the ZapAPI
     *
     * @param requestId aka MessageId
     * @return HAR of the requestId
     */
    Har getHarForRequest(String requestId) {
        HarReader harReader = new HarReader();

        try {
            String harString = new String(api.core.messageHar(requestId));
            return harReader.readFromString(harString);
        } catch (ClientApiException e) {
            log.warn("Could not fetch Request HAR Object from ZAP.", e.getMessage());
        } catch (HarReaderException e) {
            log.warn("Could not parse Request HAR Object returned from ZAP.", e.getMessage());
        }
        return null;
    }

    HarRequest getHarRequestPortionForRequest(String requestId){
        Har har = getHarForRequest(requestId);

        if(har.getLog().getEntries().size() == 0){
            return null;
        }

        return har.getLog().getEntries().get(0).getRequest();
    }

    /**
     * This status checks if the configured ZAP API is reachable and returning a API result.
     *
     * @return
     */
    @Override
    public StatusDetail statusDetail() {
        try {
            String version = this.getVersion();

            if (version != null && !version.isEmpty()) {
                log.debug("Internal status check: ok");
                return StatusDetail.statusDetail("ZAP API", Status.OK, "The ZAP API is up and running", singletonMap("ZAP Version", version));
            } else {
                return StatusDetail.statusDetail("ZAP API", Status.WARNING, "Warning, couldn't find any ZAP version information's. Propably an error occurred!");
            }
        } catch (ClientApiException e) {
            log.debug("Error: indicating a status problem!", e);
            return StatusDetail.statusDetail(getClass().getSimpleName(), Status.ERROR, e.getMessage());
        }
    }

    public String getVersion() throws ClientApiException {
        return getSingleResult(api.core.version());
    }

    public String getRawReport() throws ClientApiException {
        return new String(api.core.xmlreport());
    }
}
