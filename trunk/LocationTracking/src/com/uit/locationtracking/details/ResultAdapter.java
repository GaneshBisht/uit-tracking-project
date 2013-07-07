package com.uit.locationtracking.details;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.uit.locationtracking.R;
import com.uit.locationtracking.models.Place;

public class ResultAdapter extends ArrayAdapter<Place>{
	
	private List<Place> items;
	private Context mContext;
	
	public ResultAdapter(Context context, int textViewResourceId, List<Place> items) {
		super(context, textViewResourceId, items);
		mContext = context;
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.result_row, null);
        }
        
        Place item = items.get(position);
        if (item != null) {
            TextView placeName = (TextView) v.findViewById(R.id.placeName);
            TextView placeAddress = (TextView) v.findViewById(R.id.placeAddress);
            RatingBar ratingBar = (RatingBar) v.findViewById(R.id.result_rating);
            if (placeName != null) {
            	placeName.setText(item.getName());
        	}
            if(placeAddress != null) {
            	placeAddress.setText(item.getAddress());
            }
            if(ratingBar != null ) {
            	ratingBar.setRating(item.getRating());
            }
        }
        
        
        // Add event click to each result
        final int pos = position;
        v.setOnClickListener(new View.OnClickListener() {
			
			
			public void onClick(View v) {
				//---------- Add click effect here --------
				
				try {
					Bundle bundle = new Bundle();
					bundle.putString("reference", items.get(pos).getReference());
					Intent intent = new Intent(mContext.getApplicationContext(), PlaceDetailActivity.class);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				} catch (Exception ex) {
					ex.getMessage();
					Log.d("EEEEEEEEEEEEEEEE", ex.getMessage());
				}
			}
		});

        return v;
	}
}
