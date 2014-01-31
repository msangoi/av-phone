package makeit.airvantage.monitoring;

import makeit.airvantage.monitoring.service.LogMessage;
import makeit.airvantage.monitoring.service.MonitoringService;
import makeit.airvantage.monitoring.service.MonitoringService.ServiceBinder;
import makeit.airvantage.monitoring.service.NewData;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {

    private AlarmManager alarmManager;

    private String phoneUniqueId;

    private ServiceListener serviceListener = new ServiceListener(this);

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // register service listener
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceListener, new IntentFilter(NewData.NEW_DATA));
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceListener,
                new IntentFilter(LogMessage.LOG_EVENT));

        // phone identifier
        phoneUniqueId = Build.SERIAL;
        ((TextView) findViewById(R.id.phoneid_value)).setText(phoneUniqueId);

        boolean isServiceRunning = isServiceRunning();

        Switch serviceSwitch = (Switch) findViewById(R.id.service_switch);
        serviceSwitch.setChecked(isServiceRunning);

        if (isServiceRunning) {
            connectToService();
        }

        serviceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startMonitoringService();
                } else {
                    stopMonitoringService();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromService();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MonitoringService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startMonitoringService() {
        Intent intent = new Intent(this, MonitoringService.class);
        intent.putExtra(MonitoringService.DEVICE_ID, phoneUniqueId);

        String serverHost = prefs.getString(this.getString(R.string.pref_server_key), null);
        String password = prefs.getString(this.getString(R.string.pref_password_key), null);
        String period = prefs.getString(this.getString(R.string.pref_period_key), null);

        if (password == null || password.isEmpty() || serverHost == null || serverHost.isEmpty()) {
            new AlertDialog.Builder(this).setTitle(R.string.invalid_prefs).setMessage(R.string.prefs_missing)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).show();
            Switch serviceSwitch = (Switch) findViewById(R.id.service_switch);
            serviceSwitch.setChecked(false);
            return;
        }
        intent.putExtra(MonitoringService.SERVER_HOST, serverHost);
        intent.putExtra(MonitoringService.PASSWORD, password);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // registering our pending intent with alarm manager
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, Integer.valueOf(period) * 60 * 1000,
                pendingIntent);

        this.connectToService();
    }

    private void stopMonitoringService() {

        Intent intent = new Intent(this, MonitoringService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        this.stopService(intent);

        disconnectFromService();

        serviceListener.setStartedSince(null);
    }

    // Service binding

    private void connectToService() {
        bound = this.bindService(new Intent(this, MonitoringService.class), connection, BIND_AUTO_CREATE);
    }

    private void disconnectFromService() {
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    boolean bound = false;
    ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            MonitoringService service = ((ServiceBinder) binder).getService();
            serviceListener.setStartedSince(service.getStartedSince());
            serviceListener.setNewData(service.getLastData());
            serviceListener.setLogMessage(service.getLastLog(), service.getLastRun());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }

    };

    // Preferences

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            break;
        }

        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (isServiceRunning()) {
            // restart
            stopMonitoringService();
            startMonitoringService();
        }
    }
}
