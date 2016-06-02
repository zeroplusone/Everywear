package maclab.everywear;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btn_feed;
    private Button btn_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_feed = (Button)findViewById(R.id.btn_feed);
        btn_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Feed.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle); // empty bundle
                startActivity(intent);
            }
        });

        btn_camera = (Button)findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent();
                cameraIntent.setClass(MainActivity.this, Camera.class);
                startActivity(cameraIntent);
            }
        });

    }


}
