package com.determinato.feeddroid.ads;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.ads.AdSenseSpec;

public class FeedDroidAdSenseSpec extends AdSenseSpec {
	private String mCountry;
	private String mCity;
	
	public FeedDroidAdSenseSpec(String clientId) {
		super(clientId);
	}
	
	public String getCountry() {
		return mCountry;
	}
	
	public FeedDroidAdSenseSpec setCountry(String country) {
		mCountry = country;
		return this;
	}
	
	public String getCity() {
		return mCity;
	}
	
	public FeedDroidAdSenseSpec setCity(String city) {
		mCity = city;
		return this;
	}
	
	@Override
	public List<Parameter> generateParameters(Context context) {
		List<Parameter> parameters = new ArrayList<Parameter>(
				super.generateParameters(context));
		
		if (mCountry != null)
			parameters.add(new Parameter("gl", mCountry));
		if (mCity != null) 
			parameters.add(new Parameter("gcs", mCity));
		
		return parameters;
	}
	
}
