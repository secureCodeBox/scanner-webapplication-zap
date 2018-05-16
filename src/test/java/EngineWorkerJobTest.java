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

import de.otto.edison.jobs.eventbus.JobEventPublisher;
import io.securecodebox.zap.configuration.ZapConfiguration;
import io.securecodebox.zap.jobs.definition.EngineWorkerJob;
import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.Finding;
import io.securecodebox.zap.service.engine.model.Reference;
import io.securecodebox.zap.service.engine.model.Target;
import io.securecodebox.zap.service.engine.model.zap.ZapFields;
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
import org.mockito.stubbing.Answer;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
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
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testContextCreation() throws ClientApiException{
        createSpiderTask();
        createScannerTask();
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(2)).createContext(eq("http://aSeriousUrl.com"), any(), any());
    }

    @Test
    public void testScanningWithMoreTargetShouldCreateMoreContexts() throws ClientApiException{
        createScannerTask();

        Target t2 = new Target("http://landOfPudding.com");
        t2.getAttributes().put("ZAP_BASE_URL", "http://landOfPudding.com");
        scannerTask.getTargets().add(t2);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).createContext(eq("http://landOfPudding.com"), any(), any());
        verify(zapService, times(2)).createContext(any(), any(), any());

    }

    @Test
    public void testScanningWithMoreTargetWithSameBaseShouldCreateOneContext() throws ClientApiException{
        createScannerTask();

        Target t2 = new Target("http://landOfPudding.com");
        t2.getAttributes().put("ZAP_BASE_URL", "http://aSeriousUrl.com");
        scannerTask.getTargets().add(t2);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).createContext(any(), any(), any());

    }

    @Test
    public void testSpiderConfiguredAndStarted() throws ClientApiException{
        createSpiderTask();
        when(zapService.createContext(eq("http://aSeriousUrl.com"), any(), any())).thenReturn("1");
        when(zapService.startSpiderAsUser(eq("http://aSeriousUrl.com"), any(), anyInt(), eq("1"), any())).thenReturn(null);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).startSpiderAsUser(eq("http://aSeriousUrl.com"), any(), anyInt(), eq("1"), any());
    }

    @Test
    public void testScannerConfiguredAndStarted() throws ClientApiException{
        createScannerTask();
        when(zapService.createContext(eq("http://aSeriousUrl.com"), any(), any())).thenReturn("1");
        when(zapService.startScannerAsUser(eq("http://aSeriousUrl.com"), eq("1"), any())).thenReturn(null);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).createContext(eq("http://aSeriousUrl.com"), any(), any());
        verify(zapService, times(1)).startScannerAsUser(eq("http://aSeriousUrl.com"), eq("1"), any());
    }

    @Test
    public void testAuthenticationConfigured() throws ClientApiException, UnsupportedEncodingException {
        createSpiderTask();
        createScannerTask();
        spiderTask.getTargets().get(0).getAttributes().put("ZAP_AUTHENTICATION", true);
        engineWorkerJob.execute(eventPublisher);
        verify(zapService, times(1)).configureAuthentication(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * Tests if the spider task gets executed correctly and transforms the raw results into correct findings
     * As the part of the spidering we are testing here is equal to the corresponding part of the scanning (and we mock
     * the results here) , a test for the scanner isn't necessary
     * @throws ClientApiException
     */
    @Test
    public void testCorrectResults() throws ClientApiException{

        createSpiderTask();
        List<Finding> findings = createFindings();
        String rawFindings = createRawFindings();
        when(zapService.retrieveSpiderResult(any())).thenReturn(rawFindings);
        when(taskService.createFindings(any())).thenCallRealMethod();
        doAnswer((Answer) invocation -> {
            List<Finding> result = (List<Finding>) invocation.getArguments()[1];
            assertTrue(result.size() == findings.size());
            assertTrue(result.stream().map(Finding::getLocation).collect(Collectors.toList()).containsAll(
                    findings.stream().map(Finding::getLocation).collect(Collectors.toList())
            ));
            assertTrue(findings.stream().map(Finding::getLocation).collect(Collectors.toList()).containsAll(
                    result.stream().map(Finding::getLocation).collect(Collectors.toList())
            ));
            return null;
        }).when(taskService).completeTask(any(), any(), any(), any());

        engineWorkerJob.execute(eventPublisher);
    }

    @Test
    public void testDuplicateRemovedWhenFinishedScanning() throws ClientApiException {
        createScannerTask();
        List<Finding> findings = createFindings();
        String rawFindings = createRawFindingsWithDuplicate();
        when(zapService.retrieveScannerResult(any(), any())).thenReturn(rawFindings);
        when(taskService.createFindings(any())).thenCallRealMethod();
        doAnswer((Answer) invocation -> {
            List<Finding> result = (List<Finding>) invocation.getArguments()[1];
            assertTrue(result.size() == findings.size());
            assertTrue(result.stream().map(Finding::getLocation).collect(Collectors.toList()).containsAll(
                    findings.stream().map(Finding::getLocation).collect(Collectors.toList())
            ));
            assertTrue(findings.stream().map(Finding::getLocation).collect(Collectors.toList()).containsAll(
                    result.stream().map(Finding::getLocation).collect(Collectors.toList())
            ));
            return null;
        }).when(taskService).completeTask(any(), any(), any(), any());

        engineWorkerJob.execute(eventPublisher);
    }

    @Test
    public void testDuplicateRemovalShouldEliminateDuplicates(){

        Finding f = new Finding();
        f.getAttributes().put("alert", "XSS");
        f.setLocation("http://xss.org?x=1&q=2");

        Finding f1 = new Finding();
        f1.getAttributes().put("alert", "XSS");
        f1.setLocation("http://xss.org?x=3&q=2");

        Finding f2 = new Finding();
        f2.getAttributes().put("alert", "XSS");
        f2.setLocation("http://xss.org?x=1&q=1");

        Finding f3 = new Finding();
        f3.getAttributes().put("alert", "SQL");
        f3.setLocation("http://xss.org?x=1&q=2");

        Finding f4 = new Finding();
        f4.getAttributes().put("alert", "XSS");
        f4.setLocation("http://xss2.org?x=1&q=2");

        Finding f5 = new Finding();
        f5.getAttributes().put("alert", "XSRF");
        f5.setLocation("http://xsrf.org?x=1");

        List<Finding> findings = new LinkedList<>(Arrays.asList(f, f1, f2, f3, f4, f5));
        List<Finding> uniqueFindings = new LinkedList<>(Arrays.asList(f, f3, f4, f5));

        assert (findings.size() == 6);
        EngineWorkerJob.removeDuplicateScanResults(findings);

        assertTrue(findings.size() == 4);
        assertTrue(findings.containsAll(uniqueFindings));
        assertFalse(findings.contains(f2));
    }

    @Test
    public void duplicateRemovalWithEmptyListShouldDoNothing(){
        List<Finding> findings = new LinkedList<>();

        EngineWorkerJob.removeDuplicateScanResults(findings);
        assertTrue(findings.size() == 0);
    }

    @Test
    public void duplicateRemovalWithoutAlertsShouldWork(){

        Finding f = new Finding();
        f.setLocation("http://x.org?x=1&q=2");

        Finding f1 = new Finding();
        f1.setLocation("http://x.org?x=234&q=34543");

        List<Finding> findings = new LinkedList<>(Arrays.asList(f, f1));

        EngineWorkerJob.removeDuplicateScanResults(findings);
        assertTrue(findings.size() == 1);
    }

    private void createSpiderTask(){

        spiderTask = new ZapTask();
        spiderTask.setJobId("1");

        List<Target> targets = new LinkedList<>();

        Target t1 = new Target("http://aSeriousUrl.com");
        Map<String, Object> t1Attributes = new HashMap<>();
        t1Attributes.put(ZapFields.ZAP_BASE_URL.name(), "http://aSeriousUrl.com");
        t1.setAttributes(t1Attributes);

        targets.add(t1);

        spiderTask.setTargets(targets);
        when(taskService.getTask(ZapTopic.ZAP_SPIDER)).thenReturn(spiderTask);
    }

    private void createScannerTask(){

        scannerTask = new ZapTask();
        scannerTask.setJobId("1");

        List<Target> targets = new LinkedList<>();

        Target t1 = new Target("http://aSeriousUrl.com");
        Map<String, Object> t1Attributes = new HashMap<>();
        t1Attributes.put(ZapFields.ZAP_BASE_URL.name(), "http://aSeriousUrl.com");
        t1.setAttributes(t1Attributes);

        targets.add(t1);

        scannerTask.setTargets(targets);
        when(taskService.getTask(ZapTopic.ZAP_SCANNER)).thenReturn(scannerTask);
    }

    private List<Finding> createFindings(){

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
        f2.setAttributes(attributes2);

        findings.add(f1); findings.add(f2);

        return findings;
    }

    private String createRawFindings() {
        return
        "[" +
            "{" +
                "\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                "\"name\":\"Epic Finding\"," +
                "\"description\":\"I'm a Finding\"," +
                "\"category\":\"EPIC\"," +
                "\"reference\":\"http://theMostImportantSiteEver.org\"," +
                "\"attributes\":" +
                "{" +
                    "\"BEER\":\"nice\"," +
                    "\"NullpointerException\":\"Oh No!\"," +
                    "\"ZAP_BASE_URL\":\"http://aSeriousUrl.com\"" +
                "}," +
                "\"location\":\"http://locationOfSecurityDeath.org\"" +
            "}," +
            "{" +
                "\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                "\"name\":\"More Epic Finding\"," +
                "\"description\":\"I'm the best Finding\"," +
                "\"category\":\"MASSIVE EPICNESS\"," +
                "\"reference\":\"http://dontlookatme.org\"," +
                "\"attributes\":" +
                "{" +
                    "\"CAKE\":\"amazing\"," +
                    "\"NullpointerException\":\"Oh No not again!\"," +
                    "\"ZAP_BASE_URL\":\"http://aSeriousUrl.com\"" +
                "}," +
                "\"location\":\"http://yourOwnFaultToVisitMe.org\"" +
            "}" +
        "]";
    }

    private String createRawFindingsWithDuplicate() {
        return
                "[" +
                        "{" +
                        "\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                        "\"name\":\"Epic Finding\"," +
                        "\"description\":\"I'm a Finding\"," +
                        "\"category\":\"EPIC\"," +
                        "\"reference\":\"http://theMostImportantSiteEver.org\"," +
                        "\"attributes\":" +
                        "{" +
                        "\"BEER\":\"nice\"," +
                        "\"NullpointerException\":\"Oh No!\"," +
                        "\"ZAP_BASE_URL\":\"http://aSeriousUrl.com\"" +
                        "}," +
                        "\"location\":\"http://locationOfSecurityDeath.org\"" +
                        "}," +
                        "{" +
                        "\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                        "\"name\":\"Epic Finding\"," +
                        "\"description\":\"I'm a Finding\"," +
                        "\"category\":\"EPIC\"," +
                        "\"reference\":\"http://theMostImportantSiteEver.org\"," +
                        "\"attributes\":" +
                        "{" +
                        "\"BEER\":\"nice\"," +
                        "\"NullpointerException\":\"Oh No!\"," +
                        "\"ZAP_BASE_URL\":\"http://aSeriousUrl.com\"" +
                        "}," +
                        "\"location\":\"http://locationOfSecurityDeath.org\"" +
                        "}," +
                        "{" +
                        "\"id\":\"49bf7fd3-8512-4d73-a28f-608e493cd726\"," +
                        "\"name\":\"More Epic Finding\"," +
                        "\"description\":\"I'm the best Finding\"," +
                        "\"category\":\"MASSIVE EPICNESS\"," +
                        "\"reference\": \"http://dontlookatme.org\"," +
                        "\"attributes\":" +
                        "{" +
                        "\"CAKE\":\"amazing\"," +
                        "\"NullpointerException\":\"Oh No not again!\"," +
                        "\"ZAP_BASE_URL\":\"http://aSeriousUrl.com\"" +
                        "}," +
                        "\"location\":\"http://yourOwnFaultToVisitMe.org\"" +
                        "}" +
                        "]";
    }
}
