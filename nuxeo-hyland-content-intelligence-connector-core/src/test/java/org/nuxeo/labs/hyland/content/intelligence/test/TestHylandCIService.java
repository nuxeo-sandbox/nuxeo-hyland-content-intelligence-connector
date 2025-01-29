package org.nuxeo.labs.hyland.content.intelligence.test;

import static org.junit.Assert.assertNotNull;
import static org.nuxeo.labs.hyland.content.intelligence.service.HylandCIServiceImpl.CONTENT_INTELL_CACHE;
import static org.nuxeo.labs.hyland.content.intelligence.service.HylandCIServiceImpl.getCacheKey;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIService;
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

    @Inject
    protected HylandCIService hylandCIService;

    @Test
    public void testService() {
        assertNotNull(hylandCIService);
    }

    @Test
    public void testGetImageDescription() throws Exception {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        byte[] fileContent = FileUtils.readFileToByteArray(
                new File(getClass().getResource("/files/musubimaru.png").getPath()));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        String payload = String.format("""
                {
                    "type" : "base64",
                    "media_type": "image/png",
                    "override_request": "",
                    "data": "%s"
                }
                """, encodedString);

        String response = hylandCIService.invoke("/description", payload);
        JSONObject responseBody = new JSONObject(response);
        Assert.assertNotNull(responseBody);
    }

    @Test
    public void testGetTextEmbeddings() {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        String titanModelId = "amazon.titan-embed-text-v2:0";
        String payload = """
                {
                    "inputText":"This some sample text"
                }"
                """;
        String response = hylandCIService.invoke(titanModelId, payload);
        JSONObject responseBody = new JSONObject(response);
        double[] embeddings = responseBody.getJSONArray("embedding")
                                          .toList()
                                          .stream()
                                          .mapToDouble(v -> ((BigDecimal) v).doubleValue())
                                          .toArray();
        Assert.assertNotNull(embeddings);
        Assert.assertEquals(embeddings.length, 1024);
    }

    @Test
    public void testGetImageEmbeddings() throws IOException {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        byte[] fileContent = FileUtils.readFileToByteArray(
                new File(getClass().getResource("/files/musubimaru.png").getPath()));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        String titanModelId = "amazon.titan-embed-image-v1";
        String payload = String.format("""
                {
                    "inputText" : "An image that shows the mascot of sendai city in japan eating a rice ball",
                    "inputImage": "%s"
                }
                """, encodedString);
        String response = hylandCIService.invoke(titanModelId, payload);
        JSONObject responseBody = new JSONObject(response);
        double[] embeddings = responseBody.getJSONArray("embedding")
                                          .toList()
                                          .stream()
                                          .mapToDouble(v -> ((BigDecimal) v).doubleValue())
                                          .toArray();
        Assert.assertNotNull(embeddings);
        Assert.assertEquals(embeddings.length, 1024);
    }

    @Test
    public void testResponseCaching() {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        String titanModelId = "amazon.titan-embed-text-v2:0";
        String payload = """
                {
                    "inputText":"This some sample text"
                }"
                """;
        String response = hylandCIService.invoke(titanModelId, payload, true);
        Assert.assertNotNull(response);

        CacheService cacheService = Framework.getService(CacheService.class);
        Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
        Assert.assertTrue(cache.hasEntry(getCacheKey(titanModelId, payload)));
    }

    @Test
    public void testCacheHit() {

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        String modelId = "the model that don't exist yet";
        String payload = """
                {
                    "inputText":"Let's see some magic"
                }"
                """;

        String cachedResponse = "123";

        CacheService cacheService = Framework.getService(CacheService.class);
        Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
        cache.put(getCacheKey(modelId, payload), cachedResponse);

        String response = hylandCIService.invoke(modelId, payload, true);
        Assert.assertEquals(cachedResponse, response);
    }

}
