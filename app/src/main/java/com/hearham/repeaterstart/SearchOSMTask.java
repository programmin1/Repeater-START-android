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
import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import io.sentry.Sentry;

public class SearchOSMTask extends AsyncTask<String,Void,String>
{
	private final SearchResponse delegate;
	private JSONArray results;
	public interface SearchResponse {
		void processFinish(JSONArray output);
	}

	public SearchOSMTask(SearchResponse delegate){
		this.delegate = delegate;
	}

	@Override
	protected String doInBackground(String... strings)
	{
		String inputLine;
		StringBuilder json = new StringBuilder();
		Log.w("Search",strings[0]);

		Breadcrumb breadcrumb = new Breadcrumb();
		breadcrumb.setCategory("search");
		breadcrumb.setMessage("SearchOSM for: " + strings[0]);
		breadcrumb.setLevel(SentryLevel.INFO);
		Sentry.addBreadcrumb(breadcrumb);
		try {
			//URL search = new URL("https://nominatim.openstreetmap.org/search/"+ URLEncoder.encode(strings[0], "UTF-8") +"?format=json&limit=50");
			URL search = new URL("https://nominatim.openstreetmap.org/search/"+ strings[0] +"?format=json&limit=50");

			BufferedReader in = new BufferedReader(
					new InputStreamReader(search.openStream()));
			while ((inputLine = in.readLine()) != null)
				json.append(inputLine);

			in.close();
			results = new JSONArray(json.toString());
			for (int i = 0; i < results.length(); i++) {
				JSONObject c = results.getJSONObject(i);
				//with string lat, long, display_name.

			}

		} catch (UnsupportedEncodingException e) {
			Sentry.captureException(e);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Sentry.captureException(e);
			e.printStackTrace();
		} catch (IOException e) {
			Sentry.captureException(e);
			e.printStackTrace();
		} catch (JSONException e) {
			Sentry.captureException(e);
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String s)
	{
		super.onPostExecute(s);
		delegate.processFinish(results);
	}
}
