package com.abqwtb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

public class RTView extends ListActivity {

	final Handler mHandler = new Handler();

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rt);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new String[]{"Loading..."});
		setListAdapter(adapter);
		refreshInfo();

	}

	ArrayList<Bus> times;
	int id;

	private void refreshInfo() {
		// TODO Auto-generated method stub

		Bundle extras = getIntent().getExtras();

		id = extras.getInt("com.abqwtb.stop_id");

		Thread t = new Thread() {
			@Override
			public void run() {
				times = new ArrayList<Bus>();

				String response = serverQuery(id);

				if (response != null){
					JSONObject j = null;
					try {
						j = new JSONObject(response);
					} catch (JSONException e) {
						Log.v("JSON",response);
						e.printStackTrace();
					}
					for (Iterator<?> i = j.keys(); i.hasNext();) {
						String route = (String)i.next();
						try {
							JSONObject busses = j.getJSONObject(route);
							for (Iterator<?> k = busses.keys(); k.hasNext();) {
								String busid = (String) k.next();
								JSONObject bus = busses.getJSONObject(busid);
								times.add(new Bus(0.621371 * bus.getDouble("distance"),Integer.parseInt(busid),Integer.parseInt(route)));
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						Collections.sort(times);

					}
				}else{
					times.add(new Bus());
				}
				mHandler.post(mUpdateResults);
			}
		};
		t.start();



	}

	private void updateUI(){
		ArrayAdapter<Bus> adapter = new ArrayAdapter<Bus>(this,android.R.layout.simple_list_item_1,times);
		setListAdapter(adapter);
	}

	private String serverQuery(int id){
		URLConnection conn = null;
		String inputLine = null;
		try {
			Log.v("Main","Loading from url...");
			URL url = new URL("http://abqwtb.com/distance.php?stop_id="+id);
			conn = url.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							conn.getInputStream()));


			while ((inputLine = in.readLine()) != null){ 
				Log.v("Main",";"+inputLine);
				if (inputLine.equalsIgnoreCase("null")){
					return null;
				}
				return inputLine;
			}
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}

