package com.uit.locationtracking.category;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.uit.locationtracking.R;
import com.uit.locationtracking.details.SearchResultActivity;
import com.uit.locationtracking.models.Category;

public class CategoryActivity extends Activity {

	GridView m_gvCategory;
	DatabaseAdapter m_dbHelper;
	Integer[] m_imageList;
	String[] m_itemList;
	String[] m_typeList;
	Integer[] m_idList;

	final static int MENU_MAP = 1;
	final static int MENU_DIRECTION = 2;
	final static int MENU_SEARCH = 3;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_list);

		m_dbHelper = new DatabaseAdapter(this);
		m_dbHelper.open();
		List<Category> list = m_dbHelper.getAllNotes();
		int length = list.size() + 1;
		m_imageList = new Integer[length];
		m_itemList = new String[length];
		m_typeList = new String[length];
		m_idList = new Integer[length];
		for (int i = 0; i < length - 1; i++) {
			m_imageList[i] = list.get(i).getImage();
			m_itemList[i] = list.get(i).getName();
			m_typeList[i] = list.get(i).getType();
			m_idList[i] = list.get(i).getId();
		}

		// "Add new Category" item
		m_imageList[length - 1] = R.drawable.add;
		m_itemList[length - 1] = "Add New";
		m_idList[length - 1] = 0;

		m_gvCategory = (GridView) findViewById(R.id.category_grid);
		m_gvCategory.setAdapter(new ImageAdapter(this, m_imageList, m_itemList,
				m_typeList, m_idList));

		m_gvCategory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {

				if (m_idList[pos] == 0) {
					startAddActivity();
					return;
				}

				Bundle bundle = new Bundle();
				bundle.putString("categoryName", m_itemList[pos]);
				bundle.putString("categoryType", m_typeList[pos]);
				bundle.putInt("id", m_idList[pos]);
				Intent intent = new Intent(CategoryActivity.this,
						SearchResultActivity.class);
				intent.putExtras(bundle);
				CategoryActivity.this.startActivity(intent);

			}
		});

		m_gvCategory.setOnItemLongClickListener(new OnItemLongClickListener() {

			@SuppressWarnings("deprecation")
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int pos, long id) {
				if (m_idList[pos] == 0)
					return false;
				final int position = pos;
				AlertDialog alertDialog = new AlertDialog.Builder(
						CategoryActivity.this).create();
				alertDialog.setTitle("Delete");
				alertDialog.setMessage("Are you sure want to delete '"
						+ m_itemList[pos] + "'???");
				alertDialog.setIcon(ImageAdapter.resizeImage(
						CategoryActivity.this, R.drawable.delete, 24, 24));
				alertDialog.setButton("Delete",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								deleteCategory(m_idList[position]);
							}
						});
				alertDialog.setButton2("Cancel",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				alertDialog.show();
				return false;
			}
		});
	}

	public void deleteCategory(int id) {
		Intent intent = new Intent(this, CategoryActivity.class);
		m_dbHelper.delete(id);
		startActivityForResult(intent, 1);
		this.finish();
	}

	public void insertCategory(Category category) {
		Intent intent = new Intent(this, CategoryActivity.class);
		m_dbHelper.insert(category);
		startActivityForResult(intent, 1);
		this.finish();
	}

	public void startAddActivity() {
		Intent intent = new Intent(this, AddCategoryActivity.class);
		intent.putExtra("name", new String());
		startActivityForResult(intent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			String name = data.getStringExtra("returnName");
			Category category = new Category(name, "", R.drawable.star);
			insertCategory(category);
		}
	}

	// ---------------- MENU --------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		default:
			break;
		}

		return true;
	}
}