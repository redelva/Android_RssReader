package com.lgq.rssreader;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

public abstract class BaseFragment extends Fragment {

	protected Context mContext;
	protected LayoutInflater mInflater;
	
	protected void setInflater(Context c) {
		this.mContext = c;
		this.mInflater = LayoutInflater.from(c);
	}

	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	}
	
	public void refresh() {
		
	}	
}