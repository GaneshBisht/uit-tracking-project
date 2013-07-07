package com.uit.locationtracking.map;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uit.locationtracking.R;
import com.uit.locationtracking.models.Route;

public class DirectionAdapter extends ArrayAdapter<Route> {

	private ArrayList<Route> items;

	public DirectionAdapter(Context context, int textViewResourceId,
			ArrayList<Route> items) {
		super(context, textViewResourceId, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.direction_row, null);
		}

		Route item = items.get(position);
		if (item != null) {
			TextView txtGuide = (TextView) v.findViewById(R.id.txtInstruction);
			ImageView imgGuide = (ImageView) v.findViewById(R.id.imgGuide);

			if (txtGuide != null) {
				if (item.getInstruction().startsWith("Head")) {
					imgGuide.setImageResource(R.drawable.forward);
				} else if (item.getInstruction().startsWith(
						"Turn \u003cb\u003eleft")) {
					imgGuide.setImageResource(R.drawable.turn_left);
				} else if (item.getInstruction().startsWith(
						"Turn \u003cb\u003eright")) {
					imgGuide.setImageResource(R.drawable.turn_right);
				} else {
					imgGuide.setImageResource(R.drawable.forward);
				}

				txtGuide.setText(Html.fromHtml(item.getInstruction())
						+ " About : " + item.getDistance() + " - "
						+ item.getDuration());
			}
		}

		return v;
	}

}
