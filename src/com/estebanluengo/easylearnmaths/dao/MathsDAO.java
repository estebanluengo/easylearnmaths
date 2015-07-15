package com.estebanluengo.easylearnmaths.dao;

import java.util.ArrayList;

import com.estebanluengo.easylearnmaths.beans.Estadistica;
import com.estebanluengo.easylearnmaths.beans.Operacion;
import com.estebanluengo.easylearnmaths.beans.Premio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Esta clase contiene la lógica de acceso a la base de datos. Concretamente a
 * la tabla donde se alamacenan las operaciones matemáticas que el usuario puede
 * contestar y la tabla donde se almacenan los premios
 * 
 * @author eluengo
 * @version 1.0 27/06/2012
 */
public class MathsDAO {

	private static final String DATABASE_NAME = "easymaths";
	//tabla que contiene las operaciones matemáticas
	private static final String OPERACIONES_TABLE = "operaciones";
	//tabla que contiene los premios
	private static final String PREMIOS_TABLE = "premios"; 
	//tabla que contiene las estadisticas
	private static final String ESTADISTICAS_TABLE = "estadisticas"; 
	//versión de la base de datos
	private static final int DATABASE_VERSION = 2; 
	// id para ambas tablas
	private static final String KEY_ROWID = "_id"; 
	// columnas de la tabla operaciones
	private static final String KEY_OPERANDO1 = "operando1";
	private static final String KEY_OPERANDO2 = "operando2";
	private static final String KEY_OPERACION = "operacion";
	// Puede ser 0 -> no contestada, 1 -> contestada OK, 2 -> contestada NOOK
	private static final String KEY_ESTADO = "estado"; 
	private static final String KEY_VECES_CONTESTADA = "veces_contestada";
	// columnas de la tabla premio
	private static final String KEY_PREMIO = "premio";
	private static final String KEY_PUNTOS = "puntos";
	// columnas de la tabla de estadísticas
	private static final String KEY_PREGUNTAS_CONTESTADAS = "preguntas_contestadas";
	private static final String KEY_PREGUNTAS_ACERTADAS = "preguntas_acertadas";
	private static final String KEY_PUNTOS_SUMAS = "puntos_sumas";
	private static final String KEY_PUNTOS_RESTAS = "puntos_restas";
	private static final String KEY_PUNTOS_MULTIPLICACIONES = "puntos_multiplicaciones";
	private static final String KEY_PUNTOS_DIVISIONES = "puntos_divisiones";
	private static final String KEY_PUNTOS_COMPARACIONES = "puntos_comparaciones";

	private static final String TAG = "MathsDbAdapter";
	//se sigue el patron de usar un helper para controlar el acceso a la base de datos
	private DatabaseHelper mDbHelper;
	//objeto que apunta a la base de datos
	private SQLiteDatabase mDb; 

	/**
	 * SQLs para crear las tablas
	 */
	private static final String CREATE_OPERACIONES = "create table " + OPERACIONES_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_OPERANDO1 + " integer not null, " + KEY_OPERANDO2 + " integer not null, " + KEY_OPERACION + " text not null, " + KEY_ESTADO
			+ " integer not null," + KEY_VECES_CONTESTADA + " integer not null default 0);";

	private static final String CREATE_PREMIOS = "create table " + PREMIOS_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, " + KEY_PREMIO
			+ " text not null, " + KEY_PUNTOS + " integer not null);";

	private static final String CREATE_ESTADISTICAS = "create table " + ESTADISTICAS_TABLE + " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_PREGUNTAS_CONTESTADAS + " integer not null default 0, " + KEY_PREGUNTAS_ACERTADAS + " integer not null default 0, " + KEY_PUNTOS_SUMAS
			+ " integer not null default 0, " + KEY_PUNTOS_RESTAS + " integer not null default 0, " + KEY_PUNTOS_MULTIPLICACIONES
			+ " integer not null default 0, " + KEY_PUNTOS_DIVISIONES + " integer not null default 0, " + KEY_PUNTOS_COMPARACIONES
			+ " integer not null default 0 " + ");";

	private final Context mCtx;

	/**
	 * Esta clase contiene los métodos para crear la base de datos, actualizarla
	 * y obtener una referencia a ella
	 * 
	 * @author eluengo
	 * 
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		//indicará si durante la inicialización cuando tenga que hacerse se ha producido un error
		private boolean error = false; 

		/**
		 * DATABASE_VERSION indicará al Helper si tiene o no que hacer un
		 * upgrade de la base de datos
		 * 
		 * @param context
		 */
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Durante la creación o upgrade se crean las tablas y se inserta la
		 * información inicial en la base de datos
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Se crea la BBDD");
			db.execSQL(CREATE_OPERACIONES);
			db.execSQL(CREATE_PREMIOS);
			db.execSQL(CREATE_ESTADISTICAS);
			init(db);			
			Log.d(TAG, "BBDD creada e inicializada");
		}

		/**
		 * Los upgrades ocurren cuando se sube de versión. En este método es
		 * donde debe controlarse que se debe hacer en cada versión
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			if (oldVersion == 2 && newVersion == 3){
				return;  //no hacemos nada
			}else{
				if (oldVersion == 1 && newVersion > 1) {
					db.execSQL(CREATE_ESTADISTICAS);
					migrateStatsVersionOneToVersionTwo(db);
				} else { // se crea por primera vez
					onCreate(db);
				}
			}
		}

		/**
		 * Inicializa la base de datos. Básicamente se insertan todas las
		 * posibles operaciones que el jugador puede contestar. En la versión
		 * 1.0 contiene las 10 tablas de multiplicar y sumar. Las 10 tablas de
		 * restar cuyo resultado da positivo. Es decir, que el operando1 es
		 * mayor que el operando2 y las 10 tablas de dividir cuyo resultado es
		 * entero positivo (resto 0).
		 * 
		 * @param db
		 */
		public void init(SQLiteDatabase db) {
			try {
				db.beginTransaction();
				insertStats(new Estadistica(), db);
				db.delete("operaciones", null, null);
				// sumas, restas, multiplicaciones y divisiones
				Log.d(TAG, "Insertando las operaciones");
				for (int op1 = 0; op1 <= 10; op1++) {
					for (int op2 = 1; op2 <= 10; op2++) {
						String sql = "insert into " + OPERACIONES_TABLE + "(" + KEY_OPERANDO1 + ", " + KEY_OPERANDO2 + ", " + KEY_OPERACION + ", " + KEY_ESTADO
								+ ") values (" + op1 + "," + op2 + ",'+',0)";
						// Log.d(TAG, sql);
						db.execSQL(sql);
						if (op2 >= op1) {
							sql = "insert into " + OPERACIONES_TABLE + "(" + KEY_OPERANDO1 + ", " + KEY_OPERANDO2 + ", " + KEY_OPERACION + ", " + KEY_ESTADO
									+ ") values (" + op2 + "," + op1 + ",'-',0)";
							// Log.d(TAG, sql);
							db.execSQL(sql);
						}
						sql = "insert into " + OPERACIONES_TABLE + "(" + KEY_OPERANDO1 + ", " + KEY_OPERANDO2 + ", " + KEY_OPERACION + ", " + KEY_ESTADO
								+ ") values (" + op1 + "," + op2 + ",'x',0)";
						// Log.d(TAG, sql);
						db.execSQL(sql);
						int res = op1 * op2;
						sql = "insert into " + OPERACIONES_TABLE + "(" + KEY_OPERANDO1 + ", " + KEY_OPERANDO2 + ", " + KEY_OPERACION + ", " + KEY_ESTADO
								+ ") values (" + res + "," + op2 + ",'/',0)";
						// Log.d(TAG, sql);
						db.execSQL(sql);
					}
				}
				db.setTransactionSuccessful();
			} catch (Exception e) {
				error = true;
				Log.e(TAG, "Error en la inicialización de la BBDD", e);
			} finally {
				db.endTransaction();
			}
		}

		public boolean isError() {
			return error;
		}	
		
		/**
		 * 
		 * @param estadistica
		 * @param db
		 */
		private void insertStats(Estadistica estadistica, SQLiteDatabase db){
			Log.d(TAG, "Insertando los valores para las estadisticas");
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_PREGUNTAS_CONTESTADAS, estadistica.getPreguntasContestadas());
			initialValues.put(KEY_PREGUNTAS_ACERTADAS, (estadistica.getAciertos()));
			initialValues.put(KEY_PUNTOS_SUMAS, estadistica.getSumas());
			initialValues.put(KEY_PUNTOS_RESTAS, estadistica.getRestas());
			initialValues.put(KEY_PUNTOS_MULTIPLICACIONES, estadistica.getMultiplicaciones());
			initialValues.put(KEY_PUNTOS_DIVISIONES, estadistica.getDivisiones());
			initialValues.put(KEY_PUNTOS_COMPARACIONES, 0); //en la version 1 no había comparaciones	
			db.insert(ESTADISTICAS_TABLE, null, initialValues);
		}

		/**
		 * Se migra la informacion de puntos y preguntas contestadas de la tabla de operaciones a la tabla de estadisticas
		 * @param db
		 */
		private void migrateStatsVersionOneToVersionTwo(SQLiteDatabase db){			
			Estadistica estadistica = null;
			try {
				Log.d(TAG, "Migrando datos de estadisticas de la version 1 a la 2");
				db.beginTransaction();
				ArrayList<Operacion> lOper = fetchAllOperations(db);
				estadistica = new Estadistica();
				for (Operacion oper : lOper) {
					if (oper.getExito() == Operacion.OK) {						
						estadistica.setPreguntasContestadas(estadistica.getPreguntasContestadas() + 1);
						estadistica.setAciertos(estadistica.getAciertos() + 1);
						if (oper.getVecesContestadas() == 1) {
							estadistica.setaLaPrimera(estadistica.getaLaPrimera() + 1);
						} else {
							if (oper.getVecesContestadas() == 2) {
								estadistica.setaLaSegunda(estadistica.getaLaSegunda() + 1);
							} else {
								estadistica.setaLaTercera(estadistica.getaLaTercera() + 1);
							}
						}
						if (Operacion.SUMA.equals(oper.getTipoOperacion())) {
							estadistica.setSumas(estadistica.getSumas() + 1);
						} else if (Operacion.RESTA.equals(oper.getTipoOperacion())) {
							estadistica.setRestas(estadistica.getRestas() + 1);
						} else	if (Operacion.MULTIPLICACION.equals(oper.getTipoOperacion())) {
							estadistica.setMultiplicaciones(estadistica.getMultiplicaciones() + 1);
						} else if (Operacion.DIVISION.equals(oper.getTipoOperacion())) {
							estadistica.setDivisiones(estadistica.getDivisiones() + 1);
						} 					
					} else if (oper.getExito() == Operacion.NOOK) {
						estadistica.setPreguntasContestadas(estadistica.getPreguntasContestadas() + 1);						
					}
				}
				insertStats(estadistica, db);
				db.setTransactionSuccessful();
			} catch (Exception e) {
				Log.e(TAG, "Error en la migracion de estadisticas de la BBDD", e);
			} finally {
				db.endTransaction();
			}
		}
		
		/**
		 * 
		 * @param db
		 * @return
		 * @throws SQLException
		 */
		public ArrayList<Operacion> fetchAllOperations(SQLiteDatabase db) throws SQLException {
			ArrayList<Operacion> operaciones = new ArrayList<Operacion>();
			Cursor c = null;
			try {
				c = db.query(OPERACIONES_TABLE, new String[] { KEY_ROWID, KEY_OPERANDO1, KEY_OPERANDO2, KEY_OPERACION, KEY_ESTADO, KEY_VECES_CONTESTADA }, null,
						null, null, null, null);
				int iRow = c.getColumnIndex(KEY_ROWID);
				int iOperando1 = c.getColumnIndex(KEY_OPERANDO1);
				int iOperando2 = c.getColumnIndex(KEY_OPERANDO2);
				int iOperacion = c.getColumnIndex(KEY_OPERACION);
				int iEstado = c.getColumnIndex(KEY_ESTADO);
				int iVecesContestadas = c.getColumnIndex(KEY_VECES_CONTESTADA);

				Operacion op;
				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					op = new Operacion();
					op.setId(c.getInt(iRow));
					Operacion op1 = new Operacion();
					op1.setTipoOperacion(Operacion.UNITARIO);
					op1.setResultado(c.getInt(iOperando1));
					op.setOperando1(op1);
					Operacion op2 = new Operacion();
					op2.setTipoOperacion(Operacion.UNITARIO);
					op2.setResultado(c.getInt(iOperando2));
					op.setOperando2(op2);
					op.setTipoOperacion(c.getString(iOperacion));
					op.setOperador(op.getTipoOperacion());
					op.setExito(c.getInt(iEstado));
					op.setVecesContestadas(c.getInt(iVecesContestadas));
					operaciones.add(op);
				}
				return operaciones;
			} finally {
				if (c != null)
					c.close();
			}
		}
	
	}//End DatabaseHelper

	/**
	 * Constructor. Almacena el contexto para poder acceder a la base de datos
	 * en forma de lectura/escritura
	 * 
	 * @param ctx
	 *            El contexto en el que se trabaja
	 */
	public MathsDAO(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Abre una sesión con la base de datos.
	 * 
	 * @return Se retorna el mismo objeto
	 * @throws SQLException
	 *             si la BBDD no puede ser abierta o creada
	 */
	public MathsDAO open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		if (mDbHelper.isError())
			throw new SQLException("No se puede inicializar la base de datos");
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Cierra la sesión con la base de datos
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Inicializa la base de datos insertanto de nuevo toda la información de
	 * operaciones matemáticas
	 */
	public void initDB() {
		Log.d(TAG, "Inicializamos la base de datos");
		mDbHelper.init(mDb);
	}

	/**
	 * Inserta una operación en la tabla operaciones
	 * 
	 * @param operando1
	 * @param operando2
	 * @param operacion
	 * @return se retorna el identificador asociado a la nueva fila creada o -1
	 *         si ocurrió un error
	 */
	public long createOperation(Integer operando1, Integer operando2, String operacion) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_OPERANDO1, operando1);
		initialValues.put(KEY_OPERANDO2, operando2);
		initialValues.put(KEY_OPERACION, operacion);
		initialValues.put(KEY_ESTADO, Operacion.PENDIENTE);
		return mDb.insert(OPERACIONES_TABLE, null, initialValues);
	}

	/**
	 * Inserta un premio en la tabla premios
	 * 
	 * @param premio
	 * @param puntos
	 * @return retorna el identificador creado para el premio o -1 si ocurrió un
	 *         error
	 */
	public long createPrize(String premio, Integer puntos) throws SQLException {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PREMIO, premio);
		initialValues.put(KEY_PUNTOS, puntos);
		return mDb.insert(PREMIOS_TABLE, null, initialValues);
	}

	/**
	 * Se inicializan las operaciones del tipo especificadas por parametro. La
	 * inicialización implica que si la operacion especificada es una suma o
	 * resta o multiplicacion o division, entonces solo se inicializa el estado
	 * al valor 0 (pendiente de contestar) para las operaciones de dicho tipo.
	 * Si la operacion es mixta, se inicializa el estado a valor 0 para todas
	 * las operaciones. Si la operacion es null se inicilizan todas las
	 * operaciones al estado 0 y el campo veces_contestadas a cero.
	 * 
	 * @return se retorna el número de filas actualizadas
	 */
	public int initOperations(String tipoOperacion) throws SQLException {
		ContentValues args = new ContentValues();

		// si es null es porque han pulsado el reinicio completo del juego
		if (tipoOperacion == null) {
			args.put(KEY_VECES_CONTESTADA, 0);
			args.put(KEY_ESTADO, Operacion.PENDIENTE);
			// inicializamos las estadísticas del juego
			updateStats(new Estadistica());			
		} else {
			// si es mixto entonces ponemos pendiente todo
			if (Operacion.MIXTO.equals(tipoOperacion)) {
				args.put(KEY_ESTADO, Operacion.PENDIENTE);
			} else {
				// de lo contrario ponemos pendiente solo las operaciones que
				// correspondan
				args.put(KEY_ESTADO, Operacion.PENDIENTE);
				args.put(KEY_OPERACION, tipoOperacion);
			}
		}
		return mDb.update(OPERACIONES_TABLE, args, null, null);
	}

	/**
	 * Recupera las operaciones que están pendientes de contestar o contestadas
	 * incorrectamente y sean del tipo especificado
	 * 
	 * @param tipoOperacion
	 *            puede ser +,-,*,/ o mixto
	 * @return se retorna un ArrayList de objetos Operacion. Cada objeto
	 *         contiene además de los operandos y operador, la informacion de
	 *         las veces contestadas
	 */
	public ArrayList<Operacion> fetchAllOperationsNotAnswered(String tipoOperacion) throws SQLException {

		ArrayList<Operacion> operaciones = new ArrayList<Operacion>();
		Cursor c = null;
		try {
			String sqlTipoOperacion;
			if (Operacion.MIXTO.equals(tipoOperacion))
				sqlTipoOperacion = "";
			else
				sqlTipoOperacion = " and " + KEY_OPERACION + "='" + tipoOperacion + "'";

			c = mDb.query(OPERACIONES_TABLE, new String[] { KEY_ROWID, KEY_OPERANDO1, KEY_OPERANDO2, KEY_OPERACION, KEY_VECES_CONTESTADA }, KEY_ESTADO + "!="
					+ Operacion.OK + sqlTipoOperacion, null, null, null, null);
			int iRow = c.getColumnIndex(KEY_ROWID);
			int iOperando1 = c.getColumnIndex(KEY_OPERANDO1);
			int iOperando2 = c.getColumnIndex(KEY_OPERANDO2);
			int iOperacion = c.getColumnIndex(KEY_OPERACION);
			int iVecesContestadas = c.getColumnIndex(KEY_VECES_CONTESTADA);

			Operacion op;
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				op = new Operacion();
				op.setId(c.getInt(iRow));
				Operacion op1 = new Operacion();
				op1.setTipoOperacion(Operacion.UNITARIO);
				op1.setResultado(c.getInt(iOperando1));
				op.setOperando1(op1);
				Operacion op2 = new Operacion();
				op2.setTipoOperacion(Operacion.UNITARIO);
				op2.setResultado(c.getInt(iOperando2));
				op.setOperando2(op2);
				String o = c.getString(iOperacion);
				op.setOperador(o);
				op.setVecesContestadas(c.getInt(iVecesContestadas));
				op.setExito(Operacion.PENDIENTE);
				op.setTipoOperacion(tipoOperacion);
				operaciones.add(op);
			}
			return operaciones;
		} finally {
			if (c != null)
				c.close();
		}
	}

	/**
	 * Recupera todas las operaciones de la tabla operaciones
	 * 
	 * @return se retorna un ArrayList de objetos Operacion. Cada objeto
	 *         contiene además de los operandos y operador, la informacion de
	 *         las veces contestadas
	 */
	public ArrayList<Operacion> fetchAllOperations() throws SQLException {
		return mDbHelper.fetchAllOperations(mDb);
		/*
		ArrayList<Operacion> operaciones = new ArrayList<Operacion>();
		Cursor c = null;
		try {
			c = mDb.query(OPERACIONES_TABLE, new String[] { KEY_ROWID, KEY_OPERANDO1, KEY_OPERANDO2, KEY_OPERACION, KEY_ESTADO, KEY_VECES_CONTESTADA }, null,
					null, null, null, null);
			int iRow = c.getColumnIndex(KEY_ROWID);
			int iOperando1 = c.getColumnIndex(KEY_OPERANDO1);
			int iOperando2 = c.getColumnIndex(KEY_OPERANDO2);
			int iOperacion = c.getColumnIndex(KEY_OPERACION);
			int iEstado = c.getColumnIndex(KEY_ESTADO);
			int iVecesContestadas = c.getColumnIndex(KEY_VECES_CONTESTADA);

			Operacion op;
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				op = new Operacion();
				op.setId(c.getInt(iRow));
				Operacion op1 = new Operacion();
				op1.setTipoOperacion(Operacion.UNITARIO);
				op1.setResultado(c.getInt(iOperando1));
				op.setOperando1(op1);
				Operacion op2 = new Operacion();
				op2.setTipoOperacion(Operacion.UNITARIO);
				op2.setResultado(c.getInt(iOperando2));
				op.setOperando2(op2);
				op.setTipoOperacion(c.getString(iOperacion));
				op.setOperador(op.getTipoOperacion());
				op.setExito(c.getInt(iEstado));
				op.setVecesContestadas(c.getInt(iVecesContestadas));
				operaciones.add(op);
			}
			return operaciones;
		} finally {
			if (c != null)
				c.close();
		}
		*/
	}

	/**
	 * Recupera todos los premios de la tabla premios
	 * 
	 * @param labelPuntos
	 *            este label se utiliza para construir la representacion textual
	 *            de los puntos para cada premio. El label se concatena al final
	 *            después de los puntos. Por ejemplo si labelPuntos = "puntos" y
	 *            el premio tiene 500 puntos la representación textual sera
	 *            "500 puntos"
	 * @return se retorna un ArrayList de objetos Premio
	 */
	public ArrayList<Premio> fetchAllPrizes(String labelPuntos) throws SQLException {
		ArrayList<Premio> premios = new ArrayList<Premio>();
		Cursor c = null;
		try {
			c = mDb.query(PREMIOS_TABLE, new String[] { KEY_ROWID, KEY_PREMIO, KEY_PUNTOS }, null, null, null, null, null);
			int iRow = c.getColumnIndex(KEY_ROWID);
			int iPremio = c.getColumnIndex(KEY_PREMIO);
			int iPuntos = c.getColumnIndex(KEY_PUNTOS);
			Premio p;
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				p = new Premio();
				p.setId(c.getInt(iRow));
				p.setPremio(c.getString(iPremio));
				p.setPts(c.getInt(iPuntos));
				p.setPuntos(p.getPts() + " " + labelPuntos);
				premios.add(p);
			}
			return premios;
		} finally {
			if (c != null)
				c.close();
		}
	}

	/**
	 * Recupera una sola operacion de la tabla operaciones
	 * 
	 * @param rowId
	 *            representa el id de la operacion que se quiere recuperar
	 * @return se retorna un Cursor a la fila de base de datos. Es
	 *         responsabilidad del que invoca a esta operación el cerrar el
	 *         cursor
	 */
	public Cursor fetchOperacion(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, OPERACIONES_TABLE, new String[] { KEY_ROWID, KEY_OPERANDO1, KEY_OPERANDO2, KEY_OPERACION, KEY_VECES_CONTESTADA },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Recupera las estadísticas insertadas en la base de datos
	 * @return retorna un objeto Estadistica con la información rellenada según la base de datos
	 * @throws SQLException en caso de error se lanza
	 */
	public Estadistica fetchStats() throws SQLException {
		Estadistica estadistica = null;
		Cursor c = mDb.query(true, ESTADISTICAS_TABLE, new String[] { KEY_PREGUNTAS_CONTESTADAS, KEY_PREGUNTAS_ACERTADAS, 
				KEY_PUNTOS_SUMAS, KEY_PUNTOS_RESTAS, KEY_PUNTOS_MULTIPLICACIONES, KEY_PUNTOS_DIVISIONES, KEY_PUNTOS_COMPARACIONES },
				null, null, null, null, null, null);
		int iPreguntasContestadas = c.getColumnIndex(KEY_PREGUNTAS_CONTESTADAS);
		int iPreguntasAcertadas = c.getColumnIndex(KEY_PREGUNTAS_ACERTADAS);
		int iPuntosSumas = c.getColumnIndex(KEY_PUNTOS_SUMAS);
		int iPuntosRestas = c.getColumnIndex(KEY_PUNTOS_RESTAS);
		int iPuntosMultiplicaciones = c.getColumnIndex(KEY_PUNTOS_MULTIPLICACIONES);
		int iPuntosDivisiones = c.getColumnIndex(KEY_PUNTOS_DIVISIONES);
		int iPuntosComparaciones = c.getColumnIndex(KEY_PUNTOS_COMPARACIONES);		
		
		if (c != null) {
			c.moveToFirst();
			estadistica = new Estadistica();
			estadistica.setPreguntasContestadas(c.getInt(iPreguntasContestadas));
			estadistica.setAciertos(c.getInt(iPreguntasAcertadas));
			estadistica.setSumas(c.getInt(iPuntosSumas));
			estadistica.setRestas(c.getInt(iPuntosRestas));
			estadistica.setMultiplicaciones(c.getInt(iPuntosMultiplicaciones));
			estadistica.setDivisiones(c.getInt(iPuntosDivisiones));
			estadistica.setComparaciones(c.getInt(iPuntosComparaciones));
		}
		return estadistica;
	}
	
	/**
	 * Modifica el número de veces contestada y el estado de la operacion que
	 * coincida con el identificador especificado
	 * 
	 * @param rowId
	 *            identificador de la fila a actualizar
	 * @param estado
	 *            nuevo estado que tendrá la operacion
	 * @param veces_contestada
	 *            numero de veces contestada por el usuario
	 * @return retorna true si se pudo actualizar la fila de base de datos y
	 *         false en caso contrario
	 */
	public boolean updateOperation(long rowId, int estado, int veces_contestadas) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_ESTADO, estado);
		args.put(KEY_VECES_CONTESTADA, veces_contestadas);
		return mDb.update(OPERACIONES_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Modifica el premio que coincida con el identificador especificado
	 * 
	 * @param rowId
	 *            representa el identificador en la base de datos del premio que
	 *            se quiere modificar
	 * @param p
	 *            representa un objeto Premio. Se modificarán los puntos y el
	 *            texto del premio
	 * @return retorna true si se pudo actualizar la fila de base de datos y
	 *         false en caso contrario
	 */
	public boolean updatePrize(long rowId, Premio p) throws SQLException {
		ContentValues args = new ContentValues();
		args.put(KEY_PREMIO, p.getPremio());
		args.put(KEY_PUNTOS, p.getPts());
		return mDb.update(PREMIOS_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	/**
	 * Actualiza la información de estadísticas de la base de datos
	 * @param estadistica objeto Estadistica con la información a actualizar
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public void updateStats(Estadistica estadistica) throws SQLException{
		Log.d(TAG, "Insertando los valores para las estadisticas");
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PREGUNTAS_CONTESTADAS, estadistica.getPreguntasContestadas());
		initialValues.put(KEY_PREGUNTAS_ACERTADAS, estadistica.getAciertos());
		initialValues.put(KEY_PUNTOS_SUMAS, estadistica.getSumas());
		initialValues.put(KEY_PUNTOS_RESTAS, estadistica.getRestas());
		initialValues.put(KEY_PUNTOS_MULTIPLICACIONES, estadistica.getMultiplicaciones());
		initialValues.put(KEY_PUNTOS_DIVISIONES, estadistica.getDivisiones());
		initialValues.put(KEY_PUNTOS_COMPARACIONES, estadistica.getComparaciones());	
		mDb.update(ESTADISTICAS_TABLE, initialValues, null, null);
	}

	/**
	 * Elimina el premio que coincida con el identificador especificado
	 * 
	 * @param rowId
	 *            representa el identificador en la base de datos del premio que
	 *            se quiere eliminar
	 * @return retorna true si se pudo eliminar la fila de base de datos y false
	 *         en caso contrario
	 */
	public boolean deletePrize(long rowId) throws SQLException {
		return mDb.delete(PREMIOS_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
}