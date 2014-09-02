package com.octoblu.droidblu.service;

public class MeshbluException extends RuntimeException {
    public MeshbluException(Exception e) {
        setStackTrace(e.getStackTrace());
    }
}
