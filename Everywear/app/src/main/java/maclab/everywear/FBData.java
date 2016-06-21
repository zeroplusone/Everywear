package maclab.everywear;


import java.io.Serializable;

public class FBData implements Serializable {

    private String userName;
    private String userId;
    private String userPic;

    public String test;
    boolean isLogin;


    public FBData(){
        userId = "";
        userName = "";
        userPic = "";
        isLogin =false;
        test="noo";
    }

    public void setIsLogin(boolean isLogin){
        this.isLogin=isLogin;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }
    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setUserPic(String userPic){
        this.userPic = userPic;
    }

    public boolean getIsLogin(){
        return isLogin;
    }
    public String getUserId(){
        return userId;
    }
    public String getUserName(){
        return userName;
    }
    public String getUserPic(){
        return userPic;
    }


}
