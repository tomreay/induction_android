package com.hermes.induction;

import br.com.dina.ui.widget.*;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.MenuItem;
import com.hermes.induction.view.NotificationView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SettingsActivity extends SherlockActivity {

	private static final int ACTIVITY_CHANGE_DEFAULT_HOSPITAL = 1;

	private static final int ACTIVITY_HOSPITAL_PREFIX = 2;

	private UITableView tableViewSettings1 = null;

	private UITableView tableViewSettings2 = null;

	private NotificationView notificationView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		notificationView = (NotificationView) findViewById(R.id.notificationView);
		
		tableViewSettings1 = (UITableView) findViewById(R.id.tableViewSettings1);
		tableViewSettings1.setClickListener(new UITableView.ClickListener() {
			@Override
			public void onClick(int index) {
				switch (index) {
					case 0:
						onChangeDefaultHospital();
						break;

					case 1:
						onSetHospitalPrefix();
						break;
	
					default:
						break;
				}
			}
		});
		tableViewSettings1.addBasicItem(getResources().getString(R.string.Change_default_hospital));
		tableViewSettings1.addBasicItem(getResources().getString(R.string.Set_hospital_prefix));
		tableViewSettings1.commit();
		
		tableViewSettings2 = (UITableView) findViewById(R.id.tableViewSettings2);
		tableViewSettings2.setClickListener(new UITableView.ClickListener() {
			@Override
			public void onClick(int index) {
				switch (index) {
					case 0:
						onSendUsFeedback();
						break;

					case 1:
						onShareInduction();
						break;
	
					default:
						break;
				}
			}
		});
		tableViewSettings2.addBasicItem(getResources().getString(R.string.Send_us_feedback));
		tableViewSettings2.addBasicItem(getResources().getString(R.string.Share_Induction));
		tableViewSettings2.commit();
		
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

	public void onChangeDefaultHospital() {
		Intent intent = new Intent(this, ChangeDefaultHospitalActivity.class);
		startActivityForResult(intent, ACTIVITY_CHANGE_DEFAULT_HOSPITAL);
	}

	public void onSetHospitalPrefix() {
		Intent intent = new Intent(this, HospitalPrefixActivity.class);
		startActivityForResult(intent, ACTIVITY_HOSPITAL_PREFIX);
	}

	public void onSendUsFeedback() {
		String toEmail = "ed@podmedics.com";
		String subject = "Induction suggestions/feedback";
		String content = "";
		
		try {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{toEmail});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
			
			startActivity(emailIntent);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Failed to invoke email", e);
		}
	}

	public void onShareInduction() {
		String strMessage = "Be sure to checkout Induction - http://induction-app.com. The free hospital directory app for healthcare professionals.";
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		//sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, strMessage);
		startActivity(sharingIntent);
	}

	public void notification(int resId, String title, String message) {
		notificationView.show(resId, title, message);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case ACTIVITY_CHANGE_DEFAULT_HOSPITAL:
					setResult(RESULT_OK);
					finish();
					break;

				case ACTIVITY_HOSPITAL_PREFIX:
					notification(R.drawable.ic_checkmark, "Thanks", "We have updated the prefix");
					break;
	
				default:
					break;
			}
		}
	}

}
