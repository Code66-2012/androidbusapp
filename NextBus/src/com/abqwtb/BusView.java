package com.abqwtb;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class BusView extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bus);
		Bundle extras = getIntent().getExtras();
		String bus_id = extras.getString("com.abqwtb.bus_id");
		
		TextView v = (TextView) findViewById(R.id.bus_id);
		
		v.setText(bus_id);
		
	}

}
