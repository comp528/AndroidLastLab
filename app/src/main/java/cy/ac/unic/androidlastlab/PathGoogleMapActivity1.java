package cy.ac.unic.androidlastlab;

import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;

//import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.maps.model.Marker;



import android.location.Criteria;
//import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.PowerManager;

//import android.content.Context;

import android.widget.ToggleButton;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;



public class PathGoogleMapActivity1 extends FragmentActivity {


    GoogleMap googleMap;
   // final String TAG = "PathGoogleMapActivity1";

    private Marker marker;

   // private PowerManager.WakeLock wakeLock; // used to prevent device sleep
    private boolean gpsFix; // whether we have a GPS fix for accurate data
    private boolean tracking; // whether app is currently tracking

    private String device = "dima";


    //ArrayList<LatLng> points = null;
    PolylineOptions polyLineOptions = null;

    private boolean ff = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        tracking = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_google_map_activity1);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();

        polyLineOptions = new PolylineOptions();

        polyLineOptions.width(2);
        polyLineOptions.color(Color.RED);

  // create Criteria object to specify location provider's settings
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // fine location data
        criteria.setBearingRequired(true); // need bearing to rotate map
        criteria.setCostAllowed(true); // OK to incur monetary cost
        criteria.setPowerRequirement(Criteria.POWER_LOW); // try to conserve
        criteria.setAltitudeRequired(false); // don't need altitude data

        // get the LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // get the best provider based on our Criteria
        String provider = locationManager.getBestProvider(criteria, true);
        // listen for changes in location as often as possible
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        // get the app's power manager
        //PowerManager powerManager =(PowerManager) getSystemService(Context.POWER_SERVICE);

        // get a wakelock preventing the device from sleeping
        //PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
        //wl.acquire(); // acquire the wake lock -- doesn't work on an emulator :(


        ToggleButton trackingToggleButton = (ToggleButton) findViewById(R.id.trackingToggleButton1);
        trackingToggleButton.setOnCheckedChangeListener(
                new OnCheckedChangeListener() {
                    // called when user toggles tracking state
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        tracking=isChecked;
                    }
                }
                                                );
    }

    // responds to events from the LocationManager
    private final LocationListener locationListener =
            new LocationListener()
            {
                // when the location is changed
                public void onLocationChanged(Location location)
                {
                    gpsFix = true; // if getting Locations, then we have a GPS fix

                   // if (tracking) // if we're currently tracking
                        updateLocation(location); // update the location
                } // end onLocationChanged

                public void onProviderDisabled(String provider)
                {
                } // end onProviderDisabled

                public void onProviderEnabled(String provider)
                {
                } // end onProviderEnabled

                public void onStatusChanged(String provider,
                                            int status, Bundle extras)
                {
                } // end onStatusChanged
            }; // end locationListener


    public void updateLocation(Location location)
    {

        LatLng l;

        if (location != null && gpsFix) // location not null; have GPS fix
        {
            l = new LatLng( location.getLatitude(), location.getLongitude() );

           

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l,13));

            if (ff) {
               ff = false;
                marker = googleMap.addMarker( new MarkerOptions().position(l));

            } else {
                marker.setPosition(l);

            }

            if (tracking) {
                polyLineOptions.add(l);
                googleMap.addPolyline(polyLineOptions);


// save tracking info

                String base = "http://android.unic.ac.cy/put.aspx";
                String lng = "lng="+location.getLongitude();
                String ltd = "ltd="+location.getLatitude();
                String time = "time="+location.getTime();
                String url = base+"?device="+device+"&"+lng+"&"+ltd+"&"+time;

                SaveGPS sGPS = new SaveGPS();
                sGPS.execute(url);


            }


        }
    } // end method updateLocation


    private class SaveGPS extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                Log.d("save", "URL: "+url[0]);
                data = http.readUrl(url[0]);
                Log.d("save", "DATA: "+data);
            } catch (Exception e) {
                Log.d("save", "BACKGROUND: "+e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("save", "SAVE: "+result);

        }
    }



 }

