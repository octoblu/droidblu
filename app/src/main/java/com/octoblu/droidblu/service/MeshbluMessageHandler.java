package com.octoblu.droidblu.service;

public interface MeshbluMessageHandler {
    public void onMessage(String fromUUID, String topic, String payload);
}
