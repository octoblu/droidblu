package com.octoblu.droidblu.service;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by roy on 9/2/14.
 */
public class MeshbluException extends RuntimeException {
    public MeshbluException(MqttException e) {
        setStackTrace(e.getStackTrace());
    }
}
