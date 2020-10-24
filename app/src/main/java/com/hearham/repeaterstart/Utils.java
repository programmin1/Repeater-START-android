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

import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Math.sin;

public class Utils
{
	/**
	 * Distance to point in km. TODO make this part of a repeater class??
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static double distance(JSONObject repeater, double lat, double lon) throws JSONException
	{
		double earthR = 6373;
		double dlat = Math.toRadians( lat - repeater.getDouble("latitude"));
		double dlon = Math.toRadians( lon - repeater.getDouble("longitude"));
		double a = Math.pow(sin(dlat/2), 2) + Math.cos(Math.toRadians(repeater.getDouble("latitude"))) * Math.cos(Math.toRadians(lat)) * Math.pow(Math.sin(dlon/2),2);
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return earthR*c;
	}
}
