package com.nic.tnsec.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteReadOnlyDatabaseException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.nic.tnsec.constant.AppConstant;
import com.nic.tnsec.pojo.ElectionProject;

import java.util.ArrayList;


public class dbData {
    private SQLiteDatabase db;
    private SQLiteOpenHelper dbHelper;
    private Context context;

    public dbData(Context context) {
        this.dbHelper = new DBHelper(context);
        this.context = context;
    }

    public void open() {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteReadOnlyDatabaseException e) {

        }

    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public ElectionProject insertData(ElectionProject kvvtSurvey) {

        ContentValues values = new ContentValues();
        values.put("pp_id", kvvtSurvey.getPp_id());
        values.put("empcode_type", kvvtSurvey.getEmpcode_type());
        values.put("empcode", kvvtSurvey.getEmpcode_description());
        values.put("name_of_staff", kvvtSurvey.getName_of_staff());
        values.put("dept_org_name", kvvtSurvey.getDept_org_name());
        values.put("gender", kvvtSurvey.getGender());
        values.put("photo_available", kvvtSurvey.getPhoto_available());

        long id = db.insert(DBHelper.SAVE_EMP_DETAILS,null,values);
        Log.d("Inserted_id_data_LIST", String.valueOf(id));

        return kvvtSurvey;
    }


    /****** ROUser TABLE *****/

    public ArrayList<ElectionProject> getAll_dataList() {

        ArrayList<ElectionProject> cards = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from "+DBHelper.SAVE_EMP_DETAILS ,null);
            // cursor = db.query(CardsDBHelper.TABLE_CARDS,
            //       COLUMNS, null, null, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ElectionProject card = new ElectionProject();
                    card.setPp_id(cursor.getString(cursor
                            .getColumnIndexOrThrow("pp_id")));
                    card.setEmpcode_type(cursor.getString(cursor
                            .getColumnIndexOrThrow("empcode_type")));
                    card.setEmpcode_description(cursor.getString(cursor
                            .getColumnIndexOrThrow("empcode")));
                    card.setName_of_staff(cursor.getString(cursor
                            .getColumnIndexOrThrow("name_of_staff")));
                    card.setDept_org_name(cursor.getString(cursor
                            .getColumnIndexOrThrow("dept_org_name")));
                    card.setGender(cursor.getString(cursor
                            .getColumnIndexOrThrow("gender")));
                    card.setPhoto_available(cursor.getString(cursor
                            .getColumnIndexOrThrow("photo_available")));
                    cards.add(card);
                }
            }
        } catch (Exception e){
            //   Log.d(DEBUG_TAG, "Exception raised with a value of " + e);
        } finally{
            if (cursor != null) {
                cursor.close();
            }
        }
        return cards;
    }

    public ArrayList<ElectionProject> getAllpollingStationImages() {

        ArrayList<ElectionProject> cards = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from " + DBHelper.POLLING_STATION_IMAGE, null);
            // cursor = db.query(CardsDBHelper.TABLE_CARDS,
            //       COLUMNS, null, null, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    byte[] photo = cursor.getBlob(cursor.getColumnIndexOrThrow(AppConstant.IMAGE));
                    byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    ElectionProject card = new ElectionProject();
                    card.setPhotoID(cursor.getInt(cursor
                            .getColumnIndex(AppConstant.KEY_PHOTO_ID)));
                 /*   card.setDistictCode(cursor.getString(cursor
                            .getColumnIndexOrThrow(AppConstant.DISTRICT_CODE)));
                    card.setBlockCode(cursor.getString(cursor
                            .getColumnIndexOrThrow(AppConstant.BLOCK_CODE)));*/
                    card.setDescription(cursor.getString(cursor
                            .getColumnIndexOrThrow(AppConstant.DESCRIPTION)));
                    card.setImage(decodedByte);
                    card.setLatitude(cursor.getString(cursor
                            .getColumnIndexOrThrow(AppConstant.KEY_LATITUDE)));
                    card.setLongitude(cursor.getString(cursor
                            .getColumnIndexOrThrow(AppConstant.KEY_LONGITUDE)));
                    cards.add(card);
                }
            }
        } catch (Exception e) {
            //   Log.d(DEBUG_TAG, "Exception raised with a value of " + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return cards;
    }
    public void deleteServerDataTable() { db.execSQL("delete from " + DBHelper.SAVE_EMP_DETAILS); }

    public void deleteAllTables(){
        deletePollingStationImages();
        deleteServerDataTable();
    }


    public void deletePollingStationImages() {
        db.execSQL("delete from " + DBHelper.POLLING_STATION_IMAGE);
    }

}
