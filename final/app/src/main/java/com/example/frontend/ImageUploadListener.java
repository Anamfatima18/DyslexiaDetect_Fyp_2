package com.example.frontend;

public interface ImageUploadListener {
    void onUploadSuccess(String response);

    void onUploadFailure(String error);
}
