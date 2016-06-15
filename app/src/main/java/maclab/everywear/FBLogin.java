package maclab.everywear;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class FBLogin extends AppCompatActivity {

    private CallbackManager callbackManager;
    private String serverUrl = "http://140.116.245.241:9999/UserPost.php";
    private FBData fbData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);

        fbData = (FBData) getIntent().getSerializableExtra("data");
        onFblogin();
    }

    public void onFblogin() {
        callbackManager = CallbackManager.Factory.create();

        // Set permissions
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_photos", "public_profile"));

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        Log.d("FB", "Success");

                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.v("LoginActivity", response.toString());

                                        // Application code
                                        try {

                                            fbData.setIsLogin(true);
                                            fbData.setUserId(object.getString("id"));
                                            fbData.setUserName(object.getString("first_name"));
                                            fbData.setUserPic(object.getJSONObject("picture").getJSONObject("data").getString("url"));

                                            Log.d("FB", fbData.getUserId());
                                            Log.d("FB", fbData.getUserName());
                                            Log.d("FB", fbData.getUserPic());
                                            sendRequest("action", "addUser", "id", fbData.getUserId(), "name", fbData.getUserName(), "pic", fbData.getUserPic());
                                            sendDataBack();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,first_name,picture");
                        request.setParameters(parameters);
                        request.executeAsync();

                    }

                    @Override
                    public void onCancel() {
                        Log.d("FB", "On cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("FB", error.toString());
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void sendRequest(String... args) {
        class AddUserRunnable implements Runnable {

            String[] args;

            AddUserRunnable(String[] args) {
                this.args = args;
            }

            @Override
            public void run() {
                for (int i = 0; i < args.length; i += 2) {
                    args[i+1]=args[i+1].replace("&","%26");
                    serverUrl += i == 0 ? "?" : "&";
                    serverUrl += args[i] + "=" + args[i + 1];
                }
                Log.d("FB", serverUrl);
                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(serverUrl);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    Log.d("FB", readStream(urlConnection.getInputStream()));


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }
        Thread t = new Thread(new AddUserRunnable(args));
        t.start();

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



    private void sendDataBack(){
        Intent intent = new Intent();
        intent.putExtra("data",fbData);
        setResult(RESULT_OK, intent);
        finish();
    }
}
