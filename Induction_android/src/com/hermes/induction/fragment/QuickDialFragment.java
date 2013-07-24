package com.hermes.induction.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.hermes.induction.*;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class QuickDialFragment extends SherlockFragment {
	
	private EditText editTextDial = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_quick_dial, container, false);
		
		editTextDial = (EditText) view.findViewById(R.id.editTextDial);
		
		return view;
	}
	
	public void show() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (editTextDial != null) {
					editTextDial.requestFocus();
					InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.showSoftInput(editTextDial, 0);
				}
			}
		}, 100);
	}
	
	public String getDial() {
		return editTextDial.getText().toString();
	}
	
}
