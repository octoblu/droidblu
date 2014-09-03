package com.octoblu.meshblu.client;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Meshblu {
    public static final String FROM_UUID = "fromUuid";
    public static final String TOPIC = "topic";
    public static final String PAYLOAD = "payload";
    public static final String DATA = "data";
    public static final String DEVICES = "devices";
    private final String CLIENT_ID = "droidblu-";
    private final MqttAndroidClient mqttAndroidClient;
    private final ArrayList<MeshbluMessageHandler> messageHandlers;
    private final ArrayList<MeshbluErrorHandler> errorHandlers;

    public Meshblu(Context applicationContext, String host) {
        this.messageHandlers = new ArrayList<MeshbluMessageHandler>();
        this.errorHandlers = new ArrayList<MeshbluErrorHandler>();
        String clientUuid = CLIENT_ID + UUID.randomUUID().toString();
        this.mqttAndroidClient = new MqttAndroidClient(applicationContext, host, clientUuid);
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

    public void onError(MeshbluErrorHandler meshbluErrorHandler) {
        this.errorHandlers.add(meshbluErrorHandler);
    }

    public void onMessage(MeshbluMessageHandler meshbluMessageHandler) {
        this.messageHandlers.add(meshbluMessageHandler);
    }

    public void message(String toUUID, String topic, String payload) throws MeshbluException {
        try {
            JSONObject message = new JSONObject();
            JSONArray devices = new JSONArray();
            devices.put(toUUID);
            message.put(DEVICES, devices);
            message.put(PAYLOAD, payload);
            message.put(TOPIC,   topic);

            String messageString = message.toString();

            mqttAndroidClient.publish("message", messageString.getBytes(), 0, true);
        } catch (JSONException e) {
            publishError(e);
        } catch (MqttException e) {
            publishError(e);
        }
    }

    private MqttConnectOptions buildOptions(String uuid, String token) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(uuid);
        options.setPassword(token.toCharArray());
        options.setCleanSession(true);
        return options;
    }

    private HashMap<String, String> parseMessage(String messageJSON) throws JSONException {
        HashMap<String, String> map = new HashMap<String, String>();
        JSONObject message = new JSONObject(messageJSON);
        JSONObject data    = message.getJSONObject(DATA);

        map.put(FROM_UUID, data.has(FROM_UUID) ? data.getString(FROM_UUID) : null);
        map.put(TOPIC,     data.has(TOPIC)     ? data.getString(TOPIC)     : null);
        map.put(PAYLOAD,   data.has(PAYLOAD)   ? data.getString(PAYLOAD)   : null);

        return map;
    }

    private void publishError(Throwable throwable){
        for(MeshbluErrorHandler errorHandler : errorHandlers){
            errorHandler.onError(throwable);
        }
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

            meshbluConnectionHandler.onSuccess();
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            meshbluConnectionHandler.onFailure(throwable);
        }
    }


    private class MeshbluMqttMessageCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable throwable) {
            publishError(throwable);
        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) {
            HashMap<String, String> message;

            try {
                message = parseMessage(mqttMessage.toString());
            } catch (JSONException e) {
                publishError(e);
                return;
            }

            for(MeshbluMessageHandler messageHandler : messageHandlers) {
                messageHandler.onMessage(message.get(FROM_UUID), message.get(TOPIC), message.get(PAYLOAD));
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }
}
