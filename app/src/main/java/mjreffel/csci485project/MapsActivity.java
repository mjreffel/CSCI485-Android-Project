package mjreffel.csci485project;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private LocationListener ll;
    private LocationManager lm;

    private Geocoder geo;

    //School's address so you can simply type a building number for directions
    private String schoolAddress = ",900 Fifth St, Nanaimo, BC V9R5S5";

    private Location start;
    private Location destination;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Setup the Location stuff

        ll = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                Log.d("Current Location?", location.toString());
                Log.d("Latitde", Double.toString(location.getLatitude()));
                Log.d("Longitude", Double.toString(location.getLongitude()));
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        //Setup the Location Manager to get the current location
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Setup the Geocoder object so you can use it later
        geo = new Geocoder(getApplicationContext(), Locale.getDefault());

        //Setup the Start and End locations to use later
        start = new Location("Start");
        destination = new Location("Destination");

        //Setup Edit Text listener functions
        //Start Address edit text view
        final EditText startAddr = (EditText) findViewById(R.id.startAddress);
        startAddr.setOnKeyListener(new EditText.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d("enter", "The enter key was pressed in the Start Address field");
                    String temp = readTextField(startAddr);
                    Log.d("String read was", temp);

                    return true;
                }
                return false;
            }
        });
        //End Address edit text view
        final EditText endAddr = (EditText) findViewById(R.id.endAddress);
        endAddr.setOnKeyListener(new EditText.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Log.d("enter", "The enter key was pressed in the End Address field");
                    String temp = readTextField(endAddr);
                    Log.d("String read was", temp);
                    //Try to geocode the building number entered
                    Location location = getCurrentLocation(ll, lm);
                    try {
                        Log.d("Sleep", "Starting to sleep to allow for time to get location");
                        Thread.sleep(10000);// Allow for time to get location
                        Log.d("Sleep", "Ending Sleep");
                    }  catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Log.d("Current Latitude", Double.toString(location.getLatitude()));
                    Log.d("Current Longitude", Double.toString(location.getLongitude()));

                    //Try to add location as marker on map
                    /*
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude()))
                            .title("Current Location"));
                            */

                    //Geocode the Building number entered
                    setDestinationFromAddress(temp + schoolAddress);



                    return true;
                }
                return false;
            }
        });

        setUpMapIfNeeded();

    }

    //Reads the text entered and clears it
    protected String readTextField(EditText editText) {
        String temp = editText.getText().toString();
        editText.setText("");
        return temp;
    }

    protected Location getCurrentLocation(LocationListener ll, LocationManager lm ) {
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        //check if allowed to get locations
        String permissionCheck = "android.permission.ACCESS_COARSE_LOCATION";
        int res = getApplicationContext().checkCallingOrSelfPermission(permissionCheck);
        if(res == PackageManager.PERMISSION_GRANTED) {
            lm.requestSingleUpdate(lm.GPS_PROVIDER, ll, null);
        }
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return loc;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        //Hardcoded in VIU for zoomed location
        LatLng campus = new LatLng(49.156502, -123.965817);

        CameraPosition cameraPosition = new CameraPosition.Builder().target(campus).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }


    public void setDestinationFromAddress(String youraddress) {
        //Use geocoder to retrieve address latitude and longitude
        try {
            List results = geo.getFromLocationName(youraddress, 5);

            if(results.isEmpty()) {
                Log.d("ERROR", "The address returned No results");
            } else {
                Log.d("First", results.get(0).toString());
            }


        } catch (Exception e) {

        }


    }



}
