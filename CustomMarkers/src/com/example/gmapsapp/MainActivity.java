package com.example.gmapsapp;


import java.util.ArrayList;

import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements 
  GooglePlayServicesClient.ConnectionCallbacks,
  GooglePlayServicesClient.OnConnectionFailedListener,
  OnClickListener, FetchDataTask.DataFetchListener{

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	@SuppressWarnings("unused")
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9002;
	GoogleMap mMap;

	@SuppressWarnings("unused")
	private static final double SEATTLE_LAT = 47.60621,
	SEATTLE_LNG =-122.33207, 
	SYDNEY_LAT = -33.867487,
	SYDNEY_LNG = 151.20699, 
	NEWYORK_LAT = 40.714353, 
	NEWYORK_LNG = -74.005973;
	private static final float DEFAULTZOOM = 12;
	@SuppressWarnings("unused")
	private static final String LOGTAG = "Maps";
	
	LocationClient mLocationClient;
//	Marker marker;
	
	private Button fetchData = null;
	private final String url = "http://a88a4240.ngrok.io/";
	
	private final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (servicesOK()) {
			setContentView(R.layout.activity_home);

			if (initMap()) {
//				mMap.setMyLocationEnabled(true);
				mLocationClient = new LocationClient(this, this, this);
				mLocationClient.connect();
			}
			else {
				Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			setContentView(R.layout.activity_home);
		}
		
		initActivity();
//		gotoCurrentLocation();

	}
	
	private void initActivity() {
		fetchData = (Button) findViewById(R.id.fetchData);
		fetchData.setOnClickListener(this);
	}

	private void fetchDataFromServer() {
		Toast.makeText(this, "Please wait..", Toast.LENGTH_SHORT).show();
		new FetchDataTask(url,this).execute();
		fetchData.setEnabled(false);
	}
	
	@Override
	public void onFetchComplete(ArrayList<House> housesInfo) {
		Log.d(TAG,"Data loaded.. Found "+ housesInfo.size() +" houses Creating markers");
		Toast.makeText(this, "Data loaded.. Found "+ housesInfo.size() +" houses Creating markers", Toast.LENGTH_LONG).show();
		createrMarkersForHouses(housesInfo);
		fetchData.setEnabled(true);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.fetchData:
			fetchDataFromServer();
			break;
		}
	}

	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		}
		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		}
		else {
			Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private boolean initMap() {
		if (mMap == null) {
			SupportMapFragment mapFrag =
					(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = mapFrag.getMap();
		}
		return (mMap != null);
	}

	private void gotoLocation(double lat, double lng,
			float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}

	public void createrMarkersForHouses(ArrayList<House> listOfHouses) {

		if(listOfHouses.size() <= 0){
			Toast.makeText(this, "No houses found", Toast.LENGTH_SHORT).show();
			return;
		}
		
//		EditText et = (EditText) findViewById(R.id.editText1);
//		String location = et.getText().toString();
//		if (location.length() == 0) {
//			Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
//			return;
//		}
//
//		hideSoftKeyboard(v);
//		
//		Geocoder gc = new Geocoder(this);
//		List<Address> list = gc.getFromLocationName(location, 1);
//		Address add = list.get(0);
//		String locality = add.getLocality();
//		Toast.makeText(this, locality, Toast.LENGTH_LONG).show();
		
//		double lat = listOfHouses.get(0).getLatitude();
//		double lng = listOfHouses.get(0).getLongitude();
//		
//		gotoLocation(lat, lng, DEFAULTZOOM);
		
		mMap.clear();
		
		for(int i=0;i<100;i++){
			String locality = listOfHouses.get(i).getLocality();
			double lat1 = listOfHouses.get(i).getLatitude();
			double lng1 = listOfHouses.get(i).getLongitude();

			gotoLocation(lat1, lng1, DEFAULTZOOM);
			
			setMarker(listOfHouses.get(i), lat1, lng1);
//			Toast.makeText(this, "setting marker for :"+locality+",lat:"+lat1+",long:"+lng1, Toast.LENGTH_SHORT).show();
		}
		
	}

	private void hideSoftKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		MapStateManager mgr = new MapStateManager(this);
		mgr.saveMapState(mMap);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MapStateManager mgr = new MapStateManager(this);
		CameraPosition position = mgr.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(update);
			//			This is part of the answer to the code challenge
			mMap.setMapType(mgr.getSavedMapType());
		}
		
	}

	protected void gotoCurrentLocation() {
		Location currentLocation = mLocationClient.getLastLocation();
		if (currentLocation == null) {
			Toast.makeText(this, "Current location isn't available", Toast.LENGTH_SHORT).show();
		}
		else {
			LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULTZOOM);
			mMap.animateCamera(update);
		}
		
		setMarker(null, 
				currentLocation.getLatitude(), 
				currentLocation.getLongitude());
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
//		Toast.makeText(this, "Connected to location service", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDisconnected() {
	}
	
	ArrayList<Marker> markers = new ArrayList<Marker>();

	private void setMarker(House house, double lat, double lng) {
		
		
//		if (marker != null) {
//			marker.remove();
//		}

//		MarkerOptions options = new MarkerOptions()
//			.title(locality)
//			.position(new LatLng(lat, lng));
//		mMap.addMarker(options);
		
		String locality = "" ;
		
		if(house != null){
			locality = house.getLocality();
		}
		
		MarkerOptions options = new MarkerOptions()	
		.title(locality)
		.position(new LatLng(lat, lng))
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker))
		//		.icon(BitmapDescriptorFactory.defaultMarker())
		.anchor(.5f, .5f)
		.draggable(true);
		options.snippet("Gender : " + house.getGender() + "\n Sharing :" +house.getSharingCount());

		markers.add(mMap.addMarker(options));
	
	}

}
