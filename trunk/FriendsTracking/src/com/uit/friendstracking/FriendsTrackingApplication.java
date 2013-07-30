package com.uit.friendstracking;

import java.util.List;

import android.app.Application;

import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;

public class FriendsTrackingApplication extends Application {
	private List<GraphUser> m_selectedUsers;
	private GraphPlace m_selectedPlace;

	public List<GraphUser> getSelectedUsers() {
		return m_selectedUsers;
	}

	public void setSelectedUsers(List<GraphUser> users) {
		m_selectedUsers = users;
	}

	public GraphPlace getSelectedPlace() {
		return m_selectedPlace;
	}

	public void setSelectedPlace(GraphPlace place) {
		this.m_selectedPlace = place;
	}
}
