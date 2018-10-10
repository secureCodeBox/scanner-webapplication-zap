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
package io.securecodebox.zap.jobs.definition;

import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.CompleteTask;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Reference;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapTask;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.service.zap.ZapService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EngineWorkerJobTest {

    @Mock
    private JobEventPublisher eventPublisher;
    @Mock
    private ZapTaskService taskService;
    @Mock
    private ZapConfiguration config;
    @Mock
    private ZapService zapService;

    @InjectMocks
    private EngineWorkerJob engineWorkerJob = new EngineWorkerJob();

    private ZapTask spiderTask;
    private ZapTask scannerTask;

    @Before
    public void setUp() throws ClientApiException {
        MockitoAnnotations.initMocks(this);
        when(
                taskService.completeTask(any(), any(), any(), any()))
                .thenReturn(new CompleteTask("1", "1", "zap", new LinkedList<>(), "[]")
                );
        when(zapService.retrieveScannerResult(any(), any()))
                .thenReturn(new LinkedList<>());
    }

    @Test
    public void testContextCreation() throws ClientApiException {
        createSpiderTask();
        createScannerTask();
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(2)).createContext(eq("http://aSeriousUrl.com"), any(), any());
    }

    @Test
    public void testScanningWithMoreTargetShouldCreateMoreContexts() throws ClientApiException {
        createScannerTask();

        Target t2 = new Target("http://landOfPudding.com");
        t2.getAttributes().setBaseUrl("http://landOfPudding.com");
        scannerTask.getTargets().add(t2);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).createContext(eq("http://landOfPudding.com"), any(), any());
        verify(zapService, times(2)).createContext(any(), any(), any());

    }

    @Test
    public void testScanningWithMoreTargetWithSameBaseShouldCreateDifferentContexts() throws ClientApiException {
        createScannerTask();

        Target t2 = new Target("http://landOfPudding.com");
        t2.getAttributes().setBaseUrl("http://aSeriousUrl.com");
        scannerTask.getTargets().add(t2);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(2)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(2)).createContext(any(), any(), any());

    }

    @Test
    public void testSpiderConfiguredAndStarted() throws ClientApiException {
        createSpiderTask();
        when(zapService.createContext(eq("http://aSeriousUrl.com"), any(), any())).thenReturn("1");
        when(zapService.startSpiderAsUser(eq("http://aSeriousUrl.com"), any(), anyInt(), eq("1"), any())).thenReturn(null);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).startSpiderAsUser(eq("http://aSeriousUrl.com"), any(), anyInt(), eq("1"), any());
    }

    @Test
    public void testScannerConfiguredAndStarted() throws ClientApiException {
        createScannerTask();
        when(zapService.createContext(eq("http://aSeriousUrl.com"), any(), any())).thenReturn("1");
        when(zapService.startScannerAsUser(eq("http://aSeriousUrl.com"), eq("1"), any(), any(), any(), any())).thenReturn(null);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).startScannerAsUser(eq("http://aSeriousUrl.com"), eq("1"), any(), any(), any(), any());
    }

    @Test
    public void testAuthenticationConfigured() throws ClientApiException, UnsupportedEncodingException {
        createSpiderTask();
        createScannerTask();
        spiderTask.getTargets().get(0).getAttributes().setAuthentication(true);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).configureAuthentication(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testRemovedDuplicatesFromSpiderResult() throws ClientApiException {
        createSpiderTask();

        when(zapService.retrieveSpiderResult(any())).thenReturn(createFindingsWithDuplicates());
        when(taskService.createFindings(any())).thenCallRealMethod();
        when(config.isFilterSpiderResults()).thenReturn(true);

        doAnswer(invocation -> {
            List<Finding> result = (List<Finding>) invocation.getArguments()[1];
            assertTrue(findingsAreEqual(createFindings(), result));
            return null;
        }).when(taskService).completeTask(any(), any(), any(), any());

        engineWorkerJob.execute(eventPublisher);
    }

    @Test
    public void testRemovedDuplicatesFromScannerResult() throws ClientApiException {
        createScannerTask();
        when(zapService.retrieveScannerResult(any(), any())).thenReturn(createFindingsWithDuplicates());
        when(taskService.createFindings(any())).thenCallRealMethod();
        when(config.isFilterScannerResults()).thenReturn(true);
        doAnswer(invocation -> {
            List<Finding> result = (List<Finding>) invocation.getArguments()[1];
            assertTrue(findingsAreEqual(createFindings(), result));
            return null;
        }).when(taskService).completeTask(any(), any(), any(), any());

        engineWorkerJob.execute(eventPublisher);
    }

    private boolean findingsAreEqual(List<Finding> findings1, List<Finding> findings2) {
        return findings1.size() == findings2.size()
                && findings1.stream()
                .map(Finding::getLocation)
                .collect(Collectors.toList())
                .containsAll(
                        findings2.stream()
                                .map(Finding::getLocation)
                                .collect(Collectors.toList())
                )
                && findings2.stream()
                .map(Finding::getLocation)
                .collect(Collectors.toList())
                .containsAll(
                    findings1.stream()
                            .map(Finding::getLocation)
                            .collect(Collectors.toList()
                )
        );
    }

    private void createSpiderTask() {

        spiderTask = new ZapTask();
        spiderTask.setJobId("1");

        List<Target> targets = new LinkedList<>();

        Target t1 = new Target("http://aSeriousUrl.com");
        t1.getAttributes().setBaseUrl("http://aSeriousUrl.com");

        targets.add(t1);

        spiderTask.setTargets(targets);
        when(taskService.getTask(ZapTopic.ZAP_SPIDER)).thenReturn(spiderTask);
    }

    private void createScannerTask() {

        scannerTask = new ZapTask();
        scannerTask.setJobId("1");

        List<Target> targets = new LinkedList<>();

        Target t1 = new Target("http://aSeriousUrl.com");
        t1.getAttributes().setBaseUrl("http://aSeriousUrl.com");

        targets.add(t1);

        scannerTask.setTargets(targets);
        when(taskService.getTask(ZapTopic.ZAP_SCANNER)).thenReturn(scannerTask);
    }

    private List<Finding> createFindingsWithDuplicates() {

        List<Finding> findings = createFindings();

        Finding f3 = new Finding();
        f3.setName("Epic Finding");
        f3.setDescription("I'm a Finding");
        f3.setCategory("EPIC");
        f3.setReference(new Reference("http://theMostImportantSiteEver.org"));
        f3.setLocation("http://locationOfSecurityDeath.org");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("BEER", "nice");
        attributes.put("NullpointerException", "Oh No!");
        attributes.put("ZAP_BASE_URL", "http://aSeriousUrl.com");
        attributes.put("request", createRequest(f3.getLocation()));

        f3.setAttributes(attributes);

        findings.add(f3);

        return findings;
    }

    private HarRequest createRequest(String url){
        HarRequest request = new HarRequest();
        request.setMethod(HttpMethod.GET);
        request.setUrl(url);
        return request;
    }

    private List<Finding> createFindings() {

        List<Finding> findings = new LinkedList<>();

        Finding f1 = new Finding();
        f1.setName("Epic Finding");
        f1.setDescription("I'm a Finding");
        f1.setCategory("EPIC");
        f1.setReference(new Reference("http://theMostImportantSiteEver.org"));
        f1.setLocation("http://locationOfSecurityDeath.org");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("BEER", "nice");
        attributes.put("NullpointerException", "Oh No!");
        attributes.put("ZAP_BASE_URL", "http://aSeriousUrl.com");
        attributes.put("request", createRequest(f1.getLocation()));
        f1.setAttributes(attributes);

        Finding f2 = new Finding();
        f2.setName("More Epic Finding");
        f2.setDescription("I'm the best Finding");
        f2.setCategory("MASSIVE EPICNESS");
        f2.setReference(new Reference("http://dontlookatme.org"));
        f2.setLocation("http://yourOwnFaultToVisitMe.org");
        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("CAKE", "amazing");
        attributes2.put("NullpointerException", "Oh No not again!");
        attributes2.put("ZAP_BASE_URL", "http://aSeriousUrl.com");
        attributes.put("request", createRequest(f2.getLocation()));

        f2.setAttributes(attributes2);

        findings.add(f1);
        findings.add(f2);

        return findings;
    }
}