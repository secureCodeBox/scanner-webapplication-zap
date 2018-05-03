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

package io.securecodebox.zap.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;


public enum ZapFeature implements Feature {
    @Label("Toggles if this service should complete external tasks or not (e.g. for testing purpose).")
    DISABLE_COMPLETE_ZAP_PROCESS_TASKS,

    @Label("Toggles if this service should complete health checks for external components.")
    DISABLE_COMPLETE_HEALTH_CHECKS,

    @Label("Toggles if the jobs are triggered or not.")
    DISABLE_TRIGGER_ALL_JOBS;


    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }

    public void enable() {
        FeatureState feature = FeatureContext.getFeatureManager().getFeatureState(this);
        feature.enable();
        FeatureContext.getFeatureManager().setFeatureState(feature);
    }

    public void disable() {
        FeatureState feature = FeatureContext.getFeatureManager().getFeatureState(this);
        feature.disable();
        FeatureContext.getFeatureManager().setFeatureState(feature);
    }
}
