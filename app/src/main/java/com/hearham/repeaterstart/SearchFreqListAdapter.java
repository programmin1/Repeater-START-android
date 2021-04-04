/**
 Repeater START - Showing The Amateur Repeaters Tool
 (C) 2020 Luke Bryan.
 This is free software: you can redistribute it and/or modify it
 under the terms of the GNU General Public License
 as published by the Free Software Foundation; version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package com.hearham.repeaterstart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.sentry.Sentry;

/**
 * Search for frequency.
 */
public class SearchFreqListAdapter extends BaseAdapter
{
	private Context context;
	private JSONArray data;
	private static LayoutInflater inflater = null;
	private LatLng center;

	public SearchFreqListAdapter(Context context, JSONArray data, LatLng center)
	{
		this.context = context;
		this.data = data;
		this.center = center;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount()
	{
		return data.length();
	}

	@Override
	public Object getItem(int position)
	{
		try {
			return data.get(position);
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View vi = convertView;
		if (vi == null)
			vi = inflater.inflate(R.layout.searchrow, null);
		TextView label = (TextView) vi.findViewById(R.id.label);
		String lbltext = "";
		try {
			JSONObject obj = (JSONObject) data.get(position);
			double dist = Utils.distance(obj, center.getLatitude(), center.getLongitude());
			lbltext = String.format("%s (%.2f/%.2f) distance %.2f",obj.getString("callsign"),
					obj.getDouble("frequency")/1000000, (obj.getDouble("frequency")+obj.getDouble("offset"))/1000000, dist);
		} catch (JSONException ex) {
			Sentry.captureException(ex);
			ex.printStackTrace();
		}
		label.setText(lbltext);
		return vi;
	}

	public LatLng position(int i) throws JSONException
	{
		LatLng position = new LatLng();
		JSONObject repeater = (JSONObject)data.get(i);
		position.setLatitude(repeater.getDouble("latitude"));
		position.setLongitude(repeater.getDouble("longitude"));

		return position;
	}
}