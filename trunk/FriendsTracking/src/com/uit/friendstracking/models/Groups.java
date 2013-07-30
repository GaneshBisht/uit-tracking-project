package com.uit.friendstracking.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Groups {

	ArrayList<Group> listGroup = new ArrayList<Group>();
	private float distance = 0.0f;

	public Groups(float d) {
		distance = d;
	}

	public Iterator<Group> iterator() {
		return listGroup.iterator();
	}

	public void addKNote(KNote e) {
		/*
		 * float minDis=Float.MAX_VALUE; Group group = null; for(int i=0; i<listGroup.size(); i++){ float dis =
		 * e.getPosition().calcDistance(listGroup.get(i).getPosition()); if(group == null || minDis>dis){ minDis=dis; group=listGroup.get(i); } } if(group==null
		 * || minDis>distance){ Group ng = new Group(); ng.addKNote(e); listGroup.add(ng); }else{ group.addKNote(e); }
		 */
		boolean check = false;
		for (int i = 0; i < listGroup.size(); i++) {
			if (listGroup.get(i).getPosition().getLatitude().equals(e.getPosition().getLatitude())
					&& listGroup.get(i).getPosition().getLongitude().equals(e.getPosition().getLongitude())) {
				listGroup.get(i).addKNote(e);
				check = true;
				break;
			}
		}
		if (!check) {
			Group ng = new Group();
			ng.addKNote(e);
			listGroup.add(ng);
		}
	}

	public void addKNotes(List<KNote> list) {
		Iterator<KNote> it = list.iterator();
		while (it.hasNext())
			addKNote(it.next());
	}

	public String toString() {
		String t = listGroup.size() + " groups\n";
		for (int i = 0; i < listGroup.size(); i++) {
			t += listGroup.get(i).toString() + "\n";
		}
		return t;
	}

	public int size() {
		return listGroup.size();
	}

}
