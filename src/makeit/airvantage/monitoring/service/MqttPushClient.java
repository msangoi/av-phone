package makeit.airvantage.monitoring.service;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.gson.Gson;

public class MqttPushClient {

    private static final String LOGTAG = "MqttPushClient";

    private MqttClient client;
    private MqttConnectOptions opt;

    private Gson gson = new Gson();

    @SuppressLint("DefaultLocale")
    public MqttPushClient(String clientId, String password, String serverHost, MqttCallback callback)
            throws MqttException {

        Log.d(LOGTAG, "new client: " + clientId + " - " + password + " - " + serverHost);

        this.client = new MqttClient("tcp://" + serverHost + ":1883", MqttClient.generateClientId(),
                new MemoryPersistence());
        client.setCallback(callback);

        this.opt = new MqttConnectOptions();
        opt.setUserName(clientId.toUpperCase());
        opt.setPassword(password.toCharArray());
        opt.setKeepAliveInterval(30);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void connect() throws MqttSecurityException, MqttException {
        Log.d(LOGTAG, "connecting");
        client.connect(opt);
    }

    public void disconnect() throws MqttException {
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    public void push(NewData data) throws MqttException, UnsupportedEncodingException {
        if (client.isConnected()) {
            Log.i(LOGTAG, "Pushing data to the server : " + data);
            String message = this.convertToJson(data);

            Log.d(LOGTAG, "Rest content : " + message);

            MqttMessage msg = new MqttMessage(message.getBytes("UTF-8"));
            msg.setQos(0);

            this.client.publish(opt.getUserName() + "/messages/json", msg);
        }
    }

    private String convertToJson(NewData data) {
        long timestamp = System.currentTimeMillis();

        Map<String, List<DataValue>> values = new HashMap<String, List<DataValue>>();

        if (data.getRssi() != null) {
            values.put("phone.rssi", Collections.singletonList(new DataValue(timestamp, data.getRssi())));
            values.put("_RSSI", Collections.singletonList(new DataValue(timestamp, data.getRssi())));
        }

        if (data.getRsrp() != null) {
            values.put("phone.rsrp", Collections.singletonList(new DataValue(timestamp, data.getRsrp())));
            values.put("_RSRP", Collections.singletonList(new DataValue(timestamp, data.getRsrp())));
        }

        if (data.getBatteryLevel() != null) {
            values.put("phone.batterylevel",
                    Collections.singletonList(new DataValue(timestamp, data.getBatteryLevel())));
        }

        if (data.getOperator() != null) {
            values.put("phone.operator", Collections.singletonList(new DataValue(timestamp, data.getOperator())));
        }

        if (data.getNetworkType() != null) {
            values.put("phone.service", Collections.singletonList(new DataValue(timestamp, data.getNetworkType())));
            // hack for data mapping
            values.put("_NETWORK_SERVICE_TYPE",
                    Collections.singletonList(new DataValue(timestamp, data.getNetworkType())));
        }

        if (data.getLatitude() != null) {
            values.put("phone.latitude", Collections.singletonList(new DataValue(timestamp, data.getLatitude())));
            // hack for data mapping
            values.put("_LATITUDE", Collections.singletonList(new DataValue(timestamp, data.getLatitude())));
        }

        if (data.getLongitude() != null) {
            values.put("phone.longitude", Collections.singletonList(new DataValue(timestamp, data.getLongitude())));
            // hack for data mapping
            values.put("_LONGITUDE", Collections.singletonList(new DataValue(timestamp, data.getLongitude())));
        }

        if (data.getBytesReceived() != null) {
            // hack for data mapping
            values.put("phone.bytesreceived",
                    Collections.singletonList(new DataValue(timestamp, data.getBytesReceived())));
            values.put("_BYTES_RECEIVED", Collections.singletonList(new DataValue(timestamp, data.getBytesReceived())));
        }

        if (data.getBytesSent() != null) {
            // hack for data mapping
            values.put("phone.bytessent", Collections.singletonList(new DataValue(timestamp, data.getBytesSent())));
            values.put("_BYTES_SENT", Collections.singletonList(new DataValue(timestamp, data.getBytesSent())));
        }

        if (data.isWifiActive() != null) {
            values.put("phone.activewifi", Collections.singletonList(new DataValue(timestamp, data.isWifiActive())));
        }

        if (data.getRunningApps() != null) {
            values.put("phone.runningapps", Collections.singletonList(new DataValue(timestamp, data.getRunningApps())));
        }

        if (data.getMemoryUsage() != null) {
            values.put("phone.memoryusage", Collections.singletonList(new DataValue(timestamp, data.getMemoryUsage())));
        }

        return gson.toJson(Collections.singletonList(values));
    }

    class DataValue {

        DataValue(long timestamp, Object value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        long timestamp;
        Object value;
    }

}
