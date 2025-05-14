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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.cache.Cache;
import org.nuxeo.ecm.core.cache.CacheService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.DefaultComponent;

public class HylandCIServiceImpl extends DefaultComponent implements HylandCIService {

    private static final Logger log = LogManager.getLogger(HylandCIServiceImpl.class);

    public static final String ENRICHMENT_CLIENT_ID_PARAM = "nuxeo.hyland.cic.enrichment.clientId";

    public static final String ENRICHMENT_CLIENT_SECRET_PARAM = "nuxeo.hyland.cic.enrichment.clientSecret";

    public static final String DATA_CURATION_CLIENT_ID_PARAM = "nuxeo.hyland.cic.datacuration.clientId";

    public static final String DATA_CURATION_CLIENT_SECRET_PARAM = "nuxeo.hyland.cic.datacuration.clientSecret";

    public static final String ENDPOINT_AUTH_PARAM = "nuxeo.hyland.cic.endpoint.auth";

    public static final String ENDPOINT_AUTH_DEFAULT = "https://auth.iam.experience.hyland.com/idp/connect/token";

    public static final String ENDPOINT_DATA_CURATION_PARAM = "nuxeo.hyland.cic.endpoint.dataCuration";

    public static final String ENDPOINT_DATA_CURATION_DEFAULT = "https://fgg6le8a5b.execute-api.us-east-1.amazonaws.com"; // /api

    public static final String ENDPOINT_CONTEXT_ENRICHMENT_PARAM = "nuxeo.hyland.cic.endpoint.contextEnrichment";

    public static final String ENDPOINT_CONTEXT_ENRICHMENT_DEFAULT = "https://cin-context-api.experience.hyland.com/context"; // /api

    public static String enrichmentClientId = null;

    public static String enrichmentClientSecret = null;

    public static String dataCurationClientId = null;

    public static String dataCurationClientSecret = null;

    public static String authEndPoint = null;

    public static String dataCurationEndPoint = null;

    public static String contextEnrichmentEndPoint = null;

    public static final String CONTENT_INTELL_CACHE = "content_intelligence_cache";

    protected static String enrichmentAuthToken = null;

    protected static String dataCurationAuthToken = null;

    public enum CICService {
        ENRICHMENT, DATA_CURATION
    }

    public HylandCIServiceImpl() {
        initialize();
    }

    protected void initialize() {

        // ==========> Auth
        authEndPoint = Framework.getProperty(ENDPOINT_AUTH_PARAM);
        if (StringUtils.isBlank(authEndPoint)) {
            log.warn("No " + ENDPOINT_AUTH_PARAM + " provided, using default value: " + ENDPOINT_AUTH_DEFAULT);
            authEndPoint = ENDPOINT_AUTH_DEFAULT;
        }

        // ==========> EndPoints
        contextEnrichmentEndPoint = Framework.getProperty(ENDPOINT_CONTEXT_ENRICHMENT_PARAM);
        if (StringUtils.isBlank(contextEnrichmentEndPoint)) {
            log.warn("No " + ENDPOINT_CONTEXT_ENRICHMENT_PARAM + " provided, using default value: "
                    + ENDPOINT_CONTEXT_ENRICHMENT_DEFAULT);
            contextEnrichmentEndPoint = ENDPOINT_CONTEXT_ENRICHMENT_DEFAULT;
        }

        dataCurationEndPoint = Framework.getProperty(ENDPOINT_DATA_CURATION_PARAM);
        if (StringUtils.isBlank(dataCurationEndPoint)) {
            log.warn("No " + ENDPOINT_DATA_CURATION_PARAM + " provided, using default value: "
                    + ENDPOINT_DATA_CURATION_DEFAULT);
            dataCurationEndPoint = ENDPOINT_DATA_CURATION_DEFAULT;
        }

        // ==========> Clients
        enrichmentClientId = Framework.getProperty(ENRICHMENT_CLIENT_ID_PARAM);
        enrichmentClientSecret = Framework.getProperty(ENRICHMENT_CLIENT_SECRET_PARAM);
        dataCurationClientId = Framework.getProperty(DATA_CURATION_CLIENT_ID_PARAM);
        dataCurationClientSecret = Framework.getProperty(DATA_CURATION_CLIENT_SECRET_PARAM);

        // ==========> SanityCheck
        if (StringUtils.isBlank(authEndPoint)) {
            log.warn("No CIC Authentication endpoint provided (" + ENDPOINT_AUTH_PARAM
                    + "), calls to the service will fail.");
        }

        if (StringUtils.isBlank(contextEnrichmentEndPoint)) {
            log.warn("No CIC Context Enrichment endpoint provided (" + ENDPOINT_CONTEXT_ENRICHMENT_PARAM
                    + "), calls to the service will fail.");
        }

        if (StringUtils.isBlank(dataCurationEndPoint)) {
            log.warn("No CIC Data Curation endpoint provided (" + ENDPOINT_DATA_CURATION_PARAM
                    + "), calls to the service will fail.");
        }

        if (StringUtils.isBlank(enrichmentClientId)) {
            log.warn("No CIC Enrichment ClientId provided (" + ENRICHMENT_CLIENT_ID_PARAM
                    + "), calls to the service will fail.");
        }

        if (StringUtils.isBlank(enrichmentClientSecret)) {
            log.warn("No CIC Enrichment ClientSecret provided (" + ENRICHMENT_CLIENT_SECRET_PARAM
                    + "), calls to the service will fail.");
        }

        if (StringUtils.isBlank(dataCurationClientId)) {
            log.warn("No CIC Data Curation ClientId provided (" + DATA_CURATION_CLIENT_ID_PARAM
                    + "), calls to the service will fail.");
        }

        if (StringUtils.isBlank(dataCurationClientSecret)) {
            log.warn("No CIC Data Curation ClientSecret provided (" + DATA_CURATION_CLIENT_SECRET_PARAM
                    + "), calls to the service will fail.");
        }

    }

    // TODO
    // THis methid should not be public. Made public for quick unit test.
    public String fetchAuthTokenIfNeeded(CICService service) {

        // TODO
        // Use a synchronize to make sure 2 simultaneous calls stay OK

        String clientId, clientSecret;

        // Should we handle "expires_in"?
        switch (service) {
        case ENRICHMENT:
            if (StringUtils.isNotBlank(enrichmentAuthToken)) {
                return enrichmentAuthToken;
            }
            clientId = enrichmentClientId;
            clientSecret = enrichmentClientSecret;
            break;

        case DATA_CURATION:
            if (StringUtils.isNotBlank(dataCurationAuthToken)) {
                return dataCurationAuthToken;
            }
            clientId = dataCurationClientId;
            clientSecret = dataCurationClientSecret;
            break;

        default:
            throw new IllegalArgumentException("Unknown service: " + service);
        }

        String targetUrl = authEndPoint;

        try {
            URL url = new URL(targetUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            // Not JSON...
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Write request body
            String postData = "client_id=" + URLEncoder.encode(clientId, "UTF-8") + "&client_secret="
                    + URLEncoder.encode(clientSecret, "UTF-8") + "&grant_type=client_credentials"
                    + "&scope=environment_authorization";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes("UTF-8"));
            }

            // Get response code
            int status = conn.getResponseCode();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JSONObject responseJson = new JSONObject(response.toString());
                // {"error":"invalid_grant","error_description":"Caller not authorized for requested resource"}
                if (responseJson.has("error")) {
                    String msg = "Getting a token failed with error " + responseJson.getString("error") + ".";
                    if (responseJson.has("error_description")) {
                        msg += " " + responseJson.getString("error_description");
                    }
                    log.error(msg);
                } else {
                    switch (service) {
                    case ENRICHMENT:
                        enrichmentAuthToken = responseJson.getString("access_token");
                        break;

                    case DATA_CURATION:
                        dataCurationAuthToken = responseJson.getString("access_token");
                        break;
                    }
                    // should we get "expires_in"?
                }
            }

        } catch (IOException e) {
            throw new NuxeoException(e);
        }

        switch (service) {
        case ENRICHMENT:
            return enrichmentAuthToken;

        case DATA_CURATION:
            return dataCurationAuthToken;
        }

        return null;
    }

    public String invoke(String endpoint, String jsonPayload) {
        return invoke(endpoint, jsonPayload, false);
    }

    public String invoke(String endpoint, String jsonPayload, boolean useCache) {

        String response = null;

        if (useCache) {
            CacheService cacheService = Framework.getService(CacheService.class);
            Cache cache = cacheService.getCache(CONTENT_INTELL_CACHE);
            String cacheKey = getCacheKey(endpoint, jsonPayload);
            if (cache.hasEntry(cacheKey)) {
                return (String) cache.get(cacheKey);
            }
        }

        // Get config parameter values for URL to call, authentication, etc.
        String targetUrl = Framework.getProperty(HylandCIService.CONTENT_INTELL_URL_PARAM);
        String authenticationHeaderName = Framework.getProperty(HylandCIService.CONTENT_INTELL_HEADER_NAME_PARAM);
        String authenticationHeaderValue = Framework.getProperty(HylandCIService.CONTENT_INTELL_HEADER_VALUE_PARAM);

        if (!endpoint.startsWith("/")) {
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
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8))) {

                StringBuilder finalResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    finalResponse.append(line.trim());
                }

                response = finalResponse.toString();
                // System.out.println(response);

                try {
                    JSONObject responseJson = new JSONObject(response);
                    responseJson.put("responseCode", responseCode);
                    responseJson.put("responseMessage", conn.getResponseMessage());
                    response = responseJson.toString();
                } catch (JSONException e) {
                    // Ouch. This is not JSON, let it as it is
                }

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
