package com.abqwtb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import com.google.analytics.tracking.android.EasyTracker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BusView extends Activity implements OnClickListener{

	Bitmap bmp;
	String[] info;
	Button refresh;
	
	final Handler mHandler = new Handler();
	
	final Runnable mUpdateRefresh = new Runnable() {
		@Override
		public void run() {
			refresh.setEnabled(true);
			refresh.setText(R.string.refresh);
		}
	};
	
	final Runnable mUpdateResults = new Runnable() {
		@Override
		public void run() {
			updateUI();
		}
	};
	private String bus_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bus);
		bus_id = getIntent().getExtras().getString("com.abqwtb.bus_id");
		refresh = (Button) findViewById(R.id.bus_refresh);
		refresh.setEnabled(false);
		refresh.setOnClickListener(this);
		new UpdateThread().start();

	}

	protected void updateUI() {
		ImageView map = (ImageView) findViewById(R.id.busMap);
		map.setImageBitmap(bmp);
		TextView v = (TextView) findViewById(R.id.bus_id);
		v.setText(bus_id + " "+getString(R.string.next_stop)+":  "+info[0]);
	}

	public static Bitmap getBitmapMap(String src) {
		try {
			Log.v("src",src);
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			Log.v("Bitmap","returned");
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("Exception",e.getMessage());
			return null;
		}
	}

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

	@Override
	public void onClick(View v) {
		refresh.setEnabled(false);
		refresh.setText(R.string.wrefresh);
		new UpdateThread().start();		
	}
	
	class UpdateThread extends Thread{
		public void run() {
			info = serverQuery(bus_id).split(":");
			Display display = getWindowManager().getDefaultDisplay();
			bmp = getBitmapMap("http://www.mapquestapi.com/staticmap/v3/getmap?key=Fmjtd%7Cluub290yn9%2Cbx%3Do5-96zs9y&center="+info[1]+","+info[2]+"&zoom=11&size=300,200&type=map&imagetype=jpeg&pois=pcenter,"+info[1]+","+info[2]+",5,0");
			mHandler.post(mUpdateResults);
			mHandler.postDelayed(mUpdateRefresh, 60000);
		}
	};

}
