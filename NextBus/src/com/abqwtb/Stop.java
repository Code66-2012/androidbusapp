package com.abqwtb;

import java.util.Set;

public class Stop implements Comparable<Stop>{

	private double distance;
	private String stop_name;
	private int id;
	private String stop_name_long;
	private Set<Integer> routes;
	
	public Stop(double dist, String name, String stop_name_long, int id,Set<Integer> routes){
		this.stop_name = name;
		this.stop_name_long = stop_name_long;
		this.distance = dist;
		this.id = id;
		this.routes = routes;
	}
	
	public int compareTo(Stop another) {
		return (int) ((getDistance() - another.getDistance())*1000000);
	}

	private double getDistance() {
		return distance;
	}
	
	public int getId(){
		return id;
	}

	
	@Override
	public String toString() {
		return stop_name_long;
	}

	public String getShortName() {
		return stop_name;
	}

	public Set<Integer> getRoutes() {
		return routes;
	}

	public void setRoutes(Set<Integer> routes) {
		this.routes = routes;
	}
		

}
