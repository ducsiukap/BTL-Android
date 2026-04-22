package com.example.ddht.data.repository;

import com.example.ddht.data.remote.AiNetworkClient;
import com.example.ddht.data.remote.api.ChatApi;
import com.example.ddht.data.remote.dto.ChatRequest;
import com.example.ddht.data.remote.dto.ChatResponse;
import com.example.ddht.data.remote.dto.SpeechToTextResponse;
import com.example.ddht.data.remote.dto.VoiceChatResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;

public class ChatRepository {
    private final ChatApi chatApi;

    public ChatRepository() {
        chatApi = AiNetworkClient.getRetrofit().create(ChatApi.class);
    }

    public Call<ChatResponse> sendMessage(String message) {
        return sendMessage(message, null);
    }

    public Call<ChatResponse> sendMessage(String message, String sessionId) {
        return chatApi.sendMessage(new ChatRequest(message, sessionId));
    }

    public Call<SpeechToTextResponse> speechToText(File audioFile) {
        return chatApi.speechToText(toAudioPart(audioFile));
    }

    public Call<VoiceChatResponse> voiceChat(File audioFile, String sessionId) {
        return chatApi.voiceChat(toAudioPart(audioFile), sessionId);
    }

    private MultipartBody.Part toAudioPart(File audioFile) {
        if (audioFile == null || !audioFile.exists()) {
            throw new IllegalArgumentException("Audio file does not exist");
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), audioFile);
        return MultipartBody.Part.createFormData("audio", audioFile.getName(), requestBody);
    }
}
