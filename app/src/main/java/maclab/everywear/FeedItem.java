package maclab.everywear;


public class FeedItem {
    private int no;
    private String name, pic, weather_pic;
    private String imageUrl = "http://140.116.245.241:9999/PostImages/";
    public FeedItem(int no, String name, String pic, String weather_pic) {
        super();
        this.no = no;
        this.name = name;
        this.pic = pic;
        this.weather_pic = weather_pic;
    }


    public int getNo() {
        return no;
    }

    public String getName() {
        return name;
    }

    public String getPic() {
        return pic;
    }

    public String getWeather_pic() {
        return imageUrl + weather_pic;
    }

}