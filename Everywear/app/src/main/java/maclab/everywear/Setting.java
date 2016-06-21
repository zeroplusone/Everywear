package maclab.everywear;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Setting extends AppCompatActivity {

    private TextView userIdTv;
    private TextView userNameTv;
    private ImageView userPicIv;
    private ImageButton tab_feed;
    private ImageButton tab_camera;
    private FBData fbData;

    private Bitmap bmp;

    private final int UPDATE_USERPIC = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        fbData = (FBData) getIntent().getSerializableExtra("data");

        userIdTv =(TextView)findViewById(R.id.id_value);
        userNameTv =(TextView)findViewById(R.id.name_value);
        userPicIv = (ImageView)findViewById(R.id.userpic_iv);

        userIdTv.setText(": "+fbData.getUserId());
        userNameTv.setText(": "+fbData.getUserName());

        tab_feed =(ImageButton)findViewById(R.id.tab_feed);
        tab_camera =(ImageButton)findViewById(R.id.tab_camera);

        tab_feed.setOnClickListener(feedOnClickListener);
        tab_camera.setOnClickListener(cameraOnClickListener);

        getUserPic();

    }
    Handler uiHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE_USERPIC:
                    userPicIv.setImageBitmap(bmp);
            }
        }

    };

    private void getUserPic(){
        class GetUserPicRunnable implements Runnable {

            @Override
            public void run() {
                URL url = null;
                String bigPic="http://graph.facebook.com/"+fbData.getUserId()+"/picture?type=large&redirect=true&width=400&height=400";
                Log.d("FB", bigPic);
                try {
                    url = new URL(bigPic);
                    HttpURLConnection ucon = (HttpURLConnection) url.openConnection();
                    ucon.setInstanceFollowRedirects(false);
                    URL secondURL = new URL(ucon.getHeaderField("Location"));
                    Log.d("FB",secondURL.toString());
                    bmp = BitmapFactory.decodeStream(secondURL.openConnection().getInputStream());
                    Message msg = new Message();
                    msg.what = UPDATE_USERPIC;
                    uiHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Thread t = new Thread(new GetUserPicRunnable());
        t.start();
    }

    private ImageButton.OnClickListener feedOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private ImageButton.OnClickListener cameraOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            Intent cameraIntent = new Intent();
            cameraIntent.setClass(Setting.this, Camera.class);
            cameraIntent.putExtra("data", fbData);
            startActivity(cameraIntent);
            finish();
        }
    };
}
