package makeit.airvantage.monitoring.service;

import android.content.Intent;
import android.os.Bundle;

public class NewData extends Intent {

    public static final String NEW_DATA = "makeit.av.monitoring.newdata";

    // keys used for broadcasting new data events
    private static final String NEW_DATA_PREFIX = "newdata.";
    private static final String RSSI_KEY = NEW_DATA_PREFIX + "rssi";
    private static final String RSRP_KEY = NEW_DATA_PREFIX + "rsrp";
    private static final String BATTERY_KEY = NEW_DATA_PREFIX + "battery";
    private static final String OPERATOR_KEY = NEW_DATA_PREFIX + "operator";
    private static final String NETWORK_TYPE_KEY = NEW_DATA_PREFIX + "networktype";
    private static final String LATITUDE_KEY = NEW_DATA_PREFIX + "latitude";
    private static final String LONGITUDE_KEY = NEW_DATA_PREFIX + "longitude";
    private static final String BYTES_SENT_KEY = NEW_DATA_PREFIX + "bytessent";
    private static final String BYTES_RECEIVED_KEY = NEW_DATA_PREFIX + "bytesreceived";
    private static final String ACTIVE_WIFI_KEY = NEW_DATA_PREFIX + "activewifi";
    private static final String RUNNING_APPS_KEY = NEW_DATA_PREFIX + "runningapps";
    private static final String MEMORY_USAGE_KEY = NEW_DATA_PREFIX + "memory";

    public NewData() {
        super(NEW_DATA);
        this.putExtras(new Bundle());
    }

    public Integer getRssi() {
        return (Integer) this.getExtras().get(RSSI_KEY);
    }

    public void setRssi(Integer rssi) {
        if (rssi != null && rssi < 0) {
            this.putExtra(RSSI_KEY, rssi.intValue());
        }
    }

    public Integer getRsrp() {
        return (Integer) this.getExtras().get(RSRP_KEY);
    }

    public void setRsrp(Integer rsrp) {
        if (rsrp != null && rsrp < 0) {
            this.putExtra(RSRP_KEY, rsrp.intValue());
        }
    }

    public String getOperator() {
        return (String) this.getExtras().get(OPERATOR_KEY);
    }

    public void setOperator(String operator) {
        if (operator != null) {
            this.putExtra(OPERATOR_KEY, operator);
        }
    }

    public String getNetworkType() {
        return (String) this.getExtras().get(NETWORK_TYPE_KEY);
    }

    public void setNetworkType(String networkType) {
        if (networkType != null) {
            this.putExtra(NETWORK_TYPE_KEY, networkType);
        }
    }

    public Boolean isWifiActive() {
        return (Boolean) this.getExtras().get(ACTIVE_WIFI_KEY);
    }

    public void setActiveWifi(Boolean activeWifi) {
        if (activeWifi != null) {
            this.putExtra(ACTIVE_WIFI_KEY, activeWifi.booleanValue());
        }
    }

    public Float getBatteryLevel() {
        return (Float) this.getExtras().get(BATTERY_KEY);
    }

    public void setBatteryLevel(Float batteryLevel) {
        if (batteryLevel != null && batteryLevel > 0F) {
            this.putExtra(BATTERY_KEY, batteryLevel);
        }

    }

    public Double getLatitude() {
        return (Double) this.getExtras().get(LATITUDE_KEY);
    }

    public void setLatitude(Double latitude) {
        if (latitude != null) {
            this.putExtra(LATITUDE_KEY, latitude.doubleValue());
        }
    }

    public Double getLongitude() {
        return (Double) this.getExtras().get(LONGITUDE_KEY);
    }

    public void setLongitude(Double longitude) {
        if (longitude != null) {
            this.putExtra(LONGITUDE_KEY, longitude.doubleValue());
        }
    }

    public void setBytesReceived(Long received) {
        if (received != null && received > 0L) {
            this.putExtra(BYTES_RECEIVED_KEY, received.longValue());
        }
    }

    public Long getBytesReceived() {
        return (Long) this.getExtras().get(BYTES_RECEIVED_KEY);
    }

    public void setBytesSent(Long sent) {
        if (sent != null && sent > 0L) {
            this.putExtra(BYTES_SENT_KEY, sent.longValue());
        }
    }

    public Long getBytesSent() {
        return (Long) this.getExtras().get(BYTES_SENT_KEY);
    }

    public Integer getRunningApps() {
        return (Integer) this.getExtras().get(RUNNING_APPS_KEY);
    }

    public void setRunningApps(Integer nbApps) {
        if (nbApps != null && nbApps > 0) {
            this.putExtra(RUNNING_APPS_KEY, nbApps.intValue());
        }
    }

    public Float getMemoryUsage() {
        return (Float) this.getExtras().get(MEMORY_USAGE_KEY);
    }

    public void setMemoryUsage(Float memoryUsage) {
        if (memoryUsage != null && memoryUsage > 0F) {
            this.putExtra(MEMORY_USAGE_KEY, memoryUsage);
        }
    }

    public int size() {
        return this.getExtras().size();
    }

}
