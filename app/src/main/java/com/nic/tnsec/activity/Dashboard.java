package com.nic.tnsec.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.android.volley.VolleyError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikelau.croperino.Croperino;
import com.mikelau.croperino.CroperinoConfig;
import com.mikelau.croperino.CroperinoFileUtil;
import com.nic.tnsec.DataBase.DBHelper;
import com.nic.tnsec.DataBase.dbData;
import com.nic.tnsec.R;
import com.nic.tnsec.Session.PrefManager;
import com.nic.tnsec.adapter.CommonAdapter;
import com.nic.tnsec.api.Api;
import com.nic.tnsec.api.ApiService;
import com.nic.tnsec.api.ServerResponse;
import com.nic.tnsec.constant.AppConstant;
import com.nic.tnsec.databinding.DashboardBinding;
import com.nic.tnsec.dialog.MyDialog;
import com.nic.tnsec.pojo.ElectionProject;
import com.nic.tnsec.pojo.EmployeeTypeList;
import com.nic.tnsec.support.MyLocationListener;
import com.nic.tnsec.support.ProgressHUD;
import com.nic.tnsec.utils.CameraUtils;
import com.nic.tnsec.utils.UrlGenerator;
import com.nic.tnsec.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;

public class Dashboard extends AppCompatActivity implements MyDialog.myOnClickListener, Api.ServerResponseListener, View.OnClickListener {


    private DashboardBinding dashboardBinding;
    Animation smalltobig, stb2;
    final Handler handler = new Handler();
    private PrefManager prefManager;
    ArrayList<ElectionProject> employeeTypeLists;
    ArrayList<ElectionProject> employeeSearchList;
    public dbData dbData = new dbData(this);
    public static DBHelper dbHelper;
    public static SQLiteDatabase db;
    private ProgressHUD progressHUD;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 2500;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static String imageStoragePath;
    public static final int BITMAP_SAMPLE_SIZE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dashboardBinding = DataBindingUtil.setContentView(this, R.layout.dashboard);
        dashboardBinding.setActivity(this);
        /*WindowPreferencesManager windowPreferencesManager = new WindowPreferencesManager(this);
        windowPreferencesManager.applyEdgeToEdgePreference(getWindow());*/
        try {
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        smalltobig = AnimationUtils.loadAnimation(this, R.anim.smalltobig);
        stb2 = AnimationUtils.loadAnimation(this, R.anim.stb2);

        prefManager = new PrefManager(this);
        employeeSearchList=new ArrayList<>();
        dashboardBinding.empPhotoView.setVisibility(View.GONE);
        dashboardBinding.empPhotoSave.setVisibility(View.GONE);
        dashboardBinding.details.setVisibility(View.GONE);
        dashboardBinding.districtUserLayout.setTranslationX(800);
        dashboardBinding.blockUserLayout.setTranslationX(800);

        dashboardBinding.districtUserLayout.setAlpha(0);
        dashboardBinding.blockUserLayout.setAlpha(0);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dashboardBinding.districtUserLayout.animate().translationX(0).alpha(1).setDuration(1200).setStartDelay(400).start();
                dashboardBinding.blockUserLayout.animate().translationX(0).alpha(1).setDuration(1300).setStartDelay(600).start();
                       }
        }, 1000);

        Animation anim = new ScaleAnimation(
                0.95f, 1f, // Start and end values for the X axis scaling
                0.95f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        anim.setRepeatMode(Animation.INFINITE);
        anim.setRepeatCount(Animation.INFINITE);
        getEmpType();
        dashboardBinding.btnValidate.setOnClickListener(this);
        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", "/sdcard/MikeLau/Pictures");
        CroperinoFileUtil.setupDirectory(Dashboard.this);
        dashboardBinding.typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dashboardBinding.details.setVisibility(View.GONE);
                dashboardBinding.empPhotoView.setVisibility(View.GONE);
                dashboardBinding.empPhotoSave.setVisibility(View.GONE);
                dashboardBinding.typeValue.setText("");
                dashboardBinding.validateCheckIcon.setVisibility(View.GONE);
                employeeSearchList.clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dashboardBinding.typeValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dashboardBinding.details.setVisibility(View.GONE);
                dashboardBinding.empPhotoView.setVisibility(View.GONE);
                dashboardBinding.empPhotoSave.setVisibility(View.GONE);
                dashboardBinding.validateCheckIcon.setVisibility(View.GONE);
                employeeSearchList.clear();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



    }

    public void getEmpType() {
        if(Utils.isOnline()) {
            try {
                new ApiService(this).makeJSONObjectRequest("EmpType", Api.Method.POST, UrlGenerator.getMainServiceUrl(), empTypeListJsonParams(), "not cache", this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else {
            Utils.showAlert(this,"No Internet Connection");
        }
    }
    public JSONObject empTypeListJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.empTypeJsonParams(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("empTypeListJsonParams", "" + dataSet);
        return dataSet;
    }

    public void validateEmpDetails() {
        if(Utils.isOnline()) {
            try {
                new ApiService(this).makeJSONObjectRequest("ValidateEmp", Api.Method.POST, UrlGenerator.getMainServiceUrl(), validateEmpJsonParams(), "not cache", this);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Utils.showAlert(this,"No Internet Connection");
        }
    }

    public JSONObject validateEmpJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), validateEmpNormalJson().toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("empTypeListJsonParams", "" + dataSet);
        return dataSet;
    }

    public JSONObject validateEmpNormalJson(){
        String empcode_type = employeeTypeLists.get(dashboardBinding.typeSpinner.getSelectedItemPosition()).getEmpcode_type();
        String empcode=dashboardBinding.typeValue.getText().toString();
        JSONObject dataSet = new JSONObject();
        String line = empcode;
        line = line.replace("\\/", "");
        System.out.println(line);
        try {
            dataSet.put(AppConstant.KEY_SERVICE_ID, "empcode_search");
            dataSet.put("empcode_type", empcode_type);
            dataSet.put("empcode", line);
        }
        catch (JSONException e){

        }

        Log.d("empTypeListJsonParams", "" + dataSet);
        return dataSet;
    }


    @Override
    public void onButtonClick(AlertDialog alertDialog, String type) {
        alertDialog.dismiss();
        if ("Exit".equalsIgnoreCase(type)) {
            onBackPressed();
        } else {

            Intent intent = new Intent(this, LoginScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("EXIT", false);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }


    public void logout() {

                closeApplication();
    }

    public void closeApplication() {
        new MyDialog(this).exitDialog(this, "Are you sure you want to Logout?", "Logout");
    }



    public void cameraScreen(){
        Intent intent = new Intent(this,CameraScreen.class);
        intent.putExtra(AppConstant.KEY_PURPOSE, "Insert");
        intent.putExtra("empcode_type",employeeTypeLists.get(dashboardBinding.typeSpinner.getSelectedItemPosition()).getEmpcode_type());
        intent.putExtra("empcode",dashboardBinding.typeValue.getText().toString());
        //intent.putExtra("empcode","338280/EDN");
        intent.putExtra("pp_id",employeeSearchList.get(0).getPp_id());
        startActivity(intent);
        overridePendingTransition(R.anim.fleft, R.anim.fhelper);
        finish();
    }

    public void viewServerData() {
        if (Utils.isOnline()) {
            getServerDataList();
        } else {
            Utils.showAlert(Dashboard.this, "Your Internet seems to be Offline.Data can be viewed only in Online mode.");
        }
    }
    public void getServerDataList() {
        try {
            new ApiService(this).makeJSONObjectRequest("ServerDataList", Api.Method.POST, UrlGenerator.getMainServiceUrl(), ServerDataListJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public JSONObject ServerDataListJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.serverDataListJsonParams().toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("ServerDataList", "" + dataSet);
        return dataSet;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                new MyDialog(this).exitDialog(this, "Are you sure you want to exit ?", "Exit");
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();
        if ("EmpType".equals(urlType) && responseObj != null) {
            String key = responseObj.getString(AppConstant.ENCODE_DATA);
            String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
            JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
            if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                JSONArray jsonArray = new JSONArray();
                jsonArray = jsonObject.getJSONArray(AppConstant.JSON_DATA);
               LoadEmpTypeList(jsonArray);
            }
            Log.d("EmpTypeList", "" + responseDecryptedBlockKey);
        }
        if ("ValidateEmp".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String pp_id,name_of_staff,dept_org_name,gender,photo_available;
                String pp_image=responseObj.getString("pp_image");
                String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    //{"STATUS":"OK","RESPONSE":"OK","JSON_DATA":[{"pp_id":"269597","name_of_staff":"SELVARANI T","dept_org_name":"GHSS Kattathurai","gender":"F","photo_available":"Y"}]}


                    JSONArray jsonArray = new JSONArray();
                    jsonArray = jsonObject.getJSONArray(AppConstant.JSON_DATA);
                    employeeSearchList=new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        ElectionProject empSearchDetails = new ElectionProject();
                        try {
                            empSearchDetails.setPp_id(jsonArray.getJSONObject(i).getString("pp_id"));
                            empSearchDetails.setName_of_staff(jsonArray.getJSONObject(i).getString("name_of_staff"));
                            empSearchDetails.setDept_org_name(jsonArray.getJSONObject(i).getString("dept_org_name"));
                            empSearchDetails.setGender(jsonArray.getJSONObject(i).getString("gender"));
                            empSearchDetails.setPhoto_available(jsonArray.getJSONObject(i).getString("photo_available"));
                            empSearchDetails.setEmp_designation_name(jsonArray.getJSONObject(i).getString("designation_name"));
                            empSearchDetails.setEmp_ddo_code(jsonArray.getJSONObject(i).getString("ddo_code"));
                            //empSearchDetails.setEmp_image(jsonArray.getJSONObject(i).getString("pp_image"));
                            employeeSearchList.add(empSearchDetails);
                            hideKeyboard(this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    dashboardBinding.validateCheckIcon.setVisibility(View.VISIBLE);
                    dashboardBinding.validateCheckIcon.setImageResource(R.drawable.check);
                    dashboardBinding.empPhotoView.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhotoSave.setVisibility(View.GONE);
                    dashboardBinding.details.setVisibility(View.VISIBLE);
                    dashboardBinding.empName.setText(employeeSearchList.get(0).getName_of_staff());
                    dashboardBinding.empOrganaisation.setText(employeeSearchList.get(0).getDept_org_name());
                    dashboardBinding.empDesignation.setText(employeeSearchList.get(0).getEmp_designation_name());
                    dashboardBinding.empDdoCode.setText(employeeSearchList.get(0).getEmp_ddo_code());
                    if(employeeSearchList.get(0).getPhoto_available().toString().equalsIgnoreCase("Y")){
                        byte[] decodedString = Base64.decode(pp_image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        dashboardBinding.empPhoto.setImageBitmap(decodedByte);
                        dashboardBinding.empPhoto.setVisibility(View.VISIBLE);
                    }else {
                        dashboardBinding.empPhoto.setVisibility(View.GONE);
                    }
                }
                else {
                    dashboardBinding.details.setVisibility(View.GONE);
                    dashboardBinding.validateCheckIcon.setVisibility(View.VISIBLE);
                    dashboardBinding.validateCheckIcon.setImageResource(R.drawable.wrong_icon);
                    dashboardBinding.empPhotoView.setVisibility(View.GONE);
                    dashboardBinding.empPhotoSave.setVisibility(View.GONE);
                }
                Log.d("ValidateEmp", "" + responseDecryptedBlockKey);
                Log.d("responseObj", "" + responseObj.toString());
            }
        if ("ServerDataList".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                Log.d("ServerDataListResponse", "" + responseDecryptedBlockKey);
                JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    new InsertDataTask().execute(jsonObject);
                }else if(jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD") && jsonObject.getString("MESSAGE").equalsIgnoreCase("NO_RECORD")){
                    Utils.showAlert(this,"No Record Found!");
                }

            }
        if ("SaveEmpImage".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Toast.makeText(this, jsonObject.getString("MESSAGE"), Toast.LENGTH_SHORT).show();
                    dashboardBinding.empPhotoSave.setVisibility(View.GONE);
                }
                Log.d("SaveEmpImage", "" + responseDecryptedBlockKey);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public class InsertDataTask extends AsyncTask<JSONObject, Void, Void> {

        @Override
        protected Void doInBackground(JSONObject... params) {

            dbData.open();

            ArrayList<ElectionProject> all_kvvtListCount = dbData.getAll_dataList();
            if (all_kvvtListCount.size() <= 0) {

                if (params.length > 0) {
                    JSONArray jsonArray = new JSONArray();
                    try {
                        jsonArray = params[0].getJSONArray(AppConstant.JSON_DATA);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ElectionProject empDetails = new ElectionProject();
                        try {
                            empDetails.setPp_id(jsonArray.getJSONObject(i).getString("pp_id"));
                            empDetails.setEmpcode_type(jsonArray.getJSONObject(i).getString("empcode_type"));
                            empDetails.setEmpcode_description(jsonArray.getJSONObject(i).getString("empcode"));
                            empDetails.setName_of_staff(jsonArray.getJSONObject(i).getString("name_of_staff"));
                            empDetails.setDept_org_name(jsonArray.getJSONObject(i).getString("dept_org_name"));
                            empDetails.setGender(jsonArray.getJSONObject(i).getString("gender"));
                            empDetails.setPhoto_available(jsonArray.getJSONObject(i).getString("photo_available"));
                            dbData.insertData(empDetails);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD = ProgressHUD.show(Dashboard.this, "Downloading", true, false, null);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(progressHUD!=null){
                progressHUD.cancel();
            }
            Intent intent = new Intent(Dashboard.this, ViewDataScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

        }
    }

    private void LoadEmpTypeList(JSONArray jsonArray) {
         employeeTypeLists=new ArrayList<>();
        ElectionProject empDetails1 = new ElectionProject();
        empDetails1.setEmpcode_type("Select Type");
        empDetails1.setEmpcode_description("");
        employeeTypeLists.add(empDetails1);
        for (int i = 0; i < jsonArray.length(); i++) {
            ElectionProject empDetails = new ElectionProject();
            try {
                empDetails.setEmpcode_type(jsonArray.getJSONObject(i).getString(AppConstant.KEY_EMP_TYPE));
                empDetails.setEmpcode_description(jsonArray.getJSONObject(i).getString(AppConstant.KEY_EMP_DESCRIPTION));
                employeeTypeLists.add(empDetails);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dashboardBinding.typeSpinner.setAdapter(new CommonAdapter(this, employeeTypeLists, "EmpList"));
    }

    @Override
    public void OnError(VolleyError volleyError) {

    }


    @Override
    public void onClick(View view) {

    }

    public void validateDetails(){
        if(dashboardBinding.typeSpinner.getSelectedItemPosition()!=0){
            if(dashboardBinding.typeSpinner.getSelectedItemPosition()!=4){
                if(!dashboardBinding.typeValue.getText().toString().equals("")){
                    validateEmpDetails();
                }
                else {
                    Utils.showAlert(this,"Please enter Type Value");
                }

            }else {
                if(!dashboardBinding.typeValue.getText().toString().equals("")){

                    if(dashboardBinding.typeValue.getText().toString().length()==10){
                        validateEmpDetails();
                    }
                    else {
                        Utils.showAlert(this,"Please enter correct mobile number");
                    }

                }
                else {
                    Utils.showAlert(this,"Please enter Type Value");
                }
            }
        }
        else {
            Utils.showAlert(this,"Please Select Type");
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public void getPerMissionCapture(){
        if (Build.VERSION.SDK_INT >= M) {
            if (CameraUtils.checkPermissions(Dashboard.this)) {
                captureImage();

            } else {
                requestCameraPermission(MEDIA_TYPE_IMAGE);
            }
//                            checkPermissionForCamera();
        } else {
            captureImage();

        }

    }
    private void captureImage() {
        if (CroperinoFileUtil.verifyStoragePermissions(Dashboard.this)) {
            prepareCamera();
        }
      /*  if (Build.VERSION.SDK_INT >= M) {
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
        }*/
    }
    private void prepareChooser() {
        Croperino.prepareChooser(Dashboard.this, "Capture photo...", ContextCompat.getColor(Dashboard.this, android.R.color.background_dark));
    }

    private void prepareCamera() {
        Croperino.prepareCamera(Dashboard.this);
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
                        CameraUtils.openSettings(Dashboard.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CroperinoConfig.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getTempFile());
                    // Capture Image
                   /* dashboardBinding.empPhoto.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhotoSave.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhoto.setImageURI(i);
                    File fdelete = new File(i.getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            System.out.println("file Deleted :" + i.getPath());
                        } else {
                            System.out.println("file not Deleted :" + i.getPath());
                        }
                    }*/

                   //Start Crop Image
                    Croperino.runCropImage(CroperinoFileUtil.getTempFile(), Dashboard.this, true, 1, 1, R.color.gray, R.color.gray_variant);
                }
                break;
            case CroperinoConfig.REQUEST_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    CroperinoFileUtil.newGalleryFile(data, Dashboard.this);
                    Croperino.runCropImage(CroperinoFileUtil.getTempFile(), Dashboard.this, true, 1, 1, R.color.gray, R.color.gray_variant);
                }
                break;
            case CroperinoConfig.REQUEST_CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getTempFile());
                    dashboardBinding.empPhoto.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhotoSave.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhoto.setImageURI(i);
                    File fdelete = new File(i.getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            System.out.println("file Deleted :" + i.getPath());
                        } else {
                            System.out.println("file not Deleted :" + i.getPath());
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CroperinoFileUtil.REQUEST_CAMERA) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        prepareCamera();
                    }
                }
            }
        } else if (requestCode == CroperinoFileUtil.REQUEST_EXTERNAL_STORAGE) {
            boolean wasReadGranted = false;
            boolean wasWriteGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        wasReadGranted = true;
                    }
                }
                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        wasWriteGranted = true;
                    }
                }
            }

            if (wasReadGranted && wasWriteGranted) {
                prepareChooser();
            }
        }
    }
   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= M) {
                    Bitmap photo=(Bitmap) data.getExtras().get("data");
                    *//*Uri tempUri = getImageUri(getApplicationContext(), photo);
                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    performCrop(tempUri);*//*
                    dashboardBinding.empPhoto.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhotoSave.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhoto.setImageBitmap(photo);
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
                    dashboardBinding.empPhoto.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhotoSave.setVisibility(View.VISIBLE);
                    dashboardBinding.empPhoto.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }*/
    private Uri getImageUri(Context applicationContext, Bitmap photo)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(Dashboard.this.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }
/*
    private void performCrop(Uri tempUri) {
        // take care of exceptions
        try{
            CropImage.activity(tempUri).setAllowRotation(true).setAllowFlipping(false)
                    .start(this);
        }
        catch (Exception e){

        }
    }
*/
    public void previewCapturedImage() {
        try {
            // hide video preview
            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);
            dashboardBinding.empPhoto.setVisibility(View.VISIBLE);
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
            dashboardBinding.empPhoto.setImageBitmap(rotatedBitmap);
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
        ImageView imageView = (ImageView) findViewById(R.id.empPhoto);
        String image_str = "";
        byte[] imageInByte = new byte[0];
        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            imageInByte = baos.toByteArray();
            image_str = Base64.encodeToString(imageInByte, Base64.DEFAULT);
            String line = dashboardBinding.typeValue.getText().toString();
            line = line.replace("\\/", "");
            System.out.println(line);


            try {
                dataSet.put(AppConstant.KEY_SERVICE_ID, "save_pp_image");
                dataSet.put("empcode_type", employeeTypeLists.get(dashboardBinding.typeSpinner.getSelectedItemPosition()).getEmpcode_type());
                dataSet.put("empcode", line);
                dataSet.put("pp_id", employeeSearchList.get(0).getPp_id());
                dataSet.put("pp_image", image_str);
            }
            catch (JSONException e){

            }
        } catch (Exception e) {
            Utils.showAlert(Dashboard.this, "Atleast Capture one Photo");
            //e.printStackTrace();
        }


        Log.d("empphotoJson", "" + dataSet);
        return dataSet;
    }
}
