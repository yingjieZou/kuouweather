package com.zyj.weather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zyj.weather.R;
import com.zyj.weather.db.CoolWeatherDB;
import com.zyj.weather.model.City;
import com.zyj.weather.model.County;
import com.zyj.weather.model.Province;
import com.zyj.weather.util.HttpCallbackListener;
import com.zyj.weather.util.HttpUtil;
import com.zyj.weather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zouyingjie on 15/5/5.
 */
public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView textView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<String>();

    private List<Province> provincesList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;

    private int currentLevel = 0;

    private boolean isFromActivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(sp.getBoolean("city_selected",false) && !isFromActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        Log.i("Main", "初始化");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        listView = (ListView) findViewById(R.id.lv_list_view);
        textView = (TextView) findViewById(R.id.tv_title_text);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);

        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provincesList.get(position);
                    Log.i("Main","查询的省份"+selectedProvince.getProvinceName());
                    queryCitis();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){

                        String countyCode = countyList.get(position).getCountyCode();
                        Log.i("Main","查询的县区"+countyCode);
                        Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                        intent.putExtra("county_code",countyCode);
                        startActivity(intent);
                        finish();


                }
            }
        });
        Log.i("Main","开始加载省级数据");
        queryProvince();//加载省级数据
    }

/*    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);


    }*/

    /**
     * 查询全国所有的省份 优先从数据库开始查询，如果没有则联网查询
     */
    private void queryProvince() {
        provincesList = coolWeatherDB.loadProvince();
        if (provincesList.size() > 0) {
            Log.i("Main","从数据库得到数据");
            dataList.clear();

            for (Province p : provincesList) {
                dataList.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            Log.i("Main","联网获取数据");
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询选中的省内所有的市，优先从数据库查询，如果没有的话再从服务器查询
     */
    private void queryCitis() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getId());

        if (cityList.size() > 0) {
            Log.i("Main","从数据库读取城市列表");
            dataList.clear();
            for (City c : cityList) {
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            Log.i("Main","联网获取城市列表");
            Log.i("Main",selectedProvince.getProvinceCode());
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    private void queryCounties() {
        Log.i("Main","*******查询县区****");
        countyList = coolWeatherDB.loadCounty(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County c : countyList) {
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            Log.i("Main","*******联网查询县区****");
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 根据传入的代号的类型从服务器查询省市县的数据
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        Log.i("Main",address);
        showProcessDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    Log.i("Main",response.toString());
                    result = Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
                }

                if (result) {
                    //通过runOnUiThread()方法回到主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProcessDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCitis();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProcessDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示对话框
     */

    private void showProcessDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProcessDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {

        if(currentLevel == LEVEL_COUNTY){
            queryCitis();
        }else if (currentLevel == LEVEL_CITY){
            queryProvince();
        }else{
            if(isFromActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}

