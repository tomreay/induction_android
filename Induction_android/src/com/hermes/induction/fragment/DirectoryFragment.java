package com.hermes.induction.fragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.widget.SearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.hermes.induction.AddNumberActivity;
import com.hermes.induction.MainActivity;
import com.hermes.induction.R;
import com.hermes.induction.ChangeDefaultHospitalActivity.GetDataTask;
import com.hermes.induction.adapter.*;
import com.hermes.induction.common.Constants;
import com.hermes.induction.common.Utilities;
import com.parse.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class DirectoryFragment extends SherlockFragment {
	
	protected static final String TAG = DirectoryFragment.class.getSimpleName();

	private final int ACTIVITY_ADD_NUMBER = 1;

	private final int ACTIVITY_CAMERA_CAPTURE = 2;

	private SearchView searchViewDirectory = null;

	private PullToRefreshListView pullToRefreshListViewDirectory = null;

	private ArrayList<ParseObject> numbers = null;

	private ArrayList<ParseObject> items = null;

	private NumberAdapter adapter = null;

	private ParseObject hospital = null;

	private ProgressDialog progressDialog = null;

	private String hospitalId = null;

	private String keyword = null;

	private File filePicture = null;
	
	private boolean bVisited = false;

	public class GetDataTask extends AsyncTask<Void, Void, List<ParseObject>> {
		
		@Override
		protected List<ParseObject> doInBackground(Void... params) {
			List<ParseObject> results = null;
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Number");
			query.whereEqualTo("parent", ParseObject.createWithoutData("Hospital", hospitalId));
			query.whereExists("name");
			query.whereNotEqualTo("forReview", Boolean.valueOf(true));
            query.orderByAscending("name");
            query.setLimit(Constants.PARSE_QUERY_LIMIT);
            query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
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
				numbers.clear();
				items.clear();
				for (ParseObject hospital : results) {
					numbers.add(hospital);
					items.add(hospital);
				}
				adapter.setItems(items);
				adapter.notifyDataSetChanged();

				// Call onRefreshComplete when the list has been refreshed.
				pullToRefreshListViewDirectory.onRefreshComplete();
				
				if (keyword != null && keyword.length() > 0) {
					onSearch(keyword);
				}
			}
		}
	}

	public void setHospitalId(ParseObject hospital) {
		this.hospital = hospital;
	}
	
	public void setVisited(boolean bVisited) {
		this.bVisited = bVisited;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_directory, container, false);
		
		searchViewDirectory = (SearchView) view.findViewById(R.id.searchViewDirectory);
		searchViewDirectory.setQuery("", false);
		searchViewDirectory.clearFocus();
		searchViewDirectory.onActionViewExpanded();

		searchViewDirectory.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchViewDirectory.setQuery("", false);
				searchViewDirectory.clearFocus();
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				onSearch(newText);
				return true;
			}
		});
		
		pullToRefreshListViewDirectory = (PullToRefreshListView) view.findViewById(R.id.pullToRefreshListViewDirectory);

		pullToRefreshListViewDirectory.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				// Do work to refresh the list here.
				new GetDataTask().execute();
			}
		});

		ListView listview = pullToRefreshListViewDirectory.getRefreshableView();
		
		if (numbers == null) {
			numbers = new ArrayList<ParseObject>();
		}
		
		if (items == null) {
			items = new ArrayList<ParseObject>();
		}
		
		adapter = new NumberAdapter(getActivity(), R.layout.listview_row_number, items);
		
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onSelectNumber(position - 1);
			}
		});
		
		File dir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		filePicture = new File(dir, "induction_temp.jpg");
		
		return view;
	}

	public void show() {
		if (!bVisited) {
			bVisited = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					initDownloadNumbers();
					searchViewDirectory.clearFocus();
				}
			}, 100);
		}
		else {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (items.size() == 0) {
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								searchViewDirectory.clearFocus();
								InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(searchViewDirectory.getWindowToken(), 0);
								((MainActivity) getActivity()).showAddHint();
								
						        if (!Utilities.existDefaultHospitalPrefix(getActivity())) {
						        	((MainActivity) getActivity()).notification(R.drawable.ic_ekg, "Warning", "We don't have a prefix for your hospital yet. Please set it in the settings.");
								}
							}
						}, 100);
					}
					else {
						searchViewDirectory.clearFocus();
						((MainActivity) getActivity()).hideAddHint();
						adapter.notifyDataSetChanged();
				        if (!Utilities.existDefaultHospitalPrefix(getActivity())) {
				        	((MainActivity) getActivity()).notification(R.drawable.ic_ekg, "Warning", "We don't have a prefix for your hospital yet. Please set it in the settings.");
						}
				        
				        new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								// Show the virtual keyboard
//								if (searchViewDirectory != null) {
//									searchViewDirectory.requestFocus();
//								}
								
								// Hide the virtual keyboard
								((MainActivity) getActivity()).hideKeyboard();
							}
						}, 100);
					}
				}
			}, 100);
		}
	}

	public void clearFocus() {
		searchViewDirectory.clearFocus();
	}

	private void initDownloadNumbers() {
		SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_NAME, 0);
		hospitalId = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_ID, "");
		
		numbers.clear();
		items.clear();
		adapter.setItems(items);
		adapter.notifyDataSetChanged();
		
		progressDialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.Loading____));
		progressDialog.setCancelable(true);
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Number");
		query.whereEqualTo("parent", ParseObject.createWithoutData("Hospital", hospitalId));
		query.whereExists("name");
		query.whereNotEqualTo("forReview", Boolean.valueOf(true));
		query.orderByAscending("name");
		query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ELSE_CACHE);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> numberList, ParseException e) {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
					
					if (e == null && numberList != null) {
						for (ParseObject parseObject : numberList) {
							numbers.add(parseObject);
							items.add(parseObject);
						}
					}
					
					if (items.size() == 0) {
						((MainActivity) getActivity()).showAddHint();
						searchViewDirectory.clearFocus();
					}
					else {
						((MainActivity) getActivity()).hideAddHint();
					}
					
					adapter.setItems(items);
					adapter.notifyDataSetChanged();
				}

		        if (!Utilities.existDefaultHospitalPrefix(getActivity())) {
		        	((MainActivity) getActivity()).notification(R.drawable.ic_ekg, "Warning", "We don't have a prefix for your hospital yet. Please set it in the settings.");
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
					for (ParseObject number : numbers) {
						if (number.getString("name").toLowerCase().contains(keyword.toLowerCase()) || number.getString("extension").contains(keyword)) {
							filters.add(number);
						}
					}
				}
				else {
					for (ParseObject number : numbers) {
						filters.add(number);
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

	public void onSelectNumber(int position) {
		if (items.get(position).getString("type") != null && items.get(position).getString("type").equals("bleep")) {
			showBleepAction(position);
		}
		else {
			showCallingAction(position);
		}
	}

	public void showBleepAction(final int position) {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setTitle("");
		alertDialog.setMessage(String.format("%s is on bleep %s", items.get(position).getString("name"), items.get(position).getString("extension")));
		alertDialog.setButton("Remove", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   removeAction(position);
		   }
		});
		alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.cancel();
		   }
		});
		alertDialog.show();
	}

	public void showCallingAction(final int position) {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setTitle("");
		alertDialog.setMessage(String.format("Call %s on %s?", items.get(position).getString("name"), items.get(position).getString("extension")));
		alertDialog.setButton("Call", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   callNumber(items.get(position));
		   }
		});
		alertDialog.setButton2("Remove", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   removeAction(position);
		   }
		});
		alertDialog.setButton3("Cancel", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
				   dialog.cancel();
			   }
			});
		alertDialog.show();
	}

	public void callNumber(ParseObject item) {
		SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_NAME, 0);
		String prefix = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_PREFIX, "");
		String extension = item.getString("extension");
		String number = null;
		
		if (prefix.length() > 0) {
			if (extension.length() < 6) {
				number = prefix + extension;
			}
			else {
				number = extension;
			}
			
			try {
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + number));
				startActivity(intent);

				item.increment("calledCounter");
				item.saveEventually();
			} catch (Exception e) {
				showNoCallAlert();
			}
		}
		else {
			if (extension.length() < 6) {
				AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
				alertDialog.setTitle("Woops!");
				alertDialog.setMessage("Sorry, you can't call an extension without first setting a prefix for your hospital.");
				alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				   public void onClick(DialogInterface dialog, int which) {
					   dialog.dismiss();
				   }
				});
				alertDialog.show();
			}
			else {
				number = extension;
				try {
					Intent intent = new Intent(Intent.ACTION_CALL);
					intent.setData(Uri.parse("tel:" + number));
					startActivity(intent);

					item.increment("calledCounter");
					item.saveEventually();
				} catch (Exception e) {
					showNoCallAlert();
				}
			}
		}
		
	}

	private void showNoCallAlert() {
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setTitle("Woops!");
		alertDialog.setMessage("Sorry, your device cannot make calls.");
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.dismiss();
		   }
		});
		alertDialog.show();
	}

	public void removeAction(final int position) {
		   ParseObject item = items.get(position);
		   item.deleteEventually();
		   
		   showSuccessNotificationWithTitle("Updated", "Thanks. We will get that number removed");
	}

	public void onAddNumber() {
		Intent intent = new Intent(getActivity(), AddNumberActivity.class);
		startActivityForResult(intent, ACTIVITY_ADD_NUMBER);
	}

	public void onPhotographList() {
		try {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			filePicture.delete();
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(filePicture));
			startActivityForResult(intent, ACTIVITY_CAMERA_CAPTURE);
		} catch (Exception e) {
			AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
			alertDialog.setTitle("Woops!");
			alertDialog.setMessage("Sorry. Your device does not have a camera.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.show();
		}
	}

	private void updatePicture() {
		SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_NAME, 0);
		String strHospitalId = prefs.getString(Constants.KEY_DEFAULT_HOSPITAL_ID, "");
		
		Bitmap bitmap = BitmapFactory.decodeFile(filePicture.getAbsolutePath());
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bao);
		byte[] imagearray = bao.toByteArray();
		ParseFile parseFile = new ParseFile("image.jpg", imagearray);
		try {
			bao.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ParseObject parseObject = new ParseObject("UserPhoto");
		parseObject.put("imageFile", parseFile);
		parseObject.put("relation", ParseObject.createWithoutData("Hospital", strHospitalId));
		parseObject.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					showSuccessNotificationWithTitle("Uploaded", "We got it. It will be in your phone by tomorrow.");
				}
				else {
					Log.e(TAG, e.getMessage());
				}
				filePicture.delete();
			}
		});
	}

	public void showSuccessNotificationWithTitle(String title, String message) {
		((MainActivity) getActivity()).notification(R.drawable.ic_checkmark, title, message);
	}

	public void showFailedNotificationWithTitle(String title, String message) {
		((MainActivity) getActivity()).notification(R.drawable.ic_ekg, title, message);
	}

	public void onEmailNumber() {
		String toEmail = "ed@podmedics.com";
		String subject = "Email Number List";
		String content = "Please paste your numbers below or send us an attachment. We will add them to your hospital within the next 24 hours, or sooner.";
		
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
				case ACTIVITY_CAMERA_CAPTURE:
					updatePicture();
					break;

				case ACTIVITY_ADD_NUMBER:
					if (data != null) {
						if (data.getBooleanExtra("done", false)) {
							showSuccessNotificationWithTitle("Saved!", "Thanks for sharing");
							initDownloadNumbers();
						}
						else {
							showFailedNotificationWithTitle("Failed!", "Failed to add new number");
						}
					}
					break;
	
				default:
					break;
			}
		}
	}

}
