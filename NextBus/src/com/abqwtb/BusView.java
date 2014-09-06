package com.abqwtb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class BusView extends FragmentActivity{

	//Bitmap bmp;
	String[] info;
	//Button refresh;

	private GoogleMap map;

	final Handler mHandler = new Handler();

	final Runnable mUpdateRefresh = new Runnable() {
		@Override
		public void run() {
			new UpdateThread().start();
		}
	};

	final Runnable mUpdateResults = new Runnable() {
		@Override
		public void run() {
			updateUI();
		}
	};

	private String bus_id;
	private Marker mark;

	//private DefaultItemizedOverlay poiOverlay;

	//private AnnotationView annotation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.busmap)).getMap();
		//map.setBuiltInZoomControls(true);
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(35.078581, -106.6764191), 14);
		map.moveCamera(update);
		
		mark = map.addMarker(new MarkerOptions()
        .position(new LatLng(35.078581, -106.6764191))
        .title("Hello world"));
		bus_id = getIntent().getExtras().getString("com.abqwtb.bus_id");

		//Drawable icon = getResources().getDrawable(R.drawable.location_marker);
		//poiOverlay = new DefaultItemizedOverlay(icon);

		//annotation = new AnnotationView(map);
		//annotation.tryToKeepBubbleOnScreen(true);
		//map.getController().setZoom(14);
		//map.getOverlays().add(poiOverlay);
		//refresh = (Button) findViewById(R.id.bus_refresh);
		//refresh.setEnabled(false);
		//refresh.setOnClickListener(this);
		//new UpdateThread().start();

	}

	protected void updateUI() {
		if (info.length > 1){
			LatLng pos = new LatLng(Double.parseDouble(info[1]), Double.parseDouble(info[2]));
			CameraUpdate update = CameraUpdateFactory.newLatLng(pos);
			mark.setPosition(pos);
			map.animateCamera(update);
			
			//annotation.showAnnotationView(busoverlay);

		}
		//		ImageView map = (ImageView) findViewById(R.id.busMap);
		//		map.setImageBitmap(bmp);
		//TextView v = (TextView) findViewById(R.id.bus_id);
		//v.setText(bus_id + " "+getString(R.string.next_stop)+":  "+info[0]);
	}

	//	public static Bitmap getBitmapMap(String src) {
	//		try {
	//			Log.v("src",src);
	//			URL url = new URL(src);
	//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	//			connection.setDoInput(true);
	//			connection.connect();
	//			InputStream input = connection.getInputStream();
	//			Bitmap myBitmap = BitmapFactory.decodeStream(input);
	//			Log.v("Bitmap","returned");
	//			return myBitmap;
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//			Log.e("Exception",e.getMessage());
	//			return null;
	//		}
	//	}

	private String serverQuery(String id){
		URLConnection conn = null;
		String inputLine = null;
		try {
			Log.v("Main","Loading from url...");
			URL url = new URL("http://www.abqwtb.com/android_bus.php?bus_id="+id);
			conn = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((inputLine = in.readLine()) != null){ 
				Log.v("Main",";"+inputLine);
				return inputLine;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Server Error";
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	//	@Override
	//	public void onClick(View v) {
	//		//		refresh.setEnabled(false);
	//		//		refresh.setText(R.string.wrefresh);
	//		new UpdateThread().start();		
	//	}

	class UpdateThread extends Thread{
		public void run() {
			info = serverQuery(bus_id).split(":");
			//Display display = getWindowManager().getDefaultDisplay();
			//			bmp = getBitmapMap("http://www.mapquestapi.com/staticmap/v3/getmap?key=Fmjtd%7Cluub290yn9%2Cbx%3Do5-96zs9y&center="+info[1]+","+info[2]+"&zoom=11&size=300,200&type=map&imagetype=jpeg&pois=pcenter,"+info[1]+","+info[2]+",5,0");
			mHandler.post(mUpdateResults);
			mHandler.postDelayed(mUpdateRefresh, 60000);
		}
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacksAndMessages(null);
		super.onPause();
	}

	@Override
	protected void onResume() {
		new UpdateThread().start();
		super.onResume();
	}

}
