package com.hermes.induction.view;

import com.hermes.induction.*;

import android.content.*;
import android.os.Handler;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.view.animation.Animation.*;
import android.widget.*;

	public class NotificationView extends LinearLayout {

	private ImageView imageViewNotification = null;
	
	private TextView textViewNotificationTitle = null;

	private TextView textViewNotificationMessage = null;

	private Animation animSideDown = null;

	private Animation animSideUp = null;

	public NotificationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = li.inflate(R.layout.view_notification, this);
		
		imageViewNotification = (ImageView) view.findViewById(R.id.imageViewNotification);
		
		textViewNotificationTitle = (TextView) view.findViewById(R.id.textViewNotificationTitle);
		
		textViewNotificationMessage = (TextView) view.findViewById(R.id.textViewNotificationMessage);
		
		animSideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
		animSideDown.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				clearAnimation();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						startAnimation(animSideUp);
						setVisibility(View.GONE);
					}
				}, 2000);
			}
		});
		
		animSideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
		animSideUp.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				clearAnimation();
			}
		});
		
	}
	
	public void show(int resId, String title, String message) {
		imageViewNotification.setImageResource(resId);
		textViewNotificationTitle.setText(title);
		textViewNotificationMessage.setText(message);
		
		startAnimation(animSideDown);
		setVisibility(View.VISIBLE);
	}

}
