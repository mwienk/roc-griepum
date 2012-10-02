package nl.wienkit.roc_griepum;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class CoordUpdater extends android.app.Service {
	// Row ID
	public static String rowid;
	
	// Google Fusion Tables settings
	private static GoogleCredential credential;
	private static Fusiontables fusiontables;
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			credential = new GoogleCredential.Builder()
							.setTransport(HTTP_TRANSPORT)
							.setJsonFactory(JSON_FACTORY)
							.setServiceAccountId("759684958125@developer.gserviceaccount.com")
							.setServiceAccountScopes("https://www.googleapis.com/auth/fusiontables")
							.setServiceAccountPrivateKeyFromP12File(new File(Environment
																	.getExternalStorageDirectory()
																	.getPath()
																	+ "/data/roc.griepum/key.p12"))
							.build();
			fusiontables = new Fusiontables.Builder(HTTP_TRANSPORT,	JSON_FACTORY, credential).setApplicationName(
													"WienkIT-ROC_Griepum/1.0").build();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		updateCoords();
		return START_STICKY;
	}

	/**
	 * Houdt de locatie van de gebruiker bij
	 */
	private void updateCoords() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationListener listener = new LocationListener() {

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}

			/**
			 * De enige optie die gebruikt wordt, update de Google Fusion
			 * table waarde met de nieuwe coords
			 */
			public void onLocationChanged(Location location) {
				readSettingsFile(); // Bekijk o fer nieuwe settings zijn (rowid kan zijn veranderd)
				if(rowid != null && !rowid.equals("null") && !rowid.equals("")) {
					new UpdateTableTask(fusiontables).execute(location.getLatitude() + "", location.getLongitude() + "", rowid);
				}
			}
		};
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, listener);
	}

	/**
	 * Leest de settings van de frontend (alleen rowID)
	 * @throws IOException 
	 */
	private static void readSettingsFile() {
		try {
			File file = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/data/roc.griepum/player");
			if (file.exists()) {
				Scanner scanner = new Scanner(file);
				rowid = scanner.next();
				file.delete();
				Log.w("Griepum", "Updated ROWID to " + rowid);
			}
		} catch (Exception e) {
		}
	}
}
