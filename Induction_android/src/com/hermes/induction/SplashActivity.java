package com.hermes.induction;

import com.actionbarsherlock.app.SherlockActivity;
import com.hermes.induction.common.Constants;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class SplashActivity extends SherlockActivity {

	private static final int ACTIVITY_CHOOSE_YOUR_HOSPITAL = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		getSupportActionBar().hide();
		
		if (checkEnableGPS()) {
			
			initParse();
			
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					init();
				}
			}, 1000);
		}
	}

	private void initParse() {
		// Add your initialization code here
		Parse.initialize(this, Constants.APPLICATION_ID, Constants.CLIENT_KEY);
		
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
	    
		// If you would like all objects to be private by default, remove this line.
		defaultACL.setPublicReadAccess(true);
		
		ParseACL.setDefaultACL(defaultACL, true);
	}

	public void init() {
		onChooseYourHospital();
	}

	public void onChooseYourHospital() {
		Intent intent = new Intent(this, SetupHospitalActivity.class);
		startActivityForResult(intent, ACTIVITY_CHOOSE_YOUR_HOSPITAL);
	}

	private boolean checkEnableGPS() {
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

	    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ||  !manager.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
	        buildAlertMessageNoGps();
	        return false;
	    }
	    
	    return true;
	}

	private void buildAlertMessageNoGps() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("");
		alertDialog.setMessage("GPS is disabled in your device. Would you like to enable it?");
		alertDialog.setButton("Enable", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		        finish();
		   }
		});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
				   finish();
			   }
			});
		alertDialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			finish();
		}
	}

}
