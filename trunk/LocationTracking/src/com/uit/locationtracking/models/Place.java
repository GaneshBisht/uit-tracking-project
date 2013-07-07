package com.uit.locationtracking.models;

public class Place {

	private String m_id;
	private String m_name;
	private double m_lat, m_lng;
	private String m_address;
	private String m_phone;
	private float m_rating;
	private String m_reference;
	private String m_url;

	public Place() {
		m_address = "";
		m_phone = "";
		m_rating = 0;
		m_url = "";
	}

	public String getId() {
		return m_id;
	}

	public void setId(String id) {
		this.m_id = id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public double getLat() {
		return m_lat;
	}

	public void setLat(double lat) {
		this.m_lat = lat;
	}

	public double getLng() {
		return m_lng;
	}

	public void setLng(double lng) {
		this.m_lng = lng;
	}

	public String getAddress() {
		return m_address;
	}

	public void setAddress(String address) {
		this.m_address = address;
	}

	public float getRating() {
		return m_rating;
	}

	public void setRating(float rating) {
		this.m_rating = rating;
	}

	public String getReference() {
		return m_reference;
	}

	public void setReference(String reference) {
		this.m_reference = reference;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		this.m_url = url;
	}

	public String getPhone() {
		return m_phone;
	}

	public void setPhone(String phone) {
		this.m_phone = phone;
	}

}