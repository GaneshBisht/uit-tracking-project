package com.uit.locationtracking.category;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uit.locationtracking.R;

public class ImageAdapter extends BaseAdapter {
	
	private Context mContext;
	private Integer[] imageList;
	private String[] itemList;

    public ImageAdapter(Context c, Integer[] imageList, String[] itemList, String[] typeList, Integer[] idList) {
        mContext = c;
        this.imageList = imageList;
        this.itemList = itemList;
    }    

    
    public Object getItem(int position) {
        return null;
    }

   
    public long getItemId(int position) {
        return 0;
    }

	
	public int getCount() {
		return imageList.length;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = (View) convertView;
		if (convertView == null) {
			// Inflate the layout
			LayoutInflater li = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = li.inflate(R.layout.category_item, null);
		}
		
		// Add the text
		TextView tv = (TextView) view.findViewById(R.id.grid_item_text);
		String str = itemList[position];
		tv.setText(str);

		// Add the image
		ImageView iv = (ImageView) view.findViewById(R.id.grid_item_image);
		iv.setImageDrawable(resizeImage(mContext,imageList[position], 70, 70));
		return view;
	}
	
	@SuppressWarnings("deprecation")
	public static Drawable resizeImage(Context ctx, int resId, int w, int h) {

		// load the original Bitmap
		Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),
				resId);

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return new BitmapDrawable(resizedBitmap);

	}

}
