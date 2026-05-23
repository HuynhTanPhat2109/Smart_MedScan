package ntu.tanphat.smart_medscan.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ntu.tanphat.smart_medscan.utils.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String patientName = intent.getStringExtra("patientName");
        String roomName = intent.getStringExtra("roomName");
        String time = intent.getStringExtra("time");

        if (title == null) title = "Lịch chăm sóc";
        if (patientName == null) patientName = "Bệnh nhân";
        if (roomName == null) roomName = "Chưa có phòng";
        if (time == null) time = "";

        String notificationTitle = "Đến giờ chăm sóc";
        String notificationContent = time + " • " + title + " cho " + patientName + " - " + roomName;

        NotificationHelper.showCareScheduleNotification(
                context,
                notificationTitle,
                notificationContent
        );
    }
}