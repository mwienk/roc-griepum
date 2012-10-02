package nl.wienkit.roc_griepum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.DroidGap;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

public class GriepumActivity extends DroidGap {

	@Override
    public void onCreate(Bundle savedInstanceState) {
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
	

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
          out.write(buffer, 0, read);
        }
    }
}
