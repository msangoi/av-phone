package makeit.airvantage.monitoring.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryChangeListener extends BroadcastReceiver {

    private float batteryLevel;

    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryLevel = level / (float) scale;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

}
