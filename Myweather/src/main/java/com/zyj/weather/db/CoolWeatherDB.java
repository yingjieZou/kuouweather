package com.zyj.weather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zyj.weather.model.City;
import com.zyj.weather.model.County;
import com.zyj.weather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zouyingjie on 15/5/4.
 *封装数据库的操作
 */

/**
 * 单例类，通过单例模式获得对象，提供六个方法
 * 用来对省市区数据的存储和读取
 */
public class CoolWeatherDB {

    //数据库名
    public static final String DB_NAME = "cool_weather";

    //数据库版本
    public static final int VERSION = 1;

    //
    private SQLiteDatabase db;

    private static CoolWeatherDB coolWeatherDB;

    public CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper helper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = helper.getWritableDatabase();
    }

    //单例模式获取CoolWeatherDB的实例
    public synchronized static CoolWeatherDB getInstance(Context context) {
        if (coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /**
     * 将Province实例存储到数据库
     */
    public void saveProvince(Province province) {

        if (province != null) {
            ContentValues values = new ContentValues();

            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());

            db.insert("Province", null, values);
        }
    }

    /**
     * 从数据库读取全国所有的身省份信息
     */
    public List<Province> loadProvince() {
        List<Province> list = new ArrayList<Province>();

        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));

                list.add(province);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 将City实例存储到数据库
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();

            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());

            db.insert("City", null, values);
        }
    }

    /**
     * 读取数据库中城市的信息
     */
    public List<City> loadCity(int provinceId) {
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();

                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("cith_code")));
                city.setProviceId(cursor.getInt(cursor.getColumnIndex("province_id")));

                list.add(city);
            } while (cursor.moveToNext());
        }
        if(cursor != null){
            cursor.close();
        }
        return list;
    }

    /**
     * 存储县区的数据
     */
    public void saveCounty(County county) {

        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("city_id", county.getCityId());
            db.insert("County", null, values);
        }
    }
    /**
     * 读取县区的信息
     */
    public List<County> loadCounty(int cityId){
        List<County> list = new ArrayList<County>();
       Cursor cursor =  db.query("County",null,"city_id = ?",new String[]{String.valueOf(cityId)},null,null,null);

        if(cursor.moveToFirst()){
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
                list.add(county);
            }while (cursor.moveToNext());
        }
        if(cursor != null){
            cursor.close();
        }
        return list;
    }


}
