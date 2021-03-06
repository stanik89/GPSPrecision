package pl.pollub.gpsprecision;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {
    public DatabaseHelper database;
    GPSTracker gpsTracker;
    NetworkTracker networkTracker;
    NetworkTrackerWithInternetConnectivity networkTrackerWithInternetConnectivity;
    Button btnShowGPSLocation;
    Button btnShowNetworkLocation;
    Button btnShowNetworkLocationWithInternetConnectivity;

    private GoogleMap mMap; //musi być null jesli google play serwisy apk nie sa dostepne
    private GoogleApiClient client;
    List<String[]> markersForTest = null;
    private MarkerOptions longClickMarker;
    private MarkerOptions shortClickMarker;
    List<ScanResult> apList;
    WifiManager wifiManager;

    //@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        database = new DatabaseHelper(this);
        markersForTest = new ArrayList<String[]>();

        //lokalizacja z gpsTracker
        btnShowGPSLocation = (Button) findViewById(R.id.GPSButton);
        btnShowGPSLocation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                gpsTracker = new GPSTracker(MapsActivity.this);

                if (gpsTracker.canGetLocation()) {
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();

                    apList = wifiManager.getScanResults();

                    //Toast.makeText(getApplicationContext(), "Liczba access pointów: " + apList.size(), Toast.LENGTH_LONG).show();

                    if (latitude != 0 && longitude != 0) {
                        Toast.makeText(getApplicationContext(),
                                "Twoje położenie to -\nSzerokość: " + latitude + "\nDługość: "
                                        + longitude, Toast.LENGTH_LONG).show();
                        List<MyMarker> markers = database.getNearestMarker();

                        for (MyMarker marker : markers) {

                            Location nearestMarker = new Location("Lokalizacja z testu");
                            nearestMarker.setLatitude(marker.getLatitude());
                            nearestMarker.setLongitude(marker.getLongitude());

                            Location locationFromGPS = new Location("Lokalizacja z GPS");
                            locationFromGPS.setLatitude(latitude);
                            locationFromGPS.setLongitude(longitude);

                            float distanceTo = nearestMarker.distanceTo(locationFromGPS);

                            marker.setDistanceFrom(distanceTo);

                            BigDecimal roundDistance;
                            roundDistance = round(distanceTo, 2);

                            markersForTest.add(new String[]{
                                    "GPS",
                                    String.valueOf(latitude),
                                    String.valueOf(longitude),
                                    String.valueOf(roundDistance + " m"),
                                    String.valueOf(apList.size())
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Nie można odczytać położenia z GPS", Toast.LENGTH_LONG).show();
                    }

                } else {
                    gpsTracker.showGPSAlert();
                }
            }
        });

        //lokalizacja z sieci
        btnShowNetworkLocation = (Button) findViewById(R.id.networkButton);
        btnShowNetworkLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                networkTracker = new NetworkTracker(MapsActivity.this);

                if (networkTracker.canGetLocation()) {
                    double latitude = networkTracker.getLatitude();
                    double longitude = networkTracker.getLongitude();

                    if (latitude != 0 && longitude != 0) {
                        Toast.makeText(getApplicationContext(),
                                "Twoje położenie to -\nSzerokość: " + latitude + "\nDługość: "
                                        + longitude, Toast.LENGTH_LONG).show();

                        List<MyMarker> markers = database.getNearestMarker();

                        for (MyMarker marker : markers) {

                            Location nearestMarker = new Location("Lokalizacja z testu");
                            nearestMarker.setLatitude(marker.getLatitude());
                            nearestMarker.setLongitude(marker.getLongitude());

                            Location locationFromNetwork = new Location("Lokalizacja z internetu");
                            locationFromNetwork.setLatitude(latitude);
                            locationFromNetwork.setLongitude(longitude);

                            float distanceTo = nearestMarker.distanceTo(locationFromNetwork);

                            marker.setDistanceFrom((int) distanceTo);

                            BigDecimal roundDistance;
                            roundDistance = round(distanceTo, 2);

                            markersForTest.add(new String[]{
                                    "Sieć",
                                    String.valueOf(latitude),
                                    String.valueOf(longitude),
                                    String.valueOf(roundDistance + " m"),
                                    String.valueOf(apList.size())
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Nie można odczytać położenia z sieci", Toast.LENGTH_LONG).show();
                    }

                } else {
                    networkTracker.showNetworkAlert();
                }
            }
        });

        //lokalizacja z sieci z włączonym dostępem do internetu
        btnShowNetworkLocationWithInternetConnectivity = (Button) findViewById(R.id.internetButton);
        btnShowNetworkLocationWithInternetConnectivity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                networkTrackerWithInternetConnectivity = new NetworkTrackerWithInternetConnectivity(MapsActivity.this);

                if (networkTrackerWithInternetConnectivity.canGetLocation()) {
                    double latitude = networkTrackerWithInternetConnectivity.getLatitude();
                    double longitude = networkTrackerWithInternetConnectivity.getLongitude();

                    if (latitude != 0 && longitude != 0) {
                        Toast.makeText(getApplicationContext(),
                                "Twoje położenie to -\nSzerokość: " + latitude + "\nDługość: "
                                        + longitude, Toast.LENGTH_LONG).show();

                        List<MyMarker> markers = database.getNearestMarker();

                        for (MyMarker marker : markers) {

                            Location nearestMarker = new Location("Lokalizacja z testu");
                            nearestMarker.setLatitude(marker.getLatitude());
                            nearestMarker.setLongitude(marker.getLongitude());

                            Location locationFromNetworkWithInternetConnectivity = new Location("Lokalizacja z internetu");
                            locationFromNetworkWithInternetConnectivity.setLatitude(latitude);
                            locationFromNetworkWithInternetConnectivity.setLongitude(longitude);

                            float distanceTo = nearestMarker.distanceTo(locationFromNetworkWithInternetConnectivity);

                            marker.setDistanceFrom((int) distanceTo);

                            BigDecimal roundDistance;
                            roundDistance = round(distanceTo, 2);

                            markersForTest.add(new String[]{
                                    "Internet",
                                    String.valueOf(latitude),
                                    String.valueOf(longitude),
                                    String.valueOf(roundDistance + " m"),
                                    String.valueOf(apList.size())
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Nie można odczytać położenia z internetu", Toast.LENGTH_LONG).show();
                    }

                } else {
                    networkTrackerWithInternetConnectivity.showInternetAlert();
                }
            }
        });

        // dodawanie markera na długie dotkniecie mapy
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

                longClickMarker = new MarkerOptions()
                        .position(latLng)
                        .title("Start")
                        .snippet(latLng.latitude + ", " + latLng.longitude)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                mMap.clear();

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                mMap.addMarker(longClickMarker);
            }
        });

        // dodawanie markera na krotkie dotkniecie mapy
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                shortClickMarker = new MarkerOptions()
                        .position(latLng)
                        .title("Marker do badania")
                        .snippet(latLng.latitude + ", " + latLng.longitude)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                updateShortDistance();
            }
        });
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void onChangeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.startTest:
                Toast.makeText(getApplicationContext(),
                        "Do testu zostanie wybranych 5 punktów w pobliżu Twojej obecnej lokalizacji", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),
                        "Przeliczanie odległości...", Toast.LENGTH_SHORT).show();
                updateDistance();
                getMarkersForTest();
                return true;
            case R.id.saveResults:
                Toast.makeText(getApplicationContext(),
                        "Zapisywanie...", Toast.LENGTH_SHORT).show();
                try {
                    saveToCSVFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.showAllMarkers:
                getAllMarkers();
                return true;
            case R.id.clearMap:
                Toast.makeText(getApplicationContext(),
                        "Czyszczenie mapy...", Toast.LENGTH_SHORT).show();
                mMap.clear();
                return true;
            case R.id.closeApp:
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            if (mMap != null) {
                setUpMap();
            }
        }
    }

    public void getAllMarkers() {
        List<MyMarker> markers = database.getAllMarkers();

        for (MyMarker myMarker : markers) {
            LatLng latLng = new LatLng(myMarker.getLatitude(), myMarker.getLongitude());

            MarkerOptions marker = new MarkerOptions()
                    .position(latLng)
                    .title(myMarker.getName())
                    .snippet("Współrzędne: " + myMarker.getLatitude() + ", " + myMarker.getLongitude())
                    .anchor(0.5f, 0.5f);

            mMap.addMarker(marker);
        }
    }

    public void getMarkersForTest() {
        List<MyMarker> markersFortest = database.getMarkersForTest();

        for (MyMarker myMarker : markersFortest) {
            LatLng latLng = new LatLng(myMarker.getLatitude(), myMarker.getLongitude());

            MarkerOptions marker = new MarkerOptions()
                    .position(latLng)
                    .title(myMarker.getName())
                    .snippet("Współrzędne: " + myMarker.getLatitude() + ", " + myMarker.getLongitude())
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_36dp));

            mMap.addMarker(marker);
        }
    }

    public void updateDistance() {
        List<MyMarker> allMarkers = database.getAllMarkers();

        for (MyMarker marker : allMarkers) {
            Location currentLocation = new Location("Moja Lokalizacja");
            currentLocation.setLatitude(longClickMarker.getPosition().latitude);
            currentLocation.setLongitude(longClickMarker.getPosition().longitude);

            Location markerLocation = new Location("Lokalizacja markera");
            markerLocation.setLatitude(marker.getLatitude());
            markerLocation.setLongitude(marker.getLongitude());

            float distanceTo = currentLocation.distanceTo(markerLocation);

            marker.setDistanceFrom((int) distanceTo);

            database.updateMarker(marker);
        }
    }

    public void updateShortDistance() {
        List<MyMarker> testMarkers = database.getMarkersForTest();

        for (MyMarker marker : testMarkers) {
            Location currentLocation = new Location("Moja Lokalizacja");
            currentLocation.setLatitude(shortClickMarker.getPosition().latitude);
            currentLocation.setLongitude(shortClickMarker.getPosition().longitude);

            Location markerLocation = new Location("Lokalizacja markera");
            markerLocation.setLatitude(marker.getLatitude());
            markerLocation.setLongitude(marker.getLongitude());

            float distanceTo = currentLocation.distanceTo(markerLocation);

            marker.setDistanceFrom((int) distanceTo);

            database.updateMarker(marker);
        }

        Toast.makeText(getApplicationContext(),
                "Przeliczanie odległości...", Toast.LENGTH_SHORT).show();
    }

    private void saveToCSVFile() throws IOException {
        //tworzenie folderu, jeśli nie istnieje
        File directory = new File(Environment.getExternalStorageDirectory().toString() + "/GPSPrecision");

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

        if (directory != null && !directory.exists()
                && !directory.mkdirs()) {
            try {
                throw new IOException("Cannot create dir "
                        + directory.getAbsolutePath());
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        CSVWriter writer = new CSVWriter(new FileWriter("/storage/emulated/0/GPSPrecision/wyniki_z_" + dateFormat.format(date) + ".csv"), ',');

        List<String[]> data = new ArrayList<String[]>();
        List<MyMarker> makrersToCSV = database.getMarkersForTest();

        data.add(new String[]{"Nazwa Markera", "Szerokosc", "Długosc", "Odleglosc", "Ilość punktów dostępu"});
        for (MyMarker myMarker : makrersToCSV) {
            data.add(new String[]{
                    myMarker.getName(),
                    String.valueOf(myMarker.getLatitude()),
                    String.valueOf(myMarker.getLongitude())
            });
        }
        data.add(new String[]{"Pomiary z testu"});
        writer.writeAll(data);

        if (markersForTest != null) {
            writer.writeAll(markersForTest);
        }
        writer.close();
    }

    private void setUpMap() {
        gpsTracker = new GPSTracker(MapsActivity.this);

        double latitude = 0;
        double longitude = 0;

        if (gpsTracker.canGetLocation()) {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }

        LatLng myLoc = new LatLng(latitude, longitude);

        if (myLoc != null) {
            mMap.setMyLocationEnabled(true);
        }

        LatLng lublinStareMiasto = new LatLng(51.246465, 22.572695);

        //ustawia przy wlaczeniu aplikacji na punkt w ktorym jestesmy
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lublinStareMiasto, 11));

        //właczenie zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.lukasz.gpsprecision/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.lukasz.gpsprecision/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }
}
