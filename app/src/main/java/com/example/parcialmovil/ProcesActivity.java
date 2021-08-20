package com.example.parcialmovil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;

public class ProcesActivity extends AppCompatActivity implements OnMapReadyCallback{
    private TextView txtPAIS;
    private ImageView imgpais;
    private GoogleMap mMap;
    // variables para mantener los datos
    private SharedPreferences preferences;
    private String Alpha2Code,pais,capital, CodeISO2, CodeISONum, CodeISO3, CodeFIPS, TelPref, Center, geowest,geoeast,geonorth,geosouth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        init();
        getdata();
        txtPAIS.setText(pais);
        Glide.with(this).load("http://www.geognos.com/api/en/countries/flag/"+Alpha2Code+".png").into(imgpais);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);






    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        Log.d("Ce",Center);
        LatLng ltLng = new LatLng(-1.0127291289943696, -79.4694318818125);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(ltLng, 17);
        mMap.animateCamera(cameraUpdate);


        PolylineOptions lines = new PolylineOptions()
                .add(new LatLng(Double.parseDouble(geonorth),Double.parseDouble(geowest)))
                .add(new LatLng(Double.parseDouble(geonorth),Double.parseDouble(geoeast)))
                .add(new LatLng(Double.parseDouble(geosouth),Double.parseDouble(geoeast)))
                .add(new LatLng(Double.parseDouble(geosouth),Double.parseDouble(geowest)))
                .add(new LatLng(Double.parseDouble(geonorth),Double.parseDouble(geowest)));
        mMap.addPolyline(lines);

    }

    private void init(){
        txtPAIS = (TextView) findViewById(R.id.txtPAIS);
        imgpais = (ImageView) findViewById(R.id.imgpais);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
    }

    public void getdata(){
        Alpha2Code = preferences.getString("Alpha2Code",null);
        pais= preferences.getString("pais",null);
        capital= preferences.getString("capital",null);
        CodeISO2= preferences.getString("CodeISO2",null);
        CodeISONum= preferences.getString("CodeISONum",null);
        CodeISO3= preferences.getString("CodeISO3",null);
        CodeFIPS= preferences.getString("CodeFIPS",null);
        TelPref= preferences.getString("TelPref",null);
        Center= preferences.getString("Center",null);
        geowest= preferences.getString("geowest",null);
        geoeast= preferences.getString("geoeast",null);
        geonorth= preferences.getString("geonorth",null);
        geosouth= preferences.getString("geosouth",null);
    }

}
