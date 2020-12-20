package com.hearham.repeaterstart;

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

public class SearchWhatThreeWordsTask extends AsyncTask<String,Void,String>
{
	private final SearchResponse delegate;
	private JSONObject result;
	public interface SearchResponse {
		void processFinish(JSONObject output);
	}

	public SearchWhatThreeWordsTask(SearchWhatThreeWordsTask.SearchResponse delegate){
		this.delegate = delegate;
	}

	@Override
	protected String doInBackground(String... strings)
	{
		String inputLine;
		StringBuilder json = new StringBuilder();
		Log.w("Search",strings[0]);
		try {
			URL search = new URL("https://hearham.com/api/whatthreewords/v1?words="+ strings[0] );

			BufferedReader in = new BufferedReader(
					new InputStreamReader(search.openStream()));
			while ((inputLine = in.readLine()) != null)
				json.append(inputLine);

			in.close();
			result = new JSONObject(json.toString());

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String s)
	{
		super.onPostExecute(s);
		delegate.processFinish(result);
	}
}
