package com.lenovo.smarttraffic.util;

import android.util.Log;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.lenovo.smarttraffic.InitApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public class MyVolley {
    static String path = "http://%s:8088/transportservice/action/%s.do";


    public enum B{
        SetCarMove("{\"CarId\":%s, \"CarAction\":\"%s\", \"UserName\":\"%s\"}"),
        GetCarAccountBalance("{\"CarId\":%s, \"UserName\":\"%s\""),
        GetCarInfo("{\"UserName\":\"%s\"}"),
        testresult("{\"UserName\":\"%s\"}")
        ;

        String param;

        B(String param) {
            this.param = param;
        }

        public Executor exec(Object... obj) {
                       //{"CarId":%s, "CarAction":"%s", "UserName":"%s"}
            return new MyVolley.Executor(this.name(), String.format(param, obj).trim());
                       // ( SetCarMove,"{"CarId":1, "CarAction":"Stop", "UserName":"user1"}")
        }
    }

    public static class Executor{
        String methodName;
        String parma;

        public Executor(String methodName,String param){
            this.methodName = methodName;
            this.parma = param;
        }

        public void exec(Object caller,Object...args){
            //拼接    链接  String.format(path,InitApp.IP, methodName)
            JsonObjectRequest jsonObjectRequest = null;
            try {
                jsonObjectRequest = new JsonObjectRequest(String.format(path,
                        InitApp.IP, methodName),new JSONObject(parma), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        for (Method method : caller.getClass().getDeclaredMethods()) {
                            if(method.getName().equals(methodName.trim())){
                                try {
                                    if(args.length == 0){
                                        method.invoke(caller,jsonObject);
                                    }else{
                                        Object[] objects = new Object[args.length + 1];
                                        objects[0] = jsonObject;
                                        for (int i = 0; i < args.length; i++) {
                                            objects[i + 1] = args[i];
                                        }
                                        method.invoke(caller, (Object[])objects);
                                    }
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        Log.e("onErrorResponse", "onErrorResponse: " + methodName + " : " + parma+":"+String.format(path,
                                InitApp.IP, methodName));
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getInstance().requestQueue.add(jsonObjectRequest);
        }
    }

    static RequestQueue requestQueue = Volley.newRequestQueue(InitApp.context);
    /**
     * 单例模式
     */
    private static MyVolley instance;

    private MyVolley() {

    }
    public synchronized static MyVolley getInstance() {
        if (instance == null) {
            synchronized (MyVolley.class) {
                if (instance == null) {
                    instance = new MyVolley();
                }
            }
        }
        return instance;
    }
}
