package com.lilong.blogaigc.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import com.lilong.blog.exception.BusinessException;
import com.lilong.blog.respone.agent.*;
import com.lilong.blog.respone.ollama.Model;
import com.lilong.blog.respone.ollama.OllamaChatCompletionResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;



@Data
@Slf4j
public class OllamaClient {
    // 列出本地可用的模型
    public static final String API_TAGS = "/api/tags";
    public static final String CHAT_COMPLETION_SUFFIX = "/api/chat";
    private String baseUrl;

    private String model;

    public OllamaClient() {
    }

    public OllamaClient(String baseUrl, String model) {
        if (StringUtils.isEmpty(baseUrl)) {
            throw new BusinessException("请输入ollama的baseUrl");
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        this.baseUrl = baseUrl;

        if (modelExist(model) == false) {
            throw new BusinessException(String.format("模型%s不存在，请检查模型是否存在", model));
        }

        this.model = model;
    }

    /**
     * 判断模型是否存在
     * @author wangjinfei
     * @date 2025/2/14 16:46
     * @param model
     * @return Boolean
    */
    public Boolean modelExist(String model) {
        if(StringUtils.isEmpty(model)){
            throw new BusinessException("请输入模型名称");
        }
        String url = this.baseUrl + API_TAGS;
        try  {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() == false) {
                log.error("ollama请求{}失败，请检查ollama是否启动", url);
                throw new BusinessException("ollama请求失败，请检查ollama是否启动");
            }
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            List<Model> modelList = JSONArray.parseArray(jsonObject.getString("models"), Model.class);
            return modelList.stream().anyMatch(m -> model.equals(m.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ollama请求{}失败，请检查ollama是否启动", url, e);
            throw new BusinessException("ollama请求失败，请检查ollama是否启动");
        }
    }

    public ChatCompletionResponse ChatCompletion(ChatCompletionRequest request) throws IOException {
        request.stream = false;
        // 设置超时时间 本地ollama api响应很慢
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        System.out.println(new Gson().toJson(request));
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, new Gson().toJson(request));
        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(getChatCompletionUrl())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        try {
            okhttp3.Response response = client.newCall(httpRequest).execute();
            String responseBody = response.body().string();
            Gson gson = new Gson();
            OllamaChatCompletionResponse ollamaResult = gson.fromJson(responseBody, OllamaChatCompletionResponse.class);
            return ollamaResultToChatCompletionResponse(ollamaResult);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String getChatCompletionUrl() {
        return baseUrl + CHAT_COMPLETION_SUFFIX;
    }

    /**
     * ollama调用结果 封装上游对象
     * @author wangjinfei
     * @date 2025/2/19 20:37
     * @param ollamaResult
     * @return ChatCompletionResponse
    */
    private ChatCompletionResponse ollamaResultToChatCompletionResponse(OllamaChatCompletionResponse ollamaResult){
        ChatCompletionResponse chatCompletionResponse = new ChatCompletionResponse();
        chatCompletionResponse.setModel(ollamaResult.getModel());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        ChatCompletionChoice choice = new ChatCompletionChoice();
        ChatCompletionMessage message = new ChatCompletionMessage(ollamaResult.getMessage().getRole(), ollamaResult.getMessage().getContent());
        choice.setMessage(message);
        choices.add(choice);
        chatCompletionResponse.setChoices(choices);
        return chatCompletionResponse;
    }

    public Flowable<ChatCompletionStreamResponse> ChatCompletionStream(ChatCompletionRequest request) throws IOException {
        request.stream = true;
        // 设置超时时间 本地ollama api响应很慢
        okhttp3.OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, new Gson().toJson(request));
        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(getChatCompletionUrl())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        okhttp3.Response response = client.newCall(httpRequest).execute();
        if (response.code() != 200) {
            throw new RuntimeException("Failed to start stream: " + response.body().string());
        }

        Flowable<ChatCompletionStreamResponse> objectFlowable = Flowable.create(emitter -> {
            okhttp3.ResponseBody responseBody = response.body();
            String line;
            while ((line = responseBody.source().readUtf8Line()) != null) {
                OllamaChatCompletionResponse ollamaResult = JSONObject.parseObject(line, OllamaChatCompletionResponse.class);
                if(ollamaResult.getDone()){
                    System.out.println("---------------->>>> 结束");
                    emitter.onComplete();
                    return;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                ChatCompletionStreamResponse chatCompletionStreamResponse = ollamaResultToChatCompletionStreamResponse(ollamaResult);
                emitter.onNext(chatCompletionStreamResponse);
            }
        }, BackpressureStrategy.BUFFER);
        return objectFlowable;
    }

    /**
     * ollama调用结果 封装上游对象
     * @author wangjinfei
     * @date 2025/2/19 20:38
     * @param ollamaResult
     * @return ChatCompletionStreamResponse
    */
    private ChatCompletionStreamResponse ollamaResultToChatCompletionStreamResponse(OllamaChatCompletionResponse ollamaResult){
        ChatCompletionStreamResponse chatCompletionStreamResponse = new ChatCompletionStreamResponse();
        chatCompletionStreamResponse.setModel(ollamaResult.getModel());
        List<ChatCompletionStreamChoice> choices = new ArrayList<>();
        ChatCompletionStreamChoiceDelta delta = new ChatCompletionStreamChoiceDelta(ollamaResult.getMessage().getContent(), ollamaResult.getMessage().getRole());
        ChatCompletionStreamChoice choice = new ChatCompletionStreamChoice(0, delta, null, null);
        choices.add(choice);
        chatCompletionStreamResponse.setChoices(choices);
        return chatCompletionStreamResponse;
    }
}
