package com.v5kf.mcss.entity;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SiteInfo extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5090536307918360903L;
	private long site_id;
	private String kexi_plus_url;

	public void updateFromJSON(JSONObject json) throws JSONException {
		// TODO Auto-generated method stub
		setSite_id(json.optLong("site_id"));
		
		if (json.has("kexi")) {
			JSONObject kexi = json.getJSONObject("kexi");
			if (kexi != null && kexi.has("plus")) {
				JSONArray plus = kexi.getJSONArray("plus");
				if (plus != null && plus.length() > 0) {
					for (int i = 0; i < plus.length(); i++) {
						JSONObject item = plus.getJSONObject(i);
						if (item != null && item.has("url")) {
							kexi_plus_url = item.getString("url");
						}
					}
				}
			}
		}
	}

	public long getSite_id() {
		return site_id;
	}

	public void setSite_id(long site_id) {
		this.site_id = site_id;
	}

	public String getKexi_plus_url() {
		return kexi_plus_url;
	}

	public void setKexi_plus_url(String kexi_plus_url) {
		this.kexi_plus_url = kexi_plus_url;
	}
	
}
