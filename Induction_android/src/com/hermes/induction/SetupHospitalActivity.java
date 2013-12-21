package com.hermes.induction;

import com.actionbarsherlock.app.*;
import com.actionbarsherlock.view.*;
import com.actionbarsherlock.widget.SearchView;

import com.handmark.pulltorefresh.library.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase.*;

import com.hermes.induction.adapter.*;
import com.hermes.induction.common.*;
import com.parse.*;

import android.app.ProgressDialog;
import android.content.*;
import android.location.*;
import android.os.*;
import android.text.format.*;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.*;

public class SetupHospitalActivity extends SherlockActivity {
	
	public static final String TAG = SetupHospitalActivity.class.getSimpleName();

	private static final int ACTIVITY_MAIN = 1;

	private static final int ACTIVITY_SEARCH_HOSPTIAL = 2;

	public class GetDataTask extends AsyncTask<Void, Void, List<ParseObject>> {
		
		@Override
		protected List<ParseObject> doInBackground(Void... params) {
			List<ParseObject> parseList = null;
			
			if (currentLocation != null) {
				Log.i(TAG, "Download hospitals near by here ( " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + " )");
				ParseGeoPoint userLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
				ParseQuery<ParseObject> query = ParseQuery.getQuery("Hospital");
				query.whereNear("location", userLocation);
				query.setLimit(Constants.LIMIT_SHOW_SETUP_HOSPITAL_COUNT);
				
				try {
					parseList = query.find();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
			
			return parseList;
		}
		
		protected void onPostExecute(List<ParseObject> result) {
			if (result != null) {
				items.clear();
				for (ParseObject parseObject : result) {
					items.add(parseObject);
				}
				adapter.setItems(items);
				adapter.setCurrentLocation(currentLocation);
				adapter.notifyDataSetChanged();
			}
			
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshListView.onRefreshComplete();
			
			super.onPostExecute(result);
		}
	}

	private PullToRefreshListView mPullRefreshListView = null;
	
	private ArrayList<ParseObject> items = null;

	private HospitalAdapter adapter = null;

	private LocationManager locationManager = null;
	
	private LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
        	currentLocation = location;

    		Log.i(TAG, "Stop the gps location manager");
        	locationManager.removeUpdates(this);
        	locationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    
    private LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
        	currentLocation = location;

    		Log.i(TAG, "Stop the network location manager");
        	locationManager.removeUpdates(this);
        	locationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

	private Location currentLocation = null;

	private ProgressDialog progressDialog = null;

	private SearchView searchView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_hospital);

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});

		ListView listview = mPullRefreshListView.getRefreshableView();
		
		items = new ArrayList<ParseObject>();
		
		adapter = new HospitalAdapter(this, R.layout.listview_row_hospital, items);
		
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onSelectHospital(position - 1);
			}
		});
		
		if (Utilities.existDefaultHospitalID(getApplicationContext())) {
			onMain();
		}
		else {
			startLocationManager();
			
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (currentLocation != null) {
						initDownloadHospitalNearby();
					}
					else {
						init();
					}
				}
			}, 100);
		}
		
	}

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListenerGps);
        locationManager.removeUpdates(locationListenerNetwork);
    }

	public void onSelectHospital(int position) {
		ParseObject defaultItem = items.get(position);
		defaultItem.increment("setDefaultCounter");
		defaultItem.saveEventually();
		
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.KEY_DEFAULT_HOSPITAL_ID, defaultItem.getObjectId());
		editor.putString(Constants.KEY_DEFAULT_HOSPITAL_NAME, defaultItem.getString("name"));
		editor.putString(Constants.KEY_DEFAULT_HOSPITAL_NICKNAME, defaultItem.getString("nickname"));
		if (defaultItem.getString("prefix") != null) {
			editor.putString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, defaultItem.getString("prefix"));
		}
		else {
			editor.putString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, "");
		}
		editor.commit();
		
		onMain();
	}
	
	private void onMain() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivityForResult(intent, ACTIVITY_MAIN);
	}

	private void startLocationManager() {
		Log.i(TAG, "Start the location manager");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		String mlocProvider;
		Criteria hdCrit = new Criteria();

		hdCrit.setAccuracy(Criteria.ACCURACY_COARSE);

		mlocProvider = locationManager.getBestProvider(hdCrit, true);
		
		boolean bEnableGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean bEnableNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		Log.i(TAG, "Enable GPS: " + String.valueOf(bEnableGPS));
		Log.i(TAG, "Enable Network: " + String.valueOf(bEnableNetwork));

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
		
		currentLocation = locationManager.getLastKnownLocation(mlocProvider);
		
	}
    
	private void init() {		
		progressDialog = ProgressDialog.show(this, "", "Getting the current location ...");
		progressDialog.setCancelable(true);
		
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				while (currentLocation == null) {
					try { Thread.sleep(500); } catch (Exception e) { }
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
					initDownloadHospitalNearby();
				}
			}
		}.execute();
	}

	private void initDownloadHospitalNearby() {
		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.Loading____));
		progressDialog.setCancelable(true);
		
		new AsyncTask<Void, Void, List<ParseObject>>() {
			@Override
			protected List<ParseObject> doInBackground(Void... params) {
				List<ParseObject> hospitalList = null;
				
				if (currentLocation != null) {
					Log.i(TAG, "Download hospitals near by here ( " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + " )");
					ParseGeoPoint userLocation = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
					ParseQuery<ParseObject> query = ParseQuery.getQuery("Hospital");
					query.whereNear("location", userLocation);
					query.setLimit(Constants.LIMIT_SHOW_SETUP_HOSPITAL_COUNT);
					try {
						hospitalList = query.find();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return hospitalList;
			}
			
			protected void onPostExecute(List<ParseObject> result) {
				super.onPostExecute(result);
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
					if (result != null && result.size() > 0) {
						items.clear();
						for (ParseObject hospital : result) {
							items.add(hospital);
						}
						adapter.setItems(items);
						adapter.setCurrentLocation(currentLocation);
						adapter.notifyDataSetChanged();
					}
				}
			}
		}.execute();
	}
	
	public void onSearch() {
		Intent intent = new Intent(this, SearchHospitalActivity.class);
		startActivityForResult(intent, ACTIVITY_SEARCH_HOSPTIAL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.setup_hospital, menu);		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			onSearch();
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
			setResult(RESULT_OK);
			finish();
		}
	}

}
