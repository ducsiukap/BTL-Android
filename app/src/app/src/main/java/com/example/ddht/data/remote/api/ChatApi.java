package com.example.ddht.data.remote.api;

import com.example.ddht.data.remote.dto.ChatRequest;
import com.example.ddht.data.remote.dto.ChatResponse;
import com.example.ddht.data.remote.dto.SpeechToTextResponse;
import com.example.ddht.data.remote.dto.VoiceChatResponse;

import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ChatApi {
    @POST("chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);

    @Multipart
    @POST("speech-to-text")
    Call<SpeechToTextResponse> speechToText(@Part MultipartBody.Part audio);

    @Multipart
    @POST("voice-chat")
    Call<VoiceChatResponse> voiceChat(
            @Part MultipartBody.Part audio,
            @Query("session_id") String sessionId);
}
