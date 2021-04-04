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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main repeater listing with sortability.
 */
public class RepeaterListAdapter extends BaseAdapter
{
	private Context context;
	private ArrayList<JSONObject> data;
	private static LayoutInflater inflater = null;
	private LatLng center;
	private SharedPreferences sharedPrefs;

	public RepeaterListAdapter(Context context, ArrayList<JSONObject> data, final LatLng center)
	{
		this.context = context;
		this.data = data;
		this.center = center;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		Collections.sort(this.data, new Comparator<JSONObject>()
		{
			@Override
			public int compare(JSONObject ob1, JSONObject ob2)
			{
				double d1 = 0;
				double d2 = 0;
				try {
					d1 = Utils.distance(ob1, center.getLatitude(), center.getLongitude());
					d2 = Utils.distance(ob2, center.getLatitude(), center.getLongitude());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (d1 > d2) return 1;
				if (d1 == d2) return 0;
				return -1;
			}
		});
	}

	@Override
	public int getCount()
	{
		return data.size();
	}

	@Override
	public Object getItem(int position)
	{
		return data.get(position);
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
			vi = inflater.inflate(R.layout.repeaterrow, null);
		TextView label1 = (TextView) vi.findViewById(R.id.label1);
		TextView label2 = (TextView) vi.findViewById(R.id.label2);
		TextView distlbl = (TextView) vi.findViewById(R.id.distlbl);
		try {
			JSONObject obj = data.get(position);
			String mhz = String.valueOf(obj.getDouble("frequency") / 1000000.0) + "mhz";
			String iNode = obj.getString("internet_node");
			if (null != iNode && iNode != "null") {
				label1.setText("Node " + iNode + ", " + obj.getString("callsign") + " at " + mhz);
			} else {
				label1.setText(obj.getString("callsign") + ", " + mhz);
			}
			if( obj.getInt("operational") < 1 ) {
				label1.setPaintFlags(label1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}
			label2.setText("PL " + obj.getString("encode") + ", Offset " + String.valueOf(obj.getInt("offset") / 1000000.0) + ",\n" +
					data.get(position).getString("description"));

			double dist = Utils.distance(obj, center.getLatitude(), center.getLongitude());

			String units = sharedPrefs.getString("display_units", "mi");
			if( units.equals("mi")) {
				dist = .62137119 * dist;
			}
			distlbl.setText(String.format("%.2g",dist)+units);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return vi;
	}

	public LatLng repeaterPos(int i) throws JSONException
	{
		LatLng position = new LatLng();
		position.setLatitude(data.get(i).getDouble("latitude"));
		position.setLongitude(data.get(i).getDouble("longitude"));
		return position;
	}

	private ArrayList<String> getAllLinksFromTheText(String text) {
		//https://stackoverflow.com/questions/5713558
		String LINK_REGEX = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,2083}\\.){1,4}([a-zA-Z]){2,6}(\\/(([a-zA-Z-_\\/\\.0-9#:?=&;,]){0,2083})?){0,2083}?[^ \\n]*)";
		ArrayList<String> links = new ArrayList<>();
		Pattern p = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		while (m.find()) {
			links.add(m.group());
		}
		return links;
	}

	public PopupMenu menuForPos(int i, View view) throws JSONException
	{
		final JSONObject selection = data.get(i);
		final String url = "https://hearham.com/repeaters/"+String.valueOf(data.get(i).getInt("id") +"?src=Android");
		final String commentUrl = url + "/comment?src=Android";
		final ArrayList<String> links = getAllLinksFromTheText(data.get(i).getString("description"));
		PopupMenu popup = new PopupMenu(this.context,view);

		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_repeater, popup.getMenu());
		for( int l = 0; l<links.size(); l++ ) {
			popup.getMenu().add(1, 1, 0, links.get(l));
		}
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem menuItem)
			{
				switch( menuItem.getItemId() ) {
					case R.id.Go:
						Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( url ) );
						context.startActivity(browse);
						return true;
					case R.id.GoComment:
						Intent comment = new Intent( Intent.ACTION_VIEW , Uri.parse( commentUrl ) );
						context.startActivity(comment);
						return true;
				}
				//Other links in description
				CharSequence descURL = menuItem.getTitle();
				Intent special = new Intent( Intent.ACTION_VIEW , Uri.parse((String) descURL) );
				context.startActivity(special);

				return false;
			}
		});
		return popup;
	}
}