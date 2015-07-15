package com.estebanluengo.easylearnmaths;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * Esta actividad permite presentar las preferencias de la aplicación
 * @author eluengo
 * @version 1.0 27/06/2012
 *
 */
public class Ajustes extends SherlockPreferenceActivity {

	public static final String TAG = "Ajustes";
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		Log.d("Ajustes", "onCreate Ajustes Activity");
		//layout que se presenta cuando el usuario pulsa Prefs sobre el menu contextual que se abre cuando pulsa menu
		setTitle(R.string.ajustes);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.settings);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.d(TAG, "se crea el menu de opciones");		
		return true;
	}

	/**
	 * Controla que opción del menú se ha pulsado.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);		
		if (item.getItemId() == android.R.id.home){
			Intent intent = new Intent(this, EasyLearnMaths.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
			Log.d(TAG, "hacia home");
		}
		return true;
	}
}
