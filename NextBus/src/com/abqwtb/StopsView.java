package com.abqwtb;

import java.io.IOException;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StopsView extends ListActivity  {

	LocationManager locationManager;
	LocationListener locationListener;

	static Stop[] list = new Stop[]{new Stop(0,"Please Wait ...",0)};
	private SQLiteDatabase db;
	ArrayAdapter<Stop> adapter;

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.v("Main","StopId:"+list[position].getId());
		Intent i = new Intent(StopsView.this, StopView.class);

		i.putExtra("com.abqwtb.stop_id",list[position].getId());
		i.putExtra("com.abqwtb.stop_name",list[position].toString());

		StopsView.this.startActivity(i);
		super.onListItemClick(l, v, position, id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loadDatabase();

		adapter = new ArrayAdapter<Stop>(this,R.layout.item_layout, list);
		setListAdapter(adapter);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
			Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

			reloadLocation(loc);
		}

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location provider.
				reloadLocation(location);
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {}

			public void onProviderEnabled(String provider) {

			}

			public void onProviderDisabled(String provider) {

			}
		};

	}

	@Override
	protected void onStart(){
		super.onStart();
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	@Override
	protected void onPause(){
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	private void loadDatabase() {
		DatabaseHelper myDbHelper = new DatabaseHelper(getApplicationContext());
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

	}

	public void reloadLocation(Location location){
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();

		//longitude = -106.568586;
		//latitude = 35.075896;
		//Log.v("Location",latitude+" "+longitude);

		Cursor cursor = db.rawQuery("SELECT * FROM `stops_local` WHERE `stop_lat` > "+(latitude-0.006)+" AND `stop_lat` < "+(latitude+0.006)+" AND `stop_lon` > "+(longitude-0.006)+" AND `stop_lon` < "+(longitude+0.006)+"", null);


		cursor.moveToFirst();
		Stop[] temp = new Stop[cursor.getCount()];
		int i = 0;
		while (cursor.isAfterLast() == false) 
		{
			double x_dist = latitude - cursor.getDouble(0);
			double y_dist = longitude - cursor.getDouble(2);
			double dist = Math.sqrt((x_dist*x_dist)+(y_dist*y_dist));
			Cursor cursor1 = db.rawQuery("SELECT * FROM `routes_map` WHERE `stop` = "+cursor.getInt(3), null);
			String stop_name = cursor.getString(4);
			cursor1.moveToFirst();
			while (cursor1.isAfterLast() == false){
				stop_name += "("+cursor1.getInt(0)+")";
				cursor1.moveToNext();
			}
			stop_name += "("+cursor.getString(5)+")";
			temp[i]  = new Stop(dist,stop_name,cursor.getInt(3));
			i++;
			cursor.moveToNext();
		}
		Arrays.sort(temp);

		list = temp;

		adapter = new ArrayAdapter<Stop>(this,R.layout.item_layout, list);
		setListAdapter(adapter);

	}

}
