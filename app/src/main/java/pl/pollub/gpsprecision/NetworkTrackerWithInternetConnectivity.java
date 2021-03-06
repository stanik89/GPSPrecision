package pl.pollub.gpsprecision;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

public class NetworkTrackerWithInternetConnectivity extends Service implements LocationListener {
	private final Context context;

	boolean isMobileInternetEnabled = false;
	boolean isWifiInternetEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;

	String bestProvider;
	Criteria criteria;

	Location location;

	double latitude;
	double longitude;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;//1m
	private static final long MIN_TIME_BW_UPDATES = 1000;//1 sekunda

	LocationManager locationManager;

	public NetworkTrackerWithInternetConnectivity(Context context) {
		this.context = context;
		getInternetLocation();
	}

	public Location getInternetLocation() {
		try {
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

			ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

			isMobileInternetEnabled = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED;
			isWifiInternetEnabled = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if(!isMobileInternetEnabled && !isWifiInternetEnabled) {

			} else {
				this.canGetLocation = true;

				if(isNetworkEnabled) {
					if(location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

						if(locationManager != null) {
							location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

							if(location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}


	public void stopUsingGPS() {
		if (locationManager != null) {

			locationManager.removeUpdates(NetworkTrackerWithInternetConnectivity.this);
		}
	}

	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		return latitude;
	}

	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		return longitude;
	}

	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	public void showInternetAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Wallpaper));

		alertDialog.setTitle("Internet wyłączony");

		alertDialog.setMessage("Brak dostępu do internetu. Czy chcesz uruchomić go teraz?");

		alertDialog.setPositiveButton("Tak", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
				context.startActivity(intent);
			}
		});

		alertDialog.setNegativeButton("Nie", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		alertDialog.show();
	}

	private void refresh() {
		bestProvider = locationManager.getBestProvider(criteria, true);
		location = locationManager.getLastKnownLocation(bestProvider);

	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			getLatitude();
			getLongitude();
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(this, "Status changed " + status,
				Toast.LENGTH_SHORT).show();
	}


	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

}

