package com.abqwtb;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class NextBusActivity extends Activity {
	private SQLiteDatabase db;
	private DatabaseHelper myDbHelper;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		myDbHelper = new DatabaseHelper(getApplicationContext());
		myDbHelper = new DatabaseHelper(this);

		try {

			myDbHelper.createDataBase();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		try {

			myDbHelper.openDataBase();

		}catch(SQLException sqle){

			throw sqle;

		}

		db = myDbHelper.getDatabase();
		
		
		
		
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();

		Log.v("Location",latitude+" "+longitude);
		
		Cursor cursor = db.rawQuery("SELECT * FROM `stops_simple` WHERE `stop_lat` > "+(latitude-0.002)+" AND `stop_lat` < "+(latitude+0.002)+" AND `stop_lon` > "+(longitude-0.002)+" AND `stop_lon` < "+(longitude+0.002)+"", null);
		
		
		cursor.moveToFirst();
		String desc = cursor.getString(6);
		Log.v("Main","From Database: "+desc);
		
	}
	
	@Override
	protected void onDestroy() {
		myDbHelper.close();
		super.onDestroy();
	}
	
	
	
}