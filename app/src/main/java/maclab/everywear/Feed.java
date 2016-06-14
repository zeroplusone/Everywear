package maclab.everywear;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class Feed extends AppCompatActivity {
    private ListView listView;
    private FeedListAdapter listAdapter;
    private ArrayList<FeedItem> feedItems = new ArrayList<>(5);
    private FeedItem feedItem;

    private ImageButton tab_feed;
    private ImageButton tab_camera;
    private ImageButton tab_setting;


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

        loadFeed();
    }

    private void loadFeed(){
        feedItem = new FeedItem(1, "John", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);
        feedItem = new FeedItem(2, "Mary", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);
        feedItem = new FeedItem(3, "Jim", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);
        feedItem = new FeedItem(4, "Jenny", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);




        listView = (ListView)findViewById(R.id.listview_feed);
        listAdapter = new FeedListAdapter(this, feedItems);
        listView.setAdapter(listAdapter);
        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> patent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "You choose " + feedItems.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private ImageButton.OnClickListener feedOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {

        }
    };

    private ImageButton.OnClickListener cameraOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            Intent cameraIntent = new Intent();
            cameraIntent.setClass(Feed.this, Camera.class);
            startActivity(cameraIntent);
        }
    };

    private ImageButton.OnClickListener settingOnClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {

        }
    };
}
