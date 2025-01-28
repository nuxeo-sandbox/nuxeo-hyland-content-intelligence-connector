package org.nuxeo.labs.hyland.content.intelligence.automation;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIService;

@Operation(id = HylandCIInvokeOp.ID, category = "Hyland Content Intelligence", label = "Invoke Hyland COntent Intelligence and return the JSON response as a blob",
        description = "Invoke the Hyland Content Intelligence API")
public class HylandCIInvokeOp {

    public static final String ID = "HylandContentIntelligence.Invoke";

    @Param(name = "modelName", required = true)
    protected String modelName;

    @Param(name = "jsonPayload", required = true)
    protected String jsonPayload;

    @Param(name = "useCache", required = false)
    protected boolean useCache = false;

    @Context
    HylandCIService ciService;

    @OperationMethod
    public Blob run() {
        String response = ciService.invoke(modelName, jsonPayload, useCache);
        return new StringBlob(response, "application/json");
    }

}
