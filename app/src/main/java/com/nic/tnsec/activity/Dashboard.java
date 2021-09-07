package com.nic.tnsec.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.android.volley.VolleyError;
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
import com.nic.tnsec.support.ProgressHUD;
import com.nic.tnsec.utils.UrlGenerator;
import com.nic.tnsec.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity implements MyDialog.myOnClickListener, Api.ServerResponseListener, View.OnClickListener {


    private DashboardBinding dashboardBinding;
    Animation smalltobig, stb2;
    final Handler handler = new Handler();
    private PrefManager prefManager;
    ArrayList<ElectionProject> employeeTypeLists;
    ArrayList<ElectionProject> employeeSearchList;
    ArrayList<ElectionProject> employeeDetails;
    public dbData dbData = new dbData(this);
    public static DBHelper dbHelper;
    public static SQLiteDatabase db;
    private ProgressHUD progressHUD;
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

        dashboardBinding.typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dashboardBinding.details.setVisibility(View.GONE);
                dashboardBinding.empPhotoView.setVisibility(View.GONE);
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

/*
    public void openViewDataScreen(JSONObject jsonObject) {
        JSONArray jsonArray = new JSONArray();
        ArrayList<ElectionProject> employeeDetails=new ArrayList<>();
        try {
            jsonArray = jsonObject.getJSONArray(AppConstant.JSON_DATA);
            if (jsonArray != null && jsonArray.length() > 0) {
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
                        employeeDetails.add(empDetails);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                Intent intent = new Intent(Dashboard.this, ViewDataScreen.class);
                intent.putExtra("ServerList", ((Serializable)employeeDetails));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();


            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
*/


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
                            employeeSearchList.add(empSearchDetails);
                            hideKeyboard(this);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    dashboardBinding.validateCheckIcon.setVisibility(View.VISIBLE);
                    dashboardBinding.validateCheckIcon.setImageResource(R.drawable.check);
                    dashboardBinding.empPhotoView.setVisibility(View.VISIBLE);
                    dashboardBinding.details.setVisibility(View.VISIBLE);
                    dashboardBinding.empName.setText(employeeSearchList.get(0).getName_of_staff());
                    dashboardBinding.empOrganaisation.setText(employeeSearchList.get(0).getDept_org_name());
                }
                else {
                    dashboardBinding.details.setVisibility(View.GONE);
                    dashboardBinding.validateCheckIcon.setVisibility(View.VISIBLE);
                    dashboardBinding.validateCheckIcon.setImageResource(R.drawable.wrong_icon);
                    dashboardBinding.empPhotoView.setVisibility(View.GONE);
                }
                Log.d("ValidateEmp", "" + responseDecryptedBlockKey);
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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public class InsertDataTask extends AsyncTask<JSONObject, Void, Void> {

        @Override
        protected Void doInBackground(JSONObject... params) {

            dbData.open();
            dbData.deleteServerDataTable();
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
}
//7034432/EDN