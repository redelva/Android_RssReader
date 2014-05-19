package com.lgq.rssreader.controls;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerDialog extends DialogPreference{
	private Context mContext;
	private SummaryChangeListener mSummaryChangeListener;
	/**
	* @param context
	* @param attrs
	*/
	public TimePickerDialog(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		this.mContext = context;		
		setPersistent(true);
	}


	public TimePickerDialog(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setPersistent(true);
	}

	private TimePicker mTimePicker;

	public SummaryChangeListener getmSummaryChangeListener() {
		return mSummaryChangeListener;
	}


	public void setmSummaryChangeListener(
		SummaryChangeListener mSummaryChangeListener) {
			this.mSummaryChangeListener = mSummaryChangeListener;
		}


	/*
	* (non-Javadoc)
	* 
	* @seeandroid.preference.DialogPreference#setPositiveButtonText(java.lang.
	* CharSequence)
	*/
	@Override
	public void setPositiveButtonText(CharSequence positiveButtonText) {
		super.setPositiveButtonText(positiveButtonText);
	}


	public interface SummaryChangeListener{
		public void onSummaryChangeListener();
	}

	/*
	* (non-Javadoc)
	* 
	* @see
	* android.preference.DialogPreference#onClick(android.content.DialogInte***ce
	* , int)
	*/
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which != -1)
			return;
		int hour = this.mTimePicker.getCurrentHour().intValue();
		int minute = this.mTimePicker.getCurrentMinute().intValue();
		String str = String.format("%02d:%02d", new Object[] {
				Integer.valueOf(hour), Integer.valueOf(minute) });
		persistString(str);
		setSummary(str);
		callChangeListener(str);
	}
	
	/*
	* (non-Javadoc)
	* 
	* @see android.preference.DialogPreference#onCreateDialogView()
	*/
	@Override
	protected View onCreateDialogView() {
		this.mTimePicker = new TimePicker(getContext());
		this.mTimePicker.setIs24HourView(false);
		String timeValue = getPersistedString(mDefaultValue);
		String[] hourAndMinute = {};
		if ((timeValue == null) || (!timeValue.matches("[0-2]*[0-9]:[0-5]*[0-9]"))) {
			hourAndMinute = new String[] { "00", "00" };
		} else {
			hourAndMinute = timeValue.split(":");
		}
		int i = Integer.valueOf(hourAndMinute[0]).intValue();
		int j = Integer.valueOf(hourAndMinute[1]).intValue();
		if ((i >= 0) && (j >= 0)) {
			this.mTimePicker.setCurrentHour(i);
			this.mTimePicker.setCurrentMinute(j);
		}
		return mTimePicker;
	}


	private String mDefaultValue;


	/*
	* (non-Javadoc)
	* 
	* @see android.preference.Preference#setDefaultValue(java.lang.Object)
	*/
	@Override
	public void setDefaultValue(Object defaultValue) {
		setSummary(defaultValue.toString());
		super.setDefaultValue(defaultValue);
		this.mDefaultValue = (String) defaultValue;
	}
}
