package nl.wienkit.roc_griepum;

import java.io.IOException;

import android.util.Log;

import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;


public class UpdateTableTask extends android.os.AsyncTask<String, Void, Void> {
	// service account credential
	private static final String TABLE_ID = "1LtYYxL7RCyZHj8mV3syeR7MkyGDlJbIBW7hFTmk";
	private Fusiontables fusiontables;
	
	public UpdateTableTask(Fusiontables fusiontables) {
		this.fusiontables = fusiontables;
	}
	
	@Override
	/**
	 * Update de FusionTable met de nieuwe coördinaten
	 */
	protected Void doInBackground(String... params) {
		try {
			String latitude = params[0];
			String longitude = params[1];
			String rowid = params[2];
			Sql sql = fusiontables.query().sql(
						"UPDATE " + TABLE_ID + " " + "SET "
								+ "location = '"
								+ latitude + ","
								+ longitude + "', "
								+ "timestamp = '"
								+ System.currentTimeMillis() / 1000
								+ "' " + "WHERE ROWID = '" + rowid + "'");
			Log.w("Griepum", "Updating: ID:" + rowid + "LOC:"
							+ latitude + ","
							+ longitude);
			sql.execute();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
