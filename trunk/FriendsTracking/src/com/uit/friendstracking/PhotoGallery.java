package com.uit.friendstracking;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.uit.friendstracking.models.Group;
import com.uit.friendstracking.models.KNote;
import com.uit.friendstracking.models.KPhoto;

public class PhotoGallery extends Activity {

	private Gallery myGallery;
	private ImageView image;
	private TextView et_comment;
	public static Group selectedGroup;
	public static List<KNote> listNotes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		listNotes = new ArrayList<KNote>();
		
		FriendsTrackingApplication app = (FriendsTrackingApplication) getApplication();
		listNotes.addAll(app.getListNotes());
		
		image = (ImageView) findViewById(R.id.gallery_imageview);
		et_comment = (TextView) findViewById(R.id.gallery_notes);
		
		if (listNotes.size() > 0) {
			if (listNotes.get(0).getHasPhoto()) {
				image.setImageDrawable(((KPhoto) listNotes.get(0).getPhoto()).getBitMap());
				et_comment.setText(listNotes.get(0).getNote());
			} else {
				et_comment.setText(listNotes.get(0).getNote());
			}
		}
		
		myGallery = (Gallery) findViewById(R.id.photoGallery);
		myGallery.setAdapter(new ImageAdapter(this));

		myGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView parent, View v, int position, long id) {

				if (listNotes.get(position).getHasPhoto()) {
					image.setImageDrawable(((KPhoto) listNotes.get(position).getPhoto()).getBitMap());
					et_comment.setText(listNotes.get(position).getNote());
				} else {
					image.setImageBitmap(null);
					et_comment.setText(listNotes.get(position).getNote());
				}

			}

			@Override
			public void onNothingSelected(AdapterView arg0) {

			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_photo_gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		default:
			break;
		}
		return true;
	}

	public class ImageAdapter extends BaseAdapter {
		int itemBackground;
		private Context cont;

		public ImageAdapter(Context c) {
			cont = c;
			TypedArray typArray = obtainStyledAttributes(R.styleable.PhotoGallery);
			itemBackground = typArray.getResourceId(R.styleable.PhotoGallery_android_galleryItemBackground, 0);
			typArray.recycle();
		}

		public int getCount() {
			return listNotes.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// View the image position in the gallery
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView image = new ImageView(cont);

			// If the note have image
			if (listNotes.get(position).getHasPhoto()) {
				image.setImageDrawable(((KPhoto) listNotes.get(position).getPhoto()).getBitMap());
			} else {
				// If the note do not have image we show a already defined image to notes
				image.setImageResource(R.drawable.notes);

			}
			// Sets gallery parameters
			image.setLayoutParams(new Gallery.LayoutParams(150, 100));
			image.setScaleType(ImageView.ScaleType.FIT_XY);
			image.setBackgroundResource(itemBackground);

			return image;
		}
	}

}