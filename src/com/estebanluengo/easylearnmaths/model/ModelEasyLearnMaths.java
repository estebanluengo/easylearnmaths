package com.estebanluengo.easylearnmaths.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.database.SQLException;
import android.util.Log;
import com.estebanluengo.easylearnmaths.beans.Estadistica;
import com.estebanluengo.easylearnmaths.beans.Operacion;
import com.estebanluengo.easylearnmaths.beans.Premio;
import com.estebanluengo.easylearnmaths.dao.MathsDAO;
import com.estebanluengo.easylearnmaths.utils.Utiles;

/**
 * Contiene la lógica del proyecto. Esta clase representa el modelo en un patrón MVC. Las actividades representan los controladores.
 * Dichas actividades acceden a este modelo cuando necesitan cálculos o acceder a la base de datos. Luego acceden a las vistas 
 * que están alojadas en los layouts.
 * 
 * Esta clase implementa el patrón singleton. No es Thread-safe. Ojo si se usa de forma concurrente
 * 
 * @author eluengo
 * @version 1.0 27/06/2012
 */
public class ModelEasyLearnMaths {

	public static final String TAG = "ModelEasyLearnMaths";
	private static ModelEasyLearnMaths model = new ModelEasyLearnMaths();	
	private MathsDAO mda;                //objeto para acceder a la base de datos
	private Random random = new Random(); 
	private ArrayList<Premio> premios;   //contiene los premios recuperados de la tabla premios
	private Premio premioSeleccionado;   //contiene el premio seleccionado por el jugador en la UI

	/**
	 * El constructor es privado para que nadie pueda instanciar esta clase
	 */
	private ModelEasyLearnMaths(){
		
	}
	
	/**
	 * Para poder acceder a esta clase se invoca este método para obtener la única instancia que existe de esta clase
	 * @return se retorna un objeto ModelEasyLearnMaths (el único que existe)
	 */
	public static ModelEasyLearnMaths getInstance() {
		return model;
	}

	/**
	 * Antes de invocar a cualquier operación, los controladores tienen que invocar a este método pasándo por parámetro el contexto
	 * en el que se encuentran. Este contexto es necesario para poder crear el objeto de acceso a base de datos.
	 * El objeto de acceso a base de datos solo se crea una vez.
	 * @param ctx contexto de ejecución del controlador que invoca a este método
	 */
	public void init(Context ctx) {
		if (mda == null) {
			mda = new MathsDAO(ctx);
			Log.d(TAG, "Se crea el objeto MDA para conexión a base de datos");
		}
	}

	/**
	 * Reinicia todas las operaciones en la base de datos. Se invoca desde el controlador cuando el usuario pulsa el botón de reiniciar.
	 * @return se retorna el número de operaciones actualizadas en la base de datos
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public int reiniciar() throws SQLException{
		Log.d(TAG, "Aceptado reinicio");
		try {
			mda.open();
			int n = mda.initOperations(null);
			Log.d(TAG, "actualizadas " + n + " filas en la BBDD");
			return n;
		} finally {
			mda.close();
		}
	}

	/**
	 * Recupera todos los premios almacenados en la tabla de premios
	 * @param labelPuntos este label se utiliza para construir la representacion textual de los puntos para cada premio. El label
	 * se concatena al final después de los puntos. Por ejemplo si labelPuntos = "puntos" y el premio tiene 500 puntos la representación
	 * textual sera "500 puntos"
	 * @return se retorna un ArrayList de objetos Premio
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public ArrayList<Premio> findAllPremios(String labelPuntos) throws SQLException{
		try {
			mda.open();
			premios = mda.fetchAllPrizes(labelPuntos);
			return premios;
		} finally {
			mda.close();
		}
	}

	/**
	 * Almacena un premio en la tabla de operaciones. Puede ser que se inserte si el premio no existe o se modifique si el premio
	 * existe.
	 * @param modoInsert sera true para indicar que lo que queremos es insertarlo. False para indicar que queremos modificarlo. En caso
	 * de querer modificar, la información del premio está almacenada en la propiedad premioSeleccionado del objeto. Es responsabilidad
	 * del que llame a este método, guardar la información del objeto en dicha propiedad
	 * @param premioText contiene el texto que tendrá el premio
	 * @param puntos contiene los puntos del premio
	 * @param labelPuntos contiene el label que se concatena después de los puntos para tener la presentación textual de los puntos
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public void savePremio(boolean modoInsert, String premioText, int puntos, String labelPuntos) throws SQLException{
		try {
			mda.open();
			if (modoInsert) {
				long id = mda.createPrize(premioText, puntos);
				Premio p = new Premio(id, premioText, puntos + " " + labelPuntos, puntos);
				premios.add(p);
			} else {
				premioSeleccionado.setPremio(premioText);
				premioSeleccionado.setPts(puntos);
				premioSeleccionado.setPuntos(puntos + " " + labelPuntos);
				mda.updatePrize(premioSeleccionado.getId(), premioSeleccionado);
			}
		} finally {
			mda.close();
		}
	}

	/**
	 * Elimina el premio que ha seleccionado el usuario. Este premio se almcena en la propiedad premioSeleccionado del objeto y es responsabilidad
	 * del que llame a este método, guardar la información del objeto en dicha propiedad
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public void deletePremio() throws SQLException{
		try {
			mda.open();
			mda.deletePrize(premioSeleccionado.getId());
			premios.remove(premioSeleccionado);
		} finally {
			mda.close();
		}
	}

	/**
	 * Construye toda la información estadística del juego
	 * @return se retorna un objeto Estadistica que contiene todas la información que se mostrará en la UI
	 * @throws SQLException en caso de error se lanza una SQLException
	 * TODO: las estadisticas no cuadran con las operaciones que no son simples. Pensar una manera de cambiarlo 
	 */
	public Estadistica getEstadisticas() throws SQLException{
		Estadistica estadistica = null;
		try {
			mda.open();
			ArrayList<Operacion> lOper = mda.fetchAllOperations();
			//recuperamos las estadisticas de la tabla de estadisticas
			estadistica = mda.fetchStats();
			//Ahora miramos la tasa de aciertos en la BBDD
			for (Operacion oper : lOper) {
				if (oper.getExito() == Operacion.OK) {
					estadistica.setPreguntasBasicasContestadas(estadistica.getPreguntasBasicasContestadas() + 1);
					if (oper.getVecesContestadas() == 1) {
						estadistica.setaLaPrimera(estadistica.getaLaPrimera() + 1);
					} else {
						if (oper.getVecesContestadas() == 2) {
							estadistica.setaLaSegunda(estadistica.getaLaSegunda() + 1);
						} else {
							estadistica.setaLaTercera(estadistica.getaLaTercera() + 1);
						}
					}
					/*
					if (Operacion.SUMA.equals(oper.getTipoOperacion())) {
						estadistica.setSumas(estadistica.getSumas() + 1);
					} else if (Operacion.RESTA.equals(oper.getTipoOperacion())) {
						estadistica.setRestas(estadistica.getRestas() + 1);
					} else	if (Operacion.MULTIPLICACION.equals(oper.getTipoOperacion())) {
						estadistica.setMultiplicaciones(estadistica.getMultiplicaciones() + 1);
					} else if (Operacion.DIVISION.equals(oper.getTipoOperacion())) {
						estadistica.setDivisiones(estadistica.getDivisiones() + 1);
					} 
					*/					
				}else if (oper.getExito() == Operacion.NOOK) {
					//estadistica.setPreguntasContestadas(estadistica.getPreguntasContestadas() + 1);
					estadistica.setPreguntasBasicasContestadas(estadistica.getPreguntasBasicasContestadas() + 1);
				}
			}
			return estadistica;
		} finally {
			mda.close();
		}
	}

	/**
	 * Calcula los puntos conseguidos en la partida. Para realizar el cáculo se utilizan los parámetros definidos en los ajustes que contienen la información
	 * de los puntos que se dan para las sumas, restas, multiplicaciones, divisiones contestadas de forma correcta y los puntos conseguidos si se responde
	 * a todas las operaciones de forma correcta.
	 * Además de calcular los puntos conseguidos se actualiza la tabla de estadísticas con los puntos conseguidos en esta partia y la
	 * tabla de operaciones básicas con la información de si se ha respondido correctamente o no la operación.
	 * @param resultados contiene un array de objetos Operacion donde se almacena la operacion y el resultado que ha contestado el jugador
	 * @param puntosSumas puntos que se dan para cada suma contestada de manera correcta
	 * @param puntosRestas puntos que se dan para cada resta contestada de manera correcta
	 * @param puntosMultiplicaciones puntos que se dan para cada multiplicacion contestada de manera correcta
	 * @param puntosDivisiones puntos que se dan para cada division contestada de manera correcta
	 * @param puntosPruebaCompleta puntos que se dan si se contesta a todas las operaciones de manera correcta
	 * @return retorna un entero con los puntos conseguidos en la partida.
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public int calculaPuntosConseguidos(Operacion resultados[], int puntosSumas, int puntosRestas, int puntosMultiplicaciones, 
			int puntosDivisiones, int puntosComparaciones, int puntosPruebaCompleta,String tipoOperacion) throws SQLException{
		int puntosConseguidos = 0;
		int filasActualizadas = 0;
		int aciertos = 0;
		String operador = null;
		try {
			int exito;
			Log.d(TAG, "Inicio del procedimiento para actualizar las operaciones contestadas en la base de datos");
			mda.open();
			Estadistica estadistica = mda.fetchStats();
			Log.d(TAG, "Se recuperan las estadisticas de la base de datos");
			estadistica.setPreguntasContestadas(estadistica.getPreguntasContestadas() + resultados.length);
			for (Operacion op : resultados) {
				exito = op.isExito() ? Operacion.OK : Operacion.NOOK;
				//Si no es una operación de comparación y tenemos identificador de la operacion en la base de datos
				if (!Operacion.COMPARA.equals(op.getTipoOperacion()) && op.getId() != 0){
					//actualizamos las veces que la hemos contestado y si con exito o no. Esto tiene repercusión en las estadisticas simples
					op.setVecesContestadas(op.getVecesContestadas() + 1);
					//solo se actualiza si op.getId() tiene valor y eso solo pasa en nivel facil
					if (mda.updateOperation(op.getId(), exito, op.getVecesContestadas())) {
						filasActualizadas += 1;
					}
				}
				if (exito == Operacion.OK) {
					if (Operacion.SUMA.equals(tipoOperacion) || Operacion.RESTA.equals(tipoOperacion) ||
						Operacion.MULTIPLICACION.equals(tipoOperacion) || Operacion.DIVISION.equals(tipoOperacion)){
						operador = tipoOperacion;
					}else{
						if (Operacion.COMPARA.equals(tipoOperacion)){
							operador = tipoOperacion;
						}else{
							if (op.getOperando1().getOperador() == null)
								operador = op.getOperador();
							else
								operador = op.getOperando1().getOperador();
						}	
					}
					if (Operacion.SUMA.equals(operador)){
						estadistica.setSumas(estadistica.getSumas() + 1);
						puntosConseguidos += puntosSumas;
					}else if (Operacion.RESTA.equals(operador)){
						estadistica.setRestas(estadistica.getRestas() + 1);
						puntosConseguidos += puntosRestas;
					}else if (Operacion.MULTIPLICACION.equals(operador)){
						estadistica.setMultiplicaciones(estadistica.getMultiplicaciones() + 1);
						puntosConseguidos += puntosMultiplicaciones;
					}else if (Operacion.DIVISION.equals(operador)){
						estadistica.setDivisiones(estadistica.getDivisiones() + 1);
						puntosConseguidos += puntosDivisiones;
					}else if (Operacion.COMPARA.equals(operador)){
						estadistica.setComparaciones(estadistica.getComparaciones() + 1);
						puntosConseguidos += puntosComparaciones;
					}					
					aciertos++;
				}
			}
			estadistica.setAciertos(estadistica.getAciertos() + aciertos);
			mda.updateStats(estadistica);
			Log.d(TAG, "Estadisticas actualizadas");
			// si las ha acertado todas
			if (aciertos == resultados.length) {
				puntosConseguidos += puntosPruebaCompleta;
				Log.d(TAG, "Prueba resuelta satisfactoriamente al completo");
			}
			return puntosConseguidos;
		} finally {
			Log.d(TAG, "Se actualizan " + filasActualizadas + " operaciones en la base de datos");
			mda.close();
		}
	}

	/**
	 * Calcula de 0 a 100 la tasa de aciertos de las operaciones que se han contestado. 0 es que no ha acertado ni una bien. 100 es que
	 * las ha contestado todas bien
	 * @param resultados contiene las operaciones contestadas
	 * @return
	 */
	public double calculaTasaAcierto(Operacion resultados[]){
		int aciertos = 0;
		for (Operacion op: resultados){
			aciertos += (op.isExito()) ? 1 : 0;
		}
		return ((double)aciertos / (double)resultados.length ) * 100.0;
	}

	/**
	 * Construye las operaciones que el jugador deberá contestar. Para construir las operaciones se recuperan de la base de datos
	 * aquellas operaciones que corresponden con el tipo de operacion especificado por parámetro y que no se han contestado aún o 
	 * se han contestado de manera incorrecta en jugadas anteriores. Tras recuperarlas se eligen de manera aleatoria las operaciones
	 * que compondrán la partida
	 * @param tipoOperacion contiene el tipo de operacion entre suma, resta, multiplicacion, division o mixto
	 * @param totalPreguntas contiene el número máximo de preguntas el jugador debe responder
	 * @param dificultad puede ser E (Easy), N (Normal) o D (Difficult).
	 * @return se retorna un array de objetos Operacion
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	public Operacion[] creaJugadas(String tipoOperacion, int totalPreguntas, String dificultad) throws SQLException{
		if (tipoOperacion.equals(Operacion.COMPARA)){
			return creaOperacionesComparacion(totalPreguntas, dificultad);
		}else if (Operacion.NIVEL_FACIL.equals(dificultad)){
			return creaJugadaSimple(tipoOperacion, totalPreguntas);
		}else{
			return creaJugadaCompleja(tipoOperacion, totalPreguntas, dificultad);
		}

	}

	/**
	 * @param totalPreguntas
	 * @param dificultad
	 * @return
	 */
	private Operacion[] creaOperacionesComparacion(int totalPreguntas,
			String dificultad) {
		Operacion[] operaciones = new Operacion[totalPreguntas];
		Operacion op;
		int max = 1;
		ArrayList<Operacion> lOp = null;
		if (Operacion.NIVEL_FACIL.equals(dificultad)){
			max = 100;
		}else if (Operacion.NIVEL_NORMAL.equals(dificultad)){
			lOp = getOperaciones(Operacion.MIXTO, totalPreguntas);
			max = 1000;
		}else{
			lOp = getOperaciones(Operacion.MIXTO, totalPreguntas);
			if (Operacion.NIVEL_DIFICIL.equals(dificultad)){
				max = 1000;
			}else if (Operacion.NIVEL_AVANZADO.equals(dificultad)){ //se deja este nivel aunque se ha eliminado de los ajustes
				max = 100000;
			}
		}
		//boolean generaOperacion = false; //una jugada si, una jugada no para generar una operacion con tres operandos
		Operacion op1, op2, op3;
		int index;
		int size = 0;
		if (lOp != null)
			size = lOp.size() - 1;
		for (int i=0;i<totalPreguntas;i++){
			op = new Operacion();
			op.setId(i);
			//if (generaOperacion && lOp != null){ //lOp tendrá valor para nivel DIFICIL y AVANZADO				
			if (lOp != null){ //lOp tendra valor para nivel DIFICIL y NORMAL
				index = Utiles.getRandomNumber(random, size);
				op1 = lOp.get(index);
				if (Operacion.NIVEL_NORMAL.equals(dificultad)){
					index = Utiles.getRandomNumber(random,size);
					op2 = lOp.get(index);
				}else{ //Dificil
					index = Utiles.getRandomNumber(random,size);
					op2 = lOp.get(index);
					index = Utiles.getRandomNumber(random,size);
					op3 = lOp.get(index);
					op1.setOperando2(op3);
				}
			}else{
				op1 = new Operacion();
				op1.setTipoOperacion(Operacion.UNITARIO);
				op1.setResultado(Utiles.getRandomNumber(random,max));
				op2 = new Operacion();
				op2.setTipoOperacion(Operacion.UNITARIO);
				op2.setResultado(Utiles.getRandomNumber(random,max));								
			}
			op.setOperando1(op1);
			op.setOperando2(op2);
			//generaOperacion = !generaOperacion;						
			
			op.setComparador();
			op.setTipoOperacion(Operacion.COMPARA);
			op.setExito(Operacion.PENDIENTE);
			op.setResultado();
			operaciones[i] = op;
		}
		return operaciones;
	}


	/**
	 * Construye las operaciones de operaciones basicas que el jugador deberá contestar. Para construir las operaciones se recuperan de la base de datos
	 * aquellas operaciones que corresponden con el tipo de operacion especificado por parámetro y que no se han contestado aún o 
	 * se han contestado de manera incorrecta en jugadas anteriores. Tras recuperarlas se eligen de manera aleatoria las operaciones
	 * que compondrán la partida
	 * @param tipoOperacion contiene el tipo de operacion entre suma, resta, multiplicacion, division o mixto
	 * @param totalPreguntas contiene el número máximo de preguntas el jugador debe responder
	 * @return se retorna un array de objetos Operacion
	 * @throws SQLException en caso de error se lanza una SQLException
	 */
	private Operacion[] creaJugadaSimple(String tipoOperacion, int totalPreguntas){
		ArrayList<Operacion> lOp = getOperaciones(tipoOperacion, totalPreguntas);		
		if (lOp.size() == 0)
			return null;
		try {
			Operacion[] operaciones = new Operacion[totalPreguntas];			
			Operacion op;
			int queNumeroFalta;
			//en este hashMap controlamos los randoms que ya han salido
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>(totalPreguntas);
			int r;
			for (int i = 0; i < totalPreguntas; i++) {
				r = getRandom(hm, lOp.size());
				op = lOp.get(r);				
				if (tipoOperacion.equals(Operacion.QUENUMEROFALTA)){
					queNumeroFalta = Utiles.getRandomNumber(random,3);
					op.setQueNumeroFalta(queNumeroFalta);
					op.setTipoOperacion(Operacion.QUENUMEROFALTA);
				}
				op.setResultado();
				operaciones[i] = op;
			}
			return operaciones;
		} catch (Exception e) {
			Log.e(TAG, "error al generar las preguntas", e);
			return null;
		}
	}

	private Operacion[] creaJugadaCompleja(String tipoOperacion, int totalPreguntas, String dificultad) {
		//Se puede crear para nivel normal (3 + 5) + 2
		//para nivel dificil (4 + 6) + (2 + 8)  --> en este caso todo son sumas
		//para nivel avanzado (10 + 1) * (2 - 1)  --> es una operacion con operando1 = 11, operando2 = 1, operador = * y expresion1 = 10 + 1 y expresion2 = 2 - 1
		//hay que ver que pasa con las estadisticas de % de aciertos primer, segundo y tercer o mas intentos
		ArrayList<Operacion> lOp = getOperaciones(tipoOperacion, totalPreguntas);		
		if (lOp.size() == 0)
			return null;
		try {
			Operacion[] operaciones = new Operacion[totalPreguntas];			
			Operacion op, op1, op2, operando1, operando2;
			String operador;
			int queNumeroFalta;
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>(totalPreguntas);
			int r;
			for (int i = 0; i < totalPreguntas; i++) {
				r = getRandom(hm, lOp.size());
				op1 = lOp.get(r);
				if (Operacion.NIVEL_DIFICIL.equals(dificultad) || Operacion.NIVEL_AVANZADO.equals(dificultad)
						&& !tipoOperacion.equals(Operacion.QUENUMEROFALTA)){
					r = getRandom(hm, lOp.size());
					op2 = lOp.get(r);					
				}else{
					op2 = new Operacion();
					op2.setTipoOperacion(Operacion.UNITARIO);
					if (Operacion.NIVEL_DIFICIL.equals(dificultad))
						op2.setResultado(Utiles.getRandomNumber(random, 20));
					else if (Operacion.NIVEL_AVANZADO.equals(dificultad))
						op2.setResultado(Utiles.getRandomNumber(random, 50));
					else 
						op2.setResultado(Utiles.getRandomNumber(random, 11));
				}
				op1.setResultado();
				op2.setResultado();
				op = new Operacion();
				int r1 = op1.getResultado();
				int r2 = op2.getResultado();
				operando1 = op1;
				operando2 = op2;
				//determinamos que operador debe ir en la operacion raiz
				if (Operacion.SUMA.equals(tipoOperacion)){
					if ((r1 < 10 || r2 < 10) && Operacion.NIVEL_AVANZADO.equals(dificultad))
						operador = Operacion.MULTIPLICACION;
					else
						operador = Operacion.SUMA;					
				}else if (Operacion.RESTA.equals(tipoOperacion)){
					operador = Operacion.RESTA;	
					if (r1 < r2){  //evitamos un resultado negativo
						operando1 = op2;
						operando2 = op1;
					}
				}else if (Operacion.MULTIPLICACION.equals(tipoOperacion)){
					if ((r1 < 10 || r2 < 10) && Operacion.NIVEL_AVANZADO.equals(dificultad))
						operador = Operacion.MULTIPLICACION;
					else
						operador = Operacion.SUMA;					
				}else if (Operacion.DIVISION.equals(tipoOperacion)){
					if ((r1 < 10 || r2 < 10) && Operacion.NIVEL_AVANZADO.equals(dificultad))
						operador = Operacion.MULTIPLICACION;
					else
						operador = Operacion.RESTA;
					if (r1 < r2){  //evitamos un resultado negativo
						operando1 = op2;
						operando2 = op1;
					}
				}else if (Operacion.MIXTO.equals(tipoOperacion)){
					if (r1 > 10 || r2 > 10){
						operador = Operacion.RESTA;
						if (r1 < r2){  //evitamos un resultado negativo
							operando1 = op2;
							operando2 = op1;
						}
					}
					else if ((r1 < 10 || r2 < 10) && Operacion.NIVEL_AVANZADO.equals(dificultad))
						operador = Operacion.MULTIPLICACION;
					else
						operador = Operacion.SUMA;
				}else {
					operador = Operacion.SUMA;
					queNumeroFalta = Utiles.getRandomNumber(random,4);
					if (queNumeroFalta == 2 || queNumeroFalta == 0){
						op.setQueNumeroFalta(2);
					}else{
						op.setQueNumeroFalta(1);
					}												
				} 
				op.setOperando1(operando1);
				op.setOperando2(operando2);
				op.setTipoOperacion(tipoOperacion);
				op.setOperador(operador);
				op.setResultado();				
				operaciones[i] = op;
			}
			return operaciones;
		} catch (Exception e) {
			Log.e(TAG, "error al generar las preguntas", e);
			return null;
		}
	}
	
	/**
	 * @param tipoOperacion
	 * @return
	 */
	private ArrayList<Operacion> getOperaciones(String tipoOperacion, int totalPreguntas) {
		ArrayList<Operacion> lOp = null;
		String operador;
		try {
			//si estamos en modo que numero falta el operador será aleatorio por lo que elegimos el modo MIXTO
			if (tipoOperacion.equals(Operacion.QUENUMEROFALTA)){
				operador = Operacion.MIXTO;
			}else{
				operador = tipoOperacion;
			}
			mda.open();
			lOp = mda.fetchAllOperationsNotAnswered(operador);
			// Ya no hay suficientes operaciones por contestar
			if (lOp.size() < totalPreguntas) {
				mda.initOperations(tipoOperacion); // reiniciamos
				lOp = mda.fetchAllOperationsNotAnswered(operador);// volvemos a recuperar las operaciones
			}
		}catch (SQLException e) {
			Log.e(TAG, "error sql al recuperar operaciones de la BBDD", e);
			return null;
		} catch (Exception e) {
			Log.e(TAG, "error al recuperar operaciones de la BBDD", e);
			return null;
		} finally {
			mda.close();			
		}
		return lOp;
	}

	/**
	 * Retorna un numero aleatorio que no se hubiese generado antes y menor que un máximo
	 * 
	 * @param hm contiene los números que ya se han generado
	 * @param max no se puede retornar un número mayor que max
	 * @return se retorna un int aleatorio
	 */
	private int getRandom(HashMap<Integer, Integer> hm, int max) {
		boolean find = false;
		int r;
		do {
			r = Utiles.getRandomNumber(random,max);
			if (hm.get(r) == null) {
				find = true;
				hm.put(r, r);
				Log.d(TAG, "generado el numero:" + r);
			} else {
				Log.d(TAG, "el numero:" + r + " ya se ha generado antes");
			}
		} while (!find);
		return r;
	}
	
	/**
	 * 
	 * @param op
	 * @return
	 */
	public String[] getOpcionesPregunta(Operacion op){
		String[] opciones = new String[4];
		int r = Utiles.getRandomNumber(random,3);
		int operando1 = op.getOperando1().getResultado();
		int operando2 = op.getOperando2().getResultado();
		String operador = op.getOperador();
		int opcion = op.getResultado();		
		opciones[r%4] = ""+opcion;
		Log.d(TAG,"Opcion1 a pregunta (la buena):"+opcion+" en posicion:"+(r%4));
		
		r = (r+1)%4;
		if (Operacion.DIVISION.equals(operador) || Operacion.RESTA.equals(operador) || 
				((Operacion.QUENUMEROFALTA.equals(op.getTipoOperacion()) && op.getQueNumeroFalta() != 2))){
			if (opcion > 1)
				opciones[r] = ""+(opcion-1);
			else
				opciones[r] = ""+(opcion+1);
		}else
			opciones[r] = ""+Operacion.opera(operando1+1, operando2, operador);
		Log.d(TAG,"Opcion1 a pregunta (la mala):"+opciones[r]+" en posicion:"+r);		
		
		r = (r+1)%4;
		if (Operacion.DIVISION.equals(operador) || Operacion.RESTA.equals(operador) || 
				((Operacion.QUENUMEROFALTA.equals(op.getTipoOperacion()) && op.getQueNumeroFalta() != 2))){
			opciones[r] = ""+(opcion+3);
		}else
			opciones[r] = ""+Operacion.opera(operando1+2, operando2, operador);
		Log.d(TAG,"Opcion1 a pregunta (la mala):"+opciones[r]+" en posicion:"+r);		
		
		r = (r+1)%4;
		if (Operacion.DIVISION.equals(operador) || Operacion.RESTA.equals(operador) || 
				((Operacion.QUENUMEROFALTA.equals(op.getTipoOperacion()) && op.getQueNumeroFalta() != 2))){			
			opciones[r] = ""+(opcion+2);
		}else
			opciones[r] = ""+Operacion.opera(operando1+2, operando2+1, operador);
		Log.d(TAG,"Opcion1 a pregunta (la mala):"+opciones[r]+" en posicion:"+r);
		
		return opciones;
	}

	/**
	 * Retorna los premios que se han cargado de la base de datos previamente desde el método findAllPremios
	 * @return retorna un ArrayList de objetos Premio
	 */
	public ArrayList<Premio> getPremios() {
		return this.premios;
	}

	/**
	 * Retorna el premio seleccionado por el jugador
	 * @return retonra un objeto Premio
	 */
	public Premio getPremioSeleccionado() {
		return premioSeleccionado;
	}

	/**
	 * Establece el premio seleccionado por el jugador
	 * @param premioSeleccionado objeto de tipo Premio que representa la selección del jugador
	 */
	public void setPremioSeleccionado(Premio premioSeleccionado) {
		this.premioSeleccionado = premioSeleccionado;
	}

}
