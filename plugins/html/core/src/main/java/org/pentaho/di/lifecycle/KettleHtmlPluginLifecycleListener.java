/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.di.lifecycle;

import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataExternalResourceConsumer;
import org.pentaho.di.trans.steps.gethtmldata.GetHTMLDataStepAnalyzer;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
@LifecyclePlugin(id = "KettleHtmlPlugin", name = "KettleHtmlPlugin")
public class KettleHtmlPluginLifecycleListener implements LifecycleListener {

    @Override
    public void onStart(LifeEventHandler handler) throws LifecycleException {
        GetHTMLDataStepAnalyzer getHTMLDataStepAnalyzer = new GetHTMLDataStepAnalyzer();
        GetHTMLDataExternalResourceConsumer getHTMLDataExternalResourceConsumer = new GetHTMLDataExternalResourceConsumer();
        getHTMLDataStepAnalyzer.setExternalResourceConsumer(getHTMLDataExternalResourceConsumer);

        PentahoSystem.registerObject(getHTMLDataStepAnalyzer);
        PentahoSystem.registerObject(getHTMLDataExternalResourceConsumer);
    }

    @Override
    public void onExit(LifeEventHandler handler) throws LifecycleException {
        // no-op
    }
}
