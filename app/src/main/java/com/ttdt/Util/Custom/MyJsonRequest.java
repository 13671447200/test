package com.ttdt.Util.Custom;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/19.
 */

public class MyJsonRequest extends JsonRequest<JSONObject> {

    public MyJsonRequest(int method, String url, String requestBody, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        //(int method, String url, String requestBody, Listener<T> listener, ErrorListener errorListener)
        super(method, url, requestBody, listener, errorListener);
    }

    public MyJsonRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        //(int method, String url, String requestBody, Listener<T> listener, ErrorListener errorListener)
        super(method, url, null, listener, errorListener);
    }

    public MyJsonRequest(String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        //(int method, String url, String requestBody, Listener<T> listener, ErrorListener errorListener)
        super(Method.GET, url, null, listener, errorListener);
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        RetryPolicy retryPolicy = new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 15;
            }

            @Override
            public int getCurrentRetryCount() {
                return 0;
            }

            @Override
            public void retry(VolleyError volleyError) throws VolleyError {

            }
        };

        return retryPolicy;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String je = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(je), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException var3) {
            return Response.error(new ParseError(var3));
        } catch (JSONException var4) {
            return Response.error(new ParseError(var4));
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Charset", "UTF-8");
//        headers.put("Content-Type", "text/html");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

}
