package com.octoblu.meshblu.client;

public class MeshbluException extends RuntimeException {
    public MeshbluException(Exception e) {
        setStackTrace(e.getStackTrace());
    }
}
