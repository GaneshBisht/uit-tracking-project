package com.uit.friendstracking.imagesupport;

import java.util.ArrayList;
import java.util.List;


import com.uit.friendstracking.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CropOptionAdapter extends ArrayAdapter<CropOption> {
	

	public CropOptionAdapter(Context context, int textViewResourceId,
			List<CropOption> objects) {
		super(context, textViewResourceId, objects);
		mOptions = objects;
		mInflater	= LayoutInflater.from(context);
	}


	private List<CropOption> mOptions;
	private LayoutInflater mInflater;
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.crop_selector, null);
		
		CropOption item = mOptions.get(position);
		
		if (item != null) {
			((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
			((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);
			
			return convertView;
		}
		
		return null;
	}
}