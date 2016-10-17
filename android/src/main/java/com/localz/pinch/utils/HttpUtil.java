package com.localz.pinch.utils;

import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.localz.pinch.models.HttpRequest;
import com.localz.pinch.models.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpUtil {
    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    private String getResponseBody(InputStream responseStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseStream));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        bufferedReader.close();

        return sb.toString();
    }

    private JSONObject getResponseHeaders(HttpsURLConnection connection) {
        JSONObject jsonHeaders = new JSONObject();
        Map<String, List<String>> headerMap = connection.getHeaderFields();

        try {
            for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
                if (entry.getKey() != null) {
                    jsonHeaders.put(entry.getKey(), entry.getValue().get(0));
                }
            }
        } catch (JSONException e) {
            Log.e("HTTP Client", "Error retrieving response headers! " + e);
        }

        return jsonHeaders;
    }

    private HttpsURLConnection prepareRequestHeaders(HttpsURLConnection connection, ReadableMap headers) {
        ReadableMapKeySetIterator iterator = headers.keySetIterator();

        connection.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
        connection.setRequestProperty("Accept", DEFAULT_CONTENT_TYPE);
        while (iterator.hasNextKey()) {
            String nextKey = iterator.nextKey();
            connection.setRequestProperty(nextKey, headers.getString(nextKey));
        }

        return connection;
    }

    private HttpsURLConnection prepareRequest(HttpRequest request)
            throws IOException, KeyStoreException, CertificateException, KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection connection;
        URL url = new URL(request.endpoint);

        connection = (HttpsURLConnection) url.openConnection();
        if (request.certFilename != null) {
            connection.setSSLSocketFactory(KeyPinStoreUtil.getInstance(request.certFilename).getContext().getSocketFactory());
        }
        connection.setRequestMethod(request.method.toUpperCase());

        connection = prepareRequestHeaders(connection, request.headers);

        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setAllowUserInteraction(false);
        connection.setConnectTimeout(request.timeout);
        connection.setReadTimeout(request.timeout);

        if (request.body != null && (request.method.equals("post") || request.method.equals("put"))) {
            // Set the content length of the body.
            connection.setRequestProperty("Content-length", request.body.getBytes().length + "");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Send the JSON as body of the request.
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(request.body.getBytes("UTF-8"));
            outputStream.close();
        }

        return connection;
    }

    private InputStream prepareResponseStream(HttpsURLConnection connection) throws IOException {
        try {
            return connection.getInputStream();
        } catch (IOException e) {
            return connection.getErrorStream();
        }
    }

    public HttpResponse sendHttpRequest(HttpRequest request)
            throws IOException, KeyStoreException, CertificateException, KeyManagementException, NoSuchAlgorithmException {
        InputStream responseStream;
        HttpResponse response = new HttpResponse();
        HttpsURLConnection connection;
        int status;
        String statusText;

        connection = prepareRequest(request);

        connection.connect();

        status = connection.getResponseCode();
        statusText = connection.getResponseMessage();
        responseStream = prepareResponseStream(connection);

        response.statusCode = status;
        response.statusText = statusText;
        response.bodyString = getResponseBody(responseStream);
        response.headers = getResponseHeaders(connection);

        return response;
    }
}