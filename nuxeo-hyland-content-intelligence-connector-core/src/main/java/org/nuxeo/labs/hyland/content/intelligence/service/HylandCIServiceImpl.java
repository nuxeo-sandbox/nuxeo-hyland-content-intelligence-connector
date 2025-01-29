package org.nuxeo.labs.hyland.content.intelligence.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

public class HylandCIServiceImpl extends DefaultComponent implements HylandCIService {

    public static final String CONTENT_INTELL_CACHE = "content_intelligence_cache";

    public String invoke(String endpoint, String jsonPayload) {
        return invoke(endpoint,jsonPayload,false);
    }

    public String invoke(String endpoint, String jsonPayload, boolean useCache) {

        if (useCache) {
            CacheService cacheService = Framework.getService(CacheService.class);
            Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
            String cacheKey = getCacheKey(endpoint,jsonPayload);
            if (cache.hasEntry(cacheKey)) {
                return (String) cache.get(cacheKey);
            }
        }

        // Get config parameter values for URL to call, authentication, etc.
        String url = Framework.getProperty(HylandCIService.CONTENT_INTELL_URL_PARAM);
        String authenticationHeaderName = Framework.getProperty(HylandCIService.CONTENT_INTELL_HEADER_NAME_PARAM);
        String authenticationheaderValue = Framework.getProperty(HylandCIService.CONTENT_INTELL_HEADER_VALUE_PARAM);

        
        if(!endpoint.startsWith("/")) {
            url += "/";
        }
        url += endpoint;

        // Prepare the request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header(authenticationHeaderName, authenticationheaderValue)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
        
        String aa = request.headers().toString();
        // Go.
        String response = null;
        try {
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            response = httpResponse.body();
            
        } catch (IOException e) {
            throw new NuxeoException("Network error", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Preserve interrupted state
            throw new NuxeoException("Request was interrupted", e);
        } catch (Exception e) {
            throw new NuxeoException("Unexpected error", e);
        }
        

        return response;
    }

    public static String getCacheKey(String endpoint, String jsonPayload) {
        return endpoint + jsonPayload;
    }

}
