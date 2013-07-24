package com.hermes.induction;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.*;
import com.hermes.induction.common.Constants;
import com.hermes.induction.common.Utilities;
import com.hermes.induction.fragment.*;
import com.hermes.induction.view.NotificationView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends SherlockFragmentActivity {
	
	private static final int ACTIVITY_DIRECTORY_ADDING = 1;

	private static final int ACTIVITY_SETTINGS = 2;

	private ToggleButton toggleButtonDirectory = null;
	
	private ImageView imageViewAdd = null;

	private ImageView imageViewCall = null;
	
	private ToggleButton toggleButtonQuickDial = null;

	private DirectoryFragment tabDirectoryFragment = null;

	private QuickDialFragment tabQuickDialFragment = null;

	private MenuItem menuItemSettings = null;

	private String hospitalName = null;

	private NotificationView notificationView = null;

	private LinearLayout linearLayoutAddHint = null;

	private TextView textViewAddHint = null;

	private boolean bTabDirectory;

	private boolean bTabQickDial;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		hospitalName = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_NAME, "");
		
		notificationView = (NotificationView) findViewById(R.id.notificationView);
		
		linearLayoutAddHint = (LinearLayout) findViewById(R.id.linearLayoutAddHint);
		linearLayoutAddHint.setVisibility(View.GONE);
		
		textViewAddHint = (TextView) findViewById(R.id.textViewAddHint);
		textViewAddHint.setTypeface(Typeface.createFromAsset(getAssets(), "HelveticaNeueLTCom-It.ttf"));
		
		toggleButtonDirectory = (ToggleButton) findViewById(R.id.toggleButtonDirectory);
		toggleButtonDirectory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!bTabDirectory) {
					onTabDirectory();
				}
				else {
					toggleButtonDirectory.setChecked(true);
				}
			}
		});
		
		imageViewAdd = (ImageView) findViewById(R.id.imageViewAdd);
		imageViewAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onDirectoryAdding();
			}
		});
		
		imageViewCall = (ImageView) findViewById(R.id.imageViewCall);
		imageViewCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onCall();
			}
		});
		
		toggleButtonQuickDial = (ToggleButton) findViewById(R.id.toggleButtonQuickDial);
		toggleButtonQuickDial.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!bTabQickDial) {
					onTabQuickDial();
				}
				else {
					toggleButtonQuickDial.setChecked(true);
				}
			}
		});
		
		init();
		
	}

	private void init() {
		onTabDirectory();
	}
	
	public void showAddHint() {
		linearLayoutAddHint.setVisibility(View.VISIBLE);
	}
	
	public void hideAddHint() {
		linearLayoutAddHint.setVisibility(View.GONE);
	}

	public void onTabDirectory() {
		bTabDirectory = true;
		bTabQickDial = false;
		
		notificationView.clearAnimation();
		
		getSupportActionBar().setTitle(hospitalName);
		
		if (menuItemSettings != null) {
			menuItemSettings.setVisible(true);
		}
		
		toggleButtonDirectory.setChecked(true);
		toggleButtonQuickDial.setChecked(false);
		imageViewAdd.setVisibility(View.VISIBLE);
		imageViewCall.setVisibility(View.GONE);
		
		if (tabDirectoryFragment == null) {
			tabDirectoryFragment = new DirectoryFragment();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, tabDirectoryFragment);
        ft.commit();
        
        tabDirectoryFragment.show();
	}
	
	public void hideKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.fragment).getWindowToken(), 0);
	}
	
	public void onDirectoryAdding() {
		Intent intent = new Intent(this, DirectoryAddingActivity.class);
		startActivityForResult(intent, ACTIVITY_DIRECTORY_ADDING);
	}
	
	private void callbackDirectoryAdding(int type) {
		switch (type) {
			case Constants.OPTION_DIRECTORY_ADD_NUMBER:
				tabDirectoryFragment.onAddNumber();
				break;

			case Constants.OPTION_DIRECTORY_PHOTOGRAPH_LIST:
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("Important");
				alertDialog.setMessage("Please make sure that the photo contains both the number and the name for that number");
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						tabDirectoryFragment.onPhotographList();
					}
				});
				alertDialog.show();
				break;
				
			case Constants.OPTION_DIRECTORY_EMAIL_NUMBER:
				tabDirectoryFragment.onEmailNumber();
				break;
			default:
				break;
		}
	}

	public void onCall() {
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		final String strPrefix = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, "");
		final String strPhone = tabQuickDialFragment.getDial();
		
		if (strPrefix.length() > 0) {
			if (strPhone.length() >= 3) {
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setTitle("");
				alertDialog.setMessage(PhoneNumberUtils.formatNumber(strPrefix + strPhone));
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse("tel:" + (strPrefix + strPhone)));
						startActivity(callIntent);
					}
				});
				alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				alertDialog.show();
			}
			else {
				notification(R.drawable.ic_ekg, "Woops", "Extensions/full numbers must be 3 or more numbers long");
			}
		}
		else {
			AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
			alertDialog.setTitle("Woops!");
			alertDialog.setMessage("Sorry, you can't call an extension without first setting a prefix for your hospital.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.show();
		}
	}

	public void onTabQuickDial() {
		bTabDirectory = false;
		bTabQickDial = true;
		
		notificationView.clearAnimation();
        tabDirectoryFragment.clearFocus();
        
		getSupportActionBar().setTitle(R.string.Quick_Dial);
		
		hideAddHint();
		
		if (menuItemSettings != null) {
			menuItemSettings.setVisible(false);
		}
		
		toggleButtonDirectory.setChecked(false);
		toggleButtonQuickDial.setChecked(true);
		imageViewAdd.setVisibility(View.GONE);
		imageViewCall.setVisibility(View.VISIBLE);
		
		if (tabQuickDialFragment == null) {
			tabQuickDialFragment = new QuickDialFragment();
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, tabQuickDialFragment);
        ft.commit();
        
        tabQuickDialFragment.show();
        
        if (!Utilities.existDefaultHospitalPrefix(getApplicationContext())) {
			notification(R.drawable.ic_ekg, "Warning", "We don't have a prefix for your hospital yet, only calling full numbers is possible");
		}
	}

	public void onSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, ACTIVITY_SETTINGS);
	}

	private void callbackSettings() {
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		hospitalName = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_NAME, "");
		
		getSupportActionBar().setTitle(hospitalName);
		
		if (tabDirectoryFragment != null) {
			tabDirectoryFragment.setVisited(false);
	        tabDirectoryFragment.show();
	        
	        if (!Utilities.existDefaultHospitalPrefix(getApplicationContext())) {
				notification(R.drawable.ic_ekg, "Warning", "We don't have a prefix for your hospital yet. Please set it in the settings.");
			}
		}
	}

	public void notification(int resId, String title, String message) {
		notificationView.show(resId, title, message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		
		menuItemSettings = menu.findItem(R.id.action_settings);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				onSettings();
				break;
	
			default:
				break;
		}
		return true;
	}
	
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case ACTIVITY_DIRECTORY_ADDING:
					if (data != null) {
						callbackDirectoryAdding(data.getIntExtra("option", 0));
					}
					break;
					
				case ACTIVITY_SETTINGS:
					callbackSettings();
					break;
	
				default:
					break;
			}
		}
	}

}
