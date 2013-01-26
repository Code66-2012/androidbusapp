package com.abqwtb;

public class Trip {

	private String title;
	private String bus_id;
	
	public Trip(String title, String data){
		this.title = title;
		bus_id = data;
	}
	
	public String getBusId(){
		return bus_id;
	}
	
	@Override
	public String toString() {
		return title;
	}
}
