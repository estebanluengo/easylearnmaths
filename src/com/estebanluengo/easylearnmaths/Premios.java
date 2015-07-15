package com.estebanluengo.easylearnmaths;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.estebanluengo.easylearnmaths.adapters.PremioAdapter;
import com.estebanluengo.easylearnmaths.beans.Premio;
import com.estebanluengo.easylearnmaths.model.ModelEasyLearnMaths;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Esta actividad permite presentar la lista de premios para que el jugador pueda comprar los que le interese. Se entiende
 * que esta compra es imaginaria y tiene que ser la persona que controla el juego quien lo sepa para corresponder con el premio
 * conseguido por el jugador. El premio se compra pulstando sobre el elemento deseado. Si tiene suficientes puntos se le pide
 * confirmación y si acepta se descuenta de su total de puntos ganados el precio del premio. Luego, es responsabilidad de la persona
 * que controla el juego el darle dicho premio.
 * @author eluengo
 * @version 1.0 27/06/2012
 *
 */
public class Premios extends SherlockListActivity implements OnItemClickListener {

	public static final String TAG = "Premios";

	private ModelEasyLearnMaths model;
	private SharedPreferences datosJugador;
	private int puntos = 0;
	
	private ListView lvPremios;
	private TextView tvPuntosConseguidos;
	private Dialog alertCompra;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate de Premios");
		super.onCreate(savedInstanceState);
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Log.d(TAG, "onCreate premios activity");
		setContentView(R.layout.premios);
		setTitle(R.string.compra_premios);
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(true);
		
		lvPremios = (ListView) findViewById(android.R.id.list);
	
		model = ModelEasyLearnMaths.getInstance();
		try{
			model.init(this);
			model.findAllPremios(getString(R.string.puntos));			
		}catch(SQLException e){
			Log.e(TAG,"Error al recuperar los premios de la BBDD",e);
			Toast.makeText(this, getString(R.string.error_general) + getString(R.string.error_no_premios), Toast.LENGTH_SHORT).show();
			finish();
		}
		
		View header;
		LayoutInflater inflater = getLayoutInflater();
		header = (View) inflater.inflate(R.layout.premios_header, null);
		lvPremios.addHeaderView(header);
		PremioAdapter adapter = new PremioAdapter(this, R.layout.premios_item, model.getPremios());
		lvPremios.setAdapter(adapter);
		lvPremios.setOnItemClickListener(this);

		datosJugador = getSharedPreferences(EasyLearnMaths.SHARED_PREFS_FILE, 0);
		puntos = datosJugador.getInt(EasyLearnMaths.PUNTOS, 0);

		// usamos header para buscar este componente
		tvPuntosConseguidos = (TextView) header.findViewById(R.id.tvTotalPuntos);
		tvPuntosConseguidos.setText("" + puntos);

		creaDialogoConfirmacionCompra();
	}

	private void creaDialogoConfirmacionCompra() {
		DialogInterface.OnClickListener confirmarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion Aceptar compra pulsada");
				compra();
				dialogo1.dismiss();
			}
		};
		DialogInterface.OnClickListener cancelarListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogo1, int id) {
				Log.d(TAG, "Accion Cancelar compra pulsada");
				dialogo1.dismiss();
			}
		};

		alertCompra = Utiles.createAlertDialog(this, getString(R.string.confirmacion), getString(R.string.seguro_comprar_premio), true, confirmarListener,
				cancelarListener, R.drawable.ic_dialog_operator);
	}

	/**
	 * Controla que premio se ha seleccionado. Si no tiene suficientes puntos para comprarlo entonces se informa mediante un diálogo
	 * En caso contrario se presenta un diálogo de confirmación
	 */
	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		Log.d(TAG, "onItemClick pulsado. Posicion:" + position);
		if (position > 0) {
			Premio p = model.getPremios().get(position - 1);
			if (p.getPts() > puntos) {
				Log.d(TAG, "No tienes suficientes puntos");
				Toast.makeText(this, getString(R.string.no_puntos), Toast.LENGTH_SHORT).show();
			} else {
				model.setPremioSeleccionado(p);				
				alertCompra.show();
			}
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
	 * La compra consiste en descontar de los puntos ganados el precio en puntos del premio comprado
	 */
	private void compra() {
		Premio p = model.getPremioSeleccionado(); 
		puntos -= p.getPts();
		tvPuntosConseguidos.setText("" + puntos);
		Log.d(TAG, "Se compra el premio "+p.getPremio()+" ahora tiene "+puntos+" puntos");		
		// guardar los puntos en el SharedPreferences
		Editor editor = datosJugador.edit();
		editor.putInt(EasyLearnMaths.PUNTOS, puntos);
		editor.commit();
		Toast.makeText(this, getString(R.string.acabas_comprar) + p.getPremio(), Toast.LENGTH_LONG).show();
	}
}