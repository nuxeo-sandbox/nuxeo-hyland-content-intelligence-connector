package org.nuxeo.labs.hyland.content.intelligence.test;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.labs.hyland.content.intelligence.automation.HylandCIInvokeOp;
import org.nuxeo.labs.hyland.content.intelligence.test.ConfigCheckerFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({AutomationFeature.class, ConfigCheckerFeature.class})
@Deploy("nuxeo-hyland-content-intelligence-connector-core")
public class TestHylandCIInvokeOp {

    @Inject
    protected CoreSession session;

    @Inject
    protected AutomationService automationService;

    // Notice
    @Test
    public void testImageDescription() throws OperationException, IOException {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        OperationContext ctx = new OperationContext(session);
        Map<String, Object> params = new HashMap<>();
        // Ask for /description for a small image
        params.put("endpoint","/description");

        // . . . build the JSON with the image and a quick description . . .
        byte[] fileContent = FileUtils.readFileToByteArray(new File(getClass().getResource("/files/musubimaru.png").getPath()));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);
        String payload = String.format("""
                {
                    "type" : "base64",
                    "media_type": "image/png",
                    "override_request": "",
                    "data": "%s"
                }
                """, encodedString);
        params.put("jsonPayload",payload);

        Blob json = (Blob) automationService.run(ctx, HylandCIInvokeOp.ID, params);
        Assert.assertNotNull(json);
        JSONObject responseBody = new JSONObject(json.getString());
        double[] embeddings = responseBody.getJSONArray("response")
                .toList().stream().mapToDouble(v -> ((BigDecimal) v).doubleValue()).toArray();
        Assert.assertNotNull(embeddings);
        Assert.assertEquals(embeddings.length, 1024);
    }
}
