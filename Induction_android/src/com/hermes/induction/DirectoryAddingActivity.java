package com.hermes.induction;

import com.hermes.induction.common.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DirectoryAddingActivity extends Activity {

	private Button buttonAddNumber = null;
	
	private Button buttonPhotographList = null;

	private Button buttonEmailNumber = null;

	private Button buttonCancel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directory_adding);
		
		buttonAddNumber = (Button) findViewById(R.id.buttonAddNumber);
		buttonAddNumber.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onAddNumber();
			}
		});
		
		buttonPhotographList = (Button) findViewById(R.id.buttonPhotographList);
		buttonPhotographList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPhotographList();
			}
		});
		
		buttonEmailNumber = (Button) findViewById(R.id.buttonEmailNumber);
		buttonEmailNumber.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEmailNumber();
			}
		});
		
		buttonCancel = (Button) findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCancel();
			}
		});
		
	}

	protected void onAddNumber() {
		Intent data = getIntent();
		data.putExtra("option", Constants.OPTION_DIRECTORY_ADD_NUMBER);
		setResult(RESULT_OK, data);
		finish();
	}

	protected void onPhotographList() {
		Intent data = getIntent();
		data.putExtra("option", Constants.OPTION_DIRECTORY_PHOTOGRAPH_LIST);
		setResult(RESULT_OK, data);
		finish();
	}

	protected void onEmailNumber() {
		Intent data = getIntent();
		data.putExtra("option", Constants.OPTION_DIRECTORY_EMAIL_NUMBER);
		setResult(RESULT_OK, data);
		finish();
	}

	protected void onCancel() {
		finish();
	}

}
