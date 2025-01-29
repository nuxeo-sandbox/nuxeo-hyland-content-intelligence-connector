package org.nuxeo.labs.hyland.content.intelligence.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.labs.hyland.content.intelligence.service.HylandCIServiceImpl.CONTENT_INTELL_CACHE;
import static org.nuxeo.labs.hyland.content.intelligence.service.HylandCIServiceImpl.getCacheKey;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIService;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIServiceImpl;
import org.nuxeo.labs.hyland.content.intelligence.test.ConfigCheckerFeature;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, ConfigCheckerFeature.class })
@Deploy("nuxeo-hyland-content-intelligence-connector-core")
public class TestHylandCIService {
    
    protected static String TEST_IMAGE_PATH = "/files/musubimaru.png";
    
    protected static String TEST_IMAGE_MIMETYPE = "image/png";
    
    protected static String testImageBase64 = null;

    @Inject
    protected HylandCIService hylandCIService;
    
    @Before
    public void onceExecutedBeforeAll() throws Exception {
        
        if(testImageBase64 == null) {
            byte[] fileContent = FileUtils.readFileToByteArray(
                    new File(getClass().getResource(TEST_IMAGE_PATH).getPath()));
            testImageBase64 = Base64.getEncoder().encodeToString(fileContent);
        }
    }
    
    @Test
    public void testService() {
        assertNotNull(hylandCIService);
    }

    @Test
    public void testGetImageDescription() throws Exception {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        String payload = String.format("""
                {
                    "type" : "base64",
                    "media_type": "image/png",
                    "override_request": "",
                    "data": "%s"
                }
                """, testImageBase64);

        String response = hylandCIService.invoke("/description", payload);
        assertNotNull(response);
        
        JSONObject responseBody = new JSONObject(response);
        assertNotNull(responseBody);
        
        String description = responseBody.getString("response");
        assertNotNull(description);
    }

    // TODO: unit test the /metadata endpoint
    @Test
    public void testGetImageMetadata() throws Exception {
        
    }


    // TODO: unit test the /embedding endpoint
    @Test
    public void testGetImageEmbedding() throws Exception {
        
    }

    @Test
    public void testResponseCaching() {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());
        
        String endpoint = "/description";

        String payload = String.format("""
                {
                    "type" : "base64",
                    "media_type": "image/png",
                    "override_request": "",
                    "data": "%s"
                }
                """, testImageBase64);

        String response = hylandCIService.invoke(endpoint, payload, true);
        assertNotNull(response);

        CacheService cacheService = Framework.getService(CacheService.class);
        Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
        assertTrue(cache.hasEntry(HylandCIServiceImpl.getCacheKey(endpoint, payload)));
    }

    @Test
    public void testCacheHit() {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        String endpoint = "the endpoint that don't exist yet";
        String payload = """
                {
                    "inputText":"Let's see some magic"
                }"
                """;

        String cachedResponse = "123";

        CacheService cacheService = Framework.getService(CacheService.class);
        Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
        cache.put(getCacheKey(endpoint, payload), cachedResponse);

        String response = hylandCIService.invoke(endpoint, payload, true);
        Assert.assertEquals(cachedResponse, response);
    }

}
