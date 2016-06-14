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
import android.view.View;
import android.widget.Button;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private Button btn_feed;
    private Button btn_camera;
    private Button btn_login;

    private CallbackManager callbackManager;

    private String serverUrl = "http://140.116.245.241:9999/User.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initial facebook sdk. put before the super.onCreate().
        FacebookSdk.sdkInitialize(getApplicationContext());

        Log.d("FB", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_feed = (Button) findViewById(R.id.btn_feed);
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


        btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent();
                cameraIntent.setClass(MainActivity.this, Camera.class);
                startActivity(cameraIntent);
            }
        });

        // FB login
        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onFblogin();
            }
        });

    }

    // Private method to handle Facebook login and callback
    private void onFblogin() {
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
                                            Log.d("FB", object.getString("id"));
                                            Log.d("FB", object.getString("first_name"));
                                            Log.d("FB", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                            sendRequest("action", "addUser", "id", object.getString("id"), "name", object.getString("first_name"), "pic", object.getJSONObject("picture").getJSONObject("data").getString("url"));

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
                        checkNetworkStatus();
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

    private void checkNetworkStatus() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();

        //如果未連線的話，mNetworkInfo會等於null
        if (mNetworkInfo != null) {
            //網路是否可使用
            if (!mNetworkInfo.isAvailable()) {
                dialog("網路無法連線");
            }

        } else {
            dialog("請連接網路");
        }
    }

    protected void dialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override


            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }


        });

        builder.create().show();
    }

}
