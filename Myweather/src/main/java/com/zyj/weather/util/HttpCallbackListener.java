package com.zyj.weather.util;

/**
 * Created by zouyingjie on 15/5/5.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
