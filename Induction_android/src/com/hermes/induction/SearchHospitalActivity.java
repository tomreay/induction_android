package com.hermes.induction;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.hermes.induction.adapter.HospitalAdapter;
import com.hermes.induction.common.Constants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SearchHospitalActivity extends SherlockActivity {
	
	public static final String TAG = SearchHospitalActivity.class.getSimpleName();

	private static final int ACTIVITY_MAIN = 0;

	private SearchView searchViewHospital = null;

	private PullToRefreshListView pullToRefreshListViewHospital = null;
	
	private ArrayList<ParseObject> items = null;

	private HospitalAdapter adapter = null;

	public String keyword = "";

	private ProgressDialog progressDialog = null;

	public ArrayList<ParseObject> hospitals = null;

	public class GetDataTask extends AsyncTask<Void, Void, List<ParseObject>> {
		
		@Override
		protected List<ParseObject> doInBackground(Void... params) {
			List<ParseObject> results = null;
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Hospital");
			query.orderByAscending("name");
			query.setLimit(Constants.PARSE_QUERY_LIMIT);
			try {
				results = query.find();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return results;
		}
		
		@Override
		protected void onPostExecute( List<ParseObject> results) {
			super.onPostExecute(results);
			if (results != null) {
				hospitals.clear();
				items.clear();
				for (ParseObject hospital : results) {
					hospitals.add(hospital);
					items.add(hospital);
				}
				adapter.setItems(items);
				adapter.notifyDataSetChanged();

				// Call onRefreshComplete when the list has been refreshed.
				pullToRefreshListViewHospital.onRefreshComplete();
				
				if (keyword != null && keyword.length() > 0) {
					onSearch(keyword);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_hospital);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		searchViewHospital = (SearchView) findViewById(R.id.searchViewHospital);
		searchViewHospital.setQuery("", false);
		searchViewHospital.clearFocus();
		searchViewHospital.onActionViewExpanded();

		searchViewHospital.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchViewHospital.setQuery("", false);
				searchViewHospital.clearFocus();
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				onSearch(newText);
				return false;
			}
		});
		
		pullToRefreshListViewHospital = (PullToRefreshListView) findViewById(R.id.pullToRefreshListViewHospital);

		pullToRefreshListViewHospital.setOnRefreshListener(new OnRefreshListener<ListView>() {
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

		ListView listview = pullToRefreshListViewHospital.getRefreshableView();
		
		items = new ArrayList<ParseObject>();
		
		adapter = new HospitalAdapter(this, R.layout.listview_row_hospital, items);
		
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onChangeDefaultHospital(position - 1);
			}
		});
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				initDownloadHospital();
			}
		}, 100);
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

	private void initDownloadHospital() {
		progressDialog = ProgressDialog.show(this, "", "Loading ...");
		progressDialog.setCancelable(true);
		
		if (hospitals == null) {
			hospitals = new ArrayList<ParseObject>();
		}
		else {
			hospitals.clear();
		}
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Hospital");
		query.orderByAscending("name");
		query.setLimit(Constants.PARSE_QUERY_LIMIT);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> results, ParseException e) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
					if (e == null) {
						items.clear();
						for (ParseObject parseObject : results) {
							hospitals.add(parseObject);
						}
						for (ParseObject hospital : hospitals) {
							items.add(hospital);
						}
						adapter.setItems(items);
						adapter.notifyDataSetChanged();
					}
				}
			}
		});
	}

	public void onSearch(String query) {
		keyword = query;
		new AsyncTask<Void, Void, ArrayList<ParseObject>>() {
			@Override
			protected ArrayList<ParseObject> doInBackground(Void... params) {
				ArrayList<ParseObject> filters = new ArrayList<ParseObject>();
				
				if (keyword.length() > 0) {
					for (ParseObject hospital : hospitals) {
						if (hospital.getString("name").toLowerCase().contains(keyword.toLowerCase())) {
							filters.add(hospital);
						}
					}
				}
				else {
					for (ParseObject hospital : hospitals) {
						filters.add(hospital);
					}
				}
				
				return filters;
			}
			
			protected void onPostExecute(ArrayList<ParseObject> result) {
				super.onPostExecute(result);
				if (result != null) {
					items.clear();
					for (ParseObject hospital : result) {
						items.add(hospital);
					}
					adapter.setItems(items);
					adapter.notifyDataSetChanged();
				}
			}
		}.execute();
	}

	public void onChangeDefaultHospital(int position) {
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setResult(RESULT_OK);
		finish();
	}

}
