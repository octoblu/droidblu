package com.octoblu.droidblu.service;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class Meshblu {
    private final String CLIENT_ID = "meshblu-client";
    private final MqttAndroidClient mqttAndroidClient;
    private final ArrayList<MeshbluMessageHandler> messageHandlers;

    public Meshblu(Context applicationContext, String host) {
        this.messageHandlers = new ArrayList<MeshbluMessageHandler>();
        this.mqttAndroidClient = new MqttAndroidClient(applicationContext, host, CLIENT_ID);
    }

    public void connect(String uuid, String token, final MeshbluConnectionHandler meshbluConnectionHandler) {
        mqttAndroidClient.setCallback(new MeshbluMqttMessageCallback());

        MqttConnectOptions options = buildOptions(uuid, token);

        try {
            mqttAndroidClient.connect(options, "", new MeshbluMqttConnectCallback(meshbluConnectionHandler));
        } catch (MqttException exception) {
            meshbluConnectionHandler.onFailure(new Throwable("Failed to connect to Meshblu", exception));
        }
    }

    public void onMessage(MeshbluMessageHandler meshbluMessageHandler) {
        this.messageHandlers.add(meshbluMessageHandler);
    }

    public void message(String toUUID, String payload) throws MeshbluException {
        String json = "{\"devices\": [\"3cc5df60-2f99-11e4-96a1-89ac5135be97\"], \"payload\": \"Hello\"}";
        try {
            mqttAndroidClient.publish("message", json.getBytes(), 0, true);
        } catch (MqttException e) {
            throw new MeshbluException(e);
        }
    }

    private MqttConnectOptions buildOptions(String uuid, String token) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(uuid);
        options.setPassword(token.toCharArray());
        options.setCleanSession(true);
        return options;
    }

    private class MeshbluMqttConnectCallback implements IMqttActionListener {
        private final MeshbluConnectionHandler meshbluConnectionHandler;

        public MeshbluMqttConnectCallback(MeshbluConnectionHandler meshbluConnectionHandler) {
            this.meshbluConnectionHandler = meshbluConnectionHandler;
        }

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            try {
                mqttAndroidClient.subscribe("bce15650-2fc3-11e4-bb6b-0d33aad5b861", 0);
            } catch (MqttException exception) {
                meshbluConnectionHandler.onFailure(new Throwable("Failed to connect to Meshblu", exception));
            }
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            meshbluConnectionHandler.onFailure(throwable);
        }
    }


    private class MeshbluMqttMessageCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable throwable) {

        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) {
            String message = mqttMessage.toString();

            for(MeshbluMessageHandler messageHandler : messageHandlers) {
                messageHandler.onMessage("some uuid", message);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }
}
