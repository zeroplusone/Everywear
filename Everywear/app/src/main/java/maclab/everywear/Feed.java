package maclab.everywear;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Feed extends AppCompatActivity {
    private ListView listView;
    private FeedListAdapter listAdapter;
    //private ArrayList<FeedItem> feedItems;
    private FeedItem feedItem;

    private ImageButton tab_feed;
    private ImageButton tab_camera;
    private ImageButton tab_setting;

    ProgressDialog  progress;

    private FBData fbData;
    private String databaseUrl = "http://140.116.245.241:9999/UserPost.php";

    private final int SETTING_LOGIN_REQUEST = 10;
    private final int CAMERA_LOGIN_REQUEST = 20;
    private final int UPDATE_FEEDS_REQUEST = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        tab_feed =(ImageButton)findViewById(R.id.tab_feed);
        tab_camera =(ImageButton)findViewById(R.id.tab_camera);
        tab_setting =(ImageButton)findViewById(R.id.tab_setting);

        tab_feed.setOnClickListener(feedOnClickListener);
        tab_camera.setOnClickListener(cameraOnClickListener);
        tab_setting.setOnClickListener(settingOnClickListener);

        listView = (ListView)findViewById(R.id.listview_feed);
        listAdapter = new FeedListAdapter(this, new ArrayList<FeedItem>());
        listView.setAdapter(listAdapter);
        progress  = new ProgressDialog(this);

        checkNetworkStatus();
        loadFeed();
        fbData = new FBData();
    }

    private void loadFeed(){
        //getPostRequest();
        progress.setTitle("Updating Feed");
        progress.setMessage("Wait while loading...");
        progress.show();
        new GetPostRequest().execute();
    }/*

    private void getPostRequest() {
        class AddPostRunnable implements Runnable {

            @Override
            public void run() {
                databaseUrl+="?action=getPost";


                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(databaseUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    try {

                        JSONArray array = new JSONArray(readStream(urlConnection.getInputStream()));

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            feedItem = new FeedItem(Integer.valueOf(jsonObject.getString("no")), jsonObject.getString("name"), jsonObject.getString("pic"), jsonObject.getString("weather_pic"));
                            feedItems.add(feedItem);
                        }
                        listAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.e("Everywear", "unexpected JSON exception", e);
                        // Do something to recover ... or kill the app.
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }
        Thread t = new Thread(new AddPostRunnable());
        t.start();
    }*/
    // Using an AsyncTask to load the slow images in a background thread
    private class GetPostRequest extends AsyncTask<Void, Void, ArrayList<FeedItem>>{

        @Override
        protected ArrayList<FeedItem> doInBackground(Void... params) {
            databaseUrl="http://140.116.245.241:9999/UserPost.php?action=getPost";

            URL url = null;
            HttpURLConnection urlConnection = null;
            ArrayList<FeedItem> postList = new ArrayList<FeedItem>();
            try {
                url = new URL(databaseUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                try {

                    JSONArray array = new JSONArray(readStream(urlConnection.getInputStream()));

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonObject = array.getJSONObject(i);
                        feedItem = new FeedItem(Integer.valueOf(jsonObject.getString("no")), jsonObject.getString("name"), jsonObject.getString("pic"), jsonObject.getString("weather_pic"));
                        postList.add(feedItem);
                    }

                } catch (JSONException e) {
                    Log.e("Everywear", "unexpected JSON exception", e);
                    // Do something to recover ... or kill the app.
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return postList;
        }

        @Override
        protected void onPostExecute(ArrayList<FeedItem> result) {
            super.onPostExecute(result);
            listAdapter.swipeItem(result);
            progress.dismiss();
        }
    }


    private String readStream(InputStream in) {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line = null;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString();
    }

    private ImageButton.OnClickListener feedOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            loadFeed();
        }
    };

    private ImageButton.OnClickListener cameraOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(!fbData.getIsLogin()) {
                login(CAMERA_LOGIN_REQUEST);
            } else {
                Intent cameraIntent = new Intent();
                cameraIntent.setClass(Feed.this, Camera.class);
                cameraIntent.putExtra("data", fbData);
                startActivityForResult(cameraIntent, UPDATE_FEEDS_REQUEST);
            }
        }
    };

    private ImageButton.OnClickListener settingOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {

            if(!fbData.getIsLogin()) {
                login(SETTING_LOGIN_REQUEST);
            }
            else {
                Intent settingIntent = new Intent();
                settingIntent.setClass(Feed.this, Setting.class);
                settingIntent.putExtra("data", fbData);
                startActivity(settingIntent);
            }
        }
    };

    private void login(int resultIndex){
        Intent fbIntent = new Intent();
        fbIntent.setClass(Feed.this, FBLogin.class);
        fbIntent.putExtra("data", fbData);
        startActivityForResult(fbIntent, resultIndex);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTING_LOGIN_REQUEST) {
            if(resultCode == RESULT_OK){
                fbData = (FBData) data.getSerializableExtra("data");
                Intent settingIntent = new Intent();
                settingIntent.setClass(Feed.this, Setting.class);
                settingIntent.putExtra("data", fbData);
                startActivity(settingIntent);
            }
        } else if (requestCode == CAMERA_LOGIN_REQUEST) {
            if(resultCode == RESULT_OK){
                fbData = (FBData) data.getSerializableExtra("data");
                Intent cameraIntent = new Intent();
                cameraIntent.setClass(Feed.this, Camera.class);
                cameraIntent.putExtra("data", fbData);
                startActivityForResult(cameraIntent, UPDATE_FEEDS_REQUEST);
            }
        }else if (requestCode == UPDATE_FEEDS_REQUEST) {
            if(resultCode == RESULT_OK){
                loadFeed();
            }
        }
    }

    private void checkNetworkStatus() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

        //如果未連線的話，mNetworkInfo會等於null
        if (mNetworkInfo != null) {
            //網路是否可使用
            if (!mNetworkInfo.isAvailable()) {
                dialog("網路無法連線");
            }

        } else {
            dialog("請連接網路");
        }
    }

    protected void dialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override


            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }


        });

        builder.create().show();
    }

}
