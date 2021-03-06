package com.zyj.weather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zouyingjie on 15/5/5.
 */

/**
 * 联网的工具类
 */
public class HttpUtil {
    /**
     * 请求旺网络获取数据
     * @param address : 请求的网络地址
     * @param listener : 请求完成之后的回调函数。保证了线程之间的通信
     */
    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //使用Java提供的方式发送请求
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuilder response = new StringBuilder();
                    String line = "";
                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    if(listener != null){
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    listener.onError(e);
                }finally {
                    if(connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
