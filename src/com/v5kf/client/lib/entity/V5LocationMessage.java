package com.v5kf.client.lib.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;

public class V5LocationMessage extends V5Message implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5952242250836389285L;
	private double x;
	private double y;
	private double scale;
	private String label;
	
	public V5LocationMessage() {
		// TODO Auto-generated constructor stub
		super();
		this.message_type = V5MessageDefine.MSG_TYPE_LOCATION;
	}
	
	public V5LocationMessage(double x, double y, double scale, String label) {
		this(x, y);
		this.label = label;
		this.scale = scale;
	}

	public V5LocationMessage(double x, double y) {
		this.x = x;
		this.y = y;
		this.message_type = V5MessageDefine.MSG_TYPE_LOCATION;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5LocationMessage(JSONObject jsonMsg) throws NumberFormatException, JSONException {
		super(jsonMsg);
		x = jsonMsg.optDouble(V5MessageDefine.MSG_X);
		y = jsonMsg.optDouble(V5MessageDefine.MSG_Y);
		if (jsonMsg.has(V5MessageDefine.MSG_SCALE)) {
			scale = jsonMsg.getDouble(V5MessageDefine.MSG_SCALE);
		}
		label = jsonMsg.optString(V5MessageDefine.MSG_LABEL);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		json.put(V5MessageDefine.MSG_X, this.x);
		json.put(V5MessageDefine.MSG_Y, this.y);
		if (this.scale != Double.NaN && this.scale != 0) {
			json.put(V5MessageDefine.MSG_SCALE, this.scale);
		}
		if (null != label) {
			json.put(V5MessageDefine.MSG_LABEL, this.label);
		}
		return json.toString();
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
