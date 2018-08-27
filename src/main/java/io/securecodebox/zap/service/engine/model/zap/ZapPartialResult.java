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

import io.securecodebox.zap.service.engine.model.Finding;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Results of a single Target scan.
 * This result should get appended with the once of other Targets to a Full CompleteTask class
 */
@Data
@AllArgsConstructor
public class ZapPartialResult {
    private List<Finding> findings;
    private String rawFindings;
}
