package com.example.dongja94.samplegooglemap;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by dongja94 on 2015-10-20.
 */
public class NetworkManager {
    private static NetworkManager instance;
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    AsyncHttpClient client;
    Gson gson;
    Gson routeGson;
    Header[] headers;

    private NetworkManager() {

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client = new AsyncHttpClient();
            client.setSSLSocketFactory(socketFactory);
            client.setCookieStore(new PersistentCookieStore(MyApplication.getContext()));
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }


        gson = new Gson();
        routeGson = new GsonBuilder().registerTypeAdapter(Geometry.class, new GeometryDeserializer()).create();

        headers = new Header[2];
        headers[0] = new BasicHeader("Accept", "application/json");
        headers[1] = new BasicHeader("appKey", "458a10f5-c07e-34b5-b2bd-4a891e024c2a");

   }

    public HttpClient getHttpClient() {
        return client.getHttpClient();
    }

    public interface OnResultListener<T> {
        public void onSuccess(T result);
        public void onFail(int code);
    }

    public void cancelAll(Context context) {
        client.cancelRequests(context, true);
    }

    public static final String FIND_POI_URL = "https://apis.skplanetx.com/tmap/pois";
    public void findPOI(Context context, String keyword, final OnResultListener<SearchPOIInfo> listener) {
        RequestParams params = new RequestParams();
        params.put("version", 1);
        params.put("searchKeyword", keyword);
        params.put("resCoordType", "WGS84GEO");

        client.get(context, FIND_POI_URL, headers, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                POIResult result = gson.fromJson(responseString, POIResult.class);
                listener.onSuccess(result.searchPoiInfo);
            }
        });
    }

    public static final String CAR_ROUTE_UTL = "https://apis.skplanetx.com/tmap/routes?version=1";
    public void findPath(Context context, LatLng start, LatLng end, final OnResultListener<CarRouteInfo> listener) {
        RequestParams params = new RequestParams();
        params.put("endX",end.longitude);
        params.put("endY",end.latitude);
        params.put("startX",start.longitude);
        params.put("startY",start.latitude);
        params.put("resCoordType","WGS84GEO");
        params.put("reqCoordType","WGS84GEO");

        client.post(context, CAR_ROUTE_UTL, headers, params, null, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                listener.onFail(statusCode);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                CarRouteInfo info = routeGson.fromJson(responseString, CarRouteInfo.class);
                listener.onSuccess(info);
            }
        });
    }


}
