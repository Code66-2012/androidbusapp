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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class ScheduleView extends ActionBarActivity implements OnTouchListener, OnClickListener {

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
				}else if (!times.contains("|")){
					Looper.prepare();
					AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleView.this);
					builder.setTitle("No Data");
					builder.setMessage("The server didn't respond, you may be offline.");
					builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finish();
						}
					});
					builder.show();
					Looper.loop();
				}
				else{
					sched = times.split("\\|");
					final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					schedule = new ArrayList<Trip>();
					for (int i = 0; i < sched.length; i++) {
						String[] data = sched[i].split(";");
						Date dateObj;
						Trip t = null;
						try {
							dateObj = sdf.parse(data[0]);
							DateFormat df = DateFormat.getTimeInstance();
							t = new Trip(df.format(dateObj),data[3],data[1]);
						} catch (ParseException e) {
							e.printStackTrace();
						}
						
						String late = "-1";
						if (data.length > 1){
							late = data[2];
						}
						
						if (late.equals("-1")){
							late = "";
						}else if(late.equals("0")){
							late = getString(R.string.on_time);
						}else if(Float.parseFloat(late) > 0){
							NumberFormat nf = NumberFormat.getInstance();
							nf.setMaximumFractionDigits(1);
							float time_late = Float.parseFloat(late);
							late = nf.format(time_late / 60) + " "+getString(R.string.late);
							t.setStatus((short) 2);
						}else{
							NumberFormat nf = NumberFormat.getInstance();
							nf.setMaximumFractionDigits(1);
							float time_late = Float.parseFloat(late);
							late = nf.format(Math.abs(time_late) / 60) + " "+getString(R.string.early);
							t.setStatus((short) 3);
						}
						t.append(" "+late);
						schedule.add(t);
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
		title.setTextSize(22f);
		title.setText(name+" "+getString(R.string.schedule));
		top.addView(title);
		for (int i = 0; i < schedule.size(); i++) {
			LinearLayout ll = new LinearLayout(this);
			ll.setId(i+2000);
			TextView r = new TextView(this);
			r.setText(schedule.get(i).getRoute());
			r.setPadding(dpToPx(3), 0, dpToPx(3), 0);
			r.setTextColor(Color.WHITE);
			r.setTextSize(20f);
			r.setGravity(Gravity.CENTER);
			r.setBackgroundColor(colors.get(Integer.parseInt(schedule.get(i).getRoute())));
			LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			p.setMargins(dpToPx(3), 0, dpToPx(5), 0);
			r.setLayoutParams(p);
			ll.addView(r);
			TextView main_time = new TextView(this);
			main_time.setText(schedule.get(i).toString());
			main_time.setTextSize(24f);
			if (schedule.get(i).getStatus() == 3){
				main_time.setBackgroundColor(Color.YELLOW);
			}else if(schedule.get(i).getStatus() == 2){
				main_time.setBackgroundColor(Color.RED);
			}
			ll.addView(main_time);
			ll.setOnTouchListener(this);
			ll.setOnClickListener(this);
			top.addView(ll);
			View line = new View(this);
			LayoutParams p1 = new LayoutParams(LayoutParams.MATCH_PARENT,1);
			line.setLayoutParams(p1);
			line.setBackgroundColor(Color.LTGRAY);
			top.addView(line);
		}
	}


	private String serverQuery(String id){
		URLConnection conn = null;
		String inputLine = null;
		try {
			Log.v("Main","Loading from url...");
			URL url = new URL("http://www.abqwtb.com/android.php?version=6&stop_id="+id);
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

	public int dpToPx(int dp) {
		DisplayMetrics displayMetrics = getBaseContext().getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
		return px;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schedule_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	long last_refreshed = 0;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_refresh_schedule){
			//Log.i("time",(System.nanoTime() - last_refreshed)+"");
			if (System.nanoTime() - last_refreshed > (30 * 1000000000l)){
				last_refreshed = System.nanoTime();
				refreshSched();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
