package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties()
public class Variables {


    @JsonProperty("ZAP_LAST_SERVICE_MESSAGE")
    private ProcessVariable lastServiceMessage;
    private ProcessVariable processUuid;
    @JsonProperty("ZAP_AUTHENTICATION")
    private ProcessVariable authentication;
    private ProcessVariable loggedInIndicator;
    private ProcessVariable loggedOutIndicator;
    private ProcessVariable loginPassword;
    private ProcessVariable loginSite;
    private ProcessVariable loginUser;
    private ProcessVariable passwordFieldId;
    @JsonProperty("ZAP_TARGET_URL")
    private ProcessVariable targetUrl;
    private ProcessVariable usernameFieldId;
    private ProcessVariable csrfTokenId;

    // TODO: rename
    @JsonProperty("PROCESS_SPIDER_FINDINGS")
    private ProcessVariable spiderResult;
    @JsonProperty("PROCESS_SPIDER_RAW_FINDINGS")
    private ProcessVariable spiderRawResult;
    @JsonProperty("PROCESS_SPIDER_ID")
    private ProcessVariable spiderMicroserviceId;
    @JsonProperty("ZAP_SPIDER_EXCLUDE_DUPLICATES")
    private ProcessVariable spiderExcludeDuplicates;
    @JsonProperty("ZAP_SPIDER_API_SPEC_URL")
    private ProcessVariable spiderApiSpecUrl;
    @JsonProperty("ZAP_SPIDER_EXCLUDE_REGEX")
    private ProcessVariable spiderExcludeRegexes;
    @JsonProperty("ZAP_SPIDER_INCLUDE_REGEX")
    private ProcessVariable spiderIncludeRegexes;
    @JsonProperty("ZAP_SPIDER_MAX_DEPTH")
    private ProcessVariable spiderMaxDepth;
    @JsonProperty("ZAP_SPIDER_TARGET_URL")
    private ProcessVariable spiderTargetUrl;

    @JsonProperty("PROCESS_SCANNER_ID")
    private ProcessVariable scannerMicroserviceId;
    @JsonProperty("PROCESS_FINDINGS")
    private ProcessVariable scannerResult;
    @JsonProperty("PROCESS_RAW_FINDINGS")
    private ProcessVariable rawScannerResult;
    @JsonProperty("ZAP_SCANNER_EXCLUDE_REGEX")
    private ProcessVariable scannerExcludeRegexes;
    @JsonProperty("ZAP_SCANNER_INCLUDE_REGEX")
    private ProcessVariable scannerIncludeRegexes;
    @JsonProperty("ZAP_SCANNER_TARGET_URL")
    private ProcessVariable scannerTargetUrl;

    public static List<String> getVariablesAsStringArray(){

        List<String> result = new LinkedList<>();

        for(Field field : Variables.class.getDeclaredFields()){
            if(field.getAnnotationsByType(JsonProperty.class).length != 0){
                result.add(field.getAnnotation(JsonProperty.class).value());
            }
            else {
                result.add(field.getName());
            }
        }
        return result;
    }
}
