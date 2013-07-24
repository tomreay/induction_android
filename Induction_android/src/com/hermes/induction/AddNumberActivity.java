package com.hermes.induction;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.hermes.induction.common.Constants;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class AddNumberActivity extends SherlockActivity {

	protected static final String TAG = AddNumberActivity.class.getSimpleName();

	private ToggleButton toggleButtonPhone = null;
	
	private ToggleButton toggleButtonBleep = null;

	private EditText editTextNumberName = null;

	private EditText editTextNumberExtension = null;

	private Button buttonSave = null;

	private ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_number);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		toggleButtonPhone = (ToggleButton) findViewById(R.id.toggleButtonPhone);
		toggleButtonPhone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCheckedPhone();
			}
		});
		
		toggleButtonBleep = (ToggleButton) findViewById(R.id.toggleButtonBleep);
		toggleButtonBleep.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCheckedBleep();
			}
		});
		
		editTextNumberName = (EditText) findViewById(R.id.editTextNumberName);
		
		editTextNumberExtension = (EditText) findViewById(R.id.editTextNumberExtension);
		
		buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSave();
			}
		});
		
		onCheckedPhone();		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
	
			default:
				break;
		}
		return true;
	}

	public void onCheckedPhone() {
		toggleButtonPhone.setChecked(true);
		toggleButtonPhone.setTextColor(Color.WHITE);
		
		toggleButtonBleep.setChecked(false);
		toggleButtonBleep.setTextColor(Color.BLACK);
	}

	public void onCheckedBleep() {
		toggleButtonPhone.setChecked(false);
		toggleButtonPhone.setTextColor(Color.BLACK);
		
		toggleButtonBleep.setChecked(true);
		toggleButtonBleep.setTextColor(Color.WHITE);
	}

	public void onSave() {
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		final String strDefaultHospitalId = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_ID, "");
		final String strName = editTextNumberName.getText().toString();
		final String strExtension = editTextNumberExtension.getText().toString();
		
		if (strName.length() < 3 || strExtension.length() < 3) {
			 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Warning");
				alertDialog.setMessage("Please make sure both the name and extension are 3 or more letters/numbers");
				alertDialog.setButton("Got it", new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int which) {
					   dialog.dismiss();
				   }
				});
				alertDialog.show();
		}
		else {
			ParseObject parseObject = new ParseObject("Number");
			parseObject.put("extension", strExtension);
			parseObject.put("name", strName);
			parseObject.put("parent", ParseObject.createWithoutData("Hospital", strDefaultHospitalId));
			if (toggleButtonBleep.isChecked()) {
				parseObject.put("type", "bleep");
			}
			
			progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.Saving____));
			progressDialog.setCancelable(true);
			
			parseObject.saveInBackground(new SaveCallback() {
				@Override
				public void done(ParseException e) {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
						if (e == null) {
							Log.i(TAG, "Successed to add number: " + strName);
							Intent data = getIntent();
							data.putExtra("done", true);
							setResult(RESULT_OK, data);
							finish();
						}
						else {
							Log.e(TAG, "Failed to add number: " + e.getMessage());
							Intent data = getIntent();
							data.putExtra("done", false);
							setResult(RESULT_OK, data);
							finish();
						}
					}
				}
			});
		}
	}

}
