/*
 * (C) Copyright 2025 Hyland (http://hyland.com/) and others.
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
 *     Michael Vachette
 *     Thibaud Arguillere
 */
package org.nuxeo.labs.hyland.content.intelligence.service;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.runtime.api.Framework;

public interface HylandCIService {

    // ==================== These 3 params were for quick test and demos before CIC API changed
    public static final String CONTENT_INTELL_URL_PARAM = "nuxeo.hyland.content.intelligence.baseUrl";

    public static final String CONTENT_INTELL_HEADER_NAME_PARAM = "nuxeo.hyland.content.intelligence.authenticationHeaderName";

    public static final String CONTENT_INTELL_HEADER_VALUE_PARAM = "nuxeo.hyland.content.intelligence.authenticationHeaderValue";
    // ====================

    String invoke(String endpoint, String jsonPayload);

    String invoke(String endpoint, String jsonPayload, boolean useCache);

}
