/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */
package io.securecodebox.zap.service.engine.model.zap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@JsonPropertyOrder(alphabetic = true)
@Data
public class ZapTargetAttributes {
    @JsonProperty("ZAP_BASE_URL")
    private String baseUrl;

    @JsonProperty("ZAP_SITEMAP")
    private List<ZapSitemapEntry> sitemap = new LinkedList<>();

    @JsonProperty("ZAP_SPIDER_MAX_DEPTH")
    private Integer spiderMaxDepth;
    @JsonProperty("ZAP_SPIDER_INCLUDE_REGEX")
    private List<String> spiderIncludeRegex;
    @JsonProperty("ZAP_SPIDER_EXCLUDE_REGEX")
    private List<String> spiderExcludeRegex;

    @JsonProperty("ZAP_SPIDER_API_SPEC_URL")
    private String spiderApiSpecUrl;

    @JsonProperty("ZAP_SCANNER_INCLUDE_REGEX")
    private List<String> scannerIncludeRegex;
    @JsonProperty("ZAP_SCANNER_EXCLUDE_REGEX")
    private List<String> scannerExcludeRegex;

    @JsonProperty("ZAP_AUTHENTICATION")
    private Boolean authentication;
    @JsonProperty("ZAP_LOGIN_SITE")
    private String loginSite;
    @JsonProperty("ZAP_LOGIN_USER")
    private String loginUser;
    @JsonProperty("ZAP_USERNAME_FIELD_ID")
    private String loginUsernameFieldId;
    @JsonProperty("ZAP_LOGIN_PW")
    private String loginPw;
    @JsonProperty("ZAP_PW_FIELD_ID")
    private String pwFieldId;
    @JsonProperty("ZAP_LOGGED_IN_INDICATOR")
    private String loggedInIndicator;
    @JsonProperty("LOGGED_OUT_INDICATOR")
    private String loggedOutIndicator;
    @JsonProperty("ZAP_CSRF_TOKEN_ID")
    private String csrfTokenId;

    @JsonProperty("ZAP_SPIDER_CONFIGURATION_TYPE")
    private String spiderConfigurationType;
    @JsonProperty("ZAP_SCANNER_CONFIGURATION_TYPE")
    private String scannerConfigurationType;

    @JsonProperty("ZAP_REPLACER_RULES")
    private ZapReplacerRule[] zapReplacerRules;

    /**
     * When set rest like structures will be used in reducing duplicates from Spider results.
     */
    @JsonProperty("SECURECODEBOX_REDUCE_SPIDER_RESULT_ON_REST_SCHEMAS")
    private boolean reduceSpiderOnRestSchemas = false;

    @JsonIgnore
    private List<String> removeEmptyAndNullValues(List<String> list){
        if (list == null) {
            return new LinkedList<>();
        }

        return list.stream()
                .filter(Objects::nonNull)
                .filter(regex -> !regex.trim().equals(""))
                .collect(Collectors.toList());
    }

    public List<String> getSpiderIncludeRegex() {
        return removeEmptyAndNullValues(spiderIncludeRegex);
    }
    public List<String> getSpiderExcludeRegex() {
        return removeEmptyAndNullValues(spiderExcludeRegex);
    }
    public List<String> getScannerIncludeRegex() {
        return removeEmptyAndNullValues(scannerIncludeRegex);
    }
    public List<String> getScannerExcludeRegex() {
        return removeEmptyAndNullValues(scannerExcludeRegex);
    }
}
