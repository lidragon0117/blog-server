package com.lilong.blogaigc.client;

import com.google.gson.Gson;
import com.lilong.blog.respone.agent.ChatCompletionRequest;
import com.lilong.blog.respone.agent.ChatCompletionResponse;
import com.lilong.blog.respone.agent.ChatCompletionStreamResponse;
import com.lilong.blog.respone.agent.ModelsList;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author lilong
 * @webSite com.lilong
 * @Date 2024/3/21 01:34
 * @description
 */
public class MoonShotAIClient {

    private static final String DEFAULT_BASE_URL = "https://api.moonshot.cn/v1";

    private static final String CHAT_COMPLETION_SUFFIX = "/chat/completions";
    private static final String MODELS_SUFFIX = "/models";
    private static final String FILES_SUFFIX = "/files";

    private String baseUrl;
    private String apiKey;

    public MoonShotAIClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    public MoonShotAIClient(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;
    }

    public String getChatCompletionUrl() {
        return baseUrl + CHAT_COMPLETION_SUFFIX;
    }

    public String getModelsUrl() {
        return baseUrl + MODELS_SUFFIX;
    }

    public String getFilesUrl() {
        return baseUrl + FILES_SUFFIX;
    }

    public String getApiKey() {
        return apiKey;
    }

    public ModelsList ListModels() throws IOException {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(getModelsUrl())
                .addHeader("Authorization", "Bearer " + getApiKey())
                .build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            String body = response.body().string();
            Gson gson = new Gson();
            return gson.fromJson(body, ModelsList.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public ChatCompletionResponse ChatCompletion(ChatCompletionRequest request) throws IOException {
        request.stream = false;
        okhttp3.OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, new Gson().toJson(request));
        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(getChatCompletionUrl())
                .addHeader("Authorization", "Bearer " + getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        try {
            okhttp3.Response response = client.newCall(httpRequest).execute();
            String responseBody = response.body().string();
            System.out.println(responseBody);
            Gson gson = new Gson();
            return gson.fromJson(responseBody, ChatCompletionResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // return a stream of ChatCompletionStreamResponse
    public Flowable<ChatCompletionStreamResponse> ChatCompletionStream(ChatCompletionRequest request) throws IOException {
        request.stream = true;
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, new Gson().toJson(request));
        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(getChatCompletionUrl())
                .addHeader("Authorization", "Bearer " + getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        okhttp3.Response response = client.newCall(httpRequest).execute();
        if (response.code() != 200) {
            throw new RuntimeException("Failed to start stream: " + response.body().string());
        }

        // get response body line by line
        return Flowable.create(emitter -> {
            okhttp3.ResponseBody responseBody = response.body();
            if (responseBody == null) {
                emitter.onError(new RuntimeException("Response body is null"));
                return;
            }
            String line;
            while ((line = responseBody.source().readUtf8Line()) != null) {
                if (line.startsWith("data:")) {
                    line = line.substring(5);
                    line = line.trim();
                }
                if (Objects.equals(line, "[DONE]")) {
                    emitter.onComplete();
                    return;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Gson gson = new Gson();
                ChatCompletionStreamResponse streamResponse = gson.fromJson(line, ChatCompletionStreamResponse.class);
                emitter.onNext(streamResponse);
            }
            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }
}
