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
package io.securecodebox.zap.service.zap.deduplication;

import de.sstoehr.harreader.model.HarPostData;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Target;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SpiderDuplicateReducerTest {

    Target basicTarget, restConfiguredTarget;

    @Before
    public void setUp(){
        basicTarget = new Target("http://bodgeit:8080/");

        restConfiguredTarget = new Target("http://bodgeit:8080/");
        restConfiguredTarget.getAttributes().setReduceSpiderOnRestSchemas(true);
    }

    Finding spiderFinding(String url, HttpMethod method, String payload){
        Finding finding = new Finding();
        finding.setLocation(url);

        HarRequest request = new HarRequest();
        request.setMethod(method);
        HarPostData postData = new HarPostData();
        postData.setText(payload);

        finding.getAttributes().put("request", request);

        return finding;
    }

    Finding spiderFinding(String url){
        return spiderFinding(url, HttpMethod.GET, null);
    }

    @Test
    public void removesBasicDuplicatesFromGetRequests(){
        List<Finding> findings = new LinkedList<>();
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=1&baz=3"));
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=2&baz=4"));

        assertEquals(2, findings.size());

        new SpiderDuplicateReducer().reduce(findings, basicTarget);

        assertEquals(1, findings.size());
    }

    @Test
    public void removesOnlyDuplicatesFromGetRequests(){
        List<Finding> findings = new LinkedList<>();
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=1&baz=3"));
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=1&trolol=3"));
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=2&baz=4"));

        assertEquals(3, findings.size());

        new SpiderDuplicateReducer().reduce(findings, basicTarget);

        assertEquals(2, findings.size());
    }



    @Test
    public void restbasedReductionsRemovesTheRightPartsOfTheUrl(){
        String url = "http://bodgeit:8080/bodgeit/foo/123/bar";
        String reducedUrl = new SpiderDuplicateReducer().reduceParametersFromRestLikeString(url);

        assertEquals("http://bodgeit:8080/bodgeit/foo//bar", reducedUrl);

        url = "http://bodgeit:8080/bodgeit/foo12/123/bar";
        reducedUrl = new SpiderDuplicateReducer().reduceParametersFromRestLikeString(url);

        assertEquals("http://bodgeit:8080/bodgeit/foo12//bar", reducedUrl);

        url = "http://bodgeit:8080/bodgeit/foo12/123/bar/123";
        reducedUrl = new SpiderDuplicateReducer().reduceParametersFromRestLikeString(url);

        assertEquals("http://bodgeit:8080/bodgeit/foo12//bar/", reducedUrl);
    }

    @Test
    public void restBasedReductionStillWorksOnQueryParameters(){
        List<Finding> findings = new LinkedList<>();
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=1&baz=3"));
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=1&trolol=3"));
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo?bar=2&baz=4"));

        assertEquals(3, findings.size());

        new SpiderDuplicateReducer().reduce(findings, restConfiguredTarget);

        assertEquals(2, findings.size());
    }

    @Test
    public void restBasedReductionReducesRestBasedUrlStructuresSimple(){
        List<Finding> findings = new LinkedList<>();
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo/123/bar"));
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo/124/bar"));

        assertEquals(2, findings.size());

        new SpiderDuplicateReducer().reduce(findings, restConfiguredTarget);

        assertEquals(1, findings.size());
    }

    @Test
    public void restBasedReductionReducesRestBasedUrlStructuresInvolved(){
        List<Finding> findings = new LinkedList<>();
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo/123/bar")); // In
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo/124/bar")); // Out
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo/1555/bar")); // Out
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo12/1555/bar")); // In
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo12/1336/bar")); // Out
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo12/1336/bar")); // Out
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo12/1336/bar/123")); // In
        findings.add(spiderFinding("http://bodgeit:8080/bodgeit/foo12/1336/bar/12666")); // Out

        assertEquals(8, findings.size());

        new SpiderDuplicateReducer().reduce(findings, restConfiguredTarget);

        assertEquals(3, findings.size());
    }

}