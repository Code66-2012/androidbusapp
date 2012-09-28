package com.abqwtb;

public class Stop implements Comparable<Stop>{

	private double distance;
	private String stop_name;
	private int id;
	
	public Stop(double dist, String name, int id){
		this.stop_name = name;
		this.distance = dist;
		this.id = id;
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
		return stop_name;
	}
		

}
