package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;

import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private TextView feedback;
    private int markerCnt = 0;
    public LatLng lol;
    public JSONObject ob = new JSONObject();
    final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public String test = "{\n" +
            "    \"id\": 2, \n" +
            "    \"lat\": 2.0, \n" +
            "    \"lo\": 1.0, \n" +
            "    \"message\": \"test\", \n" +
            "    \"name\": \"albert\", \n" +
            "    \"when\": \"Mon, 31 Jan 2022 08:31:56 GMT\"\n" +
            "  }";
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        feedback = findViewById(R.id.feedback);
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lol = new LatLng(location.getLatitude(),location.getLongitude());
                            feedback.setText(lol.toString());
                        }
                    }
                });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Innsbruck and move the camera
        LatLng ibk = new LatLng(47.259659,11.400375);
        mMap.addMarker(new MarkerOptions().position(ibk).title("Marker in Innsbruck"));

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ibk,10)); //Values from 2 to 21 possible
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        SimpleAsyncTask1 task = new SimpleAsyncTask1();
        task.execute("https://os-beyond.at/htl/geo/geoinfos");

        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lol, 18));
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        feedback.setText(marker.getTitle());

        return true; //event is consumed
    }

    @Override
    public void onMapClick(LatLng latLng) {

        mMap.addMarker(new MarkerOptions().position(latLng).title(String.format("Marker %d", ++markerCnt)));
        Info todo = new Info();
        todo.lo =latLng.longitude;
        todo.lat =latLng.latitude;
        todo.name = "FabianL";
        todo.message = "Testthefirst";
        Gson g = new Gson();
        String jso= g.toJson(todo);
         ob = new JSONObject();

        try {
            JSONObject obl = new JSONObject(jso);
            obl.put("long", obl.get("lo"));
            obl.remove("lo");
            ob.put("info", obl );


        } catch (JSONException e) {
            e.printStackTrace();
        }
        SimpleAsyncTask taskPut = new SimpleAsyncTask();
        taskPut.execute(" https://os-beyond.at/htl/geo/geoinfo/0");

    }

    private class SimpleAsyncTask1 extends AsyncTask<String, Void, String> {
        @lombok.SneakyThrows  //to avoid exception handling
        @Override
        protected String doInBackground(String... urls) {
            //find more information on https://github.com/square/okhttp
            OkHttpClient client = new OkHttpClient();
            Request request =
                    new Request.Builder()
                            .url(urls[0])
                            .build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response.isSuccessful()) {
                try {
                    return response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return "Download failed";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.w("warn",result);
            JSONArray array = new JSONArray();
            try {
                 array = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            for (int i = 0; i < array.length(); i++) {
                Gson gson = new Gson();
                try {
                    JSONObject jso = array.getJSONObject(i);
                    jso.put("lo", jso.get("long"));
                    jso.remove("long");
                    Info todo = gson.fromJson(array.getString(i), Info.class);
                    LatLng latlng = new LatLng(todo.lat,todo.lo);
                   // Log.w("warn", String.valueOf(todo.lo));
                    mMap.addMarker(new MarkerOptions().position(latlng).title(String.format("Marker %s", todo.message)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }



    private class SimpleAsyncTask extends AsyncTask<String, Void, String> {
        @lombok.SneakyThrows  //to avoid exception handling
        @Override
        protected String doInBackground(String... urls) {
            //find more information on https://github.com/square/okhttp
            Gson g = new Gson();

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(ob.toString(), JSON);
            Log.w("warn",ob.toString());
            Request request =
                    new Request.Builder()
                            .url(urls[0])
                            .addHeader("Accept","application/json")
                            .addHeader("appkey","f8585fa4-e1a4-40dc-b07f-1dc2a9281ae4")
                            .put(body)
                            .build();

            String responseStr = "";
            try  {
                Response response = client.newCall(request).execute();
                //responseStr =  response.body().string();
                responseStr =  response.toString();
                Log.d("response", responseStr);
            } catch (IOException e)
            {
                Log.e("response", String.valueOf(e));
            }
            return responseStr;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    class Info
    {
        @Getter
        private int id;
        private double lat;
        private double lo;
        private String message;
        private String name;
        private String when;
    }

}