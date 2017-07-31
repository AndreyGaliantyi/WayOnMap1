package com.example.andrey.wayonmap1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    AppCompatButton button;
    GoogleMap gMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=(AppCompatButton) findViewById(R.id.button);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnectingToInternet())
                try {
                    addPolyline();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                else Toast.makeText(MainActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap=googleMap;
        gMap.getUiSettings().setZoomControlsEnabled(true);
        button.setEnabled(true);
    }


    public void addPolyline() throws JSONException {
        FetchRouteTask fetchRouteTask=new FetchRouteTask();
        fetchRouteTask.execute();
        String jsonString= null;

        try {
            jsonString = (String) fetchRouteTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(jsonString==null){
            Toast.makeText(this, "Путь не загружен", Toast.LENGTH_SHORT).show();
            return;}
        JSONObject jsonObject= new JSONObject(jsonString);
        JSONArray jsonArray=jsonObject.getJSONArray("coords");
        int arrayLength=jsonArray.length();
        ArrayList<LatLng> latLngList=new ArrayList<>(arrayLength);
        for (int i=0;i<arrayLength;i++){
            JSONObject jsonLatLng=jsonArray.getJSONObject(i);
            latLngList.add(i,new LatLng(jsonLatLng.getDouble("la"),jsonLatLng.getDouble("lo")));
        }
        PolylineOptions options=new PolylineOptions();
        options.addAll(latLngList);
        gMap.addPolyline(options);
        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        for(LatLng latLng:latLngList)builder.include(latLng);
        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 30));
    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null)
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    return true;
                }

        }
        return false;
    }
}