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

import io.securecodebox.zap.service.engine.ZapTaskService;
import io.securecodebox.zap.service.engine.model.zap.ZapTopic;
import io.securecodebox.zap.togglz.ZapFeature;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static org.springframework.boot.actuate.health.Health.down;
import static org.springframework.boot.actuate.health.Health.up;


/**
 * Note: This health indicator checks if the engine process is working correctly. Therefore it tries to fetch some tasks via the engine API.
 */
@Component
@ConditionalOnProperty(prefix = "securecodebox.zap.processEngine.healthcheck", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@ToString
public class EngineHealthIndicator implements HealthIndicator {
    @Autowired
    private ZapTaskService service;


    @Override
    public Health health() {
        if (ZapFeature.DISABLE_COMPLETE_HEALTH_CHECKS.isActive()) {
            return up().build();
        } else {
            try {
                if (service.isApiAvailable()) {
                    log.debug("Internal engine API health check is: OK");
                    return up().build();
                } else {
                    return down().build();
                }
            } catch (RuntimeException e) {
                log.error("Error: Indicating a engine API health problem!", e);
                return down().build();
            }
        }
    }
}
