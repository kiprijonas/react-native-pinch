package com.localz.pinch.models;

import org.json.JSONObject;

public class HttpRequest {
    public String endpoint;
    public String method;
    public JSONObject headers;
    public String body;
    public String certFilename;
    public int timeout;
    public boolean isAttachment;

    private static final int DEFAULT_TIMEOUT = 10000;

    public HttpRequest() {
        this.timeout = DEFAULT_TIMEOUT;
    }

    public HttpRequest(String endpoint) {
        this.endpoint = endpoint;
        this.timeout = DEFAULT_TIMEOUT;
    }

    public HttpRequest(String endpoint, String method, JSONObject headers, String body, String certFilename, int timeout, boolean isAttachment) {
        this.endpoint = endpoint;
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.certFilename = certFilename;
        this.timeout = timeout;
        this.isAttachment = isAttachment;
    }
}
