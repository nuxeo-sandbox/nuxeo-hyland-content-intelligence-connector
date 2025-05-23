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
package org.nuxeo.labs.hyland.content.intelligence.automation;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIService;

@Operation(id = HylandCIInvokeOp.ID, category = "Hyland Content Intelligence", label = "Invoke Hyland Content Intelligence and return the JSON response as a blob",
        description = "Invoke the Hyland Content Intelligence API")
public class HylandCIInvokeOp {

    public static final String ID = "HylandContentIntelligence.Invoke";

    @Param(name = "endpoint", required = true)
    protected String endpoint;

    @Param(name = "jsonPayload", required = true)
    protected String jsonPayload;

    @Param(name = "useCache", required = false)
    protected boolean useCache = false;

    @Context
    protected HylandCIService ciService;

    @OperationMethod
    public Blob run() {
        String response = ciService.invokeObsoleteQuickDemo(endpoint, jsonPayload, useCache);
        return new StringBlob(response, "application/json");
    }

}
