package com.abqwtb;

import java.text.DecimalFormat;

public class Bus implements Comparable<Bus> {
	
	private double distance;
	private int id;
	private int route;
	
	public Bus(){
		id = -1;
	}
	
	public Bus(double distance, int id, int route) {
		this.distance = distance;
		this.id = id;
		this.route = route;
	}

	public int compareTo(Bus arg0) {
		return (int) ((getDistance() - arg0.getDistance())*1000000);
	}

	private double getDistance() {
		return distance;
	}

	public int getId() {
		return id;
	}

	public int getRoute() {
		return route;
	}
	
	@Override
	public String toString() {
		if (id == -1){
			return "Sorry, No Data Available";
		}else{
			DecimalFormat df = new DecimalFormat("#.##");
			return "A Route "+route+" bus "+df.format(distance)+" miles away";
		}
	}

}
