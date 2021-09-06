package com.nic.tnsec.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteReadOnlyDatabaseException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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



    /****** ROUser TABLE *****/


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

    public void deleteAllTables(){
        deletePollingStationImages();
    }


    public void deletePollingStationImages() {
        db.execSQL("delete from " + DBHelper.POLLING_STATION_IMAGE);
    }

}
