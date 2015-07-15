package com.estebanluengo.easylearnmaths;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.estebanluengo.easylearnmaths.beans.Operacion;
import com.estebanluengo.easylearnmaths.model.ModelEasyLearnMaths;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Esta actividad controla el juego consistente en que el usuario debe respnder N operaciones matemáticas. El número de operaciones
 * a contestar así como el tiempo que tiene para cada operación se definen desde los ajustes en el menú accesible desde la actividad
 * principal 
 * @author eluengo
 * @version 1.0 27/06/2012
 */
public class Jugar extends SherlockActivity implements OnClickListener {

	public static final String TAG = "Jugar";
	public static final String NUMERO_PREGUNTA = "com.estebanluengo.easylearnmaths.NUMERO_PREGUNTA";
	public static final String OPERACIONES = "com.estebanluengo.easylearnmaths.OPERACIONES";
	public static final String SI = "S";
	public static final String NO = "N";
	
	private int numeroPregunta; //indicador del numero de operacion presentado en pantalla
	private int totalPreguntas; //numero total de operaciones que se presentaran
	private int tiempoPregunta; //tiempo que tiene como maximo en segudos para responder
	private int tiempoTranscurrido; //tiempo que llevamos transcurrido desde que empez� cada jugada
	private String dificultad;  //nivel de dificultad
	private String mostrarOpciones;
	private Operacion operaciones[]; //lista de operaciones que debe responder de forma consecutiva	
	private Handler mHandler;   //permite controlar el tiempo que lleva el usuario para responder la operacion
	private String tipoOperacion; //indica el tipo de operación matemática: suma, resta, multiplicación, división o mezclado
	private ModelEasyLearnMaths model;
	
	private TextView tvNumeroPregunta;
	private TextView tvOperando1, tvOperando2, tvOperacion, tvIgual, tvRespuesta, tvTiempoRestante;
	private EditText etRespuesta;
	private Button bSiguiente, bRespuesta1, bRespuesta2, bRespuesta3, bRespuesta4;
	private AlertDialog alertCancelar;
	private LinearLayout linearLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);				
		Log.d(TAG, "onCreate Jugar Activity");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//Recuperamos las opciones de usuario guardadas
		SharedPreferences ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		dificultad = ajustes.getString("dificultad", Operacion.NIVEL_FACIL);
		mostrarOpciones = ajustes.getString("mostrarOpciones", SI);
		totalPreguntas = Utiles.getInt(ajustes.getString("numeroPreguntas", "10"), 10);
		tiempoPregunta = Utiles.getInt(ajustes.getString("tiempoPregunta", "10"), 10);
		Log.d(TAG, "mostrarOpciones:	"+mostrarOpciones);
		
		//creamos componentes graficos y dialogos
		creaComponentesOperacion(mostrarOpciones);
		creaDialogos();
		
		//recuperamos el tipo de operacion necesario para construir la pantalla
		if (savedInstanceState == null) {
			Bundle basket = getIntent().getExtras();
			tipoOperacion = (String) basket.get(EasyLearnMaths.TIPO_OPERACION);	
			tiempoTranscurrido = 1;
		}else{
			tipoOperacion = (String) savedInstanceState.getSerializable(EasyLearnMaths.TIPO_OPERACION);
			tiempoTranscurrido = (Integer) savedInstanceState.getSerializable(EasyLearnMaths.TIEMPO_TRANSCURRIDO);
		}
		Log.d(TAG, "Tipo operacion elegido:" + tipoOperacion);
		
		
		//la operacion compara siempre sera con opciones visibles
		if (mostrarOpciones.equals(NO) && !Operacion.COMPARA.equals(tipoOperacion)){
			setContentView(R.layout.jugar);			
			bSiguiente = (Button) findViewById(R.id.bSiguiente);
			bSiguiente.setOnClickListener(this);
		}
		else{
			if (Operacion.COMPARA.equals(tipoOperacion)){
				setContentView(R.layout.jugar_mostrar_3_opciones);
			}
			else{
				setContentView(R.layout.jugar_mostrar_opciones);
			}
			bRespuesta1 = (Button) findViewById(R.id.bRespuesta1);
			bRespuesta2 = (Button) findViewById(R.id.bRespuesta2);
			bRespuesta3 = (Button) findViewById(R.id.bRespuesta3);
			bRespuesta1.setOnClickListener(this);
			bRespuesta2.setOnClickListener(this);
			bRespuesta3.setOnClickListener(this);
			if (!Operacion.COMPARA.equals(tipoOperacion)){
				bRespuesta4 = (Button) findViewById(R.id.bRespuesta4);
				bRespuesta4.setOnClickListener(this);
			}
		}
		linearLayout = (LinearLayout) findViewById(R.id.llOperacion);						
		tvNumeroPregunta = (TextView) findViewById(R.id.tvNumeroPregunta);
		tvTiempoRestante = (TextView) findViewById(R.id.tvTiempoRestante);
		tvTiempoRestante.setText(String.valueOf(tiempoTranscurrido));
		mHandler = new Handler();
		//inicializamos la logica de aplicación
		if (savedInstanceState == null) {
			Log.d(TAG, "no hay estado almacenado, creamos todos los objetos");
			numeroPregunta = 1;						
			// creamos jugadas
			model = ModelEasyLearnMaths.getInstance();
			try{
				model.init(this);
				operaciones = model.creaJugadas(tipoOperacion, totalPreguntas, dificultad);
				if (operaciones == null || (operaciones != null && operaciones.length == 0)){
					Toast.makeText(this, getString(R.string.no_operaciones) + getString(R.string.error_reinicio), Toast.LENGTH_LONG).show();
					finish();
				}
				else
					muestraJugada();				
			}catch(SQLException e){
				Log.e(TAG,"Se produce un error al crear las jugadas",e);
				Toast.makeText(this, getString(R.string.no_operaciones) + getString(R.string.error_reinicio), Toast.LENGTH_LONG).show();
				finish();
			}			
			
		} else {
			numeroPregunta = (Integer) savedInstanceState.getSerializable(NUMERO_PREGUNTA);
			operaciones = (Operacion[]) savedInstanceState.getSerializable(OPERACIONES);			
			Log.d(TAG, "Hay estado almacenado. Recuperamos numeroPregunta con valor:" + numeroPregunta);
			muestraJugada();
		}	
		setTitle(getTituloOperacion(tipoOperacion));		
	}
	
	private void creaComponentesOperacion(String mostrarOpciones){
		int textStyle = R.style.super_big_text;
		int textStyleBold = R.style.super_big_text_bold;
		//si estamos en el modo de no mostrar las respuestas o en el nivel dificil, el tama�o del texto lo reducimos para que quepa
		if (mostrarOpciones.equals(NO) || Operacion.NIVEL_DIFICIL.equals(dificultad)){
			textStyle = R.style.big_text;
			textStyleBold = R.style.big_text_bold; 
		} 
		tvOperando1 = new TextView(this);
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 3, 0, 0);
		tvOperando1.setLayoutParams(params);			
		tvOperando1.setGravity(Gravity.CENTER);
		tvOperando1.setPadding(3, 3, 3, 3);		
		tvOperando1.setTextAppearance(getApplicationContext(), textStyle);
				
		tvOperando2 = new TextView(this);
		params.setMargins(0, 3, 0, 0);
		tvOperando2.setLayoutParams(params);			
		tvOperando2.setGravity(Gravity.CENTER);
		tvOperando2.setPadding(3, 3, 3, 3);		
		tvOperando2.setTextAppearance(getApplicationContext(), textStyle);
		
		tvOperacion = new TextView(this);
		params.setMargins(0, 3, 0, 0);
		tvOperacion.setLayoutParams(params);			
		tvOperacion.setGravity(Gravity.CENTER);
		tvOperacion.setPadding(3, 3, 3, 3);		
		tvOperacion.setTextAppearance(getApplicationContext(), textStyleBold);
		
		tvRespuesta = new TextView(this);
		params.setMargins(0, 3, 0, 0);
		tvRespuesta.setLayoutParams(params);			
		tvRespuesta.setGravity(Gravity.CENTER);
		tvRespuesta.setPadding(3, 3, 3, 3);		
		tvRespuesta.setTextAppearance(getApplicationContext(), textStyle);
		
		tvIgual = new TextView(this);
		params.setMargins(0, 3, 0, 0);
		tvIgual.setLayoutParams(params);			
		tvIgual.setGravity(Gravity.CENTER);
		tvIgual.setPadding(3, 3, 3, 3);
		tvIgual.setText("=");		
		tvIgual.setTextAppearance(getApplicationContext(), textStyle);
		
		etRespuesta = new EditText(this);			
		etRespuesta.setLayoutParams(params);
		etRespuesta.setGravity(Gravity.CENTER);
		etRespuesta.setPadding(3, 3, 3, 3);
		etRespuesta.setText("");
		etRespuesta.setEms(3);
		etRespuesta.setInputType(InputType.TYPE_CLASS_NUMBER);
		etRespuesta.setTextAppearance(getApplicationContext(), textStyle);		
	}
	
	private void creaDialogos(){
		DialogInterface.OnClickListener confirmarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion Aceptar cancelar partida pulsada");
				finish();
			}
		};
		DialogInterface.OnClickListener cancelarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion Cancelar cancelar partida pulsada");
				dialogo1.dismiss();
			}
		};
		alertCancelar = Utiles.createAlertDialog(this, getString(R.string.atencion), getString(R.string.abandonar_partida), true, confirmarListener,
				cancelarListener, R.drawable.ic_dialog_operator);
	}
	
	/**
	 * Especifica cual debe ser el título de la actividad en función de la operación matemática escogida
	 * @param operacion
	 * @return
	 */
	public String getTituloOperacion(String operacion){
		String nombre = "";
		if (Operacion.SUMA.equals(operacion))
			return getString(R.string.sumas);
		else if (Operacion.RESTA.equals(operacion))
			return getString(R.string.restas);
		else if (Operacion.MULTIPLICACION.equals(operacion))
			return getString(R.string.multiplicaciones);
		else if (Operacion.DIVISION.equals(operacion))
			return getString(R.string.divisiones);
		else if (Operacion.MIXTO.equals(operacion))
			return getString(R.string.todas_operaciones);
		else if (Operacion.COMPARA.equals(operacion))
			return getString(R.string.compara);
		else if (Operacion.QUENUMEROFALTA.equals(operacion))
			return getString(R.string.quenumerofalta);
		return nombre;
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
			alertCancelar.show();
		}
		return true;
	}

	/**
	 * Controla si el usuario pulsa aceptar para continuar con la siguiente operación o cancelar para abandonar la partida
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.bSiguiente) {
			Log.d(TAG, "Accion Siguiente jugada pulsada");
			siguienteJugada(etRespuesta.getText().toString());
		} else if (id == R.id.bRespuesta1){
			Log.d(TAG, "Accion respuesta1 pulsada");
			siguienteJugada(bRespuesta1.getText().toString());
		} else if (id == R.id.bRespuesta2){
			Log.d(TAG, "Accion respuesta1 pulsada");
			siguienteJugada(bRespuesta2.getText().toString());			
		} else if (id == R.id.bRespuesta3){
			Log.d(TAG, "Accion respuesta1 pulsada");
			siguienteJugada(bRespuesta3.getText().toString());
		} else if (id == R.id.bRespuesta4){
			Log.d(TAG, "Accion respuesta1 pulsada");
			siguienteJugada(bRespuesta4.getText().toString());
		}
		
	}

	/**
	 * La informacion que se cachea es:
	 * - Numero de operacion por la que vamos
	 * - lista de operaciones a contestar
	 * - tipo de operacion a contestar
	 * - tiempo transcurrido desde que empezo la jugada
	 * 
	 * Se controla si se han contestado todas las preguntas en cuyo caso la actividad se destruye para no volver a ella desde
	 * la siguiente actividad de mostrar resultados si pulsan el botón de ir hacia atrás
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState generado en Jugar");
		// con esto hacemos que al volver en resultados no vuelva a esta actividad
		if (numeroPregunta > totalPreguntas) {
			Log.d(TAG, "Fin de partida. Actividad jugar destruida. Vamos a mostrar resultados");
			finish();
		} else {
			Log.d(TAG, "Tenemos que guardar el estado de la partida. Numero pregunta:" + numeroPregunta);
			outState.putSerializable(NUMERO_PREGUNTA, numeroPregunta);
			outState.putSerializable(OPERACIONES, operaciones);
			outState.putSerializable(EasyLearnMaths.TIEMPO_TRANSCURRIDO, tiempoTranscurrido);
			outState.putSerializable(EasyLearnMaths.TIPO_OPERACION, tipoOperacion);
		}
	}
	
	/**
	 * Es necesario eliminar las llamadas pendientes del handler para que no actue mientras la actividad no esta activa
	 */
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause generado en Jugar");
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * Se reactiva el handler
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume generado en Jugar");
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}

	/**
	 * Se controla si pulsan el botón de ir hacia atrás para informar que se perderá el progreso de esta partida
	 */
	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		Log.d(TAG, "onBackPressed generado en Jugar");
		alertCancelar.show();
	}

	/**
	 * Thread que se dispara cada segundo para controlar cuando se debe pasar a la siguiente jugada
	 */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			tiempoTranscurrido++;			
			if (tiempoTranscurrido == tiempoPregunta){
				Log.d(TAG, "Timer expirado. Pasamos a la siguiente jugada");
				tvTiempoRestante.setText(String.valueOf(tiempoTranscurrido));
				siguienteJugada("");
			}else{	
				tvTiempoRestante.setText(String.valueOf(tiempoTranscurrido));
				mHandler.postDelayed(mUpdateTimeTask, 1000);
			}
		}
	};

	/**
	 * Muestra la siguiente operación y actualiza en la operación actual si se ha contestado correctamente o no
	 * @param respuesta indica la respuesta seleccionada por el usuario para el modo de juego fácil o introducida para el modo de juego normal
	 */
	private void siguienteJugada(String respuesta) {
		Log.d(TAG,"Siguiente jugada. Respuesta del jugador:"+respuesta);
		int index = numeroPregunta - 1;
		//comprobamos que un doble click intente acceder al l�mite del array
		if (index >= operaciones.length){
			mHandler.removeCallbacks(mUpdateTimeTask);	
		}else{
			Operacion o = operaciones[index];
			if (Operacion.COMPARA.equals(o.getTipoOperacion())){
				respuesta = ""+Operacion.getCodigoComparador(respuesta);
			}
			o.setRespuesta(respuesta);
			if (o.isExito())
				o.setExito(R.drawable.ic_success);
			else
				o.setExito(R.drawable.ic_fail);
			numeroPregunta++;
			tiempoTranscurrido = 1;
			tvTiempoRestante.setText(String.valueOf(tiempoTranscurrido));
			mHandler.removeCallbacks(mUpdateTimeTask);
			mHandler.postDelayed(mUpdateTimeTask, 1000);
			muestraJugada();
		}
	}

	/**
	 * Muestra en la actividad la operación actual que debe contestar el jugador. Si ya no hay más operaciones para mostrar
	 * entonces se invoca la actividad para mostrar resultados
	 */
	private void muestraJugada() {
		if (numeroPregunta > totalPreguntas) {
			Log.d(TAG, "Fin de jugadas. Mostramos resultados");
			Intent iResultados = new Intent(this, Resultados.class);
			iResultados.putExtra(OPERACIONES, operaciones);
			iResultados.putExtra(EasyLearnMaths.TIPO_OPERACION, tipoOperacion);
			startActivity(iResultados);
		} else {
			Log.d(TAG, "Jugada: " + numeroPregunta);	
			Operacion operacion = operaciones[numeroPregunta - 1];
			linearLayout.removeAllViews();			
			if (Operacion.COMPARA.equals(operacion.getTipoOperacion())){
				tvOperando1.setText(operacion.getOperando1().getExpresion());
				tvOperando2.setText(operacion.getOperando2().getExpresion());				
				linearLayout.addView(tvOperando1);		
				tvOperacion.setText("?");
				linearLayout.addView(tvOperacion);				
				linearLayout.addView(tvOperando2);
			}else if (Operacion.QUENUMEROFALTA.equals(operacion.getTipoOperacion())){
				tvOperando1.setText(""+operacion.getOperando1().getExpresion());
				tvOperando2.setText(""+operacion.getOperando2().getExpresion());
				tvOperacion.setText(""+operacion.getNombreOperador());
				int r = Operacion.opera(operacion.getOperando1().getResultado(), operacion.getOperando2().getResultado(), operacion.getOperador());
				tvRespuesta.setText(""+r);
				etRespuesta.setInputType(InputType.TYPE_CLASS_NUMBER);
				etRespuesta.setText("");				
				int numeroFalta = operacion.getQueNumeroFalta();				
				if (numeroFalta == 0){
					if (mostrarOpciones.equals(NO)){
						linearLayout.addView(etRespuesta);
						etRespuesta.requestFocus();
					}else{
						tvOperando1.setText("?");
						linearLayout.addView(tvOperando1);
					}
					linearLayout.addView(tvOperacion);
					linearLayout.addView(tvOperando2);
					linearLayout.addView(tvIgual);
					linearLayout.addView(tvRespuesta);
				}else if (numeroFalta == 1){
					linearLayout.addView(tvOperando1);
					linearLayout.addView(tvOperacion);
					if (mostrarOpciones.equals(NO)){
						linearLayout.addView(etRespuesta);
						etRespuesta.requestFocus();
					}else{
						tvOperando2.setText("?");
						linearLayout.addView(tvOperando2);
					}
					linearLayout.addView(tvIgual);
					linearLayout.addView(tvRespuesta);
				}else{
					linearLayout.addView(tvOperando1);
					linearLayout.addView(tvOperacion);
					linearLayout.addView(tvOperando2);
					linearLayout.addView(tvIgual);
					if (mostrarOpciones.equals(NO)){						
						linearLayout.addView(etRespuesta);
						etRespuesta.requestFocus();
					}else{
						tvRespuesta.setText("?");
						linearLayout.addView(tvRespuesta);
					}
				}
//			}else if (Operacion.COMPLEJA.equals(operacion.getOperacion())){
//				tvOperando1.setText(operacion.getExpresionOp1());
//				etRespuesta.setInputType(InputType.TYPE_CLASS_NUMBER);
//				etRespuesta.setText("");
//				linearLayout.addView(tvOperando1);
//				linearLayout.addView(tvIgual);
//				linearLayout.addView(etRespuesta);
//				etRespuesta.requestFocus();
			}else{
				//usamos expresionOp1 y expresionOp2 por si la operacion es del tipo (3 + 5) * 4 ---> (3 + 5) es expresionOp1 y 4 es expresionOp2
				tvOperando1.setText(""+operacion.getOperando1().getExpresion());
				tvOperando2.setText(""+operacion.getOperando2().getExpresion());
				tvOperacion.setText(""+operacion.getNombreOperador());
				etRespuesta.setInputType(InputType.TYPE_CLASS_NUMBER);
				etRespuesta.setText("");
				
				linearLayout.addView(tvOperando1);
				linearLayout.addView(tvOperacion);
				linearLayout.addView(tvOperando2);
				if (mostrarOpciones.equals(NO)){
					linearLayout.addView(tvIgual);
					linearLayout.addView(etRespuesta);
					etRespuesta.requestFocus();
				}
			}							
			
			if (mostrarOpciones.equals(SI) || Operacion.COMPARA.equals(operacion.getTipoOperacion())){				
				if (Operacion.COMPARA.equals(operacion.getTipoOperacion())){
					bRespuesta1.setText(">");
					bRespuesta2.setText("<");
					bRespuesta3.setText("=");
				}else{
					//damos valores a los 4 botones de las respuestas			
					model = ModelEasyLearnMaths.getInstance();
					String[] opciones = model.getOpcionesPregunta(operaciones[numeroPregunta - 1]);
					bRespuesta4.setVisibility(View.VISIBLE);
					bRespuesta1.setText(opciones[0]);
					bRespuesta2.setText(opciones[1]);
					bRespuesta3.setText(opciones[2]);
					bRespuesta4.setText(opciones[3]);
				}
			}
			tvNumeroPregunta.setText(getString(R.string.pregunta) + " " + numeroPregunta + " " + getString(R.string.de) + " " + totalPreguntas);								
		}
	}
	
}