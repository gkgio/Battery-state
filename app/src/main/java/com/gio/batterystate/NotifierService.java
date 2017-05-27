package com.gio.batterystate;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;

public class NotifierService extends Service {

    private static final int CRITICAL_BATTERY_LEVEL = 15;
    private static final int NOTIFIER_ID = 15;

    private boolean showNotification = false;

    public NotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryStateReceiver, batteryFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Intent.ACTION_DELETE.equals(intent.getAction())) {
            showNotification = false;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(batteryStateReceiver);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    private BroadcastReceiver batteryStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int maximumBattery = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
            int currentBattery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            float fPercent = ((float) currentBattery / (float) maximumBattery) * 100f;
            int percent = Math.round(fPercent);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (percent < CRITICAL_BATTERY_LEVEL) {
                if (showNotification) {
                    Notification.Builder builder = new Notification.Builder(NotifierService.this);
                    builder.setSmallIcon(R.drawable.ic_stat_battery);
                    builder.setContentTitle(getString(R.string.warning_battery_low));


                    Intent startBattery = new Intent();
                    startBattery.setComponent(new ComponentName("com.gio.batterystate",
                            "com.gio.batterystate.MainActivity"));

                    startBattery.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    builder.setContentIntent(PendingIntent.getActivity(NotifierService.this, 10, startBattery, PendingIntent.FLAG_CANCEL_CURRENT));

                    Intent startSelf = new Intent(NotifierService.this, NotifierService.class);
                    startSelf.setAction(Intent.ACTION_DELETE);

                    builder.setDeleteIntent(PendingIntent.getService(NotifierService.this, 1, startSelf, PendingIntent.FLAG_CANCEL_CURRENT));
                    Notification notification = builder.build();

                    nm.notify(NOTIFIER_ID, notification);
                }
            } else {
                nm.cancel(NOTIFIER_ID);
                showNotification = true;
            }
        }
    };

}
