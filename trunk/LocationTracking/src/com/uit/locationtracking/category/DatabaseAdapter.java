package com.uit.locationtracking.category;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uit.locationtracking.R;
import com.uit.locationtracking.models.Category;


public class DatabaseAdapter {

	public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_IMAGE = "image";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "LocationTrackingDb";
    private static final String DATABASE_TABLE = "Category";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;
    private static final String DATABASE_CREATE =
        "create table Category (id integer primary key autoincrement, "
        + "name text not null, type text not null, image integer not null);";
    
    private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DATABASE_CREATE);
			
			// Init Category
			initCategory(db, "Cafe", "cafe", R.drawable.coffee);
			initCategory(db, "Restaurant", "restaurant", R.drawable.restaurant);
			initCategory(db, "Movie Theater", "movie_theater", R.drawable.movie);
			initCategory(db, "Store", "store", R.drawable.store);
			initCategory(db, "ATM", "atm", R.drawable.atm);
			initCategory(db, "Bank", "bank", R.drawable.bank);
			initCategory(db, "Gas Station", "gas_station", R.drawable.gas_station);
			initCategory(db, "Pharmacy", "pharmacy", R.drawable.pharmacy);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS" + DATABASE_TABLE);
			onCreate(db);
		}
		
		public void initCategory(SQLiteDatabase db, String name, String type, int image) {
			ContentValues values = new ContentValues();
			values.put(KEY_NAME, name);
			values.put(KEY_TYPE, type);
			values.put(KEY_IMAGE, image);
			db.insert(DATABASE_TABLE, null, values);
		}
    }
    
    public DatabaseAdapter open() throws SQLException{
    	mDbHelper = new DatabaseHelper(mCtx);
    	mDb = mDbHelper.getWritableDatabase();
    	return this;
    }
    
    public void close(){
    	mDbHelper.close();
    }
    
    
    public DatabaseAdapter(Context context){
    	mCtx = context;
    }
    
    
    public List<Category> getAllNotes(){
    	Cursor cursor = mDb.query(DATABASE_TABLE, new String[]{KEY_ID, KEY_NAME, KEY_TYPE, KEY_IMAGE}, 
    			null, null, null, null, null);
    	List<Category> list = new ArrayList<Category>();
    	if(cursor.getCount() > 0) {
    		while (cursor.moveToNext()) {
    			Category category = new Category(
    					cursor.getInt(cursor.getColumnIndex(KEY_ID)),
    					cursor.getString(cursor.getColumnIndex(KEY_NAME)),
    					cursor.getString(cursor.getColumnIndex(KEY_TYPE)),
    					cursor.getInt(cursor.getColumnIndex(KEY_IMAGE))
    					);
    			list.add(category);
    		}
    	}
    	return list;
    }
    
    public long insert(Category category){
    	ContentValues insertedValue = new ContentValues();
    	insertedValue.put(KEY_NAME, category.getName());
    	insertedValue.put(KEY_TYPE, category.getType());
    	insertedValue.put(KEY_IMAGE, category.getImage());
    	
    	return mDb.insert(DATABASE_TABLE, null, insertedValue);
    }
    
    public boolean delete(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
    }

}
