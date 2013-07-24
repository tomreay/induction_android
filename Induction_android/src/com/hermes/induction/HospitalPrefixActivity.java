package com.hermes.induction;

import java.util.List;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.*;
import com.hermes.induction.common.*;
import com.parse.*;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class HospitalPrefixActivity extends SherlockActivity {

	protected static final String TAG = HospitalPrefixActivity.class.getSimpleName();

	private EditText editTextHospitalPrefix = null;
	
	private ImageView imageViewHospitalPrefix = null;

	private Button buttonSave = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hospital_prefix);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		editTextHospitalPrefix = (EditText) findViewById(R.id.editTextHospitalPrefix);
		
		imageViewHospitalPrefix = (ImageView) findViewById(R.id.imageViewHospitalPrefix);
		imageViewHospitalPrefix.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onInfo();
			}
		});
		
		buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSave();
			}
		});
		
		init();
		
	}

	private void init() {
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		String prefix = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, "");
		editTextHospitalPrefix.setText(prefix);
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
	
	public void onInfo() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("");
		alertDialog.setMessage("What would you dial before the extension if you were outside the hospital? Don't know? Why not ask switchboard?");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.dismiss();
		   }
		});
		alertDialog.show();
	}
	
	public void onSave() {
		final String strPrefix = editTextHospitalPrefix.getText().toString();
		
		if (strPrefix.length() > 3) {
			SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
			SharedPreferences.Editor editor = prefs.edit();
			final String strHospitalName = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_NAME, null);
			editor.putString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, strPrefix);
			editor.commit();
			
			if (strHospitalName != null) {
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Hospital");
				query.whereEqualTo("name", strHospitalName);
				query.findInBackground(new FindCallback<ParseObject>() {
					@Override
					public void done(List<ParseObject> hospitalList, ParseException e) {
				        if (e == null && hospitalList.size() > 0) {
				        	ParseObject parseObject = hospitalList.get(0);
				        	parseObject.put("prefix", strPrefix);
				        	parseObject.saveInBackground();
				        } else {
				            Log.d(TAG, "Error: " + e.getMessage());
				        }
				    }
				});
			}
			
			setResult(RESULT_OK);
			finish();
		}
		else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Woops");
			alertDialog.setMessage("Please make sure you enter a prefix, and that it is greater than 3 digits long, before saving.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
				   editTextHospitalPrefix.requestFocus();
			   }
			});
			alertDialog.show();
		}
	}

}
