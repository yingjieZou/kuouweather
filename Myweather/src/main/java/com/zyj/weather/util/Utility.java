package com.zyj.weather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.zyj.weather.db.CoolWeatherDB;
import com.zyj.weather.model.City;
import com.zyj.weather.model.County;
import com.zyj.weather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zouyingjie on 15/5/5.
 */
public class Utility {

    //

    /**
     * 解析和处理服务器返回的省级数据,数据格式为:“代号|省份,代号|省份”
     *
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response) {
        Log.i("Main","省份"+response);
        if (!TextUtils.isEmpty(response)) {
            String[] allProvince = response.split(",");
            if (allProvince != null && allProvince.length > 0) {
                for (String p : allProvince) {
                    String[] array = p.split("\\|");
                    for(int i=0; i<array.length; i++){

                    }
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);

                    coolWeatherDB.saveProvince(province);

                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和存储服务器返回的市级数据
     *
     * @param coolWeatherDB
     * @param response
     * @param provinceId
     * @return
     */
    public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,
                                                          String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] allCitys = response.split(",");
            if (allCitys != null && allCitys.length > 0) {
                for (String c : allCitys) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProviceId(provinceId);

                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县区级数据
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
     */
    public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,
                                                                 String response, int cityId) {
        if(!TextUtils.isEmpty(response)){
            String[] allCounty = response.split(",");
            if(allCounty!=null && allCounty.length>0){
                for(String c : allCounty){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);

                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;

    }

    /**
     * 解析服务器返回的Json数据并将返回的数据存储到本地
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context, String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            //传入的参数的JSON对象的名称
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatheCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publicTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatheCode,temp1,temp2,weatherDesp,publicTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void saveWeatherInfo(Context context,String cityName,String weatherCode,
                                            String temp1,String temp2,String desp,String publishTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name",cityName);
        editor.putString("weather_code",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weather_desp",desp);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date",sdf.format(new Date()));

        editor.commit();

    }

}
