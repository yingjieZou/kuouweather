package com.zyj.weather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.zyj.weather.util.HttpCallbackListener;
import com.zyj.weather.util.HttpUtil;
import com.zyj.weather.util.Utility;

public class AutoUpateService extends Service {
    public AutoUpateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int anHour = 1000*60*60*8;//八小时的毫秒数
        Long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoUpateService.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }
    private void updateWeather(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = sp.getString("weather_code","");
        String address = "http://www.weather.com.cn/data/cityinfo"+weatherCode+".html";
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherResponse(AutoUpateService.this,response);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
