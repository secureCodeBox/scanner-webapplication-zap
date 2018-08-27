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
package io.securecodebox.zap.service.zap.deduplication;

import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SpiderDuplicateReducer {
    DuplicationReductionUtil util = new DuplicationReductionUtil();

    public void reduce(List<Finding> findings, Target target){
        if (findings == null) {
            return;
        }

        log.info("Spider sitemap size before duplicate removal: '{}'", findings.size());

        Set<String> uniqueUrls = new HashSet<>();
        Set<Finding> findingSet = new HashSet<>();

        for (Finding f : findings) {
            if(isGet(f)){
                String uniqueUrl = reduceDuplicationInUrl(f.getLocation(), target);

                if (!uniqueUrls.contains(uniqueUrl)) {
                    uniqueUrls.add(uniqueUrl);
                    findingSet.add(f);
                }
            } else {
                findingSet.add(f);
            }
        }
        findings.clear();
        findings.addAll(findingSet);

        log.info("Spider sitemap size after duplicate removal: '{}'", findings.size());

    }

    private static boolean isGet(Finding f) {
        Map<String, Object> attributes = f.getAttributes();
        if (attributes.containsKey("request")){
            try {
                HarRequest request = (HarRequest) attributes.get("request");
                return request.getMethod().equals(HttpMethod.GET);
            } catch(Exception e){
                log.error("Could not find required 'request' attribute in the spider result.");
                throw new RuntimeException("Could not find required 'request' attribute in the spider result.");
            }
        }
        return false;
    }

    /**
     *
     * @param url
     * @param target target specifing specific reduction settings
     * @return url without parts which tend to be dynamic parameters
     */
    protected String reduceDuplicationInUrl(String url, Target target){
        String reducedString = util.removeQueryValues(url);

        if(target.getAttributes().isReduceSpiderOnRestSchemas()){
            reducedString = reduceParametersFromRestLikeString(reducedString);
        }

        return reducedString;
    }


    /**
     * Removes parameter from rest like urls
     * Example "http://bodgeit/fooo/123/foo1/11" => "http://bodgeit/fooo//foo1/"
     * @param url
     * @return
     */
    String reduceParametersFromRestLikeString(String url){
        List<String> urlParts = Arrays.stream(url.split("/"))
                .map(part -> StringUtils.isNumeric(part) ? "" : part)
                .collect(Collectors.toList());

        return String.join("/", urlParts);
    }
}
