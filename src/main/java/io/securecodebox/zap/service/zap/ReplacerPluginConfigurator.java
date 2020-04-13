package io.securecodebox.zap.service.zap;

import io.securecodebox.zap.service.engine.model.zap.ZapReplacerRule;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.zaproxy.clientapi.core.ApiResponseList;
import org.zaproxy.clientapi.core.ApiResponseSet;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

@Slf4j
public class ReplacerPluginConfigurator {

    private ClientApi api;

    public ReplacerPluginConfigurator(ClientApi api) {
        this.api = api;
    }

    public void configureZapWithReplacerRules(ZapReplacerRule[] replacerRules) throws ClientApiException {
        resetReplacerRules();
        if (replacerRules != null && replacerRules.length > 0) {
            log.debug("Adding {} custom ZAP replacer rules", replacerRules.length);
            addReplacerRules(replacerRules);
        } else {
            log.info("No custom ZAP replacer rule defined.");
        }
    }

    /**
     * Gets and converts the API wrapper request "replacer.rules()"
     *
     * @return array of replacer rules
     * @throws ClientApiException can be thrown for any api request
     */
    final ZapReplacerRule[] getCurrentReplacerRules() throws ClientApiException {
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
     * Adds ZAP replacer rules
     *
     * @param rules
     * @throws ClientApiException thrown if at least one rule cannot be set
     */
    void addReplacerRules(ZapReplacerRule[] rules) throws ClientApiException {
        if (rules != null && rules.length > 0) {
            log.debug("Adding #{} exiting replacer rules", rules.length);
            for (ZapReplacerRule rule: rules) {
                if (rule != null) {
                    addReplacerRule(rule);
                } else {
                    log.warn("Couldn't add the replacer rule, the rule must not be null ot empty.");
                }
            }
        } else {
            log.warn("There is no replacer rule to add.");
        }
    }

    void resetReplacerRules() throws ClientApiException {
        log.debug("Resetting ZAP replacer rules");
        ZapReplacerRule[] currentRules = getCurrentReplacerRules();
        if (currentRules != null && currentRules.length > 0) {
            removeReplacerRules(currentRules);
        }
    }

    /**
     * Adds ZAP replacer rule
     *
     * @param rule
     * @throws ClientApiException thrown if rule cannot be set
     */
    private void addReplacerRule(@NonNull ZapReplacerRule rule) throws ClientApiException {
        api.replacer.addRule(
                rule.getDescription(),
                rule.getEnabled(),
                rule.getMatchType(),
                rule.getMatchRegex(),
                rule.getMatchString(),
                rule.getReplacement(),
                rule.getInitiators());
    }

    /**
     * Removes the given list of ZAP replacer rules.
     *
     * @param rules The list of ZAP replacer rules to remove.
     * @throws ClientApiException thrown if at least one of the rules cannot be removed
     */
    private void removeReplacerRules(@NonNull ZapReplacerRule[] rules) throws ClientApiException {
        if (rules != null && rules.length > 0) {
            log.debug("Removing #{} exiting replacer rules", rules.length);
            for (ZapReplacerRule rule : rules){
                removeReplacerRule(rule);
            }
        } else {
            log.warn("There are no replacer rules to remove.");
        }
    }

    /**
     * Removes a single ZAP replacer rule.
     *
     * @param rule The ZAP replacer rule to remove.
     * @throws ClientApiException thrown if rule cannot be removed
     */
    private void removeReplacerRule(@NonNull ZapReplacerRule rule) throws ClientApiException {
        if (rule != null) {
            api.replacer.removeRule(rule.getDescription());
        } else {
            log.warn("You can't remove a replacer rule which is null.");
        }
    }
}
