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

package io.securecodebox.zap.health;

import io.securecodebox.zap.service.zap.ZapService;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.zaproxy.clientapi.core.ClientApiException;

import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;


/**
 * Note: This health indicator checks if the process is working correctly. Therefore it tries to call the API with a simple version check.
 */
@Component
@ConditionalOnProperty(prefix = "securecodebox.zap.zapscan.healthcheck", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@ToString
public class ZapHealthIndicator implements HealthIndicator {
    @Autowired
    private ZapService service;


    @Override
    public Health health() {
        try {
            if (service.getVersion().contains("version")) {
                log.info("Internal health check: OK");
                return up().build();
            } else {
                return down().build();
            }
        } catch (ClientApiException e) {
            return down(e).build();
        }
    }
}
