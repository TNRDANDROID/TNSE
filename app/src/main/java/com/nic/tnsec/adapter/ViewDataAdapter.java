package com.nic.tnsec.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.nic.tnsec.R;
import com.nic.tnsec.Session.PrefManager;
import com.nic.tnsec.activity.FullImageActivity;
import com.nic.tnsec.databinding.ViewDataAdapterBinding;
import com.nic.tnsec.pojo.ElectionProject;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewDataAdapter extends RecyclerView.Adapter<ViewDataAdapter.MyViewHolder> {

    private static Activity context;
    private PrefManager prefManager;
    private List<ElectionProject> viewDataListValues;
    private LayoutInflater layoutInflater;

    public ViewDataAdapter(Activity context, List<ElectionProject> viewDataListValues) {

        this.context = context;
        prefManager = new PrefManager(context);

        this.viewDataListValues = viewDataListValues;
    }

    @Override
    public ViewDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        ViewDataAdapterBinding viewDataAdapterBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_data_adapter, viewGroup, false);
        return new ViewDataAdapter.MyViewHolder(viewDataAdapterBinding);

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ViewDataAdapterBinding viewDataAdapterBinding;

        public MyViewHolder(ViewDataAdapterBinding Binding) {
            super(Binding.getRoot());
            viewDataAdapterBinding = Binding;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.viewDataAdapterBinding.name.setText(viewDataListValues.get(position).getStaffName());
        holder.viewDataAdapterBinding.organisation.setText(viewDataListValues.get(position).getDept_org_name());

        holder.viewDataAdapterBinding.viewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewImages(position);
            }
        });

    }



    public void viewImages(int position){
        Activity activity = (Activity) context;
        Intent intent = new Intent(context, FullImageActivity.class);
        intent.putExtra("Key", "Online");
        intent.putExtra("pp_id", viewDataListValues.get(position).getPp_id());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }


    @Override
    public int getItemCount() {
        return viewDataListValues.size();
    }


}
