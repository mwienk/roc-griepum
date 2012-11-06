package nl.wienkit.roc_griepum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.DroidGap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

public class GriepumActivity extends DroidGap {

	@Override
    public void onCreate(Bundle savedInstanceState) {
		if(!isOnline()) {
			super.onCreate(savedInstanceState);
			 setContentView(R.layout.activity_griepum);

			 AlertDialog.Builder altDialog= new AlertDialog.Builder(this);
			 altDialog.setMessage("Er is geen internetverbinding. Om dit spel te spelen is een verbinding noodzakelijk.");
			 altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 Intent intent = new Intent(Intent.ACTION_MAIN);
					 intent.addCategory(Intent.CATEGORY_HOME);
					 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					 startActivity(intent);
					 System.exit(0);
				 }
			 });
			 altDialog.show();
		} else {
	        super.onCreate(savedInstanceState);
	        super.loadUrl("file:///android_asset/www/index.html");
	        Log.w("Griepum", Environment.getExternalStorageDirectory().getPath());
	        try {
	        	// Kopiëer de keyfile naar de lokale omgeving, zodat de backend deze kan lezen 
	    		File file = new File(Environment.getExternalStorageDirectory().getPath() + "/data/roc.griepum/key.p12");
	    		if(!file.exists()) {
	    			InputStream in = getResources().getAssets().open("key.p12");
	    			new File(Environment.getExternalStorageDirectory().getPath() + "/data/roc.griepum").mkdirs();
	    			OutputStream out = new FileOutputStream(file);
	    			copyFile(in, out);
	    			in.close();
	    			out.close();
	    		}
			} catch (IOException e) {
				Log.e("Griepum", "De keyfile is niet gevonden." + e.getMessage());
			}
	    	// Laat settings zien als GPS disabled is.
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(myIntent);
			}
			// Start de coordinaten updater & toon Griepum app
		    	startService(new Intent(this.getContext(), CoordUpdater.class));
		}
	}

    private void copyFile(InputStream in, OutputStream out) throws IOException {
    	byte[] buffer = new byte[1024];
    	int read;
    	while((read = in.read(buffer)) != -1){
    		out.write(buffer, 0, read);
    	}
    }
    
    public boolean isOnline() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo netInfo = cm.getActiveNetworkInfo();
    	if (netInfo != null && netInfo.isConnectedOrConnecting()) {
    		return true;
    	}
    	return false;
    }
}

