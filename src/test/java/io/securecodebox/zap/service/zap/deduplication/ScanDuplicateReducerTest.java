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

import io.securecodebox.zap.jobs.definition.EngineWorkerJob;
import io.securecodebox.zap.service.engine.model.Finding;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScanDuplicateReducerTest {
    @Test
    public void testDuplicateRemovalShouldEliminateDuplicates() {
        Finding f = new Finding();
        f.setName("XSS");
        f.setLocation("http://xss.org?x=1&q=2");

        Finding f1 = new Finding();
        f1.setName("XSS");
        f1.setLocation("http://xss.org?x=3&q=2");

        Finding f2 = new Finding();
        f2.setName("XSS");
        f2.setLocation("http://xss.org?x=1&q=1");

        Finding f3 = new Finding();
        f3.setName("SQL");
        f3.setLocation("http://xss.org?x=1&q=2");

        Finding f4 = new Finding();
        f4.setName("XSS");
        f4.setLocation("http://xss2.org?x=1&q=2");

        Finding f5 = new Finding();
        f5.setName("XSRF");
        f5.setLocation("http://xsrf.org?x=1");

        List<Finding> findings = new LinkedList<>(Arrays.asList(f, f1, f2, f3, f4, f5));
        List<Finding> uniqueFindings = new LinkedList<>(Arrays.asList(f, f3, f4, f5));

        assertEquals(6, findings.size());
        new ScanDuplicateReducer().reduce(findings);

        assertEquals(4, findings.size());
        assertTrue(findings.containsAll(uniqueFindings));
        assertFalse(findings.contains(f2));
    }

    @Test
    public void duplicateRemovalWithEmptyListShouldDoNothing() {
        List<Finding> findings = new LinkedList<>();

        new ScanDuplicateReducer().reduce(findings);
        assertEquals(0, findings.size());
    }

    @Test
    public void duplicateRemovalWithoutAlertsShouldWork() {

        Finding f = new Finding();
        f.setLocation("http://x.org?x=1&q=2");

        Finding f1 = new Finding();
        f1.setLocation("http://x.org?x=234&q=34543");

        List<Finding> findings = new LinkedList<>(Arrays.asList(f, f1));

        new ScanDuplicateReducer().reduce(findings);
        assertEquals(1, findings.size());
    }
}
