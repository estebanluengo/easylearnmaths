package com.estebanluengo.easylearnmaths;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.estebanluengo.easylearnmaths.adapters.ResultadoAdapter;
import com.estebanluengo.easylearnmaths.beans.Operacion;
import com.estebanluengo.easylearnmaths.model.ModelEasyLearnMaths;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Esta actividad presenta el resultado de la partida en la que para cada operación contestada se indica si la contestó
 * correctamente o incorrectamente. En caso de que se contestara incorrectamente se indica cual es el resultado correcto
 * @author eluengo
 *
 */
public class Resultados extends SherlockListActivity {

	public static final String PUNTOS_CONSEGUIDOS = "com.estebanluengo.easylearnmaths.PUNTOS_CONSEGUIDOS";
	public static final String RESULTADOS = "com.estebanluengo.easylearnmaths.RESULTADOS";
	public static final String TASA_ACIERTO = "com.estebanluengo.easylearnmaths.TASA_ACIERTO";
	public static final String TAG = "Resultados";
	
	private ListView lvResultados;
	private TextView tvPuntosConseguidos;
	private TextView tvCaretoResultado;
	
	private Operacion resultados[];
	private String tipoOperacion;
	private int puntosConseguidos; 
	private double tasaAcierto;
	//private String dificultad;

	/**
	 * La actividad recibe de Jugar la lista de operaciones contestadas por el jugador
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Log.d(TAG, "onCreate resultados activity");
		setContentView(R.layout.resultados);		
		setTitle(R.string.resultado);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		SharedPreferences ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		//dificultad = ajustes.getString("dificultad", Operacion.NIVEL_FACIL);
		int puntosSumas = Utiles.getInt(ajustes.getString("puntosSumas", "5"), 5);
		int puntosRestas = Utiles.getInt(ajustes.getString("puntosRestas", "5"), 5);
		int puntosMultiplicaciones = Utiles.getInt(ajustes.getString("puntosMultiplicaciones", "20"), 20);
		int puntosDivisiones = Utiles.getInt(ajustes.getString("puntosDivisiones", "20"), 20);		
		int puntosComparaciones = Utiles.getInt(ajustes.getString("puntosComparaciones", "5"), 5);				
		int puntosPruebaCompleta = Utiles.getInt(ajustes.getString("puntosPruebaCompleta", "50"), 50);				
		
		if (savedInstanceState == null){
			Log.d(TAG, "No hay estado almacenado. Se procede a calcular el resultado de la prueba");
			Bundle extra = this.getIntent().getExtras();
			Parcelable[] parcels = extra.getParcelableArray(Jugar.OPERACIONES);
			Log.d(TAG, "Se reciben "+parcels.length+" operaciones de la actividad Jugar");
			resultados = new Operacion[parcels.length];
			tipoOperacion = (String) extra.get(EasyLearnMaths.TIPO_OPERACION);
			int index = 0;
			for (Parcelable par : parcels) {
				resultados[index++] = (Operacion) par;
			}
			try{
				ModelEasyLearnMaths model = ModelEasyLearnMaths.getInstance();
				model.init(this);
				puntosConseguidos = model.calculaPuntosConseguidos(resultados,puntosSumas,puntosRestas,puntosMultiplicaciones,
						puntosDivisiones,puntosComparaciones,puntosPruebaCompleta,tipoOperacion);
				tasaAcierto = model.calculaTasaAcierto(resultados);
				Log.d(TAG, "se consiguen "+puntosConseguidos+" puntos");
			}catch (SQLException e) {
				Log.e(TAG,"Error al actualizar los resultados en la BBDD",e);
				Toast.makeText(this, getString(R.string.error_general), Toast.LENGTH_SHORT).show();
				finish();
			}			

			SharedPreferences datosJugador = getSharedPreferences(EasyLearnMaths.SHARED_PREFS_FILE, 0);
			int puntosAlmacenados = datosJugador.getInt(EasyLearnMaths.PUNTOS, 0);
			int partidasJugadas = datosJugador.getInt(EasyLearnMaths.PARTIDAS_JUGADADAS, 0);
			Editor editor = datosJugador.edit();
			editor.putInt(EasyLearnMaths.PUNTOS, puntosAlmacenados + puntosConseguidos);
			editor.putInt(EasyLearnMaths.PARTIDAS_JUGADADAS, partidasJugadas + 1);
			editor.commit();
			Log.d(TAG,"Se almacena en la preferencias del usuario las partidas jugadas y los puntos conseguidos");
			
		}else{
			Log.d(TAG, "Hay estado almacenado. Se procede a recuperar el resultado de la prueba");
			puntosConseguidos = (Integer)savedInstanceState.getSerializable(PUNTOS_CONSEGUIDOS);
			resultados = (Operacion[])savedInstanceState.getSerializable(RESULTADOS);
			tasaAcierto = (Double)savedInstanceState.getSerializable(TASA_ACIERTO);
			tipoOperacion = (String) savedInstanceState.getSerializable(EasyLearnMaths.TIPO_OPERACION);
		}
		
		if (resultados.length != 0) {
			Log.d(TAG, "Se recibieron " + resultados.length + " resultados");
			lvResultados = (ListView) findViewById(android.R.id.list);
			LayoutInflater inflater = getLayoutInflater();
			View header = (View) inflater.inflate(R.layout.resultados_header, null);
			lvResultados.addHeaderView(header);
			ResultadoAdapter adapter = new ResultadoAdapter(this, R.layout.resultados_item, resultados);
			lvResultados.setAdapter(adapter);
			tvPuntosConseguidos = (TextView) header.findViewById(R.id.tvPuntosConseguidos);
			tvPuntosConseguidos.setText("" + puntosConseguidos);		
			//asociamos el careto con la tasa de acierto
			tvCaretoResultado = (TextView) header.findViewById(R.id.tvCaretoResultado);
			Log.d(TAG, "Tasa de acierto:"+tasaAcierto);
			if (tasaAcierto >= 100){
				tvCaretoResultado.setText(R.string.nivel_crack);
				tvCaretoResultado.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_crack), null);
			}else if (tasaAcierto >= 80){
				Log.d(TAG, "muy bueno");
				tvCaretoResultado.setText(R.string.nivel_muybueno);
				tvCaretoResultado.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_well_done), null);
			}else if (tasaAcierto >= 50){
				tvCaretoResultado.setText(R.string.nivel_bueno);
				tvCaretoResultado.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_success), null);
			}else if (tasaAcierto >= 20){
				tvCaretoResultado.setText(R.string.nivel_malo);
				tvCaretoResultado.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_fail), null);
			}else{
				tvCaretoResultado.setText(R.string.nivel_muymalo);
				tvCaretoResultado.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.ic_wrong), null);
			}
			
		} else {
			Log.d(TAG, "No se recibieron resultados");
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

	/**
	 * Se cachea el listado de resultados,los puntos conseguidos en la partida y la tasa de acierto
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState generado en Resultados");
		outState.putSerializable(RESULTADOS, resultados);
		outState.putSerializable(PUNTOS_CONSEGUIDOS, puntosConseguidos);		
		outState.putSerializable(TASA_ACIERTO, tasaAcierto);
		outState.putSerializable(EasyLearnMaths.TIPO_OPERACION, tipoOperacion);
	}

}