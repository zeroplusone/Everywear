package maclab.everywear;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * This class is used to provide weather data.
 * The data are parsed from wunderground.com
 */
public class WeatherAPI {


    private File files;
    private String filename;

    private Activity activity;
    private Context context;

    // Open Weather Map
    // http://api.openweathermap.org/data/2.5/forecast/city?id=1668352&APPID=297a338a4dba127fedea6f2dff27fdfe
    // http://api.openweathermap.org/data/2.5/forecast/weather?q=Tainan&APPID=297a338a4dba127fedea6f2dff27fdfe
    // list of city id: http://bulk.openweathermap.org/sample/
    private final String OWM_URL_BASE = "http://api.openweathermap.org/data/2.5/forecast/weather?q=";
    private final String OWM_APIKEY = "&APPID=297a338a4dba127fedea6f2dff27fdfe";
    private String owmUrl = OWM_URL_BASE + "tainan" + OWM_APIKEY;
    private final int TAINAN_ID = 1668352;

    // Central Weather Bureau
    // http://opendata.cwb.gov.tw
    // http://opendata.cwb.gov.tw/opendataapi?dataid={dataid}&authorizationkey={apikey}
    private final String CWB_URL_BASE = "http://opendata.cwb.gov.tw/opendataapi?dataid=";
    // private final String DATAID_UV = "O-A0005-001";
    private final String DATAID_RAIN_RATE = "F-C0032-001";
    private final String CWB_APIKEY = "&authorizationkey=CWB-71F4FF9A-85E1-4109-8154-2C0072981962";
    private String cwbUrl = CWB_URL_BASE + DATAID_RAIN_RATE + CWB_APIKEY;

    //private final String UV_URL = "http://opendata.epa.gov.tw/ws/Data/UV/?%24orderby=PublishAgency&%24skip=0&%24top=1000&format=xml";
    // run-time UV
    private final String UV_URL = "http://opendata.epa.gov.tw/ws/Data/UV/?format=xml";
    // forecast UV
    // private final String UV_URL = "http://opendata.epa.gov.tw/ws/Data/UVIF/?format=xml";

    private final String PM25_URL = "http://opendata2.epa.gov.tw/AQX.xml";

    // weather reference
    // http://www.cwb.gov.tw/V7/observe
    // http://www.survivingwithandroid.com/2014/05/how-to-develop-android-weather-app.html
    // https://weather.com/weather/today/l/23.00,120.22

    private final String TAG="feedback";

    private HashMap<String, String> cityRainRate = new HashMap<String, String>();
    private HashMap<String, String> cityUV = new HashMap<String, String>();
    private HashMap<String, String> cityPM25 = new HashMap<String, String>();

    private double rain;
    private double uv;
    private double pm25;
    private double T;
    private double RH;

    String[] cityArray;
    private String city_en;
    private String city_zh;
    private String[] dateSplit;
    private String timeStr;
    private Double lat, lng;


    private boolean getByGPS = false;

    public WeatherAPI(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        cityArray = activity.getResources().getStringArray(R.array.locations_en);
    }

    public Location getLocation(){
        getGPS();
        return loc;
    }

    private void getFilter(String city, String date, String time, String itemName){
        city_en = city;
        city_zh = activity.getResources().getStringArray(R.array.locations)[Arrays.asList(cityArray).indexOf(city_en)];
        dateSplit = date.split("/");
        timeStr = time;
        filename = itemName;
        files = new File(context.getFilesDir(), filename);
        Log.v(TAG, "get filter : " + city_zh);
    }

    public String getCity_en(){
        return city_en;
    }
    public String getCity_zh(){

        return city_zh;
    }
    private void getGPSFilter(String date, String time, String itemName){
        getGPS();
        Geocoder gc = new Geocoder(activity, Locale.TRADITIONAL_CHINESE);
        List<Address> lstAddress = null;
        try {
            lstAddress = gc.getFromLocation(lat, lng, 1);
            city_zh = lstAddress.get(0).getAdminArea();
            Log.v(TAG, "Geocoder : "+city_zh);
        } catch (IOException e) {
            e.printStackTrace();
        }

        city_en = activity.getResources().getStringArray(R.array.locations_en)[Arrays.asList(activity.getResources().getStringArray(R.array.locations_google)).indexOf(city_zh)];
        city_zh = activity.getResources().getStringArray(R.array.locations)[Arrays.asList(cityArray).indexOf(city_en)];

        dateSplit = date.split("/");
        timeStr = time;
        filename = itemName.equals("gps")? city_en+".tmp" : itemName;
        files = new File(context.getFilesDir(), filename);

    }



    public double getRainbyCity(String city, String date, String time) {
        getFilter(city, date, time, "rain.tmp");
        getWeather(cwbUrl, files);
        cityRainRate = getRainRate(files);
        rain = Double.valueOf(cityRainRate.get(city_zh));
        return rain;
    }

    public int getUVbyCity(String city, String date, String time) {
        getFilter(city, date, time, "uv.tmp");
        getWeather(UV_URL, files);
        cityUV = getUV(files);
        uv =  Double.valueOf(cityUV.get(city_zh));
        if(uv <= 2)
            return 1;
        else if(uv >2 && uv <= 5)
            return 2;
        else if(uv >5 && uv <= 7)
            return 3;
        else if(uv >7 && uv <=10)
            return 4;
        else
            return 5;
    }

    public int getPM25byCity(String city, String date, String time) {
        getFilter(city, date, time, "pm25.tmp");
        getWeather(PM25_URL, files);
        cityPM25 = getPM25(files);
        pm25 = Double.valueOf(cityPM25.get(city_zh));
        if(pm25 <= 35)
            return 1;
        else if(pm25 >35 && pm25 <= 53)
            return 2;
        else if(pm25 >53 && pm25 <= 70)
            return 3;
        else if(pm25 >70)
            return 4;

        return 0;
    }

    public double getTempbyCity(String city, String date, String time) {
        getFilter(city, date, time, city + ".tmp");
        owmUrl = OWM_URL_BASE + city + OWM_APIKEY;
        getWeather(owmUrl, files);
        getCCI(files);
        return T;
    }

    public int getCCIbyCity(String city, String date, String time) {
        getFilter(city, date, time, city + ".tmp");
        owmUrl = OWM_URL_BASE + city + OWM_APIKEY;
        getWeather(owmUrl, files);
        getCCI(files);
        double CCI = T - 0.55 * (1 - RH / 100.0) * (T - 14);
        int ret = (int) (CCI / 5.0);
        ret = ret < 1 ? 1 : ret;
        ret = ret > 6 ? 6 : ret;
        return ret;
    }


    public double getRainbyGPS(String date, String time) {
        getGPSFilter(date, time, "rain.tmp");
        getWeather(cwbUrl, files);
        cityRainRate = getRainRate(files);
        rain = Double.valueOf(cityRainRate.get(city_zh));
        return rain;
    }

    public int getUVbyGPS(String date, String time) {
        getGPSFilter(date, time, "uv.tmp");
        getWeather(UV_URL, files);
        cityUV = getUV(files);
        uv = Double.valueOf(cityUV.get(city_zh));
        if(uv <= 2)
            return 1;
        else if(uv >2 && uv <= 5)
            return 2;
        else if(uv >5 && uv <= 7)
            return 3;
        else if(uv >7 && uv <=10)
            return 4;
        else
            return 5;
    }

    public int getPM25byGPS(String date, String time) {
        getGPSFilter(date, time, "pm25.tmp");
        getWeather(PM25_URL, files);
        cityPM25 = getPM25(files);
        pm25 = Double.valueOf(cityPM25.get(city_zh));
        if(pm25 <= 35)
            return 1;
        else if(pm25 >35 && pm25 <= 53)
            return 2;
        else if(pm25 >53 && pm25 <= 70)
            return 3;
        else if(pm25 >70)
            return 4;

        return 0;
    }

    public double getTempbyGPS(String date, String time) {
        getGPSFilter(date, time, "gps");
        owmUrl = OWM_URL_BASE + city_en + OWM_APIKEY;
        getWeather(owmUrl, files);
        getCCI(files);
        return T;
    }

    public int getCCIbyGPS(String date, String time) {
        getGPSFilter(date, time, "gps");
        owmUrl = OWM_URL_BASE + city_en + OWM_APIKEY;
        getWeather(owmUrl, files);
        getCCI(files);
        double CCI = T - 0.55 * (1 - RH / 100.0) * (T - 14);
        int ret = (int) (CCI / 5.0);
        ret = ret < 1 ? 1 : ret;
        ret = ret > 6 ? 6 : ret;
        return ret;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    void getWeather(String url, File file) {
        // get weather runnable task
        class GetWeatherRunnable implements Runnable {
            String url;
            FileOutputStream ofstream;
            GetWeatherRunnable(String str, FileOutputStream ofs) {
                url = str;
                ofstream = ofs;
            }
            public void run() {
                Log.d(TAG, "Request weather data and write to file");
                try {
                    URL url = new URL(this.url);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.connect();
                    //InputStream stream = con.getInputStream();
                    readStream(con.getInputStream(), ofstream);
                } catch (Exception e) {
                    Log.w(TAG, e.toString());
                }
            }
        }

        // open temp file for web content
        try {
            if (!isNetworkConnected()) {
                Toast.makeText(context, "No Internet Connection!!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "not connect to a network, return.");
                return;
            }
            FileOutputStream ofstream;
            if(file.exists()) {
                Date lastModDate = new Date(file.lastModified());
                Date now = new Date();
                Log.d(TAG, file.getName()+" exists, lastModified:  " +
                        new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(lastModDate));
                long diff = now.getTime()-lastModDate.getTime();


                // if the file is created 3hr ago, update it
                if(diff > TimeUnit.HOURS.toMillis(3)) {
                    Log.d(TAG, file.getName() + " is too old, " + TimeUnit.MILLISECONDS.toDays(diff) + " day(s) " +
                            TimeUnit.MILLISECONDS.toHours(diff) % TimeUnit.DAYS.toHours(1) + ":" +
                            TimeUnit.MILLISECONDS.toMinutes(diff) % TimeUnit.HOURS.toMinutes(1) + ":" +
                            TimeUnit.MILLISECONDS.toSeconds(diff) % TimeUnit.MINUTES.toSeconds(1) +
                            " ago, create new one");
                    ofstream = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                    // Start a thread to get weather
                    new Thread(new GetWeatherRunnable(url, ofstream)).start();
                }
            } else {
                Log.d(TAG, file.getName() + " not exists, create one");
                ofstream = context.openFileOutput(file.getName(), Context.MODE_PRIVATE);
                // Start a thread to get weather
                new Thread(new GetWeatherRunnable(url, ofstream)).start();
            }
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
    }

    private void readStream(InputStream in, FileOutputStream fos) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            StringBuffer sb = new StringBuffer("");
            String NL = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                sb.append(line + NL);
            }
            String page = sb.toString();
            fos.write(page.getBytes());
        } catch (IOException e) {
            Log.w(TAG, e.toString());
        } finally {
            if (reader != null) {
                try {
                    fos.close();
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, e.toString());
                }
            }
        }
    }


    private HashMap getRainRate(File file) {
        HashMap<String, String> rainMap = new HashMap<String, String>();

        if (!file.exists())
            return null;

        try {
            FileInputStream ifstream = new FileInputStream(file);
            // make sure the file is readable
            while(ifstream.available()==0);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(ifstream, null);
            parser.nextTag();

            int event;
            String text = null;
            String city = null;
            String rainRate = null;
            String elementName = null;
            boolean skip = false;

            for (event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                String name = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("locationName")) {
                            city = text;
                        } else if (name.equals("parameterName")) {
                            if (skip) break;
                            rainRate = text;
                            rainMap.put(city, rainRate);
                            skip = true;
                        }else if (name.equals("elementName")) {
                            elementName = text;
                            if (skip)
                                skip = false;
                            if (!elementName.equals("PoP"))
                                skip = true;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
        return rainMap;
    }

    private HashMap getUV(File file) {
        HashMap<String, String> uvMap  = new HashMap<String, String>();

        if (!file.exists())
            return null;

        try {
            FileInputStream ifstream = new FileInputStream(file);
            // make sure the file is readable
            while(ifstream.available()==0);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(ifstream, null);
            parser.nextTag();

            int event;
            String text = null;
            String city = null;
            String uvi = null;

            for (event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                String name = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("County")) {
                            city = text;
                            if (uvi!=null && uvi.trim().length()==0) break;
                            uvMap.put(city, uvi);
                        } else if (name.equals("UVI")) {
                            uvi = text;
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
        return uvMap;
    }

    private HashMap getPM25(File file) {
        HashMap<String, String> pm25Map = new HashMap<String, String>();

        if (!file.exists())
            return null;

        try {
            FileInputStream ifstream = new FileInputStream(file);
            // make sure the file is readable
            while(ifstream.available()==0);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(ifstream, null);
            parser.nextTag();

            int event;
            String text = null;
            String city = null;
            String pm25str = null;

            for (event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                String name = parser.getName();

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("County")) {
                            city = text;
                        } else if (name.equals("PM2.5")) {
                            pm25str = text;
                            if (pm25str!=null && pm25str.trim().length()==0) break;
                            pm25Map.put(city, pm25str);
                            Log.d(TAG, "city:" + city + "  pm2.5:" + pm25str);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }
        return pm25Map;
    }

    void getCCI(File file) {
        if (!file.exists())
            return;
        try {
            FileInputStream ifstream = new FileInputStream(file);
            // make sure the file is readable
            while(ifstream.available()==0);
            StringBuffer sb = new StringBuffer();
            int ch;
            while ((ch = ifstream.read()) != -1) {
                sb.append((char) ch);
            }
            ifstream.close();
            JSONObject weatherJson = new JSONObject(sb.toString());
            //iterateOverJSON(weatherJson);
            JSONArray jsonList = weatherJson.getJSONArray("list");
            JSONObject firstItem = jsonList.getJSONObject(2);

            T = firstItem.getJSONObject("main").getDouble("temp")-273.15;
            RH = firstItem.getJSONObject("main").getDouble("humidity");

        } catch (Exception e) {
            Log.w(TAG, e.toString());
        }

    }


    /** variables for GPS **/
    private final int MY_PERMISSIONS_REQUEST_GPS = 0;
    private Location loc;
    // Acquire a reference to the system Location Manager
    private LocationManager locationManager;
    // Define a listener that responds to location updates
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            Log.d(TAG,"location update");
            getGPS();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public void getGPS() {
        // default in Tainan
        lat = 22.9999900;
        lng = 120.226876;

        // initialize GPS
        locationManager = (LocationManager) activity.getSystemService(activity.LOCATION_SERVICE);
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_GPS);
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (loc != null) {
            Log.d(TAG, String.valueOf(loc.getLatitude()) + String.valueOf(loc.getLongitude()));
            lat = loc.getLatitude();
            lng = loc.getLongitude();
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
