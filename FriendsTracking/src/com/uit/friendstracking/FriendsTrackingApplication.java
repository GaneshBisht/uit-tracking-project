package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.uit.friendstracking.models.KNote;

public class FriendsTrackingApplication extends Application {
	private List<GraphUser> m_selectedUsers;
	private GraphPlace m_selectedPlace;
	private List<KNote> m_listNotes = new ArrayList<KNote>();

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

	public void putNotes(List<KNote> listNotes) {
		m_listNotes.addAll(listNotes);
	}

	public void clearNotes() {
		m_listNotes.clear();
	}
	
	public List<KNote> getListNotes() {
		return m_listNotes;
	}
}
