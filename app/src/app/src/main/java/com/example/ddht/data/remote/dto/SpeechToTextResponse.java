package com.example.ddht.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SpeechToTextResponse {
    @SerializedName("original_text")
    private String originalText;

    @SerializedName("corrected_text")
    private String correctedText;

    public String getOriginalText() {
        return originalText;
    }

    public String getCorrectedText() {
        return correctedText;
    }
}