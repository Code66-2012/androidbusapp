package com.abqwtb;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.analytics.tracking.android.EasyTracker;

import android.R.id;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Abqwtb extends Activity  {

	LocationManager locationManager;
	LocationListener locationListener;

	Location loc;

	static Stop[] list = new Stop[]{new Stop(0,"","Please Wait ...",0,null)};
	private SQLiteDatabase db;
	ArrayAdapter<Stop> adapter;
	private String provider;
	private LinearLayout l;

//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		if (list[position].getId() >0){
//			Log.v("Main","StopId:"+list[position].getId());
//			Intent i = new Intent(Abqwtb.this, ScheduleView.class);
//
//			i.putExtra("com.abqwtb.stop_id",list[position].getId());
//			i.putExtra("com.abqwtb.stop_name",list[position].getShortName());
//
//			Abqwtb.this.startActivity(i);
//		}
//		super.onListItemClick(l, v, position, id);
//
//	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		loadDatabase();

		adapter = new ArrayAdapter<Stop>(this,R.layout.item_layout,R.id.stop_text, list);
		//setListAdapter(adapter);

		String context = Context.LOCATION_SERVICE; 
		locationManager = (LocationManager) this.getSystemService(context); 
		provider = LocationManager.NETWORK_PROVIDER; 

		loc = locationManager.getLastKnownLocation(provider);
		l = (LinearLayout) findViewById(R.id.stops_layout);
		reloadLocation();

		// Acquire a reference to the system Location Manager

		//final ListView v = (ListView) findViewById(id.list);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				loc = location;
//				if (v.getFirstVisiblePosition() == 0){
					reloadLocation();
//				}
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
		EasyTracker.getInstance().activityStart(this);
		// Register the listener with the Location Manager to receive location updates
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 0,locationListener);
		}else{
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5*1000, 0, locationListener);
		}
	}

	@Override
	protected void onPause(){
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private void loadDatabase() {
		DatabaseHelper myDbHelper = new DatabaseHelper(getApplicationContext());

		try {
			myDbHelper.openDataBase();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		db = myDbHelper.getDatabase();
	}
	
	@Override
	protected void onDestroy() {
		db.close();
		super.onDestroy();
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
			double x_dist = latitude - cursor.getDouble(1);
			double y_dist = longitude - cursor.getDouble(2);
			double dist = Math.sqrt((x_dist*x_dist)+(y_dist*y_dist));
			Cursor cursor1 = db.rawQuery("SELECT * FROM `routes` WHERE `stop` = "+cursor.getInt(0), null);
			String stop_name = cursor.getString(3);
			cursor1.moveToFirst();
			String stop_name_long = stop_name;
			Set<Integer> routes = new HashSet<Integer>();
			while (cursor1.isAfterLast() == false){
				routes.add(cursor1.getInt(1));
				cursor1.moveToNext();
			}
			stop_name_long += "("+cursor.getString(4)+")";
			temp[i]  = new Stop(dist,stop_name,stop_name_long,cursor.getInt(0),routes);
			i++;
			cursor.moveToNext();
		}
		Arrays.sort(temp);

		list = temp;
		
		SparseIntArray colors = new SparseIntArray();
		Cursor cursor1 = db.rawQuery("SELECT * FROM `routeinfo`",null);
		cursor1.moveToFirst();
		while(cursor1.isAfterLast() == false)
		{
			colors.put(cursor1.getInt(0), Color.parseColor("#"+cursor1.getString(2)));
			cursor1.moveToNext();
		}
		for (int j = 0; j < l.getChildCount(); j++) {
			View child = l.getChildAt(j);
			if (child.getId() != R.id.stops_near)l.removeView(child);
		}
		
		for (int j = 0; j < list.length; j++) {
			RelativeLayout rl = new RelativeLayout(this);
			TextView t = new TextView(this);
			t.setTextSize(24.00f);
			t.setId(j+100);
			t.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			t.setText(list[j].toString());
			rl.addView(t);
			int k = 0;
			for (Integer route : list[j].getRoutes()) {
				TextView r = new TextView(this);
				r.setId(500+(100*j)+k);
				r.setText(route+"");
				r.setBackgroundColor(colors.get(route));
				RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				p.addRule(RelativeLayout.BELOW, t.getId());
				if (k>0)p.addRule(RelativeLayout.RIGHT_OF, 500+(100*j)+(k-1));
				r.setLayoutParams(p);
				rl.addView(r);
				k++;
			}
			rl.setId(j+300);
			rl.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			l.addView(rl,j+1);
		}
		
		//adapter = new ArrayAdapter<Stop>(this,R.layout.item_layout,R.id.stop_text, list);
		//setListAdapter(adapter);

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
