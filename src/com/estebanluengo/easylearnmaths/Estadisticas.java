package com.estebanluengo.easylearnmaths;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.estebanluengo.easylearnmaths.beans.Estadistica;
import com.estebanluengo.easylearnmaths.model.ModelEasyLearnMaths;
import com.estebanluengo.easylearnmaths.utils.Utiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Esta actividad presenta las estadísticas de juego. Solo es accesible desde las opciones de menú mediante la introducción
 * de una contraseña
 * @author eluengo
 * @version 1.0 27/06/2012
 *
 */
public class Estadisticas extends SherlockActivity {

	public static final String TAG = "Estadisticas";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.estadisticas);
		setTitle(R.string.estadisticas);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		SharedPreferences datosJugador = getSharedPreferences(EasyLearnMaths.SHARED_PREFS_FILE, 0);
		//int puntosAlmacenados = datosJugador.getInt(EasyLearnMaths.PUNTOS, 0);
		int partidasJugadas = datosJugador.getInt(EasyLearnMaths.PARTIDAS_JUGADADAS, 0);

		SharedPreferences ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		int puntosSumas = Utiles.getInt(ajustes.getString("puntosSumas", "5"), 5);
		int puntosRestas = Utiles.getInt(ajustes.getString("puntosRestas", "5"), 5);
		int puntosMultiplicaciones = Utiles.getInt(ajustes.getString("puntosMultiplicaciones", "20"), 20);
		int puntosDivisiones = Utiles.getInt(ajustes.getString("puntosDivisiones", "20"), 20);
		int puntosComparaciones = Utiles.getInt(ajustes.getString("puntosComparaciones", "5"), 5);

		TextView tvPartidasJugadas = (TextView) findViewById(R.id.tvPartidasJugadas);
		TextView tvPuntosConseguidos = (TextView) findViewById(R.id.tvPuntosConseguidos);
		//TextView tvPuntosBonus = (TextView) findViewById(R.id.tvPuntosBonus);
		TextView tvPreguntasContestadas = (TextView) findViewById(R.id.tvPreguntasContestadas);
		TextView tvPuntosAciertosSumas = (TextView) findViewById(R.id.tvPuntosAciertosSumas);
		TextView tvPuntosAciertosRestas = (TextView) findViewById(R.id.tvPuntosAciertosRestas);
		TextView tvPuntosAciertosMultiplicaciones = (TextView) findViewById(R.id.tvPuntosAciertosMultiplicaciones);
		TextView tvPuntosAciertosDivisiones = (TextView) findViewById(R.id.tvPuntosAciertosDivisiones);
		TextView tvPuntosAciertosComparaciones = (TextView) findViewById(R.id.tvPuntosAciertosComparaciones);
		TextView tvTasaAciertoPrimera = (TextView) findViewById(R.id.tvTasaAciertoPrimera);
		TextView tvTasaAciertoSegunda = (TextView) findViewById(R.id.tvTasaAciertoSegunda);
		TextView tvTasaAciertoTercera = (TextView) findViewById(R.id.tvTasaAciertoTercera);
		TextView tvTasaNoAciertos = (TextView) findViewById(R.id.tvTasaNoAciertos);
		
		
		try{
			ModelEasyLearnMaths model = ModelEasyLearnMaths.getInstance();		
			model.init(this);
			Estadistica estadistica = model.getEstadisticas();
			int puntosGanadosSumas = estadistica.getSumas() * puntosSumas;
			int puntosGanadosRestas = estadistica.getRestas() * puntosRestas;
			int puntosGanadosMultiplicaciones = estadistica.getMultiplicaciones() * puntosMultiplicaciones;
			int puntosGanadosDivisiones = estadistica.getDivisiones() * puntosDivisiones;
			int puntosGanadosComparaciones = estadistica.getComparaciones() * puntosComparaciones;
			//int puntosTotales = datosJugador.getInt(EasyLearnMaths.PUNTOS, 0);//NO se puede usar porque los puntos se van usando en los premios
			int puntosConseguidos = puntosGanadosSumas + puntosGanadosRestas + puntosGanadosMultiplicaciones + puntosGanadosDivisiones + puntosGanadosComparaciones;
			//int puntosBonus = puntosTotales - puntosConseguidos; 
			tvPartidasJugadas.setText("" + partidasJugadas);
			tvPuntosConseguidos.setText("" + puntosConseguidos);
			//tvPuntosBonus.setText("" + puntosBonus);
			tvPreguntasContestadas.setText("" + estadistica.getPreguntasContestadas());
			tvPuntosAciertosSumas.setText("" + puntosGanadosSumas);
			tvPuntosAciertosRestas.setText("" + puntosGanadosRestas);
			tvPuntosAciertosMultiplicaciones.setText("" + puntosGanadosMultiplicaciones);
			tvPuntosAciertosDivisiones.setText("" + puntosGanadosDivisiones);
			tvPuntosAciertosComparaciones.setText("" + puntosGanadosComparaciones);
			tvTasaAciertoPrimera.setText("" + (int) estadistica.getTasaALaPrimera() + "%");
			tvTasaAciertoSegunda.setText("" + (int) estadistica.getTasaALaSegunda() + "%");
			tvTasaAciertoTercera.setText("" + (int) estadistica.getTasaALaTercera() + "%");			
			tvTasaNoAciertos.setText("" + estadistica.getTasaFallos() + "%");
		}catch (SQLException e) {
			Log.e(TAG, "Error al actualizar los resultados en la BBDDD", e);
			Toast.makeText(this, getString(R.string.error_general), Toast.LENGTH_SHORT).show();
			finish();
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.d(TAG, "se crea el menu de opciones");		
		return true;
	}

	/**
	 * Controla que opción del menú se ha pulsado. Cabe señalar que cuando se pulsa una opción de menú, salvo la de salir.
	 * Se presenta un diálogo para que el usuario introduzca una contraseña que por defecto es 1234. Esta contrasñea es para
	 * evitar que el jugador pueda cambiarse él mismo las preferencias de juego.
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
