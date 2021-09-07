package com.nic.tnsec.pojo;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by AchanthiSundar on 01-11-2017.
 */

public class ElectionProject  {

    private String distictCode;
    private String districtName;

    private String blockCode;

    String empcode_type;
    String empcode_description;

    String pp_id,name_of_staff,dept_org_name,gender,photo_available;

    public String getPp_id() {
        return pp_id;
    }

    public ElectionProject setPp_id(String pp_id) {
        this.pp_id = pp_id;
        return this;
    }

    public String getName_of_staff() {
        return name_of_staff;
    }

    public ElectionProject setName_of_staff(String name_of_staff) {
        this.name_of_staff = name_of_staff;
        return this;
    }

    public String getDept_org_name() {
        return dept_org_name;
    }

    public ElectionProject setDept_org_name(String dept_org_name) {
        this.dept_org_name = dept_org_name;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public ElectionProject setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getPhoto_available() {
        return photo_available;
    }

    public ElectionProject setPhoto_available(String photo_available) {
        this.photo_available = photo_available;
        return this;
    }

    public String getEmpcode_type() {
        return empcode_type;
    }

    public ElectionProject setEmpcode_type(String empcode_type) {
        this.empcode_type = empcode_type;
        return this;
    }

    public String getEmpcode_description() {
        return empcode_description;
    }

    public ElectionProject setEmpcode_description(String empcode_description) {
        this.empcode_description = empcode_description;
        return this;
    }

    public String getFinancialYear() {
        return FinancialYear;
    }

    public ElectionProject setFinancialYear(String financialYear) {
        FinancialYear = financialYear;
        return this;
    }

    public ElectionProject setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    private String staffName;

    private String selectedBlockCode;

    private String FinancialYear;

    private String PvCode;
    private String PvName;

    private String blockName;

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    private String Description;
    private String Latitude;
    private String Longitude;

    public Bitmap getImage() {
        return Image;
    }

    public void setImage(Bitmap image) {
        Image = image;
    }

    private Bitmap Image;



    public String getPvName() {
        return PvName;
    }

    public void setPvName(String pvName) {
        PvName = pvName;
    }


    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }


    public String getDistictCode() {
        return distictCode;
    }

    public void setDistictCode(String distictCode) {
        this.distictCode = distictCode;
    }

    public String getBlockCode() {
        return blockCode;
    }

    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getSelectedBlockCode() {
        return selectedBlockCode;
    }

    public void setSelectedBlockCode(String selectedBlockCode) {
        this.selectedBlockCode = selectedBlockCode;
    }
    public String getPvCode() {
        return PvCode;
    }

    public void setPvCode(String pvCode) {
        this.PvCode = pvCode;
    }


    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }
    public Integer getPhotoID() {
        return photoID;
    }

    public void setPhotoID(Integer photoID) {
        this.photoID = photoID;
    }

    private Integer photoID;

    public Integer getId() {
        return id;
    }
    private Integer id;
}