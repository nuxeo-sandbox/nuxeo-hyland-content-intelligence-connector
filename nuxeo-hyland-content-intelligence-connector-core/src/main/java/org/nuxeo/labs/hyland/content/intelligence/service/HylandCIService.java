package org.nuxeo.labs.hyland.content.intelligence.service;

public interface HylandCIService {

    public static final String CONTENT_INTELL_URL_PARAM = "nuxeo.hyland.content.intelligence.baseUrl";

    public static final String CONTENT_INTELL_HEADER_NAME_PARAM = "nuxeo.hyland.content.intelligence.authenticationHeaderName";

    public static final String CONTENT_INTELL_HEADER_VALUE_PARAM = "nuxeo.hyland.content.intelligence.authenticationHeaderValue";

    String invoke(String endpoint, String jsonPayload);

    String invoke(String endpoint, String jsonPayload, boolean useCache);

}
