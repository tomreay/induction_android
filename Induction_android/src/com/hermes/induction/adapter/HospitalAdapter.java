package com.hermes.induction.adapter;

import java.util.*;

import com.hermes.induction.R;
import com.parse.*;

import android.content.*;
import android.location.Location;
import android.view.*;
import android.widget.*;

public class HospitalAdapter extends ArrayAdapter<ParseObject> {

	private Context context = null;
	
	private ArrayList<ParseObject> items = null;
	
	private ParseObject item = null;

	private TextView textViewHospitalName = null;

	private TextView textViewDistance = null;

	private ParseGeoPoint currentPGP = null;

	public HospitalAdapter(Context context, int textViewResourceId, ArrayList<ParseObject> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		item = items.get(position);
		
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = li.inflate(R.layout.listview_row_hospital, null);
		
		textViewHospitalName = (TextView) view.findViewById(R.id.textViewNumberName);
		
		textViewDistance = (TextView) view.findViewById(R.id.textViewNumberExtension);
		
		textViewHospitalName.setText(item.getString("name"));
		
		if (currentPGP != null) {
			textViewDistance.setText(String.format("%.1f m", (double) (item.getParseGeoPoint("location").distanceInKilometersTo(currentPGP) * 1000)));
		}
		else {
			textViewDistance.setText("");
		}
		
		return view;
	}

	public void setItems(ArrayList<ParseObject> items) {
		this.items = items;
	}

	public void setCurrentLocation(Location location) {
		if (location != null) {
			if (currentPGP == null) {
				currentPGP = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
			}
			else {
				currentPGP.setLatitude(location.getLatitude());
				currentPGP.setLongitude(location.getLongitude());
			}
		}
		else {
			currentPGP = null;
		}
	}

}
