package com.uit.friendstracking;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.MarshalBase64;

import com.uit.friendstracking.imagesupport.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.uit.friendstracking.models.KUserInfo;
import com.uit.friendstracking.webservices.ToServer;

public class UserInformation extends Activity implements OnClickListener {

	private Uri mImageCaptureUri;
	private ImageView mImageView;
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	
	private Button btpickImage;
	private Button bSave;
	private Button bCancel;

	private EditText et_name, et_surname, et_address, et_email, et_country,
			et_password, et_phone;

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modifyuser);
		bSave = (Button) findViewById(R.id.btupdate);
		bSave.setOnClickListener(this);
		bCancel = (Button) findViewById(R.id.btCancel);
		bCancel.setOnClickListener(this);

		et_name = (EditText) findViewById(R.id.tbName);
		et_surname = (EditText) findViewById(R.id.tbSurName);
		et_address = (EditText) findViewById(R.id.tbAddress);
		et_country = (EditText) findViewById(R.id.tbCountry);
		et_email = (EditText) findViewById(R.id.tbEmail);
		et_password = (EditText) findViewById(R.id.tbPass);
		et_phone = (EditText) findViewById(R.id.tbPhone);
		KUserInfo user;
		try {
			// Read the old values
			user = new GetUserAsyncTask().execute().get();
			et_name.setText(user.getName());
			et_surname.setText(user.getSurname());
			et_address.setText(user.getAddress());
			et_country.setText(user.getCountry());
			et_email.setText(user.getEmail());
			et_phone.setText(String.valueOf(user.getPhone()));

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Failed to try to load user's information.",
					Toast.LENGTH_LONG).show();

		}
		
		final String [] items			= new String [] {"Take from camera", "Select from gallery"};				
		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
		
		builder.setTitle("Select Image");
		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int item ) { //pick from camera
				if (item == 0) {
					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					
					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
									   "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

					try {
						intent.putExtra("return-data", true);
						
						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				} else { //pick from file
					Intent intent = new Intent();
					
	                intent.setType("image/*");
	                intent.setAction(Intent.ACTION_GET_CONTENT);
	                
	                startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
				}
			}
		} );
		
		final AlertDialog dialog = builder.create();
		
		Button button 	= (Button) findViewById(R.id.btpickimage);
		mImageView		= (ImageView) findViewById(R.id.iv_photo);
		
		button.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				dialog.show();
			}
		});

	};
	
	public Bitmap loadBitmap(String url)
	{
	    Bitmap bm = null;
	    InputStream is = null;
	    BufferedInputStream bis = null;
	    try 
	    {
	        URLConnection conn = new URL(url).openConnection();
	        conn.connect();
	        is = conn.getInputStream();
	        bis = new BufferedInputStream(is, 8192);
	        bm = BitmapFactory.decodeStream(bis);
	    }
	    catch (Exception e) 
	    {
	        e.printStackTrace();
	    }
	    finally {
	        if (bis != null) 
	        {
	            try 
	            {
	                bis.close();
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	        if (is != null) 
	        {
	            try 
	            {
	                is.close();
	            }
	            catch (IOException e) 
	            {
	                e.printStackTrace();
	            }
	        }
	    }
	    return bm;
	}
	@Override
	public void onClick(View v) {
		BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
		Bitmap bitmap = drawable.getBitmap();
		ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream);
		byte[] b = byteArrayBitmapStream.toByteArray();
		//then simple encoding to base64 and off to server
		//encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);
	}
	
	 @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    if (resultCode != RESULT_OK) return;
		   
		    switch (requestCode) {
			    case PICK_FROM_CAMERA:
			    	doCrop();
			    	break;
			    	
			    case PICK_FROM_FILE: 
			    	mImageCaptureUri = data.getData();
			    	doCrop();
			    	break;	    	
		    
			    case CROP_FROM_CAMERA:	    	
			        Bundle extras = data.getExtras();
		
			        if (extras != null) {	        	
			            Bitmap photo = extras.getParcelable("data");
			            
			            mImageView.setImageBitmap(photo);
			        }
		
			        File f = new File(mImageCaptureUri.getPath());            
			        
			        if (f.exists()) f.delete();
		
			        break;

		    }
		}
	    private void doCrop() {
			final ArrayList<CropOption> cropOptions = new ArrayList<com.uit.friendstracking.imagesupport.CropOption>();
	    	
	    	Intent intent = new Intent("com.android.camera.action.CROP");
	        intent.setType("image/*");
	        
	        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
	        
	        int size = list.size();
	        
	        if (size == 0) {	        
	        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
	        	
	            return;
	        } else {
	        	intent.setData(mImageCaptureUri);
	            
	            intent.putExtra("outputX", 200);
	            intent.putExtra("outputY", 200);
	            intent.putExtra("aspectX", 1);
	            intent.putExtra("aspectY", 1);
	            intent.putExtra("scale", true);
	            intent.putExtra("return-data", true);
	            
	        	if (size == 1) {
	        		Intent i 		= new Intent(intent);
		        	ResolveInfo res	= list.get(0);
		        	
		        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
		        	
		        	startActivityForResult(i, CROP_FROM_CAMERA);
	        	} else {
			        for (ResolveInfo res : list) {
			        	final CropOption co = new CropOption();
			        	
			        	co.title 	= getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
			        	co.icon		= getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
			        	co.appIntent= new Intent(intent);
			        	
			        	co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			        	
			            cropOptions.add(co);
			        }
		        
			        CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(),0, cropOptions);
			        
			        AlertDialog.Builder builder = new AlertDialog.Builder(this);
			        builder.setTitle("Choose Crop App");
			        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
			            public void onClick( DialogInterface dialog, int item ) {
			                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
			            }
			        });
		        
			        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
			            @Override
			            public void onCancel( DialogInterface dialog ) {
			               
			                if (mImageCaptureUri != null ) {
			                    getContentResolver().delete(mImageCaptureUri, null, null );
			                    mImageCaptureUri = null;
			                }
			            }
			        } );
			        
			        AlertDialog alert = builder.create();
			        
			        alert.show();
	        	}
	        }
		}
	
	private class GetUserAsyncTask extends AsyncTask<Void, Void, KUserInfo> {

		private ProgressDialog m_progressDialog;

		@Override
		protected void onPostExecute(KUserInfo result) {
			m_progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			m_progressDialog = ProgressDialog.show(UserInformation.this, "Loading...",
					"Data is Loading...");
		}

		@Override
		protected KUserInfo doInBackground(Void... params) {
			try {
				return ToServer.myUser();
			} catch (Exception e) {
				return null;
			}
		}
	}

}

