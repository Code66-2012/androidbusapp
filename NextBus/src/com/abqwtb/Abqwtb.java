package com.abqwtb;

import java.io.IOException;
import java.util.Arrays;

import android.R.id;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Abqwtb extends ListActivity  {

	LocationManager locationManager;
	LocationListener locationListener;

	Location loc;

	static Stop[] list = new Stop[]{new Stop(0,"","Please Wait ...",0)};
	private SQLiteDatabase db;
	ArrayAdapter<Stop> adapter;
	private String provider;

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (list[position].getId() >0){
			Log.v("Main","StopId:"+list[position].getId());
			Intent i = new Intent(Abqwtb.this, ScheduleView.class);

			i.putExtra("com.abqwtb.stop_id",list[position].getId());
			i.putExtra("com.abqwtb.stop_name",list[position].getShortName());

			Abqwtb.this.startActivity(i);
		}
		super.onListItemClick(l, v, position, id);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		loadDatabase();

		adapter = new ArrayAdapter<Stop>(this,R.layout.item_layout, list);
		setListAdapter(adapter);

		String context = Context.LOCATION_SERVICE; 
		locationManager = (LocationManager) this.getSystemService(context); 
		Criteria criteria = new Criteria(); 
		criteria.setAccuracy(Criteria.ACCURACY_FINE); 
		criteria.setAltitudeRequired(false); 
		criteria.setBearingRequired(false); 
		criteria.setCostAllowed(true); 
		provider = locationManager.getBestProvider(criteria, false); 

		loc = locationManager.getLastKnownLocation(provider);

		reloadLocation();

		// Acquire a reference to the system Location Manager

		final ListView v = (ListView) findViewById(id.list);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				loc = location;
				if (v.getFirstVisiblePosition() == 0){
					reloadLocation();
				}
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
		locationManager
		.requestLocationUpdates(
				provider, 5 * 1000, 0, 
				locationListener);
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

	public void reloadLocation(){
		if (loc == null){
			return;
		}
		double longitude = loc.getLongitude();
		double latitude = loc.getLatitude();

		//longitude = -106.568586;
		//latitude = 35.075896;
		//Log.v("Location",latitude+" "+longitude);

		Cursor cursor = db.rawQuery("SELECT * FROM `stops` WHERE `lat` > "+(latitude-0.006)+" AND `lat` < "+(latitude+0.006)+" AND `lon` > "+(longitude-0.006)+" AND `lon` < "+(longitude+0.006)+"", null);


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
			String stop_name_long = stop_name;
			while (cursor1.isAfterLast() == false){
				stop_name_long += "("+cursor1.getInt(0)+")";
				cursor1.moveToNext();
			}
			stop_name_long += "("+cursor.getString(5)+")";
			temp[i]  = new Stop(dist,stop_name,stop_name_long,cursor.getInt(3));
			i++;
			cursor.moveToNext();
		}
		Arrays.sort(temp);

		list = temp;

		adapter = new ArrayAdapter<Stop>(this,R.layout.item_layout, list);
		setListAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Log.v("menu", ""+item.getItemId());
		if (item.getItemId() == R.id.www){
			String url = "http://www.abqwtb.com/?utm_source=app&utm_medium=app&utm_campaign=Android%2Bapp";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

}