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

import io.securecodebox.zap.service.engine.model.Finding;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ScanDuplicateReducer {
    DuplicationReductionUtil util = new DuplicationReductionUtil();

    public void reduce(List<Finding> findings) {
        if (findings == null) {
            return;
        }

        log.info("Finding count before duplicate removal: '{}'", findings.size());

        Set<String> uniqueUrls = new HashSet<>();

        Set<Finding> findingSet = new HashSet<>();
        for (Finding f : findings) {
            String uniqueUrl = util.removeQueryValues(f.getLocation()) + "_" + f.getName();
            if (!uniqueUrls.contains(uniqueUrl)) {
                uniqueUrls.add(uniqueUrl);
                findingSet.add(f);
            }
        }
        findings.clear();
        findings.addAll(findingSet);

        log.info("Finding count after duplicate removal: '{}'", findings.size());
    }
}
