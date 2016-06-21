package maclab.everywear;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        TextView name = (TextView) convertView.findViewById(R.id.name);

        // get item
        FeedItem item = feedItems.get(position);
        // set user name
        name.setText(item.getName());

        ImageView profileIv = (ImageView) convertView.findViewById(R.id.pic_profile);
        ImageView postIv = (ImageView) convertView.findViewById(R.id.pic_wearing);



        // set profile picture
        new DownloadImageTask((ImageView) convertView.findViewById(R.id.pic_profile)).execute(item.getPic());
        // set weather picture
        new DownloadImageTask((ImageView) convertView.findViewById(R.id.pic_wearing)).execute(item.getWeather_pic());


        return convertView;
    }

    // load image by url
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url_display = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(url_display).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void swipeItem(ArrayList<FeedItem> items) {
        feedItems.clear();
        feedItems.addAll(items);
        notifyDataSetChanged();

    }
}

