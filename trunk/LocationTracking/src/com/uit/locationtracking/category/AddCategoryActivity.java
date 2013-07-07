package com.uit.locationtracking.category;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.uit.locationtracking.R;

public class AddCategoryActivity extends Activity {

	DatabaseAdapter m_dbHelper;
	EditText m_inputName;
	Button m_btnAdd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_new);

		m_inputName = (EditText) findViewById(R.id.category_name);
		m_btnAdd = (Button) findViewById(R.id.add_category);

		m_dbHelper = new DatabaseAdapter(this);
		m_dbHelper.open();

		m_btnAdd.setOnClickListener(addCategoryListener);
	}

	private View.OnClickListener addCategoryListener = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = getIntent();
			String name = intent.getStringExtra("name");
			name = m_inputName.getText().toString();
			intent.putExtra("returnName", name);
			setResult(RESULT_OK, intent);
			finish();
		}
	};
}
