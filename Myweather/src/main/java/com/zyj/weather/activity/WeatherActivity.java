package com.zyj.weather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zyj.weather.R;
import com.zyj.weather.service.AutoUpateService;
import com.zyj.weather.util.HttpCallbackListener;
import com.zyj.weather.util.HttpUtil;
import com.zyj.weather.util.Utility;

/**
 * Created by zouyingjie on 15/5/8.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {

    private LinearLayout weatherInfoLayout;
    private TextView cityName;
    private TextView publishTime;
    private TextView weatherDesp;
    private TextView temp1;
    private TextView temp2;
    private TextView currentDate;

    private Button switchCity;
    private Button refreshWeather;

    String countyCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        //初始化控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityName = (TextView) findViewById(R.id.tv_city_name);
        publishTime = (TextView) findViewById(R.id.tv_publish_text);
        weatherDesp = (TextView) findViewById(R.id.tv_weather_desp);
        temp1 = (TextView) findViewById(R.id.temp1);
        temp2 = (TextView) findViewById(R.id.temp2);
        currentDate = (TextView) findViewById(R.id.tv_current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);

        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);

        countyCode = getIntent().getStringExtra("county_code");

        if(!TextUtils.isEmpty(countyCode)){
            publishTime.setText("同步中");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            //如果没有县级代码那么直接显示本地天气
            showWeather();
        }
    }

    private void queryWeatherCode(String countyCode){
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address,"countyCode");
    }

    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        queryFromServer(address, "weatherCode");
    }

    private void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[] array = response.split("\\|");
                        if(array!=null && array.length==2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            publishTime.setText("同步失败");
                        }
                    });
            }
        });
    }

    public void showWeather(){
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(sp.getString("city_name",""));
        temp1.setText(sp.getString("temp1",""));
        temp2.setText(sp.getString("temp2",""));
        weatherDesp.setText(sp.getString("desp",""));
        publishTime.setText("今天"+sp.getString("publish_time","")+"点发布");
        currentDate.setText(sp.getString("current_date",""));

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpateService.class);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this,ChooseAreaActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:
                Intent intent = new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishTime.setText("同步中");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = sp.getString("weather_code","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:
                break;
        }
    }
}
