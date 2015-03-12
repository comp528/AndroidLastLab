package cy.ac.unic.androidlastlab;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

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

public class PathGoogleMapsActivity extends FragmentActivity {

    private static final LatLng UNIC = new LatLng(35.166187,33.315117);
    private static final LatLng UNICAFE = new LatLng(35.1655907,33.3210774);
    private static final LatLng MUSEUM = new LatLng(35.1718321,33.3565632);

    GoogleMap googleMap;
    //final String TAG = "PathGoogleMapActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_google_maps);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();

      //  MarkerOptions options = new MarkerOptions();
      //  options.position(UNIC);
      //  options.position(UNICAFE);
      //  options.position(MUSEUM);
      //  googleMap.addMarker(options);


        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UNIC,13));


        addMarkers();

    }

    private String getMapsApiDirectionsUrl() {
        String waypoints = "waypoints=optimize:true|"
                + UNIC.latitude + "," + UNIC.longitude
                + "|"  + UNICAFE.latitude + "," + UNICAFE.longitude
                + "|" + MUSEUM.latitude + "," + MUSEUM.longitude;

        String origin = "origin=" + UNIC.latitude + "," + UNIC.longitude;
        String destination= "destination=" + MUSEUM.latitude + "," + MUSEUM.longitude;
        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor +"&" + origin + "&"+ destination;
        String output = "json";
       return  "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;
        //return url;
    }

    private void addMarkers() {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(UNIC).title("University of Nicosia"));
            googleMap.addMarker(new MarkerOptions().position(UNICAFE).title("UNICAFE"));
            googleMap.addMarker(new MarkerOptions().position(MUSEUM).title("Museum"));
        }
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                Log.d("routes task", "URL: "+url[0]);
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("routes task", "BACKGROUND: "+e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("routes task", "ROUTES: "+result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("routes task", "JSON: "+e.toString());
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points ;
            PolylineOptions polyLineOptions = null ;


            try {

                // traversing through routes
                for (int i = 0; i < routes.size(); i++) {
                    points = new ArrayList<LatLng>();
                    polyLineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = routes.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    polyLineOptions.addAll(points);
                    polyLineOptions.width(2);
                    polyLineOptions.color(Color.BLUE);
                }

                googleMap.addPolyline(polyLineOptions);

            }
            catch (Exception e) {
            //    e.printStackTrace();
                Log.d("routes task", "Points "+e.toString());
            }

        }

    }
}
