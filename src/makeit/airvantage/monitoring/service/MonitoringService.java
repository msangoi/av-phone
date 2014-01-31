package makeit.airvantage.monitoring.service;

import java.util.List;
import java.util.Map;

import makeit.airvantage.monitoring.MainActivity;
import makeit.airvantage.monitoring.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;

public class MonitoringService extends Service {

    private static final String LOGTAG = "MonitoringService";

    private BatteryChangeListener batteryListener = new BatteryChangeListener();

    // system services
    private TelephonyManager telephonyManager;
    private ActivityManager activityManager;
    private ConnectivityManager connManager;

    // Unique Identification Number for the Notification.
    private int NOTIFICATION = R.string.notif_title;

    // Intent extra keys
    public static final String DEVICE_ID = "device_id";
    public static final String SERVER_HOST = "server_host";
    public static final String PASSWORD = "password";

    private MqttPushClient client;

    private Long startedSince;

    private Long lastRun;
    private String lastLog;
    private NewData lastData = new NewData();
    /* the date of the last location reading */
    private long lastLocation;

    @Override
    public void onCreate() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        Notification notification = new Notification.Builder(this.getApplicationContext()) //
                .setContentTitle(getText(R.string.notif_title)) //
                .setContentText(getText(R.string.notif_desc)) //
                .setSmallIcon(R.drawable.ic_notif) //
                .setOngoing(true) //
                .setContentIntent(contentIntent) //
                .build();

        // Send the notification.
        startForeground(NOTIFICATION, notification);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        this.registerReceiver(batteryListener, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        startedSince = System.currentTimeMillis();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        lastRun = System.currentTimeMillis();

        try {
            if (this.client == null) {
                client = new MqttPushClient(intent.getStringExtra(DEVICE_ID), intent.getStringExtra(PASSWORD),
                        intent.getStringExtra(SERVER_HOST), mqttCallback);
            }

            if (!client.isConnected()) {
                client.connect();
            }

            final LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            final String locationProvider = locManager.getBestProvider(new Criteria(), true);
            Log.d(LOGTAG, "Location provider : " + locationProvider);

            Location location = null;
            if (locationProvider != null) {
                location = locManager.getLastKnownLocation(locationProvider);
            }

            // retrieve data
            NewData data = new NewData();

            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            if (cellInfos != null && !cellInfos.isEmpty()) {
                CellInfo cellInfo = cellInfos.get(0);
                if (cellInfo instanceof CellInfoGsm) {
                    data.setRssi(((CellInfoGsm) cellInfo).getCellSignalStrength().getDbm());
                } else if (cellInfo instanceof CellInfoWcdma) {
                    // RSSI ?
                    data.setRssi(((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm());
                } else if (cellInfo instanceof CellInfoLte) {
                    data.setRsrp(((CellInfoLte) cellInfo).getCellSignalStrength().getDbm());
                }
            }

            data.setOperator(telephonyManager.getNetworkOperatorName());

            switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                data.setNetworkType("GPRS");
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                data.setNetworkType("EDGE");
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                data.setNetworkType("UMTS");
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                data.setNetworkType("HSDPA");
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                data.setNetworkType("HSPAP");
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                data.setNetworkType("HSPA");
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                data.setNetworkType("HSUPA");
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                data.setNetworkType("LTE");
                break;
            // to be continued
            default:
            }

            data.setActiveWifi(connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected());

            data.setRunningApps(activityManager.getRunningAppProcesses().size());

            MemoryInfo mi = new MemoryInfo();
            activityManager.getMemoryInfo(mi);
            data.setMemoryUsage((float) ((mi.totalMem - mi.availMem) / ((Long) mi.totalMem).doubleValue()));

            data.setBatteryLevel(batteryListener.getBatteryLevel());
            if (location != null && location.getTime() != lastLocation) {
                data.setLatitude(location.getLatitude());
                data.setLongitude(location.getLongitude());
                lastLocation = location.getTime();
            }
            data.setBytesReceived(TrafficStats.getMobileRxBytes());
            data.setBytesSent(TrafficStats.getMobileTxBytes());

            // save new data values
            lastData.putExtras(data.getExtras());

            // dispatch new data event to update the activity UI
            LocalBroadcastManager.getInstance(this).sendBroadcast(data);

            client.push(data);
            lastLog = data.size() + " data pushed to the server";
            LocalBroadcastManager.getInstance(this).sendBroadcast(new LogMessage(lastLog));

        } catch (Exception e) {
            Log.e(LOGTAG, "error", e);
            lastLog = "ERROR: " + e.getMessage();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new LogMessage(lastLog));
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("MonitoringService", "Stopping service");

        if (this.client != null) {
            try {
                this.client.disconnect();
            } catch (MqttException e) {
                Log.e(LOGTAG, "error", e);
            }
        }

        // unregister listeners
        this.unregisterReceiver(batteryListener);

        // Cancel the persistent notification.
        stopForeground(true);
    }

    // Service binding

    private ServiceBinder binder = new ServiceBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    public class ServiceBinder extends Binder {

        public MonitoringService getService() {
            return MonitoringService.this;
        }
    }

    public long getStartedSince() {
        return startedSince;
    }

    public NewData getLastData() {
        return lastData;
    }

    public String getLastLog() {
        return lastLog;
    }

    public Long getLastRun() {
        return lastRun;
    }

    // MQTT client callback

    @SuppressWarnings("unused")
    private MqttCallback mqttCallback = new MqttCallback() {

        class Message {
            String uid;
            long timestamp;
            Command command;
        }

        class Command {
            String id;
            Map<String, String> params;
        }

        @Override
        public void messageArrived(String topic, MqttMessage msg) throws Exception {
            Log.d(LOGTAG, "MQTT msg received: " + new String(msg.getPayload()));

            // parse json payload
            Message[] messages = new Gson().fromJson(new String(msg.getPayload(), "UTF-8"), Message[].class);

            // display a new notification
            Notification notification = new Notification.Builder(MonitoringService.this.getApplicationContext()) //
                    .setContentTitle(getText(R.string.notif_new_message)) //
                    .setContentText(messages[0].command.params.get("message")) //
                    .setSmallIcon(R.drawable.ic_notif) //
                    .setAutoCancel(true) //
                    .build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify((int) messages[0].timestamp, notification);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            //
        }

        @Override
        public void connectionLost(Throwable arg0) {
            //
        }
    };

}
