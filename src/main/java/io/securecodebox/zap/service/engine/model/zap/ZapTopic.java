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

package io.securecodebox.zap.service.engine.model.zap;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Represents all ZAP specific topic names used to identify the external tasks for this service.
 */
@AllArgsConstructor
public enum ZapTopic {
    ZAP_SPIDER("zap_spider"),
    ZAP_SCANNER("zap_scan");

    @Getter
    private final String name;


    @Override
    public String toString() {
        return name;
    }
}
