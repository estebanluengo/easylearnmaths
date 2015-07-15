package com.estebanluengo.easylearnmaths;

import java.util.ArrayList;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.estebanluengo.easylearnmaths.adapters.PremioAdapter;
import com.estebanluengo.easylearnmaths.beans.Premio;
import com.estebanluengo.easylearnmaths.model.ModelEasyLearnMaths;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Esta actividad permite definir los premios que el jugador podrá adquirir de forma imaginaria mediante el intercambio de puntos.
 * 
 * @author eluengo
 * @version 1.0 27/06/2012
 */
public class DefinirPremios extends SherlockActivity implements OnItemClickListener {

	public static final String TAG = "DefinirPremios";
	public static final String PREMIO = "com.estebanluengo.easylearnmaths.PREMIO";
	public static final String PUNTOS = "com.estebanluengo.easylearnmaths.PUNTOS";
	public static final String MODO = "com.estebanluengo.easylearnmaths.MODO";
	public static final String POSITION = "com.estebanluengo.easylearnmaths.POSITION";

	private boolean modoInsert;
	private int position;
	private PremioAdapter adapter;
	private ModelEasyLearnMaths model;
	private ArrayList<Premio> premios = null;

	private ListView lvPremios;
	private EditText etPremio;
	private EditText etPuntos;
	//private Button bGuardar;
	//private Button bCancelar;
	//private Button bEliminar;
	private AnActionMode actionMode;

	/**
	 * Durante la creación de la actividad se recuperan los premios de la base de datos y se mapean mediante la clase PremioAdapter see
	 * @com.estebanluengo.easylearnmaths.adapters.PremioAdapter
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate DefinirPremios Activity");

		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		Log.d(TAG, "onCreate DefinirPremios");
		setContentView(R.layout.premios);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		getSherlock().getActionBar().setTitle(null);
		
		lvPremios = (ListView) findViewById(android.R.id.list);

		model = ModelEasyLearnMaths.getInstance();
		try {
			model.init(this);
			premios = model.findAllPremios(getString(R.string.puntos));
		} catch (SQLException e) {
			Log.e(TAG, "Error al recuperar los premios de la BBDD", e);
			Toast.makeText(this, getString(R.string.error_general) + getString(R.string.error_no_premios), Toast.LENGTH_SHORT).show();
			finish();
		}

		View header;
		LayoutInflater inflater = getLayoutInflater();
		header = (View) inflater.inflate(R.layout.definir_premios, null);
		lvPremios.addHeaderView(header);
		adapter = new PremioAdapter(this, R.layout.premios_item, premios);
		lvPremios.setAdapter(adapter);
		lvPremios.setOnItemClickListener(this);

		etPremio = (EditText) header.findViewById(R.id.etPremio);
		etPuntos = (EditText) header.findViewById(R.id.etPuntos);

		/*
		bCancelar = (Button) header.findViewById(R.id.bCancelarPremio);
		bEliminar = (Button) header.findViewById(R.id.bEliminarPremio);
		bGuardar = (Button) header.findViewById(R.id.bAceptarPremio);
		bCancelar.setOnClickListener(this);
		bEliminar.setOnClickListener(this);
		bGuardar.setOnClickListener(this);*/
		
		actionMode = new AnActionMode();
		
		if (savedInstanceState == null) {
			modoInsert = true;
		} else {
			Log.d(TAG, "restauramos el estado");
			modoInsert = (Boolean) savedInstanceState.getSerializable(MODO);
			etPremio.setText((String) savedInstanceState.getSerializable(PREMIO));
			etPuntos.setText((String) savedInstanceState.getSerializable(PUNTOS));
			position = (Integer) savedInstanceState.getSerializable(POSITION);
			// si estamos editando un premio
			if (!modoInsert) {
				model.setPremioSeleccionado(premios.get(position - 1));
				startActionMode(actionMode);
				//bCancelar.setVisibility(View.VISIBLE);
				//bEliminar.setVisibility(View.VISIBLE);
			}
			Log.d(TAG, "premio:" + etPremio.getText().toString());
			Log.d(TAG, "puntos:" + etPuntos.getText().toString());
			Log.d(TAG, "modo:" + modoInsert);
		}			
		
	}

	/**
	 * Al pulsar sobre un elemento del listado lo presentamos en la ficha de edición para que el usuario puedar modificarlo o eliminarlo
	 */
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Log.d(TAG, "onItemClick pulsado. Posicion:" + position);
		if (position > 0) {
			
			this.position = position;
			Premio premioSeleccionado = premios.get(position - 1);
			model.setPremioSeleccionado(premioSeleccionado);
			etPremio.setText(premioSeleccionado.getPremio());
			etPuntos.setText("" + premioSeleccionado.getPts());
			//bCancelar.setVisibility(View.VISIBLE);
			//bEliminar.setVisibility(View.VISIBLE);
			if (modoInsert){
				startActionMode(actionMode);
				modoInsert = false;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.d(TAG, "se crea el menu de opciones");		
		menu.add("Save").setIcon(R.drawable.ic_stat_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
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
		}else{
			guardarPremio();
			Log.d(TAG,"Pulsa guardar");
		}
		return true;
	}
	
	/**
	 * Guarda el premio en la base de datos cuando es nuevo o lo modifica cuando ya está insertado
	 */
	private void guardarPremio(){
		Log.d(TAG, "se pulsa la opción de guardar premio");
		String premio = etPremio.getText().toString();
		if ("".equals(premio)) {
			Toast.makeText(this, getString(R.string.premio_obl), Toast.LENGTH_SHORT).show();
			return;
		}
		int puntos = Utiles.getInt(etPuntos.getText().toString(), 0);
		try {
			model.savePremio(modoInsert, premio, puntos, getString(R.string.puntos));
			updateUI();
			Toast.makeText(this, "Premio guardado", Toast.LENGTH_SHORT).show();
		} catch (SQLException e) {
			Log.e(TAG, "Error al guardar el premio en la BBDD", e);
			Toast.makeText(this, getString(R.string.error_general) + getString(R.string.error_guardar_premio), Toast.LENGTH_SHORT).show();
		}
		modoInsert();
	}
	
	/**
	 * Borra el premio de la base de datos.
	 */
	private void eliminarPremio(){
		Log.d(TAG, "se pulsa la opción de eliminar premio");
		try {
			model.deletePremio();
			updateUI();
			Toast.makeText(this, "Premio eliminado", Toast.LENGTH_SHORT).show();
		} catch (SQLException e) {
			Log.e(TAG, "Error al eliminar el premio en la BBDD", e);
			Toast.makeText(this, getString(R.string.error_general) + getString(R.string.error_eliminar_premio), Toast.LENGTH_SHORT).show();
		}
		modoInsert();
	}
	
	private void modoInsert(){
		modoInsert = true;
		//bCancelar.setVisibility(View.INVISIBLE);
		//bEliminar.setVisibility(View.INVISIBLE);
		etPremio.setText("");
		etPuntos.setText("");
	}

	/**
	 * Thread para actualizar la vista
	 */
	private void updateUI() {
		runOnUiThread(new Runnable() {
			public void run() {
				adapter.notifyDataSetChanged();
				Log.d(TAG, "Actualizando vista");
			}
		});
	}

	/**
	 * Se almacena el estado de la actividad. Este contiene la información del formulario para editar el premio, si se está insertando o modificando un premio y
	 * qué premio se ha pulsado
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState generado en Definir premios");
		Log.d(TAG, "premio:" + etPremio.getText().toString());
		Log.d(TAG, "puntos:" + etPuntos.getText().toString());
		Log.d(TAG, "modo:" + modoInsert);
		Log.d(TAG, "position:" + position);

		outState.putSerializable(PREMIO, etPremio.getText().toString());
		outState.putSerializable(PUNTOS, etPuntos.getText().toString());
		outState.putSerializable(MODO, modoInsert);
		outState.putSerializable(POSITION, position);
	}

	/**
	 * Esta clase implementa una barra de acciones para el actionbar
	 * @author eluengo
	 *
	 */
	private final class AnActionMode implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			Log.d(TAG, "se crea el menu de opciones de la actionMode");
			MenuInflater blowUp = getSupportMenuInflater(); //with sherlock bar
			blowUp.inflate(R.menu.edit_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			
			if (item.getItemId() == R.id.guardar) {
				guardarPremio();
			} else if (item.getItemId() == R.id.borrar) {
				eliminarPremio();
			} else {
				modoInsert();
			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			Log.d(TAG,"Se destruye la barra de opciones");
			modoInsert();			
		}
	}

}
