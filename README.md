Meshblu Android Client
======================

Android client library for connecting with [Meshblu](http://meshblu.octoblu.com).

Install
-------

1. Place [meshblu.client.jar](https://github.com/octoblu/droidblu/releases/download/v1.0.0/meshblu.client.jar) in your project `libs` folder.
2. Place [org.eclipse.paho.mqtt.utility-1.0.0.jar](http://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.client.mqttv3/1.0.0/org.eclipse.paho.client.mqttv3-1.0.0.jar) in your project `libs` folder.
3. Add both jars as libraries (In Android Studio, right click .jar and 'Add As Library')

Usage
-----

```java
final String uuid  = "android-uuid";
final String token = "android-token";
final String host  = "tcp://meshblu.octoblu.com:1883";

final Meshblu meshblu = new Meshblu(applicationContext, host);
meshblu.connect(uuid, token, new MeshbluConnectionHandler(){
    @Override
    public void onSuccess() {
        // Connection is ready and subscribed, can now send messages
        String toUuid =  "Some uuid";
        String topic =   "Any Topic";
        String payload = "What's up?";
        meshblu.message(toUuid, topic, payload);
    }

    @Override
    public void onFailure(Throwable throwable) {
        // Handle failure
    }
});

meshblu.onMessage(new MeshbluMessageHandler() {
    @Override
    public void onMessage(String fromUuid, String topic, String payload) {
    }
});
```



