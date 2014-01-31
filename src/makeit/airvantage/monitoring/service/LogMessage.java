package makeit.airvantage.monitoring.service;

import android.content.Intent;

public class LogMessage extends Intent {

    public static final String LOG_EVENT = "makeit.av.monitoring.event.log";

    // keys used for broadcasting log events
    private static final String LOG = "log";

    public LogMessage(String message) {
        super(LOG_EVENT);

        this.putExtra(LOG, message);
    }

    public String getMessage() {
        return this.getStringExtra(LOG);
    }

}
