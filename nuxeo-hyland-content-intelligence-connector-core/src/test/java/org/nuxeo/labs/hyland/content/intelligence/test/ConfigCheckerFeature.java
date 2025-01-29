/*
 * (C) Copyright 2023 Hyland (http://hyland.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.hyland.content.intelligence.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RunnerFeature;

import nuxeo.ldt.parser.test.SimpleFeatureCustom;

import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIService;

/**
 * Check the misc expected config parameters are set so a call to Hyland Content Intelligence
 * can be made.
 * 
 * also checks for environment variables and convert them to config. parameters, this may be useful
 * when testing quickly, so you can set the following variables:
 * HYLAND_CONTENT_INTELL_URL (correspond to nuxeo.hyland.content.intelligence.baseUrl.)
 * HYLAND_CONTENT_INTELL_HEADER_NAME (=> nuxeo.hyland.content.intelligence.authenticationHeaderName)
 * HYLAND_CONTENT_INTELL_HEADER_VALUE (=> nuxeo.hyland.content.intelligence.authenticationHeaderValue)

 * @since 2023
 */
public class ConfigCheckerFeature implements RunnerFeature {

    public static final String ENV_URL = "HYLAND_CONTENT_INTELL_URL";

    public static final String ENV_HEADER_NAME = "HYLAND_CONTENT_INTELL_HEADER_NAME";

    public static final String ENV_HEADER_VALUE = "HYLAND_CONTENT_INTELL_HEADER_VALUE";

    protected static boolean hasAllProperties = false;

    Properties systemProps = null;

    public static boolean isSetup() {
        return hasAllProperties;
    }

    @Override
    public void initialize(FeaturesRunner runner) throws Exception {

        systemProps = System.getProperties();

        Boolean hasUrl = hasheaderName = hasHeaderValue,;

        hasUrl = hasProperty(HylandCIService.CONTENT_INTELL_URL_PARAM, ENV_URL);
        hasheaderName = hasProperty(HylandCIService.CONTENT_INTELL_HEADER_NAME_PARAM, ENV_HEADER_NAME);
        hasHeaderValue = hasProperty(HylandCIService.CONTENT_INTELL_HEADER_VALUE_PARAM, ENV_HEADER_VALUE);

        hasAllProperties = hasUrl && hasheaderName && hasHeaderValue;

        if(!hasAllProperties) {
            String msg = "Missing at least a parameter to connect to Hyland Content Intelligence, ";
            msg += " we need URL and authentication header Name and authentication header Value.";
            System.out.println(msg);
        }
    }

    protected boolean hasProperty(String property, String envVar) {

        String value = systemProps.getProperty(property);

        if(Stringutils.isBlank(value)) {
            value = System.getenv(envVar);
            if(!Stringutils.isBlank(value)) {
                systemProps.put(property, value);
                return true;
            }
        }

        return false;
    }
    
    @Override
    public void stop(FeaturesRunner runner) throws Exception {

        Properties p = System.getProperties();
        
        p.remove(HylandCIService.CONTENT_INTELL_URL_PARAM);
        p.remove(HylandCIService.CONTENT_INTELL_HEADER_NAME_PARAM);
        p.remove(HylandCIService.CONTENT_INTELL_HEADER_VALUE_PARAM);

        super.stop(runner);
    }

}
