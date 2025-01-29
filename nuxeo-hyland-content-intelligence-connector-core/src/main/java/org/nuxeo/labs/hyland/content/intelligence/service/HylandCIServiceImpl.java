package org.nuxeo.labs.hyland.content.intelligence.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
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
        return invoke(endpoint, jsonPayload, false);
    }

    public String invoke(String endpoint, String jsonPayload, boolean useCache) {
        
        String response = null;

        if (useCache) {
            CacheService cacheService = Framework.getService(CacheService.class);
            Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
            String cacheKey = getCacheKey(endpoint,jsonPayload);
            if (cache.hasEntry(cacheKey)) {
                return (String) cache.get(cacheKey);
            }
        }

        // Get config parameter values for URL to call, authentication, etc.
        String targetUrl = Framework.getProperty(HylandCIService.CONTENT_INTELL_URL_PARAM);
        String authenticationHeaderName = Framework.getProperty(HylandCIService.CONTENT_INTELL_HEADER_NAME_PARAM);
        String authenticationHeaderValue = Framework.getProperty(HylandCIService.CONTENT_INTELL_HEADER_VALUE_PARAM);
        
        if(!endpoint.startsWith("/")) {
            targetUrl += "/";
        }
        targetUrl += endpoint;
        
        // For whatever reason I have don't time to explore, using the more modern java.net.http.HttpClient;
        // fails, the authentication header is not corrcetly received...
        // So, let's go back to good old HttpURLConnection.
        try {
            // Create the URL object
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set request method to POST
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // Allows sending body content

            // Set headers
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty(authenticationHeaderName, authenticationHeaderValue); // Custom Auth Header

            // Write JSON data to request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get response code
            int responseCode = conn.getResponseCode();
            
            // Read response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {

                StringBuilder finalResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    finalResponse.append(line.trim());
                }
                
                response = finalResponse.toString();
                //System.out.println(response);
                
                if (useCache) {
                    CacheService cacheService = Framework.getService(CacheService.class);
                    Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
                    cache.put(getCacheKey(endpoint, jsonPayload), response);
                }
            }

            // Disconnect the connection
            conn.disconnect();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return response;
    }

    public static String getCacheKey(String endpoint, String jsonPayload) {
        return endpoint + jsonPayload;
    }

}
