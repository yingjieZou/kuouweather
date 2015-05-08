package com.zyj.weather.util;

import android.text.TextUtils;
import android.util.Log;

import com.zyj.weather.db.CoolWeatherDB;
import com.zyj.weather.model.City;
import com.zyj.weather.model.County;
import com.zyj.weather.model.Province;

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

}
