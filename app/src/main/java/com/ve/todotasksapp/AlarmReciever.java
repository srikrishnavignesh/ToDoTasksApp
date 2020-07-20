package com.ve.todotasksapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ve.todotasksapp.taskCreation.TaskAlarmCreator;
import com.ve.todotasksapp.taskShow.MainActivity;
import com.ve.todotasksapp.taskShow.Task;

import java.util.Random;

public class AlarmReciever extends BroadcastReceiver {

    private static final String CHANNEL_ID = "1";

    @Override
    public void onReceive(Context context, Intent intent) {

        /*create notificationChannel*/
        createNotificationChannel(context);

        /*build task using intent retrieved */
        Task currentTask=buildTask(intent);

        //get a random quote from resource
        String quote=getArandomQuote(context);

        //create a notification and show user
        showNotification(context,currentTask,intent,quote);


    }

    private void showNotification(Context context,Task currentTask,Intent intent,String quote) {
        Intent sendIntent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, sendIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_profile_image)
                .setContentTitle(currentTask.getName())
                .setContentText(quote)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(currentTask.getTaskId(), builder.build());
        createAlarmForNextDay(context,currentTask);
    }

    /*getting a random quote*/
    private String getArandomQuote(Context context) {
        Random random=new Random();
        String quotes[]=context.getResources().getStringArray(R.array.heighStressQuotes);
        int index=Math.abs(random.nextInt())%quotes.length;
        return quotes[index];
    }
    /*build a task */
    private Task buildTask(Intent intent) {
        String notificationText=intent.getStringExtra("task_name");
        int id=intent.getIntExtra("task_unique_id",0);
        String time=intent.getStringExtra("task_time");
        int likablity=intent.getIntExtra("task_likablity",0);
        Task currentTask=new Task(id,notificationText,time,likablity);
        return currentTask;
    }
    //at the end update alarm to ring for next day at the same time
    private void createAlarmForNextDay(Context context,Task t) {
        TaskAlarmCreator creator=new TaskAlarmCreator(context,t);
        creator.createNextDayAlarm();
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "taskNotification";
            String description = "this notifier notifies about important tasks";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
