package com.octoblu.meshblu.client;

public interface MeshbluMessageHandler {
    public void onMessage(String fromUUID, String topic, String payload);
}
