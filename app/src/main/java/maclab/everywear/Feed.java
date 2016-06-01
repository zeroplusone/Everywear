package maclab.everywear;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class Feed extends AppCompatActivity {
    private ListView listView;
    private FeedListAdapter listAdapter;
    private ArrayList<FeedItem> feedItems = new ArrayList<>(5);
    private FeedItem feedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        feedItem = new FeedItem(1, "John", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);
        feedItem = new FeedItem(2, "Mary", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);
        feedItem = new FeedItem(3, "Jim", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);
        feedItem = new FeedItem(4, "Jenny", "wear", "status", "pic", "time", "url");
        feedItems.add(feedItem);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
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
}
