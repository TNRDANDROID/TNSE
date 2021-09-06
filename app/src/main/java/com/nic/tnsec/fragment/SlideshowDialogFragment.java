package com.nic.tnsec.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.nic.tnsec.DataBase.dbData;
import com.nic.tnsec.R;
import com.nic.tnsec.Session.PrefManager;
import com.nic.tnsec.activity.CameraScreen;
import com.nic.tnsec.constant.AppConstant;
import com.nic.tnsec.databinding.FragmentImageSliderBinding;
import com.nic.tnsec.pojo.ElectionProject;

import java.util.ArrayList;


public class SlideshowDialogFragment extends DialogFragment {
    private String TAG = SlideshowDialogFragment.class.getSimpleName();
    private ArrayList<ElectionProject> images;

    private MyViewPagerAdapter myViewPagerAdapter;

    private int selectedPosition = 0;
    public dbData dbData;
    public FragmentImageSliderBinding fragmentImageSliderBinding;
    private Context context;
    private PrefManager prefManager;
   public static SlideshowDialogFragment newInstance() {
        SlideshowDialogFragment f = new SlideshowDialogFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentImageSliderBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_image_slider, container, false);
        View view = fragmentImageSliderBinding.getRoot();
        //here data must be an instance of the class MarsDataProvider

        this.context = getActivity();
        dbData = new dbData(getActivity());
        prefManager = new PrefManager(getActivity());
      /*  images = (ArrayList<ElectionProject>) getArguments().getSerializable("images");*/
        //images = prefManager.getLocalSaveHaccpList();
        selectedPosition = getArguments().getInt("position");

        Log.i(TAG, "position: " + selectedPosition);
        Log.i(TAG, "images size: " + images.size());

        myViewPagerAdapter = new MyViewPagerAdapter();
        fragmentImageSliderBinding.viewpager.setAdapter(myViewPagerAdapter);
        fragmentImageSliderBinding.viewpager.addOnPageChangeListener(viewPagerPageChangeListener);

        setCurrentItem(selectedPosition);

        return view;
    }

    private void setCurrentItem(int position) {
        fragmentImageSliderBinding.viewpager.setCurrentItem(position, false);
        displayMetaInfo(selectedPosition);
    }

    //	page change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private void displayMetaInfo(int position) {
        fragmentImageSliderBinding.lblCount.setText((position + 1) + " of " + images.size());

       final ElectionProject image = images.get(position);
        if(!image.getDescription().equalsIgnoreCase("")){
            fragmentImageSliderBinding.description.setVisibility(View.VISIBLE);
            fragmentImageSliderBinding.description.setText(image.getDescription());
        }else{
            fragmentImageSliderBinding.description.setVisibility(View.GONE);
        }
//        if (getArguments().getString("OnOffType").equalsIgnoreCase("Online")) {
//            fragmentImageSliderBinding.editImageLayout.setVisibility(View.GONE);
//        } else {
//            fragmentImageSliderBinding.editImageLayout.setVisibility(View.VISIBLE);
//        }
        fragmentImageSliderBinding.editImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = v.getContext();
                Integer photoId = image.getPhotoID();
                Intent intent = new Intent(context, CameraScreen.class);
                intent.putExtra(AppConstant.KEY_PURPOSE, "Update");
                intent.putExtra(AppConstant.KEY_PHOTO_ID, String.valueOf(photoId));
                context.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }

        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    //	adapter
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_fullscreen_preview, container, false);

            ImageView imageViewPreview = (ImageView) view.findViewById(R.id.image_preview);

            ElectionProject image = images.get(position);

            Glide.with(getActivity()).load(image.getImage())
                    .thumbnail(0.5f)
                    .into(imageViewPreview);

            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
