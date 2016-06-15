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
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * This class is used to provide weather data.
 */
public class WeatherAPI {

    private Activity activity;
    private Context context;

    String[] cityArray;
    private String city_en;
    private String city_zh;
    private Double lat, lng;
    private Location loc;

    private final String basrUrl = "http://140.116.245.241:9999/WeatherAPI.php";

    public WeatherAPI(Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();

        //set default
        cityArray = activity.getResources().getStringArray(R.array.locations_en);
        city_en = "Tainan";
        city_zh = "台南市";
        lat = 22.9999900;
        lng = 120.226876;

        loc = null;
    }

    private String byCity(String filed, String city, String date, String time) {
        return basrUrl + "?field=" + filed + "&city=" + city + "&date=" + date + "&time=" + time;
    }

    private String byGPS(String filed, String date, String time) {
        city_en = getCityEnbyGPS();
        city_zh = getCityZhbyGPS(city_en);
        return basrUrl + "?field=" + filed + "&city=" + city_en + "&date=" + date + "&time=" + time;
    }

    private String byNow(String field) {
        Calendar mCal = Calendar.getInstance();
        return byGPS(field, new SimpleDateFormat("yyyy/MM/dd").format(mCal.getTime()), new SimpleDateFormat("HH").format(mCal.getTime()));
    }

    public String getCityZhbyGPS_google() {
        getGPS();
        Geocoder gc = new Geocoder(activity, Locale.TRADITIONAL_CHINESE);
        List<Address> lstAddress = null;
        try {
            Log.d("feedback", lat + " " + lng);
            lstAddress = gc.getFromLocation(lat, lng, 1);
            city_zh = lstAddress.get(0).getAdminArea();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return city_zh;
    }

    public String getCityZhbyGPS() {
        return activity.getResources().getStringArray(R.array.locations)[Arrays.asList(cityArray).indexOf(getCityEnbyGPS())];
    }

    public String getCityEnbyGPS() {
        return activity.getResources().getStringArray(R.array.locations_en)[Arrays.asList(activity.getResources().getStringArray(R.array.locations_google)).indexOf(getCityZhbyGPS_google())];
    }

    public String getCityZhbyGPS(String enCity) {
        return activity.getResources().getStringArray(R.array.locations)[Arrays.asList(cityArray).indexOf(enCity)];
    }

    public double getRainbyGPSNow() {
        String url = byNow("rain");
        String ret = getData(url);
        return Double.valueOf(ret);
    }

    public int getUVbyGPSNow() {
        String url = byNow("uv");
        String ret = getData(url);
        return rangeUV(Integer.valueOf(ret));
    }

    public int getPM25byGPSNow() {
        String url = byNow("PM25");
        String ret = getData(url);
        return rangePM25(Integer.valueOf(ret));
    }

    public int getCCIbyGPSNow() {
        String url = byNow("humidity");
        String ret = getData(url);
        return rangeCCI(Double.valueOf(ret), getTempbyGPSNow());
    }

    public double getTempbyGPSNow() {
        String url = byNow("temp");
        String ret = getData(url);
        return Double.valueOf(ret);
    }

    public int getStatusbyGPSNow() {
        String url = byNow("shortinfo");
        String ret = getData(url);
        return rangeStatus(ret);
    }


    public double getRainbyGPS(String date, String time) {
        String url = byGPS("rain", date, time);
        String ret = getData(url);
        return Double.valueOf(ret);
    }

    public int getUVbyGPS(String date, String time) {
        String url = byGPS("uv", date, time);
        String ret = getData(url);
        return rangeUV(Integer.valueOf(ret));
    }

    public int getPM25byGPS(String date, String time) {
        String url = byGPS("PM25", date, time);
        String ret = getData(url);
        return rangePM25(Integer.valueOf(ret));
    }

    public int getCCIbyGPS(String date, String time) {
        String url = byGPS("humidity", date, time);
        String ret = getData(url);
        return rangeCCI(Double.valueOf(ret), getTempbyGPS(date, time));
    }

    public double getTempbyGPS(String date, String time) {
        String url = byGPS("temp", date, time);
        String ret = getData(url);
        return Double.valueOf(ret);
    }

    public int getStatusbyGPS(String date, String time) {
        String url = byGPS("shortinfo", date, time);
        String ret = getData(url);
        return rangeStatus(ret);
    }


    public double getRainbyCity(String city, String date, String time) {
        String url = byCity("rain", city, date, time);
        String ret = getData(url);
        return Double.valueOf(ret);
    }

    public int getUVbyCity(String city, String date, String time) {
        String url = byCity("uv", city, date, time);
        String ret = getData(url);
        return rangeUV(Integer.valueOf(ret));
    }

    public int getPM25byCity(String city, String date, String time) {
        String url = byCity("PM25", city, date, time);
        String ret = getData(url);
        return rangePM25(Integer.valueOf(ret));
    }

    public int getCCIbyCity(String city, String date, String time) {
        String url = byCity("humidity", city, date, time);
        String ret = getData(url);
        return rangeCCI(Double.valueOf(ret), getTempbyGPS(date, time));
    }

    public double getTempbyCity(String city, String date, String time) {
        String url = byCity("temp", city, date, time);
        String ret = getData(url);
        return Double.valueOf(ret);
    }

    public int getStatusbyCity(String city, String date, String time) {
        String url = byCity("shortinfo", city, date, time);
        String ret = getData(url);
        return rangeStatus(ret);
    }

    private int rangeUV(int uv) {
        if (uv <= 2)
            return 1;
        else if (uv > 2 && uv <= 5)
            return 2;
        else if (uv > 5 && uv <= 7)
            return 3;
        else if (uv > 7 && uv <= 10)
            return 4;
        else
            return 5;
    }

    private int rangePM25(int pm25) {
        if (pm25 <= 35)
            return 1;
        else if (pm25 > 35 && pm25 <= 53)
            return 2;
        else if (pm25 > 53 && pm25 <= 70)
            return 3;
        else
            return 4;
    }

    private int rangeCCI(double RH, double T) {
        double CCI = T - 0.55 * (1 - RH / 100.0) * (T - 14);
        int ret = (int) (CCI / 5.0);
        ret = ret < 1 ? 1 : ret;
        ret = ret > 6 ? 6 : ret;
        return ret;
    }

    private int rangeStatus(String ret){
        int status = 1;
        // 1:sun, 2:cloud, 3:rain
        if(ret.equals("Clear")){
            return 1;
        }else if(ret.equals("Clouds")){
            return 2;
        }else if(ret.equals("Rain")){
            return 3;
        }else{
            return 1;
        }
    }

    private String getData(String url) {
        DownloadDataTask t = new DownloadDataTask(url);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return t.getRet().replace("\n", "");

    }

    private class DownloadDataTask extends Thread {

        String urlStr;
        String ret;

        DownloadDataTask(String urlStr) {
            this.urlStr = urlStr;
        }

        @Override
        public void run() {
            URL url = null;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                ret = readStream(urlConnection.getInputStream());


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        public String getRet() {
            return ret;
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


    /**
     * GPS
     **/
    private final int MY_PERMISSIONS_REQUEST_GPS = 0;
    // Acquire a reference to the system Location Manager
    private LocationManager locationManager;
    // Define a listener that responds to location updates
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
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
        } else {
            saveGPStoVar();
        }
    }

    private void saveGPStoVar() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (loc != null) {
            lat = loc.getLatitude();
            lng = loc.getLongitude();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GPS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveGPStoVar();
                } else {
                    lat = 22.9999900;
                    lng = 120.226876;
                }
                return;
            }
        }
    }

}
