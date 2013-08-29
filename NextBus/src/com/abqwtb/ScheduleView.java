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
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class ScheduleView extends Activity implements OnTouchListener, OnClickListener {

	private String[] sched;
	private ArrayList<Trip> schedule = new ArrayList<Trip>();
	private int id;
	private SQLiteDatabase db;

	final Handler mHandler = new Handler();

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};
	private String name;
	private LinearLayout top;
	private SparseIntArray colors;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle extras = getIntent().getExtras();

		id = extras.getInt("com.abqwtb.stop_id");
		name = extras.getString("com.abqwtb.stop_name");

		DatabaseHelper myDbHelper = new DatabaseHelper(getApplicationContext());

		try {
			myDbHelper.openDataBase();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		db = myDbHelper.getDatabase();
		
		colors = new SparseIntArray();
		Cursor cursor1 = db.rawQuery("SELECT * FROM `routeinfo`",null);
		cursor1.moveToFirst();
		while(cursor1.isAfterLast() == false)
		{
			colors.put(cursor1.getInt(0), Color.parseColor("#"+cursor1.getString(2)));
			cursor1.moveToNext();
		}
		cursor1.close();
		
		
		if (savedInstanceState != null && savedInstanceState.containsKey("schedule")){
			schedule = (ArrayList<Trip>) savedInstanceState.getSerializable("schedule");
			updateUI();
		}else{
			updateUI();
			refreshSched();
		}	


	}

	@Override
	public void onClick(View v) {
		//Log.v("click", schedule.get(v.getId()-2000).getBusId());
		if (schedule.get(v.getId()-2000).getBusId().matches(".*[0-9]{2,4}.*")){
			Intent i = new Intent(ScheduleView.this, BusView.class);
			i.putExtra("com.abqwtb.bus_id", schedule.get(v.getId()-2000).getBusId());
			ScheduleView.this.startActivity(i);
		}
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

	private void refreshSched(){

		Thread t = new Thread() {
			@Override
			public void run() {
				String times = serverQuery(id+"");
				if (times.contains("No More Stops Today")){
					schedule = new ArrayList<Trip>();
					schedule.add(new Trip("No more busses were found for today", "0", "0"));
				}else{
					sched = times.split("\\|");
					final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					schedule = new ArrayList<Trip>();
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
								late = getString(R.string.on_time);
							}else{
								NumberFormat nf = NumberFormat.getInstance();
								nf.setMaximumFractionDigits(1);
								float time_late = Float.parseFloat(late);
								late = "~" +  nf.format(time_late / 60) + " "+getString(R.string.late);

							}

							schedule.add(new Trip(df.format(dateObj)+" "+late,data[3],data[1]));

						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				mHandler.post(mUpdateResults);
			}
		};
		t.start();

	}

	private void updateUI(){
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, schedule);
		//setListAdapter(adapter);
		top = new LinearLayout(this);
		top.setOrientation(LinearLayout.VERTICAL);
		setContentView(top);
		TextView title = new TextView(this);
		title.setTextSize(24f);
		title.setText(name+" "+getString(R.string.schedule));
		top.addView(title);
		for (int i = 0; i < schedule.size(); i++) {
			LinearLayout ll = new LinearLayout(this);
			ll.setId(i+2000);
			TextView r = new TextView(this);
			r.setText(schedule.get(i).getRoute());
			r.setTextColor(Color.WHITE);
			r.setTextSize(20f);
			r.setGravity(Gravity.CENTER);
			r.setBackgroundColor(colors.get(Integer.parseInt(schedule.get(i).getRoute())));
			LayoutParams p = new LinearLayout.LayoutParams(25, 25);
			p.setMargins(5, 0, 10, 0);
			r.setLayoutParams(p);
			ll.addView(r);
			TextView main_time = new TextView(this);
			main_time.setText(schedule.get(i).toString());
			main_time.setTextSize(26f);
			ll.addView(main_time);
			ll.setOnTouchListener(this);
			ll.setOnClickListener(this);
			top.addView(ll);
			View line = new View(this);
			LayoutParams p1 = new LayoutParams(LayoutParams.MATCH_PARENT,1);
			line.setLayoutParams(p1);
			line.setBackgroundColor(Color.LTGRAY);
		}
	}


	private String serverQuery(String id){
		URLConnection conn = null;
		String inputLine = null;
		try {
			Log.v("Main","Loading from url...");
			URL url = new URL("http://www.abqwtb.com/android.php?version=5&stop_id="+id);
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
		outState.putSerializable("sched", schedule);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN){
			v.setBackgroundColor(Color.DKGRAY);
		}else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
			v.setBackgroundColor(Color.WHITE);
		}
		return false;
	}

}
