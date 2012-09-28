package com.abqwtb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ScheduleView extends ListActivity {

	private String[] sched = {"Loading..."};
	private int id;

	final Handler mHandler = new Handler();

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule);

		if (savedInstanceState != null && savedInstanceState.containsKey("sched")){
			sched = savedInstanceState.getStringArray("sched");
			updateUI();
		}else{
			updateUI();
			refreshSched();
		}
		
	}



	@Override
	protected void onStart() {
		super.onStart();

	}

	private void refreshSched(){

		Bundle extras = getIntent().getExtras();

		id = extras.getInt("com.abqwtb.stop_id");
		String name = extras.getString("com.abqwtb.stop_name");

		TextView v = (TextView) findViewById(R.id.stop_name);

		v.setText(name+" "+getString(R.string.schedule));
		
		Thread t = new Thread() {
			@Override
			public void run() {
				String times = serverQuery(id+"");
				sched = times.split("\\|");

				final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

				for (int i = 0; i < sched.length; i++) {

					String[] data = sched[i].split(";");
					Date dateObj;
					try {
						dateObj = sdf.parse(data[0]);
						DateFormat df = DateFormat.getTimeInstance();
						sched[i] = df.format(dateObj)+" ("+data[1]+")";

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}

				mHandler.post(mUpdateResults);
			}
		};
		t.start();

	}

	private void updateUI(){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, sched);
		setListAdapter(adapter);

	}


	private String serverQuery(String id){
		URLConnection conn = null;
		String inputLine = null;
		try {
			Log.v("Main","Loading from url...");
			URL url = new URL("http://speedycomputing.net/nextbus/?version=2&stop_id="+id);
			conn = url.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							conn.getInputStream()));


			while ((inputLine = in.readLine()) != null){ 
				Log.v("Main",";"+inputLine);
				return inputLine;
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Server Error";
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray("sched", sched);
	}

}
