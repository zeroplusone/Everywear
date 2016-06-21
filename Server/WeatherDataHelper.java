package maclab.everywear;
import org.json.*;
import org.xmlpull.v1.*;
import java.io.*;
import java.sql.*;
import java.text.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
// import java.sql.Connection;
// import java.sql.DriverManager;
public class WeatherDataHelper {
    // The JDBC Connector Class.
    private static final String dbClassName = "org.mariadb.jdbc.Driver";
    // private static final String dbClassName = "com.mysql.jdbc.Driver";

    // Database
    private final String CON_STR="jdbc:mariadb://" + DBInfo.HOST +":"+ DBInfo.PORT +"/"+ DBInfo.DATABASE;
    private Connection dbCon;
    private Statement stmt = null;
    private ResultSet rs = null;


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


    private HashMap<String, String> cityPM25 = new HashMap<String, String>();
    private HashMap<String, String> enToZhCityMap= new HashMap<String, String>();
    private HashMap<String, String> zhToEnCityMap= new HashMap<String, String>();
    private ArrayList<String> cwbOnly = new ArrayList<String>();

    WeatherDataHelper() {
        // setup zh <--> en city names
        /*
        zhToEnCityMap.put("基隆市", "Keelung");
        zhToEnCityMap.put("臺北市", "Taipei");
        zhToEnCityMap.put("新北市", "Taipei");
        zhToEnCityMap.put("桃園市", "Taoyuan");
        zhToEnCityMap.put("新竹市", "Hsinchu");
        zhToEnCityMap.put("新竹縣", "Hsinchu");
        zhToEnCityMap.put("苗栗縣", "Miaoli");
        zhToEnCityMap.put("臺中市", "Taichung");
        zhToEnCityMap.put("彰化縣", "Changhua");
        zhToEnCityMap.put("南投縣", "Nantou");
        zhToEnCityMap.put("雲林縣", "Yunlin");
        zhToEnCityMap.put("嘉義市", "Jiayi Shi");
        zhToEnCityMap.put("嘉義縣", "Jiayi Shi");
        zhToEnCityMap.put("臺南市", "Tainan");
        zhToEnCityMap.put("高雄市", "Kaohsiung");
        zhToEnCityMap.put("屏東縣", "Pingtung");
        zhToEnCityMap.put("臺東縣", "Taitung");
        zhToEnCityMap.put("花蓮縣", "Hualien");
        zhToEnCityMap.put("宜蘭縣", "Yilan");
        zhToEnCityMap.put("澎湖縣", "Penghu");
        */
        // alphabetic order
        zhToEnCityMap.put("彰化縣", "Changhua");
        zhToEnCityMap.put("新竹市", "Hsinchu");
        zhToEnCityMap.put("新竹縣", "Hsinchu");
        zhToEnCityMap.put("花蓮縣", "Hualien");
        zhToEnCityMap.put("嘉義市", "Jiayi Shi");
        zhToEnCityMap.put("嘉義縣", "Jiayi County"); //cwb only
        zhToEnCityMap.put("高雄市", "Kaohsiung");
        zhToEnCityMap.put("基隆市", "Keelung");
        zhToEnCityMap.put("金門縣", "Kinmen");
        zhToEnCityMap.put("連江縣", "Lienchiang"); //cwb only
        zhToEnCityMap.put("苗栗縣", "Miaoli"); //cwb only
        zhToEnCityMap.put("南投縣", "Nantou");
        zhToEnCityMap.put("新北市", "New Taipei"); //cwb only
        zhToEnCityMap.put("澎湖縣", "Penghu");
        zhToEnCityMap.put("屏東縣", "Pingtung");
        zhToEnCityMap.put("臺中市", "Taichung");
        zhToEnCityMap.put("臺北市", "Taipei");
        zhToEnCityMap.put("臺南市", "Tainan");
        zhToEnCityMap.put("臺東縣", "Taitung");
        zhToEnCityMap.put("桃園市", "Taoyuan");
        zhToEnCityMap.put("宜蘭縣", "Yilan");
        zhToEnCityMap.put("雲林縣", "Yunlin");

        enToZhCityMap.put("Keelung", "基隆市");
        enToZhCityMap.put("Taipei", "臺北市");
        enToZhCityMap.put("New Taipei", "新北市"); //cwb only
        enToZhCityMap.put("Taoyuan", "桃園市");
        enToZhCityMap.put("Hsinchu", "新竹市");
        enToZhCityMap.put("Hsinchu", "新竹縣");
        enToZhCityMap.put("Miaoli", "苗栗縣");
        enToZhCityMap.put("Taichung", "臺中市");
        enToZhCityMap.put("Changhua", "彰化縣");
        enToZhCityMap.put("Nantou", "南投縣");
        enToZhCityMap.put("Yunlin", "雲林縣");
        enToZhCityMap.put("Jiayi Shi", "嘉義市");
        enToZhCityMap.put("Jiayi County", "嘉義縣"); //cwb only
        enToZhCityMap.put("Tainan", "臺南市");
        enToZhCityMap.put("Kaohsiung", "高雄市");
        enToZhCityMap.put("Pingtung", "屏東縣");
        enToZhCityMap.put("Taitung", "臺東縣");
        enToZhCityMap.put("Hualien", "花蓮縣");
        enToZhCityMap.put("Yilan", "宜蘭縣");
        enToZhCityMap.put("Penghu", "澎湖縣");
        enToZhCityMap.put("Kinmen", "金門縣"); //cwb only
        enToZhCityMap.put("Lienchiang", "連江縣"); //cwb only

        cwbOnly.add("新北市");
        cwbOnly.add("New Taipei");
        cwbOnly.add("嘉義縣");
        cwbOnly.add("Jiayi County");
        cwbOnly.add("金門縣");
        cwbOnly.add("Kinmen");
        cwbOnly.add("連江縣");
        cwbOnly.add("Lienchiang");

        // connect to database, and crawl weather data
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            // Class.forName(xxx) loads the jdbc classes and
            // creates a drivermanager class factory
            // Class.forName(dbClassName);

            System.out.println(CON_STR);
            dbCon = DriverManager.getConnection(CON_STR, DBInfo.USER, DBInfo.PASSWORD);
            System.out.println("connect to database!");

            stmt = dbCon.createStatement();
            createTableIfNotExist();
            initWeather();

        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(e);
        }
        finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore
                rs = null;
            }
            /*
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore
                stmt = null;
            }
            if (dbCon != null) {
                try {
                    dbCon.close();
                    System.out.println("Close database connection.");
                } catch (SQLException e) {} // ignore
            }
            */
        }
    }
    // rs = stmt.executeQuery(query);
    // stmt.executeUpdate("insert tb1 (col1, col2) value (55, 'kelly'), (8, 'tony')");
    /*
       while(rs.next()) {
       int age = rs.getInt("col1");
       String name = rs.getString("col2");
       System.out.format("age:%d name:%s\n", age, name);
       }
       */


    private void createTableIfNotExist() throws SQLException {
        System.out.println("Create table if not exists: " + DBInfo.TABLENAME);
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + DBInfo.TABLENAME + "("
            + "no int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
            + "city_zh VARCHAR(40),"
            + "city_en VARCHAR(32),"
            + "querytime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "foretime DATETIME,"
            + "shortinfo VARCHAR(64),"
            + "longinfo VARCHAR(128),"
            + "temp FLOAT,"
            + "rain INT,"
            + "humidity INT,"
            + "clouds INT,"
            + "wind INT,"
            + "UV INT,"
            + "PM25 INT,"
            + "air INT,"
            + "UNIQUE KEY `id` (`city_en`, `foretime`)"
            + ") CHARACTER SET = UTF8";
        stmt.execute(sqlCreate);
    }

    private enum WeatherType {
        GENERAL, RAIN, UV, PM25
    }

    private void initWeather() {
        // get rain rate of Taiwan
        getWeather(cwbUrl, WeatherType.RAIN, null);

        // get UV of Taiwan
        getWeather(UV_URL, WeatherType.UV, null);

        // get PM2.5 of Taiwan
        getWeather(PM25_URL, WeatherType.PM25, null);

        // get selected city sent from mainActivity
        String city_en = null;
        String city_zh = null;

        // debug
        System.out.println("getting info of cities...");
        for (String key : zhToEnCityMap.keySet()) {
            if (cwbOnly.contains(key)) continue;
            city_zh = key;
            city_en = zhToEnCityMap.get(key);
            owmUrl = OWM_URL_BASE + city_en.replace(" ", "%20") + OWM_APIKEY;
            // System.out.println(owmUrl); // debug
            // System.out.println(city_zh + " " + city_en); // debug
            getWeather(owmUrl, WeatherType.GENERAL, city_zh);
        }
    }

    private void getWeather(String url, WeatherType wType, final String city) {
        // get weather runnable task
        class GetWeatherRunnable implements Runnable {
            String url;
            WeatherType wType;
            GetWeatherRunnable(String str, WeatherType wType) {
                url = str;
                // debug
                // System.out.println("wType: " + wType);
                this.wType = wType;
            }
            public void run() {
                // debug
                // System.out.println("Request weather data and write to file");
                try {
                    URL url = new URL(this.url);
                    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                    httpCon.setRequestMethod("GET");
                    httpCon.connect();
                    //InputStream stream = httpCon.getInputStream();
                    String webContent = null;
                    webContent = readStream(httpCon.getInputStream());
                    switch(this.wType) {
                        case GENERAL:
                            saveWeatherData(webContent, city);
                            break;
                        case RAIN:
                            System.out.println("Get rain info."); // debug
                            // writeToFile(webContent, new FileOutputStream("rain.tmp"));
                            saveRainData(webContent);
                            break;
                        case UV:
                            System.out.println("Get UV info."); // debug
                            // writeToFile(webContent, new FileOutputStream("uv.tmp"));
                            saveUVData(webContent);
                            break;
                        case PM25:
                            System.out.println("Get PM2.5 info."); // debug
                            writeToFile(webContent, new FileOutputStream("pm25.tmp"));
                            savePM25Data(webContent);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println(e.toString() + ", web failure");
                }
            }
        }
        new Thread(new GetWeatherRunnable(url, wType)).start();
    }

    private void saveWeatherData(String webContent, String city) {
        try {
            JSONObject weatherJson = new JSONObject(webContent);
            //iterateOverJSON(weatherJson);
            JSONArray jsonList = weatherJson.getJSONArray("list");
            for (int i=0; i<jsonList.length(); i++) {
                JSONObject firstItem = jsonList.getJSONObject(i);

                String city_zh = "\"" + city + "\"";
                String city_en = "\"" + zhToEnCityMap.get(city) + "\"";
                String foretime = "\"" + firstItem.getString("dt_txt") + "\"";
                String shortinfo = "\"" + firstItem.getJSONArray("weather").getJSONObject(0).getString("main") + "\"";
                String longinfo = "\"" + firstItem.getJSONArray("weather").getJSONObject(0).getString("description") + "\"";
                double temp = firstItem.getJSONObject("main").getDouble("temp")-273.15;
                // str = new DecimalFormat("#.##").format(temp) + "°C";
                double humidity = firstItem.getJSONObject("main").getDouble("humidity");
                double clouds = firstItem.getJSONObject("clouds").getDouble("all");
                double wind = firstItem.getJSONObject("wind").getDouble("speed");
                double pressure = firstItem.getJSONObject("main").getDouble("pressure");

                String sqlInsert = "INSERT " + DBInfo.TABLENAME
                    + "(city_zh, city_en, foretime, shortinfo, longinfo, temp,"
                    + " humidity, clouds, wind, air) value "
                    + "(" + city_zh + ", "
                    + city_en + ", "
                    + foretime + ", "
                    + shortinfo + ", "
                    + longinfo + ", "
                    + temp + ", "
                    + humidity + ", "
                    + clouds + ", "
                    + wind + ", "
                    + pressure + ") "
                    + "ON DUPLICATE KEY UPDATE "
                    + "shortinfo=" + shortinfo + ", "
                    + "longinfo=" + longinfo + ", "
                    + "temp=" + temp + ", "
                    + "humidity=" + humidity + ", "
                    + "clouds=" + clouds + ", "
                    + "wind=" + wind + ", "
                    + "air=" + pressure + ", "
                    + "querytime=CURRENT_TIMESTAMP";
                stmt.executeUpdate(sqlInsert);
                //System.out.println(sqlInsert); //debug
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private void saveRainData(String webContent) {
        try {
            // XmlPullParser parser = Xml.newPullParser();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new ByteArrayInputStream(webContent.getBytes()), "UTF-8");
            // StringBufferInputStream 's chinese character's are garbage,
            // use ByteArrayInputStream instead.
            // parser.setInput(new StringBufferInputStream(webContent), "UTF-8");
            // parser.setInput(new FileInputStream("rain.tmp"), "UTF-8");

            int event;
            String text = null;
            String city = null;
            String rainPoss = null;
            String elementName = null;
            String measureTime = null;
            boolean skip = false;
            int rainDataCnt = 0;
            char[] chars = new char[64];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date;
            Calendar cal = Calendar.getInstance();

            for (event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next()) {
                String name = parser.getName();
                // debug
                // System.out.println("name:" + name + ", event:" + event);

                switch (event) {
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        chars = parser.getTextCharacters(new int[]{0, 1});
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("locationName")) {
                            city = text;
                        } else if (name.equals("elementName")) {
                            elementName = text;
                            if (skip) skip = false;
                            if (!elementName.equals("PoP")) skip = true;
                        } else if (name.equals("startTime")) {
                            if (skip) break;
                            measureTime = text.substring(0, text.length()-6).replace("T", " ");
                        } else if (name.equals("parameterName")) {
                            if (skip) break;
                            if (zhToEnCityMap.get(city)==null) break;
                            rainPoss = text;

                            // the measureTime can only be 18:00 or 6:00,
                            // so distributing the raindata
                            // from 6:00 to 6:00, 9:00, 12:00, 15:00.
                            // from 18:00 to 18:00, 21:00, 00:00, 3:00.
                            date = sdf.parse(measureTime);
                            cal.setTime(date);
                            for (int i=0; i<4; i++) {
                                measureTime = sdf.format(cal.getTime());
                                String sqlInsert = "INSERT " + DBInfo.TABLENAME
                                    + " (city_zh, city_en, foretime, rain) value ("
                                    + "\"" + city + "\", "
                                    + "\"" + zhToEnCityMap.get(city) + "\", "
                                    + "\"" + measureTime + "\", "
                                    + rainPoss
                                    + ") "
                                    + "ON DUPLICATE KEY UPDATE "
                                    + "rain=" + rainPoss + ", "
                                    + "querytime=CURRENT_TIMESTAMP";
                                // System.out.println(sqlInsert); // debug
                                stmt.executeUpdate(sqlInsert);

                                cal.add(Calendar.HOUR, 3);
                            }
                            rainDataCnt++;
                            if (rainDataCnt==3) {
                                rainDataCnt=0;
                                skip = true;
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void saveUVData(String webContent) {
        try {
            // XmlPullParser parser = Xml.newPullParser();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new ByteArrayInputStream(webContent.getBytes()), "UTF-8");
            // StringBufferInputStream 's chinese character's are garbage,
            // use ByteArrayInputStream instead.
            // parser.setInput(new StringBufferInputStream(webContent), "UTF-8");
            // parser.setInput(new FileInputStream("rain.tmp"), "UTF-8");

            int event;
            String text = null;
            String city = null;
            String uv = null;
            String publishTime = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date;
            Calendar cal = Calendar.getInstance();

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
                        } else if (name.equals("UVI")) {
                            uv = text;
                        } else if (name.equals("PublishTime")) {
                            if (zhToEnCityMap.get(city)==null) break;
                            publishTime = text;

                            // the publishTime is 22:00,
                            // so distributing the UV data to each 3 hours of that day.
                            date = sdf.parse(publishTime);
                            cal.setTime(date);
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            for (; cal.get(Calendar.DAY_OF_MONTH) == day; cal.add(Calendar.HOUR,1)) {
                                if (cal.get(Calendar.HOUR) % 3 != 0) continue;
                                publishTime = sdf.format(cal.getTime());
                                String sqlInsert = "INSERT " + DBInfo.TABLENAME
                                    + " (city_zh, city_en, foretime, UV) value ("
                                    + "\"" + city + "\", "
                                    + "\"" + zhToEnCityMap.get(city) + "\", "
                                    + "\"" + publishTime + "\", "
                                    + Double.parseDouble(uv)
                                    + ") "
                                    + "ON DUPLICATE KEY UPDATE "
                                    + "UV=" + uv + ", "
                                    + "querytime=CURRENT_TIMESTAMP";
                                // System.out.println(sqlInsert); // debug
                                stmt.executeUpdate(sqlInsert);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void savePM25Data(String webContent) {
        try {
            // XmlPullParser parser = Xml.newPullParser();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new ByteArrayInputStream(webContent.getBytes()), "UTF-8");
            // StringBufferInputStream 's chinese character's are garbage,
            // use ByteArrayInputStream instead.
            // parser.setInput(new StringBufferInputStream(webContent), "UTF-8");
            // parser.setInput(new FileInputStream("rain.tmp"), "UTF-8");

            int event;
            String text = null;
            String city = null;
            String pm25 = null;
            String publishTime = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date;
            Calendar cal = Calendar.getInstance();

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
                            // it could be a empty text with several space characters.
                            pm25 = text.replace(" ","").replace("\n","");
                        } else if (name.equals("PublishTime")) {
                            if (zhToEnCityMap.get(city)==null || pm25.isEmpty()) break;
                            publishTime = text;

                            // the publishTime updates every hour,
                            // so distributing the UV data to each 3 hours of the rest of that day.
                            date = sdf.parse(publishTime);
                            cal.setTime(date);
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            for (; cal.get(Calendar.DAY_OF_MONTH) == day; cal.add(Calendar.HOUR,1)) {
                                if (cal.get(Calendar.HOUR) % 3 != 0) continue;
                                publishTime = sdf.format(cal.getTime());
                                String sqlInsert = "INSERT " + DBInfo.TABLENAME
                                    + " (city_zh, city_en, foretime, PM25) value ("
                                    + "\"" + city + "\", "
                                    + "\"" + zhToEnCityMap.get(city) + "\", "
                                    + "\"" + publishTime + "\", "
                                    + Double.parseDouble(pm25)
                                    + ") "
                                    + "ON DUPLICATE KEY UPDATE "
                                    + "PM25 = " + pm25 + ", "
                                    + "querytime=CURRENT_TIMESTAMP";
                                // System.out.println(sqlInsert); // debug
                                stmt.executeUpdate(sqlInsert);
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /*
       city  len:6
id:1668352
name:Tainan
coord  len:2
lon:120.188759
lat:23.000019
country:TW
population:0
sys  len:1
population:0
cod:200             // internal parameter
message:0.0122      // internal parameter
cnt:35
list  len:35
dt:1461769200     // time of data calculation, unix, UTC
main  len:8
temp:301.43     // unit default: Kelvin
temp_min:298.379
temp_max:301.43
pressure:1024
sea_level:1022.41
grnd_level:1024
humidity:100
temp_kf:3.05
weather  len:1
id:803
main:Clouds
description:broken clouds
icon:04n
clouds  len:1
all:68
wind  len:2
speed:1.21
deg:18.0013
rain  len:0
sys  len:1
pod:n
dt_txt:2016-04-27 15:00:00
*/
    private int recursiveLevel = 0;
    private void iterateOverJSON(JSONObject json) {
        Iterator<?> keys = json.keys();
        String output = "";

        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                Object obj = json.get(key);
                output = "";
                for (int i=0; i<recursiveLevel; i++)
                    output += "  ";
                if (obj instanceof JSONObject)
                    output += key + "  len:" + ((JSONObject) obj).length();
                else if (obj instanceof JSONArray)
                    output += key + "  len:" + ((JSONArray) obj).length();
                else
                    output += key + ":" + obj.toString();

                System.out.println(output);

                if (obj instanceof JSONObject) {
                    recursiveLevel++;
                    iterateOverJSON((JSONObject)obj);
                    recursiveLevel--;
                } else if (obj instanceof JSONArray) {
                    recursiveLevel++;
                    iterateOverJSON( ((JSONArray) obj).getJSONObject(0));
                    recursiveLevel--;
                } else {
                    //System.out.println(output + " " + obj.toString());
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        String webContent = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            StringBuffer sb = new StringBuffer("");
            String NL = System.getProperty("line.separator");

            while ((line = reader.readLine()) != null) {
                sb.append(line + NL);
            }
            webContent = sb.toString();
        } catch (IOException e) {
            System.out.println(e.toString() + ", null web connection");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println(e.toString());
                }
            }
        }
        return webContent;
    }

    private void writeToFile(String content, FileOutputStream fos) {
        try {
            fos.write(content.getBytes());
        } catch (IOException e) {
            System.out.println(e.toString() + ", null web connection");
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                System.out.println("fos.close(), " + e.toString());
            }
        }
    }

    public static void main(String[] args) {
        new WeatherDataHelper();
    }

}
