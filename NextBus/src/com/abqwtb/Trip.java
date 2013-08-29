package com.abqwtb;

import java.io.Serializable;

public class Trip implements Serializable{

	private String title;
	private String bus_id;
	private String route;
	
	public Trip(String title, String data, String r){
		this.title = title;
		bus_id = data;
		route = r;
	}
	
	public String getBusId(){
		return bus_id;
	}
	
	public String getRoute(){
		return route;
	}
	
	@Override
	public String toString() {
		return title;
	}
}
