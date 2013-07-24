package com.hermes.induction.common;

import java.util.regex.*;

import android.content.Context;
import android.content.SharedPreferences;

public class Utilities {
	
	private static final String TAG = Utilities.class.getSimpleName();
	
	public static boolean checkEmailFormat(String email) {
		if (email.length() == 0) {
			return false;
		}

		String pttn = "^\\D.+@.+\\.[a-z]+";
		Pattern p = Pattern.compile(pttn);
		Matcher m = p.matcher(email);

		if (m.matches()) {
			return true;
		}

		return false;
	}

	public static boolean existDefaultHospitalPrefix(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, 0);
		String strDefaultHospitalPrefix = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, "");
		return strDefaultHospitalPrefix.length() > 0 ? true : false;
	}

	public static boolean existDefaultHospitalID(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, 0);
		String strDefaultHospitalID = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_ID, "");
		return strDefaultHospitalID.length() > 0 ? true : false;
	}

}
