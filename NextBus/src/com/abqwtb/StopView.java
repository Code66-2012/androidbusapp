package com.abqwtb;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class StopView extends TabActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.stop);
		 
        TabHost tabHost = getTabHost();
   
 
        // Tab
        TabSpec sched = tabHost.newTabSpec(getString(R.string.schedule));
        // setting Title and Icon for the Tab
        sched.setIndicator(getString(R.string.schedule));
        Intent i = new Intent(this, ScheduleView.class);
        Bundle extras = getIntent().getExtras();
		i.putExtra("com.abqwtb.stop_id",extras.getInt("com.abqwtb.stop_id"));
		i.putExtra("com.abqwtb.stop_name",extras.getString("com.abqwtb.stop_name"));
        sched.setContent(i);
        
        //Tab
        TabSpec rt = tabHost.newTabSpec(getString(R.string.rt));
        // setting Title and Icon for the Tab
        rt.setIndicator(getString(R.string.rt));
        Intent i1 = new Intent(this, RTView.class);
        
        i1.putExtra("com.abqwtb.stop_id",extras.getInt("com.abqwtb.stop_id"));
		i1.putExtra("com.abqwtb.stop_name",extras.getString("com.abqwtb.stop_name"));
		rt.setContent(i1);
        
        tabHost.addTab(sched);
        tabHost.addTab(rt);
	}

}
