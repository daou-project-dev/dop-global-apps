package com.daou.dop.gapps.plugin.ms365.calendar.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Microsoft Graph API HTTP 클라이언트
 */
public class GraphApiClient {

    private static final Logger log = LoggerFactory.getLogger(GraphApiClient.class);
    private static final String GRAPH_API_BASE_URL = "https://graph.microsoft.com/v1.0";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final Gson gson;

    public GraphApiClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }

    /**
     * GET 요청 실행
     */
    public GraphApiResponse get(String path, String accessToken) {
        String url = GRAPH_API_BASE_URL + path;
        log.debug("GET {}", url);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .get()
                .build();

        return execute(request);
    }

    /**
     * POST 요청 실행
     */
    public GraphApiResponse post(String path, String accessToken, Object body) {
        String url = GRAPH_API_BASE_URL + path;
        log.debug("POST {}", url);

        String jsonBody = gson.toJson(body);
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        return execute(request);
    }

    /**
     * DELETE 요청 실행
     */
    public GraphApiResponse delete(String path, String accessToken) {
        String url = GRAPH_API_BASE_URL + path;
        log.debug("DELETE {}", url);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + accessToken)
                .delete()
                .build();

        return execute(request);
    }

    private GraphApiResponse execute(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            int statusCode = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            log.debug("Response status: {}, body length: {}", statusCode, responseBody.length());

            return new GraphApiResponse(statusCode, responseBody, response.isSuccessful());
        } catch (IOException e) {
            log.error("HTTP request failed: {}", e.getMessage(), e);
            return new GraphApiResponse(500, "{\"error\": \"" + e.getMessage() + "\"}", false);
        }
    }

    /**
     * Graph API 응답
     */
    public record GraphApiResponse(
            int statusCode,
            String body,
            boolean isSuccessful
    ) {}
}
