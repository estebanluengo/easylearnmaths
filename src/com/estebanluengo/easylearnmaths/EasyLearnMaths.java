package com.estebanluengo.easylearnmaths;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
//import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.actionbarsherlock.view.SubMenu;
import com.estebanluengo.easylearnmaths.beans.Operacion;
import com.estebanluengo.easylearnmaths.model.ModelEasyLearnMaths;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Actividad principal del proyecto. Presenta la pantalla inicial para que el jugador pueda elegir qué quiere hacer.
 * Desde esta actividad se puede pulsar el botón de menú para la configuración del juego.
 * La actividad almacena en las preferencias del móvil las partidas jugadas y los puntos conseguidos.
 * 
 * @author eluengo
 * @version 1.0 27/06/2012
 */
public class EasyLearnMaths extends SherlockActivity implements OnClickListener, OnMenuItemClickListener {

	public static final String TAG = "EasyLearnMaths";
	public static final String SHARED_PREFS_FILE = "EasyMaths";
	public static final String PUNTOS = "com.estebanluengo.easylearnmaths.Puntos";
	public static final String PARTIDAS_JUGADADAS = "com.estebanluengo.easylearnmaths.PartidasJugadas";
	public static final String TIPO_OPERACION = "com.estebanluengo.easylearnmaths.TipoOperacion";
	public static final String TIEMPO_TRANSCURRIDO = "com.estebanluengo.easylearnmaths.TiempoTranscurrido";

	private AlertDialog alertReinicio;
	private AlertDialog acercaDe;
	private Dialog alertPassword;
	private String mostrarActividad;
	private EditText etPassword;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//requestWindowFeature(Window.FEATURE_NO_TITLE);

		Log.d(TAG, "onCreate main activity");
		setContentView(R.layout.dashboard_layout);
		//setTitle(getString(R.string.app_name));
		setTitle(null);
		Button sumar = (Button) findViewById(R.id.bSumar);
		Button restar = (Button) findViewById(R.id.bRestar);
		Button multiplicar = (Button) findViewById(R.id.bMultiplicar);
		Button dividir = (Button) findViewById(R.id.bDividir);
		Button todasOperaciones = (Button) findViewById(R.id.bTodasOperaciones);
		Button queNumeroFalta = (Button) findViewById(R.id.bQueNumeroFalta);
		Button compara = (Button) findViewById(R.id.bCompara);
		Button premios = (Button) findViewById(R.id.bPremios);
		
		sumar.setOnClickListener(this);
		restar.setOnClickListener(this);
		multiplicar.setOnClickListener(this);
		dividir.setOnClickListener(this);
		todasOperaciones.setOnClickListener(this);
		queNumeroFalta.setOnClickListener(this);
		compara.setOnClickListener(this);
		premios.setOnClickListener(this);

		makeDialogs();
	}

	private void makeDialogs() {
		DialogInterface.OnClickListener confirmarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion Aceptar reinicio pulsada");
				aceptarReinicio();
				dialogo1.dismiss();
			}
		};
		DialogInterface.OnClickListener cancelarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion Cancelar reinicio pulsada");
				dialogo1.dismiss();
			}
		};

		alertReinicio = Utiles.createAlertDialog(this, getString(R.string.atencion), getString(R.string.borrar_progreso), true, confirmarListener,
				cancelarListener, R.drawable.ic_dialog_operator);		

		DialogInterface.OnClickListener confirmarAceptarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion aceptar acerca de");
				dialogo1.dismiss();
			}
		};
		acercaDe = Utiles.createAlertDialog(this, getString(R.string.titulo_acerca_de), getString(R.string.body_acerca_de), true,
				confirmarAceptarListener, null, R.drawable.ic_dialog_operator);

		alertPassword = new Dialog(this);
		alertPassword.setContentView(R.layout.password);
		alertPassword.setTitle(getString(R.string.password));
		Button bAceptarPassword = (Button) alertPassword.findViewById(R.id.bAceptarPassord);
		etPassword = (EditText) alertPassword.findViewById(R.id.etPassword);
		bAceptarPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "Accion Aceptar password pulsada");
				comprobarPassword(etPassword.getText().toString());
				alertPassword.dismiss();
			}
		});
		Button bCancelarPassword = (Button) alertPassword.findViewById(R.id.bCancelarPassword);
		bCancelarPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "Accion Cancelar password pulsada");
				alertPassword.dismiss();

			}
		});

	}

	/**
	 * Controla que opción ha pulsado el jugador
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.bSumar) {
			Log.d(TAG, "Accion Jugar pulsada");
			Intent iSumar = new Intent(this, Jugar.class);
			iSumar.putExtra(TIPO_OPERACION, Operacion.SUMA);
			startActivity(iSumar);
		} else if (id == R.id.bRestar) {
			Log.d(TAG, "Accion Jugar pulsada");
			Intent iRestar = new Intent(this, Jugar.class);
			iRestar.putExtra(TIPO_OPERACION, Operacion.RESTA);
			startActivity(iRestar);
		} else if (id == R.id.bMultiplicar) {
			Log.d(TAG, "Accion Jugar pulsada");
			Intent iMultiplicar = new Intent(this, Jugar.class);
			iMultiplicar.putExtra(TIPO_OPERACION, Operacion.MULTIPLICACION);
			startActivity(iMultiplicar);
		} else if (id == R.id.bDividir) {
			Log.d(TAG, "Accion Jugar pulsada");
			Intent iDividir = new Intent(this, Jugar.class);
			iDividir.putExtra(TIPO_OPERACION, Operacion.DIVISION);
			startActivity(iDividir);
		} else if (id == R.id.bTodasOperaciones) {
			Log.d(TAG, "Accion Jugar pulsada");
			Intent iDeTodo = new Intent(this, Jugar.class);
			iDeTodo.putExtra(TIPO_OPERACION, Operacion.MIXTO);
			startActivity(iDeTodo);
		} else if (id == R.id.bCompara) {
			Log.d(TAG, "Accion comparaciones pulsada");
			Intent iCompara = new Intent(this, Jugar.class);
			iCompara.putExtra(TIPO_OPERACION, Operacion.COMPARA);
			startActivity(iCompara);
		} else if (id == R.id.bQueNumeroFalta) {
			Log.d(TAG, "Accion que numero falta pulsada");
			Intent iCompara = new Intent(this, Jugar.class);
			iCompara.putExtra(TIPO_OPERACION, Operacion.QUENUMEROFALTA);
			startActivity(iCompara);
		} else if (id == R.id.bPremios) {
			Log.d(TAG, "Accion Premios pulsada");
			Intent iPremios = new Intent(this, Premios.class);
			startActivity(iPremios);
		} 

	}
	//ShareActionProvider actionProvider;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		/* En Jelly Bean salen dos barritas, una con el menu y otra con el otro menu que creamos nosotros
		Log.d(TAG, "se crea el menu de opciones");
		MenuInflater blowUp = getSupportMenuInflater(); //with sherlock bar
		//MenuInflater blowUp = getMenuInflater();
		// cargamos el menu que se visualiza abajo cuando el usuario pulsa menu sobre la aplicación
		blowUp.inflate(R.menu.principal_menu, menu);
        */
		
		SubMenu subMenu = menu.addSubMenu("Menu");		
        MenuItem menuItem = subMenu.add(R.string.ajustes);
        menuItem.setIcon(R.drawable.ic_menu_gear);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);        
        menuItem.setOnMenuItemClickListener(this);
        
        menuItem = subMenu.add(R.string.definir_premios);
        menuItem.setIcon(R.drawable.ic_menu_certificate1);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(this);
        
        menuItem = subMenu.add(R.string.estadisticas);
        menuItem.setIcon(R.drawable.ic_menu_stats_bar_chart);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(this);
        
        menuItem = subMenu.add(R.string.reiniciar);
        menuItem.setIcon(R.drawable.ic_menu_reload);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(this);
        
        menuItem = subMenu.add(R.string.acerca_de);
        menuItem.setIcon(R.drawable.ic_menu_about);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(this);
        
        menuItem = subMenu.add(R.string.salir);
        menuItem.setIcon(R.drawable.ic_menu_close);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(this);
        
        MenuItem subMenu1Item = subMenu.getItem();
        subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_light);
        subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menuItem.setOnMenuItemClickListener(this);
        
        return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Controla que opción del menú se ha pulsado. Cabe señalar que cuando se pulsa una opción de menú, salvo la de salir.
	 * Se presenta un diálogo para que el usuario introduzca una contraseña que por defecto es 1234. Esta contrasñea es para
	 * evitar que el jugador pueda cambiarse él mismo las preferencias de juego.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		SharedPreferences ajustes;
		String password;
		if (item.getItemId() == R.id.reiniciar) {
			alertReinicio.show();
			return true;
		} else if (item.getItemId() == R.id.acercaDe) {
			acercaDe.show();
			return true;
		} else if (item.getItemId() == R.id.ajustes) {
			ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			password = ajustes.getString("password", "");
			if ("".equals(password)){
				Intent intent = new Intent(this, Ajustes.class);
				startActivity(intent);
			}else{
				mostrarActividad = "ajustes";
				etPassword.setText("");
				alertPassword.show();
			}
			return true;
		} else if (item.getItemId() == R.id.definirPremios) {
			ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			password = ajustes.getString("password", "");
			if ("".equals(password)){
				Intent intent = new Intent(this, DefinirPremios.class);
				startActivity(intent);
			}else{
				mostrarActividad = "definir_premios";
				etPassword.setText("");
				alertPassword.show();
			}
			return true;
		} else if (item.getItemId() == R.id.estadisticas) {
			Intent intent = new Intent(this, Estadisticas.class);
			startActivity(intent);
			return true;
		} else if (item.getItemId() == R.id.salir) {
			finish();
			return true;
		} else if (item.getItemId() == android.R.id.home){
			finish();	
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		SharedPreferences ajustes;
		String password;
		String title = (String) item.getTitle();
		Log.d(TAG, "Menu pulsado:"+title);
		if (title.equals(getResources().getString(R.string.reiniciar))) {
			alertReinicio.show();
			return true;
		} else if (title.equals(getResources().getString(R.string.acerca_de))) {
			acercaDe.show();
			return true;
		} else if (title.equals(getResources().getString(R.string.ajustes))) {
			ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			password = ajustes.getString("password", "");
			if ("".equals(password)){
				Intent intent = new Intent(this, Ajustes.class);
				startActivity(intent);
			}else{
				mostrarActividad = "ajustes";
				etPassword.setText("");
				alertPassword.show();
			}
			return true;
		} else if (title.equals(getResources().getString(R.string.definir_premios))) {
			ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			password = ajustes.getString("password", "");
			if ("".equals(password)){
				Intent intent = new Intent(this, DefinirPremios.class);
				startActivity(intent);
			}else{
				mostrarActividad = "definir_premios";
				etPassword.setText("");
				alertPassword.show();
			}
			return true;
		} else if (title.equals(getResources().getString(R.string.estadisticas))) {
			Intent intent = new Intent(this, Estadisticas.class);
			startActivity(intent);
			return true;
		} else if (title.equals(getResources().getString(R.string.salir))) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy EasyLearnMaths");
	}

	/**
	 * Este método será llamado si el usuario ha pulsado el botón de reinicio y ha confirmado que quiere reiniciar la partida.
	 * Reiniciar significa que los puntos y partidas jugadas vuelven a cero y que las respuestas contestadas se reinician como
	 * si no se hubieran contestado.
	 */
	public void aceptarReinicio() {
		try {
			ModelEasyLearnMaths model = ModelEasyLearnMaths.getInstance();
			model.init(this);
			int n = model.reiniciar();
			SharedPreferences datosJugador = getSharedPreferences(EasyLearnMaths.SHARED_PREFS_FILE, 0);
			Editor editor = datosJugador.edit();
			editor.putInt(PUNTOS, 0);
			editor.putInt(PARTIDAS_JUGADADAS, 0);
			editor.commit();
			Log.d(TAG, "actualizadas " + n + " filas en la BBDD");
			Toast.makeText(this, getString(R.string.jugar_de_nuevo), Toast.LENGTH_SHORT).show();
		} catch (SQLException e) {
			Log.e(TAG, "Error al aceptar el reinicio", e);
			Toast.makeText(this, getString(R.string.error_general) + getString(R.string.error_intentelo_nuevo), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e(TAG, "Error al aceptar el reinicio", e);
			Toast.makeText(this, getString(R.string.error_general) + getString(R.string.error_intentelo_nuevo), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Permite comprobar si el password que ha introducido el usuario corresponde con el guardado en las preferencias. Si es así,
	 * se invocará a la actividad que se haya elegido de las opciones de menú
	 * @param passwordIntroducido
	 */
	private void comprobarPassword(String passwordIntroducido) {
		SharedPreferences ajustes = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String password = ajustes.getString("password", "1234");
		if (password.equals(passwordIntroducido)) {
			Log.d(TAG, "Password correcto, llamamos a:" + mostrarActividad);
			Intent intent;
			if ("ajustes".equals(mostrarActividad))
				intent = new Intent(this, Ajustes.class);
			else
				intent = new Intent(this, DefinirPremios.class);

			startActivity(intent);
		} else {
			Log.d(TAG, "Password incorrecto:" + passwordIntroducido);
			Toast.makeText(this, getString(R.string.passwordIncorrecto), Toast.LENGTH_LONG).show();
		}
	}
}
