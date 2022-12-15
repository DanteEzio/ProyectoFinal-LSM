package com.example.speechtotext.Model;

public class VideoModel {
    private String pregunta, url; // Exactly property name in Firebase

    public VideoModel() {
    }

    public VideoModel(String pregunta, String url) {
        this.pregunta = pregunta;
        this.url = url;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
