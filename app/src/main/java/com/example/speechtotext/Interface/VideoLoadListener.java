package com.example.speechtotext.Interface;

import com.example.speechtotext.Model.VideoModel;

import java.util.ArrayList;

public interface VideoLoadListener {
    void onVideoLoadSuccess(ArrayList<VideoModel> videoList);
    void onVideoLoadingFailed(String message);
}
