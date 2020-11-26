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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.hearham.repeaterstart.R.id.repeaterList;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionsListener
{
	private static MapboxMap mapboxMap;
	private MapView mapView;
	private static final String REPEATERICON_ID = "ICONREPEATER";
	private static final String REPEATERDOWNICON_ID = "ICONREPEATERDOWN";

	private static final String SOURCE_ID = "SOURCE_ID";
	private static final String SOURCEDOWN_ID = "SOURCE_DOWN_ID";
	private static final String LAYER_ID = "LAYER_ID";
	private static final String LAYERDOWN_ID = "LAYERDOWN_ID";
	private static final String TAG = "RepeaterSTART";
	ListView listview;
	private double currentLat =-1;
	private double currentLon =-1;
	private double DEFAULZOOM = 10;

	private PermissionsManager permissionsManager;
	private DownloadManager downloadManager;
	private long downloadJSONReference;

	private List<Feature> repeaterFeatureList = new ArrayList<>();
	private List<Feature> repeaterDownFeatureList = new ArrayList<>();
	private RepeaterListAdapter nearbyRepeaterAdapter;
	private JSONArray repeaterlist;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
		Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
				vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		vectorDrawable.draw(canvas);
		Log.e(TAG, "getBitmap: 1");
		return bitmap;
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	private static Bitmap getBitmap(Context context, int drawableId) {
		Log.e(TAG, "getBitmap: 2");
		Drawable drawable = ContextCompat.getDrawable(context, drawableId);
		if (drawable instanceof BitmapDrawable) {
			return BitmapFactory.decodeResource(context.getResources(), drawableId);
		} else if (drawable instanceof VectorDrawable) {
			return getBitmap((VectorDrawable) drawable);
		} else {
			throw new IllegalArgumentException("unsupported drawable type");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		listview = (ListView) findViewById(repeaterList);
		System.setProperty("http.agent","Repeater-Start");
		super.onCreate(savedInstanceState);
		Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
		setContentView(R.layout.activity_main);
		Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));


		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(@NonNull final MapboxMap mapboxMap) {
				MainActivity.mapboxMap = mapboxMap;
/*
				symbolLayerIconFeatureList.add(Feature.fromGeometry(
						Point.fromLngLat(number,number)));*/
				setRepeaterList();
				//https://docs.mapbox.com/help/tutorials/first-steps-android-sdk/#add-markers
				mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/programmin/ckb5u3thq35l91iq9h1d5k172")
						.withImage(REPEATERICON_ID,
								getBitmap(getApplicationContext(),
										R.drawable.ic_signaltower))
						.withImage(REPEATERDOWNICON_ID,
								getBitmap(getApplicationContext(),
										R.drawable.ic_signaltowerdown))

						// Adding a GeoJson source for the SymbolLayer icons.
						.withSource(new GeoJsonSource(SOURCE_ID,
								FeatureCollection.fromFeatures(repeaterFeatureList)))
						.withSource(new GeoJsonSource(SOURCEDOWN_ID,
								FeatureCollection.fromFeatures(repeaterDownFeatureList)))

						// Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
						// marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
						// the coordinate point. This is offset is not always needed and is dependent on the image
						// that you use for the SymbolLayer icon.
						.withLayers( //Now two layers, red/inoperable and normal repeaters:
								new SymbolLayer(LAYERDOWN_ID, SOURCEDOWN_ID)
								.withProperties(PropertyFactory.iconImage(REPEATERDOWNICON_ID),
										iconAllowOverlap(true),
										iconOffset(new Float[] {0f, 0f}),
										iconSize((float)0.333))
								,
								new SymbolLayer(LAYER_ID, SOURCE_ID)
								.withProperties(PropertyFactory.iconImage(REPEATERICON_ID),
										iconAllowOverlap(true),
										iconOffset(new Float[] {0f, 0f}),
										iconSize((float)0.333))
						), new Style.OnStyleLoaded() {
							@Override
							public void onStyleLoaded(@NonNull Style style) {
								enableLocationComponent(style);
								//mapboxMap.setMinZoomPreference(10);
							}
						}
				);
				mapboxMap.addOnCameraMoveListener(new MapboxMap.OnCameraMoveListener()
				{
					@Override
					public void onCameraMove()
					{
						setRepeaterList();
					}
				});
			}
		});

		if (PermissionsManager.areLocationPermissionsGranted(this)) {
// Permission sensitive logic called here, such as activating the Maps SDK's LocationComponent to show the device's location
		} else {
			permissionsManager = new PermissionsManager(this);
			permissionsManager.requestLocationPermissions(this);
		}

		IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(downloadReceiver, filter);

		View searchButton = findViewById(R.id.search_btn);
		final MainActivity activity = this;
		searchButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String searchstr = ((EditText)findViewById(R.id.search_text)).getText().toString();
				SearchOSMTask search = new SearchOSMTask(new SearchOSMTask.SearchResponse()
				{
					@Override
					public void processFinish(JSONArray output)
					{
						if( output == null ) {
							Toast.makeText(getApplicationContext(),R.string.SearchFailed,Toast.LENGTH_LONG).show();
						} else {
							final SearchListAdapter searchAdapter = new SearchListAdapter(activity, output);
							listview.setAdapter(searchAdapter);
							listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
							{
								@Override
								public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
								{
									try {
										mapboxMap.setCameraPosition(new CameraPosition.Builder()
												.target(searchAdapter.position(i))
												.build()
										);

									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				});
				search.execute(searchstr);
			}
		});
		View home_button = findViewById(R.id.home_button);
		home_button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Location position = mapboxMap.getLocationComponent().getLastKnownLocation();
				LatLng LatLonPos = new LatLng();
				LatLonPos.setLatitude( position.getLatitude() );
				LatLonPos.setLongitude( position.getLongitude() );
				mapboxMap.setCameraPosition(new CameraPosition.Builder()
						.target( LatLonPos )
						.build()
				);
				//Re center list
				setRepeaterList();
			}
		});
		View add_button = findViewById(R.id.add_button);
		add_button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String url = "https://hearham.com/repeaters/add";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
	}

	public void setRepeaterList()
	{
		if( mapboxMap == null || null == mapboxMap.getCameraPosition() ) return;
		LatLng center = mapboxMap.getCameraPosition().target;
		double lat = center.getLatitude();
		double lon = center.getLongitude();
		if( lat != this.currentLat || lon != this.currentLon ) {
			if (repeaterlist == null) {
				File directory = getFilesDir();
				File repeaters = new File(directory, "repeaters.json");
				InputStream in = null;
				try {
					in = new FileInputStream(repeaters);
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder out = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						out.append(line);
					}
					repeaterlist = new JSONArray(out.toString());
					for (int i = 0; i < repeaterlist.length(); i++) {
						JSONObject c = repeaterlist.getJSONObject(i);
						if( c.getInt("operational") > 0 ) {
							repeaterFeatureList.add(Feature.fromGeometry(
									Point.fromLngLat(c.getDouble("longitude"), c.getDouble("latitude"))));
						} else {
							repeaterDownFeatureList.add(Feature.fromGeometry(
									Point.fromLngLat(c.getDouble("longitude"), c.getDouble("latitude"))));
						}
					}
				} catch (FileNotFoundException e) {
					Log.w(TAG, "Repeaterlist not available yet");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if( repeaterlist != null ) {
				ArrayList<JSONObject> list = new ArrayList<JSONObject>();
				for (int i = 0; i < repeaterlist.length(); i++) {
					try {
						if (Utils.distance(repeaterlist.getJSONObject(i), lat, lon) < 100) {
							list.add(repeaterlist.getJSONObject(i));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				nearbyRepeaterAdapter = new RepeaterListAdapter(this, list, center);
				listview = findViewById(repeaterList);
				listview.setAdapter(nearbyRepeaterAdapter);
				listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
					{
						try {
							mapboxMap.setCameraPosition(new CameraPosition.Builder()
									.target( nearbyRepeaterAdapter.repeaterPos(i) )
									.build()
							);
							//Re center list
							setRepeaterList();

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@SuppressWarnings( {"MissingPermission"})
	private void enableLocationComponent(@NonNull Style loadedMapStyle) {
		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(this)) {

		// Get an instance of the component
			LocationComponent locationComponent = mapboxMap.getLocationComponent();

		// Activate with options
			locationComponent.activateLocationComponent(
					LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

		// Enable to make component visibl
		try {
			locationComponent.setLocationComponentEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Set the component's camera mode
			locationComponent.setCameraMode(CameraMode.TRACKING, 0,this.DEFAULZOOM,null,null,null);
			//https://github.com/mapbox/mapbox-gl-native/issues/15370
		// Set the component's render mode
			locationComponent.setRenderMode(RenderMode.COMPASS);
		} else {
			permissionsManager = new PermissionsManager(this);
			permissionsManager.requestLocationPermissions(this);
		}
	}

	@Override
	public void onExplanationNeeded(List<String> permissionsToExplain) {
		Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onPermissionResult(boolean granted) {
		if (granted) {
			mapboxMap.getStyle(new Style.OnStyleLoaded() {
				@Override
				public void onStyleLoaded(@NonNull Style style) {
					enableLocationComponent(style);
				}
			});
		} else {
			Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
			finish();
		}
	}


	@Override
	protected void  onResume() {
		super.onResume();
		mapView.onResume();

		downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		//Do not start multiple downloads:
		if( downloadJSONReference == 0 ) {
			//Start download
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://hearham.com/api/repeaters/v1"));
			request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "repeaters.json");
			downloadJSONReference = downloadManager.enqueue(request);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mapView.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mapView.onStop();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(downloadReceiver);
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	protected void parseData(ParcelFileDescriptor file) {
		FileInputStream fileInputStream = new ParcelFileDescriptor.AutoCloseInputStream(file);
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(fileInputStream));
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr;

		try {
			while ((inputStr = streamReader.readLine()) != null) {
				responseStrBuilder.append(inputStr);
			}

			this.setRepeaterList();
			//Write
			File directory = getFilesDir();
			File output = new File(directory, "repeaters.json");
			FileOutputStream fos = new FileOutputStream(output);
			try (Writer w = new OutputStreamWriter(fos, "UTF-8")) {
				w.write(responseStrBuilder.toString());
			}
		} catch ( IOException ioe) {
			ioe.printStackTrace();

		}
	}

	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if(downloadJSONReference == referenceId){
				try {
					//Store:
					parseData(downloadManager.openDownloadedFile(downloadJSONReference));
					String uri = downloadManager.getUriForDownloadedFile(downloadJSONReference).toString();
					//Do not leave a file in Android/data/com.hearham.repeaterstart/files/Download for every download:
					downloadManager.remove(downloadJSONReference);
					//Allow another download on resume.
					downloadJSONReference = 0;
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	};
}