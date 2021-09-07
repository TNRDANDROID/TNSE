package com.nic.tnsec.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.VolleyError;
import com.nic.tnsec.R;
import com.nic.tnsec.Session.PrefManager;
import com.nic.tnsec.adapter.ViewDataAdapter;
import com.nic.tnsec.api.Api;
import com.nic.tnsec.api.ServerResponse;
import com.nic.tnsec.databinding.ViewDataScreenBinding;
import com.nic.tnsec.pojo.ElectionProject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class ViewDataScreen extends AppCompatActivity implements Api.ServerResponseListener {
    public ViewDataScreenBinding viewDataBinding;
    private RecyclerView recyclerView;
    private ViewDataAdapter viewDataAdapter;
    private PrefManager prefManager;
    private Activity context;
    ArrayList<ElectionProject> employeeDetails;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.view_data_screen);
        viewDataBinding.setActivity(this);
        context = this;
        prefManager = new PrefManager(this);
        recyclerView = viewDataBinding.serverDataList;

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        LoadServerData();

    }

    private void LoadServerData() {
        employeeDetails=new ArrayList<>();
        String array = getIntent().getStringExtra("ServerList");

        try {
            JSONArray jsonArray = new JSONArray(array);
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
*/
    }


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
