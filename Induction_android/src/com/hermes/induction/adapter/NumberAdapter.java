package com.hermes.induction.adapter;

import java.util.*;

import com.hermes.induction.R;
import com.parse.ParseObject;

import android.content.*;
import android.view.*;
import android.widget.*;

public class NumberAdapter extends ArrayAdapter<ParseObject> {

	private Context context = null;
	
	private ArrayList<ParseObject> items = null;
	
	private ParseObject item = null;

	private ImageView imageViewType = null;

	private TextView textViewNumberName = null;

	private TextView textViewNumberExtension = null;

	public NumberAdapter(Context context, int textViewResourceId, ArrayList<ParseObject> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = convertView;
		
		item = items.get(position);
		
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = li.inflate(R.layout.listview_row_number, null);
		
		imageViewType = (ImageView) view.findViewById(R.id.imageViewType);
		
		textViewNumberName = (TextView) view.findViewById(R.id.textViewNumberName);
		
		textViewNumberExtension = (TextView) view.findViewById(R.id.textViewNumberExtension);
		
		if (item.getString("type") != null && item.getString("type").equals("bleep")) {
			imageViewType.setImageResource(R.drawable.ic_bell);
		}
		else {
			imageViewType.setImageResource(R.drawable.ic_phone);
		}
		
		textViewNumberName.setText(item.getString("name"));
		
		textViewNumberExtension.setText(item.getString("extension"));
		
		return view;
	}

	public void setItems(ArrayList<ParseObject> items) {
		this.items = items;
	}

}
