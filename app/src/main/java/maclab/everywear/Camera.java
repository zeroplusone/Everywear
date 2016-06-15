package maclab.everywear;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Camera extends AppCompatActivity {

    private final int REQUEST_CODE_CAMERA = 1;
    private ImageView mImageView;
    private Button btn_post;
    private Button btn_cancel;

    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private Bitmap sourceBitmap = null;
    private Bitmap weatherBitmap = null;
    private File sourceFile = null;
    private File weatherFile = null;

    private String pictureUrl = "http://140.116.245.241:9999/PictureUpload.php";
    private String databaseUrl = "http://140.116.245.241:9999/UserPost.php";
    private ProgressDialog progress;

    private final int SEND_SOURCE_FILE = 0;
    private final int SEND_WEATHER_FILE =1;

    private FBData fbData;
    private WeatherAPI weatherAPI;

    private String timeStampName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mImageView = (ImageView) findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
            int permsRequestCode = 200;
            requestPermissions(perms, permsRequestCode);
        } else {
            dispatchTakePictureIntent();
        }

        fbData = (FBData) getIntent().getSerializableExtra("data");
        weatherAPI = new WeatherAPI(this);

        btn_post = (Button) findViewById(R.id.btn_post);
        progress = new ProgressDialog(this);
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setTitle("Loading");
                progress.setMessage("Wait while loading...");
                progress.show();

                getTimeStampName();
                sendPicRequest(SEND_SOURCE_FILE);
                sendPicRequest(SEND_WEATHER_FILE);
                sendPostRequest();
            }
        });

        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("data",fbData);
                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case 200:

                dispatchTakePictureIntent();
                break;

            case 400:
                ouputBitmapToFile();
                break;

        }

    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;
        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
    }

    private File setUpPhotoFile() throws IOException {

        sourceFile = createImageFile("SourceFile");
        mCurrentPhotoPath = sourceFile.getAbsolutePath();

        return sourceFile;
    }

    private File createImageFile(String filename) throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + filename;
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

        return imageF;
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {


            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir("CameraSample");

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA)   // camera
            {
//                Bitmap bitmap = (Bitmap) data.getExtras().getParcelable("data");    //取得bitmap縮圖
//                imageView.setImageBitmap(bitmap);
                if (mCurrentPhotoPath != null) {
                    setPic();
                    galleryAddPic();
                    mCurrentPhotoPath = null;
                }
            }

        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
        /* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        sourceBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        Bitmap drawnBitmap = drawWeatherData(sourceBitmap);


		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(drawnBitmap);
        mImageView.setVisibility(View.VISIBLE);

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    abstract class AlbumStorageDirFactory {
        public abstract File getAlbumStorageDir(String albumName);
    }

    public final class BaseAlbumDirFactory extends AlbumStorageDirFactory {

        // Standard storage location for digital camera files
        private static final String CAMERA_DIR = "/dcim/";

        @Override
        public File getAlbumStorageDir(String albumName) {
            return new File(
                    Environment.getExternalStorageDirectory()
                            + CAMERA_DIR
                            + albumName
            );
        }
    }

    public final class FroyoAlbumDirFactory extends AlbumStorageDirFactory {

        @Override
        public File getAlbumStorageDir(String albumName) {
            // TODO Auto-generated method stub
            return new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ),
                    albumName
            );
        }
    }

    private void getTimeStampName(){
        timeStampName = fbData.getUserId()+"_";
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
        timeStampName += df.format(mCal.getTime());
    }

    private void sendPicRequest(int whichFile) {
        class AddPicRunnable implements Runnable {

            File inputFile;
            String attachmentFileName;

            AddPicRunnable(int whichFile) {

                attachmentFileName = timeStampName;
                switch(whichFile){
                    case SEND_SOURCE_FILE:
                        this.inputFile = sourceFile;
                        break;
                    case SEND_WEATHER_FILE:
                        this.inputFile = weatherFile;
                        attachmentFileName+="_revised";
                        break;
                }
                attachmentFileName+=".jpg";

            }

            @Override
            public void run() {

                String attachmentName = "uploaded_file";

                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";

                int maxBufferSize = 1 * 1024 * 1024;
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;

                try {

                    FileInputStream fileInputStream = new FileInputStream(inputFile);
                    // setup request
                    HttpURLConnection httpUrlConnection = null;
                    URL url = new URL(pictureUrl);
                    httpUrlConnection = (HttpURLConnection) url.openConnection();
                    httpUrlConnection.setUseCaches(false);
                    httpUrlConnection.setDoOutput(true);

                    httpUrlConnection.setRequestMethod("POST");
                    httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpUrlConnection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + boundary+";charset=utf-8");

                    // start content wrapper
                    DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                    request.writeBytes(crlf);

                    // convert file to bytebuffer

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        request.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    }

                    //end content wrapper
                    request.writeBytes(crlf);
                    request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

                    // flush output buffer
                    request.flush();
                    request.close();

                    // get responses
                    Log.d("feedback", "HTTP Response is : " + httpUrlConnection.getResponseMessage() + ": " + httpUrlConnection.getResponseCode());
                    Log.d("feedback", readStream(httpUrlConnection.getInputStream()));
                } catch (IOException e) {
                    Log.d("feedback", "Exception : " + e.getMessage(), e);
                }
                turnToFeed();

            }
        }
        Thread t = new Thread(new AddPicRunnable(whichFile));
        t.start();

    }

    private void sendPostRequest() {
        class AddPostRunnable implements Runnable {


            @Override
            public void run() {
                databaseUrl+="?action=addPost";
                databaseUrl+="&id="+fbData.getUserId();
                databaseUrl+="&oPic="+timeStampName+".jpg";
                databaseUrl+="&wPic="+timeStampName+"_revised.jpg";
                databaseUrl+="&city_zh="+String.valueOf(weatherAPI.getCity_zh());
                databaseUrl+="&city_en="+String.valueOf(weatherAPI.getCity_en());

                Log.d("FB", databaseUrl);
                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(databaseUrl);
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
        Thread t = new Thread(new AddPostRunnable());
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



    private void turnToFeed(){
        progress.dismiss();
        Intent intent = new Intent();
        intent.putExtra("data",fbData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private Bitmap drawWeatherData(Bitmap bitmap){
        // initial
        weatherBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(weatherBitmap);
        Paint paint = new Paint();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //draw original bitmap
        canvas.drawBitmap(bitmap, 0, 0, paint);

        //TODO: add new version weather API

        //draw rectangle
        paint.setColor(Color.WHITE);
        paint.setAlpha(150);
        canvas.drawRect((float) (width * 0.1), (float) (height * 0.8), (float) (width * 0.1 + width * 0.8), (float) (height * 0.8 + height * 0.15), paint);

        //draw status
        Bitmap statusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.sun);
        statusIcon = Bitmap.createScaledBitmap(statusIcon, (int) (height * 0.13), (int) (height * 0.13), false);
        canvas.drawBitmap(statusIcon, (float) (width * 0.11), (float) (height * 0.81), paint);

        //draw text
        Calendar mCal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_hhmmss");
        String temp=String.valueOf(weatherAPI.getTempbyGPS(new SimpleDateFormat("yyyy/MM/dd").format(mCal.getTime()), new SimpleDateFormat("HH").format(mCal.getTime())));
        paint.setTextSize(200);
        paint.setAlpha(0);
        paint.setColor(Color.BLACK);
        canvas.drawText(temp.substring(0,temp.indexOf("."))+" C", (float) (width * 0.7), (float) (height * 0.9), paint);

        //draw chinese city name
        paint.setTextSize(150);
        paint.setAlpha(0);
        paint.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(weatherAPI.getCity_zh()), (float) (width * 0.15 + height * 0.13 ), (float) (height *0.86), paint);

        //draw english city name
        paint.setTextSize(150);
        paint.setAlpha(0);
        paint.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(weatherAPI.getCity_en()), (float) (width * 0.15 + height * 0.13), (float) (height * 0.93), paint);



        getOutputPermission();
        return weatherBitmap;
    }

    private void getOutputPermission(){
        String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        int permsRequestCode = 400;
        requestPermissions(perms, permsRequestCode);
    }

    private void ouputBitmapToFile(){
        try {
            FileOutputStream out;
            sourceFile = new File(Environment.getExternalStorageDirectory().toString(), "sourceTmp.jpg");
            out = new FileOutputStream(sourceFile);
            sourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, (int)(sourceBitmap.getWidth()*0.5), (int)(sourceBitmap.getHeight()*0.5), false);
            sourceBitmap.compress(Bitmap.CompressFormat.JPEG,50, out);




            weatherFile = new File(Environment.getExternalStorageDirectory().toString(), "weatherTmp.jpg");
            out = new FileOutputStream(weatherFile);
            weatherBitmap = Bitmap.createScaledBitmap(weatherBitmap, (int)(weatherBitmap.getWidth()*0.5), (int)(weatherBitmap.getHeight()*0.5), false);
            weatherBitmap.compress(Bitmap.CompressFormat.JPEG,50, out);


            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
