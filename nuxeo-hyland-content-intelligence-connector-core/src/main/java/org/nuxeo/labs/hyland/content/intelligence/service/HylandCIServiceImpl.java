package org.nuxeo.labs.hyland.content.intelligence.service;

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

        //. . . here, send REST API to Content Intelligence . . .
        //. . . get config parameter values for URL to call, authentication, etc.
        String url = Framework.getProperty(HylandCIService.CONTENT_INTELL_URL_PARAM);
        String authenticationHeader = Framework.getProperty((HylandCIService.CONTENT_INTELL_HEADER_NAME_PARAM);
        String authenticationValue = Framework.getProperty((HylandCIService.CONTENT_INTELL_HEADER_VALUE_PARAM);
        
        String response = null;
        //. . . etc.
        if(!url.endsWith("/")) {
            url += "/";
        }
        url += endpoint;
        // (in case people start the endpoint parameter with a slash)
        url = url.replace("//", "/");

        //. . . format the request, call the API, get the result . . .

        return response;
    }

    public static String getCacheKey(String endpoint, String jsonPayload) {
        return modelName+jsonPayload;
    }

}
