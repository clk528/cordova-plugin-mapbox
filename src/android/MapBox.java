package com.clk528.mapbox;

/**
 * @author clk528@qq.com
 * @Date 2018年12月4日 20:48:08
 * 为mapbox 提供离线资源支持
 */
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.app.AlertDialog;

import com.koushikdutta.async.http.Multimap;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * This class echoes a string called from JavaScript.
 */
public class MapBox extends CordovaPlugin {

    private String SQLITE_PATH = null;

    private static final String SQLITE_NAME = "zoo.db";

    private AsyncHttpServer httpServer = null;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        SQLITE_PATH = "/data/data/" + cordova.getActivity().getPackageName() + "/databases/";
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("start")) {
            init();
            this.start(callbackContext);
            return true;
        }

        if (action.equals("stop")) {
            this.stop(callbackContext);
            return true;
        }

        if (action.equals("getMetaData")) {
            this.getMetaData(callbackContext);
            return true;
        }

        if(action.equals("test")) {
            this.Alert("test success~ my name is clk528");
            callbackContext.success("success");
            return true;
        }

        return false;
    }

    private void start(CallbackContext callbackContext){
        if (httpServer != null && httpServer instanceof AsyncHttpServer) {
            callbackContext.error("Server has started");
        } else {
            httpServer = new AsyncHttpServer();

            httpServer.get("/", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    Headers headers = response.getHeaders();
                    headers.set("Access-Control-Allow-Origin", "*"); 
                    headers.set("auth", "clk");
                    response.send("{\"name\":\"chenlongke\"}");
                }
            });

            httpServer.get("/clk", new HttpServerRequestCallback() {//\?z=[+]{0,1}(\d+)&x=[+]{0,1}(\d+)&y=[+]{0,1}(\d+)
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                    SQLiteDatabase sql = null;
                    Cursor cursor = null;

                    Headers headers = response.getHeaders();

                    headers.set("Access-Control-Allow-Origin", "*");
                    headers.set("auth", "clk");
                    headers.set("Content-Encoding", "gzip");
                    
                    try {
                        Log.i("DBManger", "request path:" + request.getPath());

                        Multimap map = request.getQuery();

                        String z = map.getString("z");
                        String x = map.getString("x");
                        String y = map.getString("y");


                        int temp = (int) Math.pow(2, Integer.parseInt(z)) - 1 - Integer.parseInt(y);

                        y = String.valueOf(temp);

                        Log.i("DBManger", "request params:\tz=" + z + "\tx=" + x + "\ty=" + y + "\tparse:" + temp);


                        String srcDbName = SQLITE_PATH + SQLITE_NAME;

                        Log.i("DBManger", "读取数据库");

                        sql = SQLiteDatabase.openDatabase(srcDbName, null, SQLiteDatabase.OPEN_READONLY);

                        cursor = sql.rawQuery("select tile_data as t from tiles where zoom_level=? and tile_column=? and tile_row=?", new String[]{z, x, y});

                        if (cursor == null || cursor.getCount() == 0) {
                            response.setContentType("application/json; charset=utf-8");

                            Log.i("DBManger", "没有数据");

                            cursor.close();
                            sql.close();

                            response.code(204);
                            response.send("{\"message\":\"Tile does not exist\"}");
                        } else {

                            cursor.moveToFirst();

                            byte[] bytes = cursor.getBlob(0);

                            cursor.close();
                            sql.close();

                            Log.i("DBManger", "读取数据库成功:");

                            response.send("application/x-protobuf", bytes);
                        }

                    } catch (Exception e) {
                        Log.i("DBManger", "读取数据库发生了了错误:" + e.getMessage());
                        e.printStackTrace();

                        if (sql instanceof SQLiteDatabase) {
                            sql.close();
                        }

                        if (cursor instanceof Cursor) {
                            cursor.close();
                        }

                        response.setContentType("application/json; charset=utf-8");
                        response.code(204);
                        response.send("{\"message\":\"Tile does not exist\"}");
                    }
                }
            });

            httpServer.get("/img/icons_pl.png", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    Headers headers = response.getHeaders();
                    headers.set("Access-Control-Allow-Origin", "*");
                    try {
                        InputStream is = cordova.getActivity().getAssets().open("img/icons_pl.png");
                        response.setContentType("image/png");
                        response.sendStream(is, is.available());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("DBManger", "读取图片发生了了错误:" + e.getMessage());
                        response.code(404);
                        response.end();
                    }
                }
            });
            httpServer.get("/img/icons_pl.json", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    Headers headers = response.getHeaders();
                    headers.set("Access-Control-Allow-Origin", "*");
                    try {
                        InputStream is = cordova.getActivity().getAssets().open("img/icons_pl.json");
                        response.setContentType("application/json");
                        response.sendStream(is, is.available());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("DBManger", "读取icons_pl.json发生了了错误:" + e.getMessage());
                        response.code(404);
                        response.end();
                    }
                }
            });

            httpServer.get("/img/icons_pl@2x.png", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    Headers headers = response.getHeaders();
                    headers.set("Access-Control-Allow-Origin", "*");
                    try {
                        InputStream is = cordova.getActivity().getAssets().open("img/icons_pl@2x.png");
                        response.setContentType("image/png");
                        response.sendStream(is, is.available());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("DBManger", "读取图片发生了了错误:" + e.getMessage());
                        response.code(404);
                        response.end();
                    }
                }
            });
            
            httpServer.get("/img/icons_pl@2x.json", new HttpServerRequestCallback() {
                @Override
                public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                    Headers headers = response.getHeaders();
                    headers.set("Access-Control-Allow-Origin", "*");
                    try {
                        InputStream is = cordova.getActivity().getAssets().open("img/icons_pl@2x.json");
                        response.setContentType("application/json");
                        response.sendStream(is, is.available());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("DBManger", "读取icons_pl.json发生了了错误:" + e.getMessage());
                        response.code(404);
                        response.end();
                    }
                }
            });

            httpServer.listen(3000);
            callbackContext.success("success");
        }
    }

    private void stop(CallbackContext callbackContext){
        if (httpServer != null && httpServer instanceof AsyncHttpServer) {
            httpServer.stop();
            httpServer = null;                
        } else {     
            callbackContext.error("Server not started");
        }
        callbackContext.success("success");
    }

    private void getMetaData(CallbackContext callbackContext){
        SQLiteDatabase sql = null;
        Cursor cursor = null;

        try {
            String srcDbName = SQLITE_PATH + SQLITE_NAME;

            sql = SQLiteDatabase.openDatabase(srcDbName, null, SQLiteDatabase.OPEN_READONLY);

            cursor = sql.rawQuery("select * from metadata", null);

            JSONObject jsonObject = new JSONObject();

            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                if (name.equals("json")) {
                    jsonObject.put(name, new JSONObject(cursor.getString(1)));
                } else {
                    jsonObject.put(cursor.getString(0), cursor.getString(1));
                }
            }

            Log.i("DBManger", jsonObject.toString());
            callbackContext.success(jsonObject.toString());

        } catch (Exception e) {
            Log.i("DBManger", "转换过程报错了：" + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private void Alert(String info){
        new AlertDialog.Builder(cordova.getContext())
                .setMessage(info)
                .setPositiveButton("OK", null).show();
    }

    private void init() {
        try {

            File file = new File(SQLITE_PATH);

            if(!file.exists()){
                file.mkdir();
            }

            String srcDbName = SQLITE_PATH + SQLITE_NAME;

            InputStream inputStream = cordova.getActivity().getAssets().open("zoo.db");

            FileOutputStream fileOutputStream = new FileOutputStream(srcDbName);

            Log.i("DBManger", "读取成功");

            Log.i("DBManger", "开始导入");

            byte[] buf = new byte[1024 * 8];

            int len = 0;

            while ((len = inputStream.read(buf)) != -1) {
                fileOutputStream.write(buf, 0, len);
            }
            fileOutputStream.close();
            inputStream.close();
            Log.i("DBManger", "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("DBManger", "发生了了错误:" + e.getMessage());
        }
    }
}
