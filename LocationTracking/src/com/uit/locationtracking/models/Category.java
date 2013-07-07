package com.uit.locationtracking.models;

public class Category {
	
	private int m_id;
	private String m_name;
	private String m_type;
	private int m_image;
	
	public Category(int id, String name, String type, int image) {
		this.m_id = id;
		this.m_name = name;
		this.m_type = type;
		this.m_image = image;
	}
	
	public Category(String name, String type, int image) {
		this.m_name = name;
		this.m_type = type;
		this.m_image = image;
	}
	
	public Category() {
		
	}

	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		this.m_id = id;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		this.m_type = type;
	}

	public int getImage() {
		return m_image;
	}

	public void setImage(int image) {
		this.m_image = image;
	}
}
