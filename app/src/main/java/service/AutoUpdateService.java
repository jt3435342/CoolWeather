package service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import reciever.AutoUpdateReceiver;
import utils.HttpListener;
import utils.HttpUtils;
import utils.Utility;

/**
 * Created by JT on 2016/6/19.
 */
public class AutoUpdateService extends Service{
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager alarmManager =(AlarmManager)getSystemService(ALARM_SERVICE);
        int hours = 20*1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+hours;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void updateWeather(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = sharedPreferences.getString("weather_code","");
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode + ".html";
        HttpUtils.sendHttpRequest(address, new HttpListener() {
            @Override
            public void onResponse(String response) {
                Utility.handleWeatherResponse(AutoUpdateService.this,response);
                Toast.makeText(AutoUpdateService.this,"Updating weather info",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
