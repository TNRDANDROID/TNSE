package com.nic.tnsec.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.VolleyError;
import com.nic.tnsec.DataBase.DBHelper;
import com.nic.tnsec.DataBase.dbData;
import com.nic.tnsec.R;
import com.nic.tnsec.Session.PrefManager;
import com.nic.tnsec.adapter.ViewDataAdapter;
import com.nic.tnsec.api.Api;
import com.nic.tnsec.api.ServerResponse;
import com.nic.tnsec.databinding.ViewDataScreenBinding;
import com.nic.tnsec.pojo.ElectionProject;
import com.nic.tnsec.support.ProgressHUD;
import java.util.ArrayList;

public class ViewDataScreen extends AppCompatActivity implements Api.ServerResponseListener {
    public ViewDataScreenBinding viewDataBinding;
    private RecyclerView recyclerView;
    private ViewDataAdapter viewDataAdapter;
    private PrefManager prefManager;
    private Activity context;
    ArrayList<ElectionProject> employeeDetails;
    ArrayList<ElectionProject> savedList = new ArrayList<>();
    public dbData dbData = new dbData(this);
    public static DBHelper dbHelper;
    public static SQLiteDatabase db;
    private ProgressHUD progressHUD;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.view_data_screen);
        viewDataBinding.setActivity(this);
        context = this;
        try {
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        prefManager = new PrefManager(this);
        recyclerView = viewDataBinding.serverDataList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        viewDataAdapter = new ViewDataAdapter(ViewDataScreen.this, savedList);
        recyclerView.setAdapter(viewDataAdapter);
        new fetchScheduletask().execute();
//        LoadServerData();

    }
    public class fetchScheduletask extends AsyncTask<Void, Void,
            ArrayList<ElectionProject>> {
        @Override
        protected ArrayList<ElectionProject> doInBackground(Void... params) {
            dbData.open();
            ArrayList<ElectionProject> savedAllList = new ArrayList<>();
            savedAllList = dbData.getAll_dataList();
            Log.d("savedList_COUNT", String.valueOf(savedAllList.size()));
            return savedAllList;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD = ProgressHUD.show(ViewDataScreen.this, "Loading", true, false, null);
        }

        @Override
        protected void onPostExecute(ArrayList<ElectionProject> savedList) {
            super.onPostExecute(savedList);
            if(progressHUD!=null){
                progressHUD.cancel();
            }
            viewDataAdapter = new ViewDataAdapter(ViewDataScreen.this, savedList);
            if (savedList.size() > 0) {
                recyclerView.setVisibility(View.VISIBLE);
                viewDataBinding.notFoundTv.setVisibility(View.GONE);
                recyclerView.setAdapter(viewDataAdapter);
            } else {
                recyclerView.setVisibility(View.GONE);
                viewDataBinding.notFoundTv.setVisibility(View.VISIBLE);
            }
        }


    }

/*
    private void LoadServerData() {
        try{

            employeeDetails=new ArrayList<>();
            //String array = getIntent().getStringExtra("ServerList");
            employeeDetails = (ArrayList<ElectionProject>) getIntent().getSerializableExtra("ServerList");
        }
        catch (Exception e){

        }



       */
/* try {
            JSONObject json = new JSONObject(array);
            JSONArray jsonArray = new JSONArray(json);
            if(jsonArray != null && jsonArray.length() >0) {
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


            }
        } catch (Exception e) {
            e.printStackTrace();
        }*//*


        if(employeeDetails != null && employeeDetails.size() >0) {
            viewDataBinding.notFoundTv.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            viewDataAdapter = new ViewDataAdapter(ViewDataScreen.this,employeeDetails);
            recyclerView.setAdapter(viewDataAdapter);
        }else {
            viewDataBinding.notFoundTv.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

*/
/*
        try {
            JSONArray jsonArray=new JSONArray(prefManager.getServerDataList(context));
            if(jsonArray != null && jsonArray.length() >0) {
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

                if(employeeDetails != null && employeeDetails.size() >0) {
                    viewDataBinding.notFoundTv.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    viewDataAdapter = new ViewDataAdapter(ViewDataScreen.this,employeeDetails);
                    recyclerView.setAdapter(viewDataAdapter);
                }else {
                    viewDataBinding.notFoundTv.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
*//*

    }
*/


    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        /*try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();

        }catch (JSONException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void OnError(VolleyError volleyError) {

    }

    public void Dashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    public void onBackPress() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }
}
