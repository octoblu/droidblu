package com.octoblu.meshblu.client;

public interface MeshbluConnectionHandler {
    public void onSuccess();
    public void onFailure(Throwable throwable);
}
