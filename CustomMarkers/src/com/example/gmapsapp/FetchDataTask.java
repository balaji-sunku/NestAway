package com.example.gmapsapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class FetchDataTask extends AsyncTask<Void, Void, ArrayList<House>> {

	private final String TAG = "FetchDataTask";
	
	private String urlToFetchFrom;
	private ArrayList<House> listOfHouses = null;
	
	private DataFetchListener dataFetchListener = null;
	
	public FetchDataTask(String urlToFetchFrom, DataFetchListener dataFetchListener){
		this.urlToFetchFrom = urlToFetchFrom; 
		this.dataFetchListener = dataFetchListener;
	}
	
	public interface DataFetchListener{
		public void onFetchComplete(ArrayList<House> housesInfo);
	}
	
	@Override
	protected ArrayList<House> doInBackground(Void... params) {
		
		InputStream inputStream = null;
        String result = "";
        listOfHouses = new ArrayList<House>();
		
		try {
			 
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
 
            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(urlToFetchFrom));
 
            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
 
            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";
 
        } catch (Exception e) {
            Log.e(TAG, "Exception while fetching the data :"+e.toString());
        }
		
		parseAndFillTheData(result);
		
		return listOfHouses;
	}
	
	private void parseAndFillTheData(String jsonResponse) {
		
		try {
			JSONObject resultObject = new JSONObject(jsonResponse);
			JSONArray jsonArray = resultObject.getJSONArray("houses");
			Log.d(TAG,"Number of houses:"+jsonArray.length());
			for(int i=0;i<jsonArray.length();i++){
				House house = new House();
				JSONObject houseJson = (JSONObject) jsonArray.get(i);
				house.setBhkDetails(houseJson.getString("bhk_details"));
				house.setLocality(houseJson.getString("locality"));
				house.setLatitude(houseJson.getDouble("lat_double"));
				house.setLongitude(houseJson.getDouble("long_double"));
				house.setGender(houseJson.getString("gender"));
				house.setSharingCount(houseJson.getInt("shared"));
				listOfHouses.add(house);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Exception while parsing the data :"+e.toString());
			e.printStackTrace();
		}
		
	}

	private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
 
        inputStream.close();
        return result;
 
    }
	
	@Override
	protected void onPostExecute(ArrayList<House> listOfHouses) {
		super.onPostExecute(listOfHouses);
		if(dataFetchListener != null){
			dataFetchListener.onFetchComplete(listOfHouses);
		}
	}

}
