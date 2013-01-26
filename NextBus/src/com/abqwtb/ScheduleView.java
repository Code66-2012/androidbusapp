package com.abqwtb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ScheduleView extends ListActivity {

	private String[] sched = {"Loading..."};
	private String[] bus_ids;
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position < bus_ids.length){
			Intent i = new Intent(ScheduleView.this, BusView.class);
			i.putExtra("com.abqwtb.bus_id", bus_ids[position]);
			ScheduleView.this.startActivity(i);
		}
		super.onListItemClick(l, v, position, id);
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

				bus_ids = new String[sched.length];
				final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

				for (int i = 0; i < sched.length; i++) {

					String[] data = sched[i].split(";");
					Date dateObj;
					try {
						dateObj = sdf.parse(data[0]);
						DateFormat df = DateFormat.getTimeInstance();
						String late = data[2];
						if (late.equals("-1")){
							late = "";
						}else if(late.equals("0")){
							late = "On Time";
						}else{
							NumberFormat nf = NumberFormat.getInstance();
							nf.setMaximumFractionDigits(1);
							late = "~" +  nf.format(Float.parseFloat(late) / 60) + " Minutes Late";
						}
						sched[i] = df.format(dateObj)+" ("+data[1]+") "+late;
						bus_ids[i] = data[3];

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
			URL url = new URL("http://www.abqwtb.com/android.php?version=4&stop_id="+id);
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArray("sched", sched);
	}

}
