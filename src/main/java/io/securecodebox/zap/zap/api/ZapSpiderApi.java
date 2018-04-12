package io.securecodebox.zap.zap.api;

import io.securecodebox.zap.zap.model.Scans;
import io.securecodebox.zap.zap.model.Status;
import io.securecodebox.zap.zap.model.ZapApiCall;
import io.securecodebox.zap.zap.model.spider.SpiderResult;
import io.securecodebox.zap.zap.model.spider.SpiderResultUrl;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;


@Service
@Slf4j
public class ZapSpiderApi extends ZapBaseApi {

    private static final Logger LOG = LoggerFactory.getLogger(ZapSpiderApi.class);

    public int startSpider(String contextName, String targetUrl, int maxChildren) {
        Map<String, String> parameters = new HashMap<>(3);
        parameters.put("contextName", contextName);
        parameters.put("url", targetUrl);
        if (maxChildren >= 0) {
            parameters.put("maxChildren", String.valueOf(maxChildren));
        }

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SpiderApi.START_SPIDER, parameters), String.class, parameters);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("Successfully started spider scan for '{}' for context '{}'.", targetUrl, contextName);
            Map<String, Object> map = convertJsonStringToMap(response);
            return Integer.parseInt(map.get("scan").toString());
        } else {
            throw new IllegalStateException(String.format("Couldn't start spider scan for '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", targetUrl, contextName, response, responseEntity.getStatusCode()));
        }
    }

    public int startSpiderAsUser(int contextId, String targetUrl, int userId, int maxChildren) {
        Map<String, String> parameter = new HashMap<>(4);
        parameter.put("url", targetUrl);
        parameter.put("contextId", String.valueOf(contextId));
        parameter.put("userId", String.valueOf(userId));
        if (maxChildren >= 0) {
            parameter.put("maxChildren", String.valueOf(maxChildren));
        }

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SpiderApi.START_SPIDER_AS_USER, parameter), String.class, parameter);
        String response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("Successfully started spider scan as user '{}' for '{}' for context '{}'.", userId, targetUrl, contextId);
            Map<String, Object> map = convertJsonStringToMap(response);
            return Integer.parseInt(map.get("scanAsUser").toString());
        } else {
            throw new IllegalStateException(String.format("Couldn't start spider scan for '%s' for context '%s', failed with response '%s' and HTTP code '%s'.", targetUrl, contextId, response, responseEntity.getStatusCode()));
        }
    }

    public SpiderResult getSpiderResults(int scanId) {
        Map<String, String> parameter = Collections.singletonMap("scanId", String.valueOf(scanId));

        ResponseEntity<SpiderResult> responseEntity = restTemplate.getForEntity(getZapApiJsonUrl(SpiderApi.VIEW_SCAN_RESULTS, parameter), SpiderResult.class, parameter);
        SpiderResult response = responseEntity.getBody();

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOG.debug("Successful retrieved spider scan {} result for '{}'.", response, scanId);
            return response;
        } else {
            throw new IllegalStateException(String.format("Couldn't start spider scan for '%s', failed with response '%s' and HTTP code '%s'.", scanId, response, responseEntity.getStatusCode()));
        }
    }

    /**
     * Filters all result URLs with HTTP status code 200 and extends the URLs with additional information.
     */
    public List<SpiderResultUrl> filterAndExtendSpiderResults(List<SpiderResultUrl> urls) {
        JSONParser parser = new JSONParser();
        List<SpiderResultUrl> result = new ArrayList<>();

        for (SpiderResultUrl url : urls) {
            HttpStatus statusCode = HttpStatus.valueOf(Integer.parseInt(url.getStatusCode()));

            if (statusCode.is2xxSuccessful() || statusCode.is3xxRedirection() || statusCode.is1xxInformational()) {
                String message = getMessageHAR(url.getMessageId());

                if (message != null && !message.isEmpty()) {
                    try {
                        Object obj = parser.parse(message);
                        JSONObject jsonObject = (JSONObject) obj;
                        JSONArray entries = (JSONArray) ((JSONObject) jsonObject.get("LOG")).get("entries");

                        if (entries.size() == 1) {
                            JSONObject entry = (JSONObject) entries.get(0);

                            String requestDateTime = (String) entry.get("startedDateTime");
                            Long responseTime = (Long) entry.get("time");

                            url.setResponseTime(responseTime);
                            url.setRequestDateTime(requestDateTime);

                            JSONObject request = (JSONObject) entry.get("request");

                            JSONObject postData = (JSONObject) request.get("postData");
                            JSONArray postParameters = (JSONArray) postData.get("params");

                            JSONArray headers = (JSONArray) request.get("headers");
                            JSONArray queryString = (JSONArray) request.get("queryString");
                            JSONArray cookies = (JSONArray) request.get("cookies");

                            url.setPostData(formatParameterDataToString(postParameters, "&"));
                            url.setHeaders(formatParameterDataToString(headers, "&"));
                            url.setQueryString(formatParameterDataToString(queryString, "&"));
                            url.setCookies(formatParameterDataToString(cookies, ";"));

                            LOG.debug("Successfully extended spider result with messageId {} to '{}'.", url, url.getMessageId());
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException("Couldn't parse message with id " + url.getMessageId(), e);
                    }
                } else {
                    throw new RuntimeException("Couldn't parse message with id " + url.getMessageId() + ", the message is empty!");
                }
                result.add(url);  // Only add URLs with status code 200 & 3xx & 1xx
            }
        }
        return result;
    }

    /**
     * Reformatting the message format from JSON to flat string.
     */
    private static String formatParameterDataToString(JSONArray postParameterList, CharSequence concatChar) {
        String postDataString = "";
        JSONObject postParameter;

        if (!postParameterList.isEmpty()) {
            for (Object object : postParameterList) {
                postParameter = (JSONObject) object;
                try {
                    postDataString += URLEncoder.encode(postParameter.get("name").toString(), "utf-8") + "=" + URLEncoder.encode(postParameter.get("value").toString(), "utf-8") + concatChar;
                } catch (UnsupportedEncodingException e) {
                    postDataString += postParameter.get("name") + "=" + postParameter.get("value") + concatChar;
                    LOG.error("Couldn't URl encode the parameter string, therefore ignoring the URLEncoding", e);
                }
            }
            postDataString = postDataString.substring(0, postDataString.length() - concatChar.length()); //remove last "&"
        }
        return postDataString;
    }

    public Status getStatus(int scanId) {
        return zapApiCall(SpiderApi.VIEW_SCAN_STATUS, "scanId", String.valueOf(scanId), Status.class);
    }

    public Scans getScans() {
        return zapApiCall(SpiderApi.VIEW_SCAN_LIST, Scans.class);
    }

    public void optionExcludeFromScan(String regex) {
        zapApiCall(SpiderApi.OPTION_EXCLUDE_FROM_SCAN, "regex", regex);
    }

    /**
     * Sets the API specification URL option, if not blank.<br>
     * <u>Note:</u> It only sets the OpenAPI parameter. SOAP, which is also supported, is disabled for now as it's less common and the provided type cannot be determined easily.
     */
    public void optionSetApiSpecUrl(String url) {
        if (!url.trim().isEmpty()) {
            zapApiCall(SpiderApi.OPTION_SET_OPENAPI_SPEC_URL, "url", url);
        }
    }


    public void optionMaxDepth(int maxDepth) {
        zapApiCall(SpiderApi.OPTION_SET_MAX_DEPTH, "Integer", String.valueOf(maxDepth));
    }

    public void optionParseComments(boolean enable) {
        zapApiCall(SpiderApi.OPTION_SET_PARSE_COMMENTS, "Boolean", String.valueOf(enable));
    }

    public void optionParseGit(boolean enable) {
        zapApiCall(SpiderApi.OPTION_SET_PARSE_GIT, "Boolean", String.valueOf(enable));
    }

    public void optionParseSvn(boolean enable) {
        zapApiCall(SpiderApi.OPTION_SET_PARSE_SVN, "Boolean", String.valueOf(enable));
    }

    public void optionParseSitemap(boolean enable) {
        zapApiCall(SpiderApi.OPTION_SET_PARSE_SITEMAP, "Boolean", String.valueOf(enable));
    }

    public void optionParseRobotsTxt(boolean enable) {
        zapApiCall(SpiderApi.OPTION_SET_PARSE_ROBOTS, "Boolean", String.valueOf(enable));
    }

    public void stopSpider(int spiderScanId) {
        zapApiCall(SpiderApi.STOP_SPIDER, "scanId", String.valueOf(spiderScanId));
    }

    public void stopAllSpiders() {
        zapApiCall(SpiderApi.STOP_ALL_SPIDERS);
    }

    public void removeSpider(int spiderScanId) {
        zapApiCall(SpiderApi.REMOVE_SPIDER, "scanId", String.valueOf(spiderScanId));
    }

    public void removeAllSpiders() {
        zapApiCall(SpiderApi.REMOVE_ALL_SPIDERS);
    }

    public int getSpiderStatus(int spiderScanId) {
        return spiderScanId;
    }


    private enum SpiderApi implements ZapApiCall {
        VIEW_SCAN_LIST("spider/view/scans/"),
        VIEW_SCAN_STATUS("spider/view/status/"),
        VIEW_SCAN_RESULTS("spider/view/fullResults/"),
        OPTION_EXCLUDE_FROM_SCAN("spider/action/excludeFromScan/"),
        /**
         * Requires <a href="https://github.com/zaproxy/zap-extensions/wiki/HelpAddonsOpenapiOpenapi">Open API Specification Support</a> addon.
         */
        OPTION_SET_OPENAPI_SPEC_URL("openapi/action/importUrl/"),
        /**
         * Requires <a href="https://github.com/zaproxy/zap-extensions/wiki/HelpAddonsSoapSoap">SOAP Scanner</a> addon.
         */
        OPTION_SET_SOAP_SPEC_URL("soap/action/importUrl/"),
        OPTION_SET_MAX_DEPTH("spider/action/setOptionMaxDepth/"),
        OPTION_SET_PARSE_COMMENTS("spider/action/setOptionParseComments/"),
        OPTION_SET_PARSE_GIT("spider/action/setOptionParseGit/"),
        OPTION_SET_PARSE_SVN("spider/action/setOptionParseSVNEntries/"),
        OPTION_SET_PARSE_ROBOTS("spider/action/setOptionParseRobotsTxt/"),
        OPTION_SET_PARSE_SITEMAP("spider/action/setOptionParseSitemapXml/"),
        START_SPIDER("spider/action/scan/"),
        START_SPIDER_AS_USER("spider/action/scanAsUser/"),
        STOP_SPIDER("spider/action/stop/"),
        STOP_ALL_SPIDERS("spider/action/stopAllScans/"),
        REMOVE_SPIDER("spider/action/removeScan/"),
        REMOVE_ALL_SPIDERS("spider/action/removeAllScans/");

        private String url = null;

        private String s;

        SpiderApi(String s) {
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
