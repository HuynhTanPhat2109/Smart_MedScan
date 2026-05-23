package ntu.tanphat.smart_medscan.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import ntu.tanphat.smart_medscan.receivers.AlarmReceiver;

public class AlarmScheduler {

    public static boolean canScheduleExactAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }

        return true;
    }

    public static boolean scheduleCareReminder(Context context,
                                               String time,
                                               String title,
                                               String patientName,
                                               String roomName) {
        if (time == null || !time.contains(":")) {
            return false;
        }

        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long triggerTime = calendar.getTimeInMillis();

            if (triggerTime <= System.currentTimeMillis()) {
                return false;
            }

            if (!canScheduleExactAlarm(context)) {
                return false;
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("patientName", patientName);
            intent.putExtra("roomName", roomName);
            intent.putExtra("time", time);

            int requestCode = Math.abs((title + patientName + roomName + time).hashCode());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager == null) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

            return true;

        } catch (SecurityException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}