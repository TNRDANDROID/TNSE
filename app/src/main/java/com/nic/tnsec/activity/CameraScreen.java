package com.nic.tnsec.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.android.volley.VolleyError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nic.tnsec.DataBase.DBHelper;
import com.nic.tnsec.DataBase.dbData;
import com.nic.tnsec.R;
import com.nic.tnsec.Session.PrefManager;
import com.nic.tnsec.api.Api;
import com.nic.tnsec.api.ApiService;
import com.nic.tnsec.api.ServerResponse;
import com.nic.tnsec.constant.AppConstant;
import com.nic.tnsec.databinding.CameraScreenBinding;
import com.nic.tnsec.support.MyLocationListener;
import com.nic.tnsec.utils.CameraUtils;
import com.nic.tnsec.utils.UrlGenerator;
import com.nic.tnsec.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.os.Build.VERSION_CODES.M;

public class CameraScreen extends AppCompatActivity implements View.OnClickListener, Api.ServerResponseListener {

    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 2500;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    final int CROP_PIC = 2;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static String imageStoragePath;
    public static final int BITMAP_SAMPLE_SIZE = 8;
    LocationManager mlocManager = null;
    LocationListener mlocListener;
    Double offlatTextValue, offlongTextValue;
    private PrefManager prefManager;
    private CameraScreenBinding cameraScreenBinding;

    String pp_id,empcode,empcode_type;

    public static DBHelper dbHelper;
    public static SQLiteDatabase db;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraScreenBinding = DataBindingUtil.setContentView(this, R.layout.camera_screen);
        cameraScreenBinding.setActivity(this);
        /*WindowPreferencesManager windowPreferencesManager = new WindowPreferencesManager(this);
        windowPreferencesManager.applyEdgeToEdgePreference(getWindow());*/
        try {
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }


        pp_id=getIntent().getStringExtra("pp_id");
        empcode=getIntent().getStringExtra("empcode");
        empcode_type=getIntent().getStringExtra("empcode_type");
        intializeUI();
    }

    public void intializeUI() {
        prefManager = new PrefManager(this);

        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
       /* cameraScreenBinding.empPhotoView.setTranslationX(800);
        cameraScreenBinding.btnSave.setTranslationX(800);

        cameraScreenBinding.empPhotoView.setAlpha(0);
        cameraScreenBinding.btnSave.setAlpha(0);

        cameraScreenBinding.empPhotoView.animate().translationX(0).alpha(1).setDuration(1200).setStartDelay(700).start();
        cameraScreenBinding.btnSave.animate().translationX(0).alpha(1).setDuration(1400).setStartDelay(900).start();*/

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    public void getPerMissionCapture(){
        if (Build.VERSION.SDK_INT >= M) {
            if (CameraUtils.checkPermissions(CameraScreen.this)) {
                captureImage();

            } else {
                requestCameraPermission(MEDIA_TYPE_IMAGE);
            }
//                            checkPermissionForCamera();
        } else {
            captureImage();

        }

    }

/*
    private void captureImage() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        if (MyLocationListener.latitude > 0) {
            offlatTextValue = MyLocationListener.latitude;
            offlongTextValue = MyLocationListener.longitude;
        }
    }
*/
    private void captureImage() {
        if (Build.VERSION.SDK_INT >= M) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (file != null) {
                imageStoragePath = file.getAbsolutePath();
            }

            Uri fileUri = CameraUtils.getOutputMediaFileUri(this, file);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
        if (MyLocationListener.latitude > 0) {
            offlatTextValue = MyLocationListener.latitude;
            offlongTextValue = MyLocationListener.longitude;
        }
    }

    public void getLatLong() {
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();


        // permission was granted, yay! Do the
        // location-related task you need to do.
        if (ContextCompat.checkSelfPermission(CameraScreen.this,
                ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //Request location updates:
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        }

        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Build.VERSION.SDK_INT >= M) {
                if (ActivityCompat.checkSelfPermission(CameraScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CameraScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{CAMERA, ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                if (ActivityCompat.checkSelfPermission(CameraScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CameraScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CameraScreen.this, new String[]{ACCESS_FINE_LOCATION}, 1);

                }
            }
            if (MyLocationListener.latitude > 0) {
                if (Build.VERSION.SDK_INT >= M) {
                    if (CameraUtils.checkPermissions(CameraScreen.this)) {
                        captureImage();
                    } else {
                        requestCameraPermission(MEDIA_TYPE_IMAGE);
                    }
//                            checkPermissionForCamera();
                } else {
                    captureImage();
                }
            } else {
                Utils.showAlert(CameraScreen.this, "Satellite communication not available to get GPS Co-ordination Please Capture Photo in Open Area..");
            }
        } else {
            Utils.showAlert(CameraScreen.this, "GPS is not turned on...");
        }
    }

    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
//                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(CameraScreen.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public void previewCapturedImage() {
        try {
            // hide video preview
            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);
            cameraScreenBinding.imagePreview.setVisibility(View.GONE);
            cameraScreenBinding.imageView.setVisibility(View.VISIBLE);
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(imageStoragePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            cameraScreenBinding.imageView.setImageBitmap(rotatedBitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void performCrop(Uri tempUri) {
        // take care of exceptions
       /* try {
            // call the standard crop action intent (the user device may not
            // support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 2);
            cropIntent.putExtra("aspectY", 1);
            // indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            // retrieve data on return
            cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_PIC);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }*/
        try{
            CropImage.activity(tempUri).setAllowRotation(false).setAllowFlipping(false)
                    .start(this);
        }
        catch (Exception e){

        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                    // get the Uri for the captured image
                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    picUri = getImageUri(getApplicationContext(), photo);
//                    picUri = data.getData();
                    performCrop();
                }
                // user is returning from cropping the image
                else if (requestCode == CROP_PIC) {
                    // get the returned data
                    // get the cropped bitmap
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    cameraScreenBinding.imagePreview.setVisibility(View.GONE);
                    cameraScreenBinding.imageView.setVisibility(View.VISIBLE);
                    cameraScreenBinding.imageView.setImageBitmap(photo);
                }
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

    }
*/
    private Uri getImageUri(Context applicationContext, Bitmap photo)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(CameraScreen.this.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= M) {
                    /*Bitmap photo = (Bitmap) data.getExtras().get("data");
                    cameraScreenBinding.imagePreview.setVisibility(View.GONE);
                    cameraScreenBinding.imageView.setVisibility(View.VISIBLE);
                    cameraScreenBinding.imageView.setImageBitmap(photo);*/
                    Bitmap photo=(Bitmap) data.getExtras().get("data");
                    Uri tempUri = getImageUri(getApplicationContext(), photo);
                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    performCrop(tempUri);
                }
                else {
                    // Refreshing the gallery
                    CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                    // successfully captured the image
                    // display it in image view
                    previewCapturedImage();}
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }


        else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // video successfully recorded
                // preview the recorded video
//                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else  if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    cameraScreenBinding.imagePreview.setVisibility(View.GONE);
                    cameraScreenBinding.imageView.setVisibility(View.VISIBLE);
                    cameraScreenBinding.imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void saveEmployeePhoto() {

        if(Utils.isOnline()) {
            try {
                new ApiService(this).makeJSONObjectRequest("SaveEmpImage", Api.Method.POST, UrlGenerator.getMainServiceUrl(), saveEmpDetailsEncryptJsonParams(), "not cache", this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Utils.showAlert(this,"No Internet Connection");
        }
    }
    public JSONObject saveEmpDetailsEncryptJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), saveEmpDetailsNormalJsonParams().toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("empTypeListJsonParams", "" + dataSet);
        return dataSet;
    }

    public JSONObject saveEmpDetailsNormalJsonParams(){
        JSONObject dataSet = new JSONObject();
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        String image_str = "";
        byte[] imageInByte = new byte[0];
        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            imageInByte = baos.toByteArray();
            image_str = Base64.encodeToString(imageInByte, Base64.DEFAULT);
            String line = empcode;
            line = line.replace("\\/", "");
            System.out.println(line);


            try {
                dataSet.put(AppConstant.KEY_SERVICE_ID, "save_pp_image");
                dataSet.put("empcode_type", empcode_type);
                dataSet.put("empcode", line);
                dataSet.put("pp_id", pp_id);
                dataSet.put("pp_image", image_str);
            }
            catch (JSONException e){

            }
        } catch (Exception e) {
            Utils.showAlert(CameraScreen.this, "Atleast Capture one Photo");
            //e.printStackTrace();
        }


        Log.d("empphotoJson", "" + dataSet);
        return dataSet;
    }


    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();
            if ("SaveEmpImage".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                   // JSONArray jsonArray = new JSONArray();
                    Toast.makeText(this, jsonObject.getString("MESSAGE"), Toast.LENGTH_SHORT).show();
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                            Intent intent = new Intent(CameraScreen.this,Dashboard.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fleft, R.anim.fhelper);
                            finish();
                        }
                    }, 1000);
                }
                Log.d("SaveEmpImage", "" + responseDecryptedBlockKey);
            }
        }
        catch (JSONException e){
                Utils.showAlert(this,"Something Wrong");
        }

    }

    @Override
    public void OnError(VolleyError volleyError) {
        Utils.showAlert(this,"Something Wrong");
    }

    public void homePage() {
        Intent intent = new Intent(this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Home", "Home");
        startActivity(intent);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CameraScreen.this,Dashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fleft, R.anim.fhelper);
        finish();
    }

    public void onBackPress() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        Intent intent = new Intent(CameraScreen.this,Dashboard.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fleft, R.anim.fhelper);
        finish();
    }


}
