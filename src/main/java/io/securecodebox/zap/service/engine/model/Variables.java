package io.securecodebox.zap.service.engine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

@Data
public class Variables {

    private ProcessVariable lastServiceMessage;
    private ProcessVariable processUuid;
    private ProcessVariable authentication;
    private ProcessVariable loggedInIndicator;
    private ProcessVariable loggedOutIndicator;
    private ProcessVariable loginPassword;
    private ProcessVariable loginSite;
    private ProcessVariable loginUser;
    private ProcessVariable passwordFieldId;
    private ProcessVariable scannerExcludeRegexes;
    private ProcessVariable scannerIncludeRegexes;
    private ProcessVariable scannerTargetUrl;
    private ProcessVariable spiderResult;
    private ProcessVariable targetUrl;
    private ProcessVariable usernameFieldId;
    private ProcessVariable csrfTokenId;
    private ProcessVariable spiderExcludeDuplicates;
    private ProcessVariable spiderApiSpecUrl;
    private ProcessVariable spiderExcludeRegexes;
    private ProcessVariable spiderIncludeRegexes;
    private ProcessVariable spiderMaxDepth;
    @JsonProperty("ZAP_SPIDER_TARGET_URL")
    private ProcessVariable spiderTargetUrl;

    public static Collection<String> getVariablesAsStringArray(){

        Collection<String> result = new LinkedList<>();

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
