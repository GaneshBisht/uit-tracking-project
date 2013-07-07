package com.uit.locationtracking.models;

public class Route {
	String distance, duration, instruction;
	public Route() {
		
	}
	public Route(String distance, String duration, String instruction) {
		super();
		this.distance = distance;
		this.duration = duration;
		this.instruction = instruction;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getInstruction() {
		return instruction;
	}
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	
	
}
