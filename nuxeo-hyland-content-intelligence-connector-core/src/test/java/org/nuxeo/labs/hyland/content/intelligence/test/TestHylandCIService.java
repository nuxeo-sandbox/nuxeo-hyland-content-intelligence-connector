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
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIService;
import org.nuxeo.labs.hyland.content.intelligence.service.HylandCIServiceImpl;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.util.Base64;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, ConfigCheckerFeature.class })
@Deploy("nuxeo-hyland-content-intelligence-connector-core")
public class TestHylandCIService {

    protected static final String TEST_IMAGE_PATH = "/files/dc-3.jpg";

    protected static final String TEST_IMAGE_MIMETYPE = "image/jpeg";

    protected static final String TEST_IMAGE_DESCRIPTION = getTestImageDescription();

    protected static String testImageBase64 = null;

    @Inject
    protected HylandCIService hylandCIService;

    @Before
    public void onceExecutedBeforeAll() throws Exception {

        if (testImageBase64 == null) {
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
                    "media_type": "%s",
                    "override_request": "",
                    "data": "%s"
                }
                """, TEST_IMAGE_MIMETYPE, testImageBase64);

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

        Assume.assumeTrue(ConfigCheckerFeature.isSetup());

        String payload = String.format("""
                {
                    "source": {
                      "type" : "base64",
                      "media_type": "%s",
                      "override_request": "",
                      "description": "%s",
                      "data": "%s"
                    },
                    "metadata_to_fill": [
                      "s1:f1",
                      "s1:f2",
                      "s1:f3"
                    ],
                    "examples": [
                      {
                        "description": "This image displays Disney characters: Mickey Mouse, Daisy Duck and Goofy. They are playing soccer and and look very happy.",
                        "s1:f1": "mickey|daisy|goofy",
                        "s1:f2": "Disney",
                        "s1:f3": "generic",
                      },
                      {
                        "description": "In this image, we can see some iconic Disney characters. We can see Goofy, who is laughing. There is also Mickey Mouse, jumping high, and Donald, sleeping.",
                        "s1:f1": "goofy|mickey|donald",
                        "s1:f2": "Disney",
                        "s1:f3": "generic",
                      },
                      {
                        "description": "This image shows 4 famous Disney characters: Mickey Mouse, Donald Duck, Daisy Duck and Scrooge McDuck.",
                        "s1:f1": "mickey|donald|scrooge|daisy",
                        "s1:f2": "Disney",
                        "s1:f3": "private",
                      },
                      {
                        "description": "Here we have mickey Mouse and Daisy Duck dancing around a fire, while Goofy is playing guitar.",
                        "s1:f1": "goofy|mickey|daisy",
                        "s1:f2": "Disney",
                        "s1:f3": "generic",
                      },
                      {
                        "description": "In this image, characters from different universes are displayed. We have Mickey Mouse talking with Darth Vador, while Indiana Jones is having a coffee with Captain America.",
                        "s1:f1": "mickey|dart|indiana|captainamerica",
                        "s1:f2": "Disney|Marvel|SW",
                        "s1:f3": "proprietary",
                      }
                    ]
                }
                """, TEST_IMAGE_MIMETYPE, TEST_IMAGE_DESCRIPTION, testImageBase64);

        String response = hylandCIService.invoke("/metadata", payload);
        assertNotNull(response);

        JSONObject responseBody = new JSONObject(response);
        assertNotNull(responseBody);

        String description = responseBody.getString("response");
        assertNotNull(description);

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
    
    public static String getTestImageDescription() {
        
        String desc = "The image contains several iconic Disney characters, including Goofy, Daisy Duck, and Mickey Mouse. There is no human face visible in the image.The image appears to be a stylized, minimalist representation of these classic cartoon characters. The characters are depicted as simple line drawings in a limited color palette of black, white, yellow, and red.Goofy is shown with his signature large ears and mouth, while Daisy Duck is recognizable by her distinctive red bow. Mickey Mouse is depicted as a minimalist silhouette, capturing his iconic rounded shape and ears.This seems to be an artistic rendering or logo design featuring these beloved Disney characters, rather than a scene from a specific show or movie.";
        
        return desc;
    }

}
